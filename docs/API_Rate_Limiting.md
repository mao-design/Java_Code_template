你这份源码里，`GLOBAL / IP / SPEL` 的**限流算法完全相同**，都是：

**Spring AOP 拦截接口 → 生成 Redis Key → Lua + Redis ZSet 做滑动窗口计数 → 超限返回 429。**

三者真正的区别是：

> **Redis Key 里的“identity（身份）”怎么生成。**

可以先用一张表理解：

|类型|按什么分组|典型用途|
|---|---|---|
|`GLOBAL`|整个接口一个桶|保护接口总流量|
|`IP`|每个 IP 一个桶|防单个客户端疯狂请求|
|`SPEL`|SpEL 计算结果一个桶|按用户、手机号、订单、业务类型等限流|

---

## 1. GLOBAL：整个接口共享一个限流桶

你项目中的代码实际上是：

```java
case GLOBAL -> "global";
```

也就是说，所有请求的 identity 都固定为：

```text
global
```

之后：

```java
String identityHash = HashUtils.sha256(identity);
```

生成 Redis Key：

```text
keyPrefix
    + ":"
    + resourceName
    + ":"
    + keyType
    + ":"
    + identityHash
```

例如你的接口：

```java
@GetMapping("/global")
@RateLimit(
    name = "demo:global",
    keyType = RateLimitKeyType.GLOBAL,
    maxRequests = 5,
    window = 10,
    timeUnit = TimeUnit.SECONDS
)
public Map<String, Object> global() {
    ...
}
```

最终大概就是：

```text
app:rate-limit:demo:global:global:xxxxxx
```

其中最后的 `xxxxxx` 是：

```java
sha256("global")
```

所以无论是谁请求：

```text
用户A
用户B
IP 1.1.1.1
IP 2.2.2.2
用户1001
用户1002
```

全部操作**同一个 Redis ZSet**。

假设配置：

```java
maxRequests = 5
window = 10秒
```

那么：

```text
10秒内：

用户A  请求1次
用户B  请求2次
用户C  请求2次
----------------
总共   5次

此时任何人的第6次请求
       ↓
直接限流
       ↓
HTTP 429
```

所以 `GLOBAL` 控制的是：

```text
这个接口整体的 QPS / 请求量
```

不过这里有一个很重要的细节：

**GLOBAL 不是整个应用所有接口共用一个桶。**

因为 Redis Key 里面还有：

```text
resourceName
```

你的 `/global` 使用：

```java
name = "demo:global"
```

另一个接口假设是：

```java
@RateLimit(
    name = "order:create",
    keyType = GLOBAL,
    ...
)
```

那么两个 Redis Key 是：

```text
app:rate-limit:demo:global:global:xxx

app:rate-limit:order:create:global:xxx
```

它们互不影响。

因此准确来说：

> `GLOBAL = 当前限流资源下所有请求共享一个桶。`

---

# 2. IP：每个客户端 IP 一个限流桶

源码这里：

```java
case IP -> resolveIp();
```

然后：

```java
private String resolveIp() {

    HttpServletRequest request =
            attributes.getRequest();

    String ip = request.getRemoteAddr();

    return ip;
}
```

所以 identity 变成客户端 IP。

例如：

```java
@GetMapping("/ip")
@RateLimit(
    name = "demo:ip",
    keyType = RateLimitKeyType.IP,
    maxRequests = 5,
    window = 10,
    timeUnit = TimeUnit.SECONDS
)
public Map<String, Object> ip() {
    ...
}
```

假设：

```text
客户端A：192.168.1.10
客户端B：192.168.1.20
```

那么 Redis Key 分别类似：

```text
app:rate-limit:demo:ip:ip:hash(192.168.1.10)

app:rate-limit:demo:ip:ip:hash(192.168.1.20)
```

于是：

```text
192.168.1.10
    ↓
自己的 ZSet
    ↓
10秒最多5次


192.168.1.20
    ↓
自己的 ZSet
    ↓
10秒最多5次
```

例如 A 已经请求：

```text
A：
1 2 3 4 5
```

A 第 6 次：

```text
429
```

但 B 此时第一次请求：

```text
正常通过
```

因为：

```text
A Redis Key != B Redis Key
```

所以 `IP` 很适合：

```text
验证码接口
登录接口
搜索接口
公开 API
防止单个 IP 刷接口
```

### 你这份代码有一个需要特别注意的问题

你现在直接用了：

```java
request.getRemoteAddr()
```

如果线上架构是：

```text
用户
 ↓
Nginx
 ↓
Spring Boot
```

Spring Boot 很可能看到的 IP 是：

```text
Nginx IP
```

结果变成：

```text
用户A ─┐
用户B ─┼→ Nginx IP → 同一个限流桶
用户C ─┘
```

那么 `IP` 限流实际上可能退化成类似 `GLOBAL`。

线上通常需要结合可信代理配置正确处理：

```text
X-Forwarded-For
Forwarded
```

而不是无条件相信客户端自己传的 `X-Forwarded-For`。

---

# 3. SPEL：根据业务参数动态创建限流桶

这是三个里面最灵活的。

源码：

```java
case SPEL -> resolveSpel(
    rateLimit.key(),
    method,
    args
);
```

假设：

```java
@RateLimit(
    name = "demo:user",
    keyType = RateLimitKeyType.SPEL,
    key = "#userId",
    maxRequests = 3,
    window = 30,
    timeUnit = TimeUnit.SECONDS
)
public Map<String, Object> user(
        @PathVariable Long userId
) {
    ...
}
```

请求：

```http
GET /api/demo/user/10001
```

方法参数就是：

```java
userId = 10001
```

你的 `resolveSpel()` 会先把参数塞入 SpEL 上下文：

```java
context.setVariable(
    "userId",
    10001L
);
```

然后执行：

```java
#userId
```

得到：

```text
10001
```

于是：

```text
identity = "10001"
```

Redis Key：

```text
app:rate-limit:demo:user:spel:hash(10001)
```

如果另一个用户：

```http
GET /api/demo/user/20002
```

则：

```text
app:rate-limit:demo:user:spel:hash(20002)
```

完全是两个桶。

所以：

```text
userId=10001
30秒最多3次

userId=20002
30秒也最多3次
```

彼此不影响。

---

## SPEL 不只能按 userId

你代码里面已经有几个很好的例子。

登录接口：

```java
@RateLimit(
    name = "demo:login",
    keyType = RateLimitKeyType.SPEL,
    key = "#request.username",
    maxRequests = 5,
    window = 1,
    timeUnit = TimeUnit.MINUTES
)
```

请求：

```json
{
    "username": "zhangsan"
}
```

SpEL：

```java
#request.username
```

得到：

```text
zhangsan
```

于是：

```text
zhangsan → 自己的限流桶
lisi      → 自己的限流桶
wangwu    → 自己的限流桶
```

因此效果是：

> 每个 username 1 分钟最多尝试登录 5 次。

这比 IP 限流更加业务化。

---

你另外这个例子：

```java
@RateLimit(
    name = "demo:business",
    keyType = RateLimitKeyType.SPEL,
    key = "#userId + ':' + #businessType",
    maxRequests = 10,
    window = 60
)
```

请求：

```text
userId = 1001
businessType = order
```

SpEL 结果：

```text
1001:order
```

于是 Redis Key：

```text
app:rate-limit:demo:business:spel:hash("1001:order")
```

同一个用户请求另外一种业务：

```text
userId = 1001
businessType = payment
```

identity：

```text
1001:payment
```

因此：

```text
1001 + order
      ↓
一个桶

1001 + payment
      ↓
另一个桶

2002 + order
      ↓
又一个桶
```

这就是 `SPEL` 最大的价值：

**你可以自己定义“谁应该共享一个限流桶”。**

---

# 4. 三种类型最终都会进入同一个限流算法

前面的：

```text
GLOBAL
IP
SPEL
```

最终只负责生成：

```java
String key
```

然后 `RateLimitAspect` 都会调用：

```java
result = redisRateLimitService.tryAcquire(
    key,
    rateLimit.maxRequests(),
    windowMillis
);
```

接下来进入：

```java
RedisRateLimitServiceImpl
```

执行 Lua：

```java
redisTemplate.execute(
    slidingWindowRateLimitScript,
    Collections.singletonList(key),
    String.valueOf(maxRequests),
    String.valueOf(windowMillis),
    member
);
```

所以整个调用链实际上是：

```text
请求
 ↓
Controller
 ↓
@RateLimit
 ↓
RateLimitAspect.around()
 ↓
RateLimitKeyResolver.resolve()
 ↓
┌────────┬─────────────┐
│ GLOBAL │ "global"    │
│ IP     │ 客户端 IP    │
│ SPEL   │ SpEL 计算值 │
└────────┴─────────────┘
 ↓
SHA-256
 ↓
组成 Redis Key
 ↓
RedisRateLimitService.tryAcquire()
 ↓
Lua
 ↓
Redis ZSet 滑动窗口
 ↓
允许 / 拒绝
```

---

# 5. Redis 到底怎么判断“超过 5 次”？

比如：

```java
maxRequests = 5
window = 10秒
```

Redis 中使用的是：

```text
ZSet
```

每次成功请求都会存：

```text
member = UUID
score  = 请求时间
```

例如：

```text
Redis Key:
app:rate-limit:demo:global:global:xxx

ZSet:

request-A    1000
request-B    2500
request-C    4000
request-D    6500
request-E    9000
```

现在假设 Redis 时间：

```text
now = 10000ms
```

窗口：

```text
10000 - 10000
=
0
```

先执行：

```lua
ZREMRANGEBYSCORE key -inf windowStart
```

也就是删除已经离开窗口的请求。

然后：

```lua
ZCARD key
```

计算：

```text
currentCount = 5
```

判断：

```lua
if currentCount >= limit then
```

即：

```text
5 >= 5
```

成立。

于是：

```lua
return {
    0,
    currentCount,
    retryAfterMillis,
    nowMillis
}
```

Java 中：

```java
allowed == 0
```

之后：

```java
if (!result.allowed()) {
    throw new RateLimitException(...);
}
```

最终全局异常处理：

```text
HTTP 429 Too Many Requests
```

并返回：

```text
Retry-After
X-RateLimit-Limit
X-RateLimit-Remaining
```

---

# 6. 为什么叫“滑动窗口”

比如限制：

```text
10 秒最多 5 次
```

不是固定统计：

```text
12:00:00 ~ 12:00:10
12:00:10 ~ 12:00:20
```

而是每一次请求到来的时候，都往前看：

```text
当前时间 - 10秒
        ↓
      当前时间
```

例如现在：

```text
12:00:17
```

统计范围就是：

```text
12:00:07 ~ 12:00:17
```

到了：

```text
12:00:18
```

统计范围自动变成：

```text
12:00:08 ~ 12:00:18
```

所以窗口一直在“滑动”。

---

# 7. 最核心的理解

你可以把 Redis 限流想象成很多个桶。

`GLOBAL`：

```text
接口 /createOrder

所有人
  ↓
┌───────────────┐
│ 一个公共桶     │
│ 最多 100 次/s │
└───────────────┘
```

`IP`：

```text
1.1.1.1 → 桶A → 5次/10s
2.2.2.2 → 桶B → 5次/10s
3.3.3.3 → 桶C → 5次/10s
```

`SPEL`：

```text
userId=1001 → 桶A → 3次/30s
userId=1002 → 桶B → 3次/30s
userId=1003 → 桶C → 3次/30s
```

所以你的这个枚举：

```java
public enum RateLimitKeyType {
    GLOBAL,
    IP,
    SPEL
}
```

本质上可以理解成：

```java
public enum RateLimitKeyType {

    // 谁来请求都算在一起
    GLOBAL,

    // 相同 IP 算在一起
    IP,

    // SpEL 算出来相同的值算在一起
    SPEL
}
```

而**真正负责“10 秒 5 次”这个限流逻辑的，不是枚举本身，而是后面的 `Redis + Lua + ZSet 滑动窗口`**。

还有一个特别关键的规则是：你这个项目只有当 **`name + keyType + identity` 都相同** 时，请求才会进入同一个限流桶。这个规则理解了，三个类型基本就彻底通了。