-- =========================================================
-- Redis 滑动窗口限流脚本
--
-- KEYS[1]：
-- 当前限流使用的 Redis Key
--
-- ARGV[1]：
-- 最大请求次数，例如 5
--
-- ARGV[2]：
-- 滑动窗口大小，单位：毫秒
-- 例如 10 秒 = 10000
--
-- ARGV[3]：
-- 当前请求唯一标识 member
-- Java 端一般使用 UUID 生成
--
-- ZSet 数据结构：
--
-- member = 当前请求唯一 ID
-- score  = 当前请求时间戳（毫秒）
--
-- 例如：
--
-- key = app:rate-limit:demo:user:spel:xxxx
--
-- ZSet：
--
-- member1   1700000000000
-- member2   1700000001000
-- member3   1700000002000
--
-- score 就是请求发生的时间
-- =========================================================


-- 获取 Redis Key
--
-- Java 中：
--
-- Collections.singletonList(key)
--
-- 会作为 KEYS 参数传入 Lua
--
-- 所以：
--
-- KEYS[1] = Java 传过来的 key
local key = KEYS[1]


-- 获取最大允许请求次数
--
-- Java：
--
-- String.valueOf(maxRequests)
--
-- 对应：
--
-- ARGV[1]
--
-- Redis 传进来的参数本质上是字符串，
-- 所以这里使用 tonumber() 转换为数字。
--
-- 例如：
--
-- ARGV[1] = "5"
--
-- tonumber("5")
--
-- 得：
--
-- limit = 5
local limit = tonumber(ARGV[1])


-- 获取滑动窗口大小
--
-- Java：
--
-- String.valueOf(windowMillis)
--
-- 对应：
--
-- ARGV[2]
--
-- 例如：
--
-- windowMillis = 10000
--
-- 表示：
--
-- 10 秒
local windowMillis = tonumber(ARGV[2])


-- 获取当前请求唯一标识
--
-- Java：
--
-- member = UUID.randomUUID()...
--
-- 对应：
--
-- ARGV[3]
--
-- 每一个请求使用不同 member，
-- 防止同一毫秒内多个请求在 ZSet 中互相覆盖。
local member = ARGV[3]


-- =========================================================
-- 获取 Redis 服务器当前时间
-- =========================================================

-- Redis TIME 命令返回两个值：
--
-- redisTime[1] = 秒
-- redisTime[2] = 微秒
--
-- 例如：
--
-- redis.call('TIME')
--
-- 返回类似：
--
-- {
--     "1700000000",
--     "123456"
-- }
--
-- 表示：
--
-- 1700000000 秒
-- +
-- 123456 微秒
--
-- 为什么使用 Redis 时间？
--
-- 因为如果有多个 Java 服务实例：
--
-- Java服务A
-- Java服务B
-- Java服务C
--
-- 它们自己的服务器时间可能存在误差。
--
-- 所有请求统一使用 Redis 服务器时间，
-- 可以避免多个服务器时间不一致。
local redisTime = redis.call('TIME')


-- 把 Redis 时间统一转换为毫秒
--
-- redisTime[1]
-- 是秒：
--
-- 秒 * 1000
--
-- 转换为毫秒
--
-- redisTime[2]
-- 是微秒：
--
-- 微秒 / 1000
--
-- 转换为毫秒
--
-- math.floor()
-- 表示向下取整
--
-- 最终得到：
--
-- 当前 Redis 时间戳，单位毫秒
local nowMillis =
        tonumber(redisTime[1]) * 1000
        + math.floor(tonumber(redisTime[2]) / 1000)


-- =========================================================
-- 计算当前滑动窗口的起始时间
-- =========================================================

-- 例如：
--
-- 当前时间：
--
-- nowMillis = 100000
--
-- 窗口：
--
-- windowMillis = 10000
--
-- 那么：
--
-- windowStart =
-- 100000 - 10000
-- = 90000
--
-- 当前有效窗口就是：
--
-- [90000, 100000]
--
-- 90000 之前的请求已经过期。
local windowStart = nowMillis - windowMillis


-- =========================================================
-- 删除滑动窗口之外的旧请求
-- =========================================================

-- ZREMRANGEBYSCORE：
--
-- 根据 score 范围删除 ZSet 中的元素
--
-- 当前：
--
-- key
--
-- 是限流 ZSet
--
-- '-inf'
--
-- 表示负无穷
--
-- windowStart
--
-- 表示当前窗口起始时间
--
-- 所以这一段表示：
--
-- 删除：
--
-- score <= windowStart
--
-- 的所有旧请求。
--
-- 例如：
--
-- 当前：
--
-- nowMillis = 100000
--
-- windowMillis = 10000
--
-- windowStart = 90000
--
-- ZSet 原来：
--
-- request1  85000
-- request2  89000
-- request3  92000
-- request4  95000
--
-- 删除之后：
--
-- request3  92000
-- request4  95000
--
-- 85000、89000 已经不属于当前窗口。
redis.call(
        'ZREMRANGEBYSCORE',
        key,
        '-inf',
        windowStart
)


-- =========================================================
-- 获取当前窗口内请求数量
-- =========================================================

-- ZCARD：
--
-- 获取 ZSet 中元素数量
--
-- 因为前面已经把窗口外的数据删除了，
-- 所以现在 ZSet 中剩下的所有记录，
-- 都属于当前滑动窗口。
--
-- currentCount 就是：
--
-- 当前窗口已经发生了多少次请求。
local currentCount = redis.call(
        'ZCARD',
        key
)


-- =========================================================
-- 判断是否已经达到最大请求次数
-- =========================================================

-- 例如：
--
-- limit = 5
--
-- currentCount = 5
--
-- 说明当前窗口已经存在 5 个请求。
--
-- 当前新请求不能再加入。
if currentCount >= limit then


    -- =====================================================
    -- 获取当前窗口中最早的一次请求
    -- =====================================================

    -- ZRANGE key 0 0 WITHSCORES
    --
    -- 因为 ZSet 默认按照 score 从小到大排序。
    --
    -- score 是请求时间戳。
    --
    -- 所以：
--
    -- 第 0 个元素
--
    -- 就是当前窗口中时间最早的请求。
--
    -- WITHSCORES
--
    -- 表示：
--
    -- 不仅返回 member，
    -- 还返回 score。
--
    -- 例如：
--
    -- oldest =
--
    -- {
    --     "request_uuid",
    --     "1700000000000"
    -- }
--
    -- Lua 数组下标从 1 开始：
--
    -- oldest[1] = member
    -- oldest[2] = score
    local oldest = redis.call(
            'ZRANGE',
            key,
            0,
            0,
            'WITHSCORES'
    )


    -- 默认至少等待 1 毫秒
    --
    -- 防止计算出来出现 0 或负数。
    local retryAfterMillis = 1


    -- 判断是否成功获取到了最早请求的 score
    --
    -- oldest[2]
    --
    -- 就是最早请求的时间戳。
    if oldest[2] ~= nil then


        -- 把最早请求时间戳转换为 number
        local oldestTimestamp = tonumber(oldest[2])


        -- =================================================
        -- 计算还需要等待多久
        -- =================================================

        -- 例如：
--
        -- 窗口大小：
--
        -- windowMillis = 10000
--
        -- 最早请求：
--
        -- oldestTimestamp = 92000
--
        -- 当前时间：
--
        -- nowMillis = 100000
--
        -- 这个最早请求已经存在：
--
        -- 100000 - 92000
        -- = 8000 ms
--
        -- 而窗口是：
--
        -- 10000 ms
--
        -- 所以还需要：
--
        -- 10000 - 8000
        -- = 2000 ms
--
        -- 等 2000ms 后，
        -- 最早请求就会离开窗口，
        -- 用户就可以再次请求。
        retryAfterMillis =
                windowMillis
                - (nowMillis - oldestTimestamp)


        -- 如果因为时间精度等问题，
        -- retryAfterMillis 小于 1，
        -- 则强制设置成 1ms。
        if retryAfterMillis < 1 then
            retryAfterMillis = 1
        end
    end


    -- =====================================================
    -- 当前请求被拒绝
    -- =====================================================

    -- 返回：
--
    -- {
    --     allowed,
    --     currentCount,
    --     retryAfterMillis,
    --     nowMillis
    -- }
--
    -- allowed = 0
--
    -- 表示：
--
    -- 不允许当前请求。
--
    -- currentCount：
--
    -- 当前窗口已有请求数量。
--
    -- retryAfterMillis：
--
    -- 建议客户端等待多少毫秒再重试。
--
    -- nowMillis：
--
    -- Redis 当前服务器时间。
    -- 拒绝请求时返回
    return {
        0,
        currentCount,
        retryAfterMillis,
        nowMillis
    }
end


-- =========================================================
-- 没有达到限流上限
-- 当前请求可以通过
-- =========================================================


-- 把当前请求加入 ZSet
--
-- ZADD：
--
-- key：
-- 当前限流 Redis Key
--
-- score：
-- nowMillis
-- 当前请求时间戳
--
-- member：
-- 当前请求唯一 UUID
--
-- 最终相当于：
--
-- ZADD key 当前时间戳 当前请求UUID
--
-- 例如：
--
-- ZADD app:rate-limit:xxx 100000 abc123
redis.call(
        'ZADD',
        key,
        nowMillis,
        member
)


-- =========================================================
-- 设置 Redis Key 的过期时间
-- =========================================================

-- PEXPIRE：
--
-- 设置 Key 的过期时间
--
-- 单位：
--
-- 毫秒
--
-- 例如：
--
-- windowMillis = 10000
--
-- 相当于：
--
-- 这个 Redis Key 如果 10 秒没有再次被访问，
-- 就自动删除。
--
-- 主要目的是：
--
-- 防止大量已经没人访问的限流 Key
-- 长期留在 Redis 中占用内存。
redis.call(
        'PEXPIRE',
        key,
        windowMillis
)


-- =========================================================
-- 返回允许请求的结果
-- =========================================================

-- 当前请求已经成功写入 ZSet，
-- 所以当前窗口请求数量应该：
--
-- currentCount + 1
--
-- 返回结构依然是：
--
-- {
--     allowed,
--     currentCount,
--     retryAfterMillis,
--     nowMillis
-- }
--
-- allowed = 1
--
-- 表示：
--
-- 当前请求允许通过。
--
-- retryAfterMillis = 0
--
-- 因为请求已经允许，
-- 不需要等待重试。
-- 允许请求时返回
return {
    1,
    currentCount + 1,
    0,
    nowMillis
}