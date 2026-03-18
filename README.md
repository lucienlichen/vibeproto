# VibeCoding 原型管理系统

一个服务于 VibeCoding 工作流的原型管理平台，支持快速纳管 AI 生成的前端原型，实现自动构建、版本管理、快速发布与一键回滚。

## 系统目标

- 统一管理 AI 编程工具生成的 Web 前端原型项目
- 支持 zip 上传、Git 导入、在线 HTML 创建等多种代码纳管方式
- 自动构建部署，生成稳定的 latest 访问地址
- 版本可追踪，支持历史版本访问与一键回滚
- 沉淀需求文档与 AI 提示词资产，与版本建立关联

## 技术栈

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.4 | 主框架 |
| TypeScript | ^5.x | 类型安全 |
| Vite | ^5.x | 构建工具 |
| Element Plus | ^2.x | UI 组件库 |
| Pinia | ^2.x | 状态管理 |
| Vue Router | ^4.x | 路由管理 |
| Axios | ^1.x | HTTP 客户端 |

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.x | 主框架 |
| Spring Security | 3.x | 认证授权 |
| MyBatis Plus | 3.x | ORM |
| Quartz | 2.x | 任务调度 |
| Lombok | latest | 代码简化 |
| Hutool | 5.x | 工具集 |
| Jackson | 2.x | JSON 处理 |

### 基础设施
| 技术 | 用途 |
|------|------|
| MySQL 8.x | 业务数据存储 |
| Redis | 缓存、任务锁、队列辅助 |
| MinIO | 源码包、日志、截图等文件存储 |
| Docker | 构建执行环境隔离 |
| Docker Compose | 本地/单机部署编排 |
| Nginx | 静态文件发布服务 |

## 目录结构

```
vibeproto/
├── frontend/               # Vue 3 前端工程
│   ├── src/
│   │   ├── api/            # API 接口封装
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # 公共组件
│   │   ├── layouts/        # 布局组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── types/          # TypeScript 类型定义
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面组件
│   │       ├── auth/       # 登录页
│   │       ├── project/    # 项目管理
│   │       ├── build/      # 构建任务
│   │       ├── release/    # 发布版本
│   │       ├── document/   # 文档管理
│   │       ├── prompt/     # 提示词管理
│   │       ├── system/     # 系统设置
│   │       └── user/       # 用户权限
│   ├── public/
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── backend/                # Spring Boot 后端工程
│   └── src/main/java/com/vibeproto/
│       ├── auth/           # 认证授权模块
│       ├── project/        # 项目管理模块
│       ├── source/         # 源码版本模块
│       ├── build/          # 构建配置与任务模块
│       ├── release/        # 发布管理模块
│       ├── document/       # 文档管理模块
│       ├── prompt/         # 提示词资产模块
│       ├── system/         # 系统配置模块
│       ├── file/           # 文件管理模块
│       └── common/         # 公共基础设施
│
├── deploy/                 # 部署配置
│   ├── docker-compose.yml  # 单机部署编排
│   ├── nginx/              # Nginx 配置
│   ├── mysql/              # 数据库初始化 SQL
│   └── scripts/            # 运维脚本
│
└── docs/                   # 设计文档
```

## 快速开始

### 前置依赖

- Docker & Docker Compose
- Node.js 18+（前端开发）
- Java 17+（后端开发）
- Maven 3.8+

### 一键启动（开发环境）

```bash
# 启动基础设施（MySQL / Redis / MinIO / Nginx）
cd deploy
docker-compose up -d

# 启动后端
cd backend
./mvnw spring-boot:run

# 启动前端
cd frontend
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端管理端 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| MinIO 控制台 | http://localhost:9001 |
| 原型预览（Nginx） | http://localhost:8090/projects/{code}/latest/ |

## 业务流程

```
创建项目 → 上传源码 → 配置构建 → 发起构建 → 生成发布版本 → 访问预览
                                                    ↓
                                              设为 latest 地址
                                                    ↓
                                            历史版本管理/回滚
```

## 开发规范

- 后端接口统一前缀 `/api`，返回结构 `{code, message, data}`
- 前端组件以 PascalCase 命名，文件以 kebab-case 命名
- 数据库字段统一 snake_case，Java 实体统一 camelCase
- 所有接口需鉴权（登录白名单除外）
- 关键操作（发布、回滚、删除）必须记录操作日志

## 文档索引

- [系统架构设计](docs/VibeCoding%20原型管理系统.md)
- [产品需求文档 PRD](docs/《VibeCoding%20原型管理系统》产品需求文档（PRD）.md)
- [前端页面与交互说明](docs/VibeCoding%20原型管理系统》前端页面与交互说明文档.md)
- [数据库与接口设计](docs/VibeCoding%20原型管理系统数据库与接口设计文档.md)
