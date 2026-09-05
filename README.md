# 拾光集市 · 二手交易平台

一个可直接运行的单体项目：Spring Boot 3 + Java 17 + MyBatis-Plus + MySQL + JWT，前端使用 Vue 3 + TypeScript + Vite + Element Plus。生产构建后的 Vue 静态文件会被打进同一个 Spring Boot JAR。

## 功能

- 普通用户：首页搜索、分类筛选、商品详情、收藏、举报、发布商品、我的发布/举报/收藏、个人资料。
- 管理员：数据概览（趋势图）、商品审核、商品上下架/删除、账号启停/封禁/删除、举报处理（包含商品详情、描述、发布用户）。
- 图片上传：本地 `uploads/` 目录，访问路径为 `/uploads/<文件名>`。
- JWT 无状态登录，后端接口按角色进行授权，封禁用户会立即失效。

## 快速运行

### 1. 启动 MySQL

需要 Docker：

```bash
docker compose up -d mysql
```

也可以自行创建 `secondhand_market` 数据库，然后设置环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。

### 2. 本地开发（前后端分开）

```bash
cd frontend
npm install
npm run dev
```

另开终端：

```bash
mvn spring-boot:run
```

前端开发地址：`http://localhost:5173`，API 会自动代理到 `http://localhost:8080`。

### 3. 打包并运行单体 JAR

```bash
cd frontend && npm install && npm run build
cd ..
mvn clean package -DskipTests
java -jar target/secondhand-market-1.0.0.jar
```

打开 `http://localhost:8080` 即可。首次启动会自动建表并写入演示数据。

## 演示账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 普通用户 | `demo` | `demo123` |

请在正式环境通过环境变量修改 `JWT_SECRET` 和数据库密码。

## 环境变量

`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`、`JWT_EXPIRATION_HOURS`、`UPLOAD_DIR`、`SERVER_PORT` 均可覆盖默认配置。
