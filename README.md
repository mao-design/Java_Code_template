## Java全栈技术模版

技术栈：

前端：Vue3、Vite、Element-Plus、Vue-Router、Pinia、TS、Axios

后端：Java17+、Spring Boot3+、Maven、Lombok、MyBatis-Plus、RabbitMQ、MySQL、Redis、Lua、RESTful API

小程序：uni-app、Vue3、TS

---

### **Dual-JWT-Login（双JWT登录）**

> 主要技术栈：Redis、双JWT（AccessToken、RefreshToken）
>
> 双JWT认证登录，支持多端同时登录（手机、电脑端可以同时登录）
>
> 主要原理：生成token时会加一个随机值，这样手机端和电脑端的token就不会互相干扰

### **API_Rate_Limiting**（AOP接口限流）

> 主要技术栈：Redis、Lua、AOP
>
> 滑动窗口接口限流，避免接口被刷，貌似企业中常用令牌桶限流
>
> 主要原理：通过AOP的方式，给要限流的接口添加限流规则，请求调用接口时会先通过AOP判断该请求是否正常，正常则放行，否则报错





## 更新日志

2026-9-5：更新Login模块，实现 双JWT（AccessToken、RefreshToken）,详细可查看 [JWT_Login.md](docs/JWT_Login.md)

2026-9-29：更新滑动窗口限流模块（Redis + Lua + AOP），实现接口限流，详细可查看 [API_Rate_Limiting.md](docs/API_Rate_Limiting.md)
