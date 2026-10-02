local key = KEYS[1]

local limit = tonumber(ARGV[1])
local windowMillis = tonumber(ARGV[2])
local member = ARGV[3]

local redisTime = redis.call('TIME')

local nowMillis =
        tonumber(redisTime[1]) * 1000
        + math.floor(tonumber(redisTime[2]) / 1000)

local windowStart = nowMillis - windowMillis

redis.call(
        'ZREMRANGEBYSCORE',
        key,
        '-inf',
        windowStart
)

local currentCount = redis.call(
        'ZCARD',
        key
)

if currentCount >= limit then

    local oldest = redis.call(
            'ZRANGE',
            key,
            0,
            0,
            'WITHSCORES'
    )

    local retryAfterMillis = 1

    if oldest[2] ~= nil then

        local oldestTimestamp = tonumber(oldest[2])

        retryAfterMillis =
                windowMillis
                - (nowMillis - oldestTimestamp)

        if retryAfterMillis < 1 then
            retryAfterMillis = 1
        end
    end

    return {
        0,
        currentCount,
        retryAfterMillis,
        nowMillis
    }
end

redis.call(
        'ZADD',
        key,
        nowMillis,
        member
)

redis.call(
        'PEXPIRE',
        key,
        windowMillis
)

return {
    1,
    currentCount + 1,
    0,
    nowMillis
}