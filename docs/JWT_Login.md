# 1. 登录时发生什么？

用户输入：

```
用户名：admin
密码：123456
```

后端验证成功。

生成两个 Token：

## Access Token

例如：

```
AccessToken-A
```

里面：

```
{
  "username":"admin",
  "type":"access",
  "exp":"10:30"
}
```

有效期：

```
30分钟
```

用途：

> 访问接口

比如：

```
GET /user/info
Authorization: Bearer AccessToken-A
```

---

## Refresh Token

例如：

```
RefreshToken-A
```

里面：

```
{
  "username":"admin",
  "type":"refresh",
  "jti":"abc123",
  "exp":"7天后"
}
```

有效期：

```
7天
```

用途：

> 换新的 Access Token

然后：

Refresh Token 存 Redis：

```
key:
refresh:admin:abc123


value:
RefreshToken-A
```

此时：

```
客户端:
    AccessToken-A
    RefreshToken-A


Redis:
    refresh:admin:abc123
            |
            ↓
      RefreshToken-A
```

---

# 2. 平时访问接口怎么办？

比如：

10:05 请求：

```
GET /order/list
```

带：

```
AccessToken-A
```

流程：

```
请求
 |
 ↓
JwtFilter
 |
 ↓
解析 AccessToken-A
 |
 ↓
检查:
    签名正确？
    有没有过期？
 |
 ↓
通过
 |
 ↓
Controller
```

注意：

这里：

## 不看 Refresh Token

## 不查 Redis

## 不生成新 Token

因为 Access Token 还有效。

---

# 3. Access Token什么时候更新？

重点：

> Access Token 不是每次请求更新。

只有：

## Access Token过期的时候更新

例如：

登录：

```
10:00
```

生成：

```
AccessToken-A
有效30分钟
```

所以：

```
10:30
过期
```

10:31 请求：

```
GET /user/info
```

带：

```
AccessToken-A
```

后端：

```
JwtFilter
    |
    ↓
发现 exp < 当前时间
    |
    ↓
ExpiredJwtException
    |
    ↓
返回401
```

返回：

```
{
 "code":401,
 "msg":"Token过期"
}
```

---

# 4. 前端收到401之后怎么办？

前端发现：

```
AccessToken失效
```

它不会让用户重新登录。

它调用：

```
POST /auth/refresh
```

携带：

```
{
 "refreshToken":"RefreshToken-A"
}
```

---

# 5. Refresh接口做什么？

后端收到：

```
RefreshToken-A
```

第一步：

验证 JWT：

```
jwtUtil.parseToken(refreshToken)
```

检查：

```
是不是refresh类型？
有没有过期？
签名对不对？
```

第二步：

查 Redis：

拿到：

```
refresh:admin:abc123
```

看看：

Redis里面是不是：

```
RefreshToken-A
```

如果：

一样：

说明：

```
这个RefreshToken有效
```

---

# 6. 刷新成功后，更新什么？

这里是重点。

刷新成功：

后端生成：

新的 Access Token：

```
AccessToken-B
```

新的 Refresh Token：

```
RefreshToken-B
```

为什么两个都生成？

因为：

## Access Token旧的已经过期

必须换。

## Refresh Token也换

为了安全。

这叫：

> Refresh Token Rotation

---

此时：

旧：

```
AccessToken-A ❌
RefreshToken-A ❌
```

新：

```
AccessToken-B ✅
RefreshToken-B ✅
```

Redis：

删除：

```
refresh:admin:abc123
```

新增：

```
refresh:admin:def456
        |
        ↓
 RefreshToken-B
```

返回前端：

```
{
 "accessToken":"AccessToken-B",
 "refreshToken":"RefreshToken-B"
}
```

---

# 7. 前端收到新的Token怎么办？

替换本地：

原来：

```
localStorage

AccessToken-A
RefreshToken-A
```

改成：

```
AccessToken-B
RefreshToken-B
```

以后请求：

带：

```
AccessToken-B
```

# 8. 流程图

```
登录
 |
 |
生成
 |
 +----------------+
 |                |
 ↓                ↓
AccessToken-A     RefreshToken-A
30分钟            7天
 |
 |
访问接口
 |
 |
30分钟后
 |
 ↓
AccessToken-A过期
 |
 ↓
401
 |
 ↓
调用refresh接口
 |
 ↓
验证RefreshToken-A
 |
 ↓
Redis检查
 |
 ↓
生成
 |
 +----------------+
 |                |
 ↓                ↓
AccessToken-B     RefreshToken-B
 |
 |
删除旧RefreshToken-A
 |
 ↓
Redis保存RefreshToken-B
 |
 ↓
返回前端
 |
 ↓
替换旧Token
```
