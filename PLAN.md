# VibeCoding 原型管理系统 — 开发计划

> 制定日期：2026-03-18
> 基于文档：系统架构设计 / PRD / 前端交互说明 / 数据库与接口设计

---

## 一、技术选型说明

### 前端
- **Vue 3 + TypeScript + Vite**：组合式 API 支持更清晰的模块拆分，TS 保障类型安全，Vite 提升开发体验
- **Element Plus**：企业级组件库，与后台管理系统风格高度匹配，表单/表格/弹窗组件成熟
- **Pinia**：Vue 官方推荐的轻量状态管理，取代 Vuex，支持 TypeScript 完整推断
- **Vue Router 4**：嵌套路由支持项目详情页 Tab 结构
- **Axios**：统一封装请求拦截、Token 注入、错误处理

### 后端
- **Spring Boot 3 + Java 17**：LTS 版本，支持 Jakarta EE 命名空间，与 Spring Security 6 配合使用
- **Spring Security + JWT**：无状态认证，适合前后端分离；JWT 存入 Authorization Header
- **MyBatis Plus**：减少 CRUD 样板代码，BaseMapper/IService 覆盖常规操作，复杂查询写 XML
- **Quartz**：构建任务异步执行与状态轮询，支持任务并发控制与超时管理
- **MinIO Java SDK**：文件上传/下载/签名 URL 生成
- **Docker Java Client（spotify/docker-client 或 com.github.docker-java）**：调用 Docker API 执行构建容器

### 数据层
- **MySQL 8.x**：业务元数据，使用 utf8mb4 字符集
- **Redis**：JWT 黑名单、构建任务锁（SETNX）、构建日志流缓存
- **MinIO**：源码 zip、构建日志文件、页面快照图片

### 部署
- **Docker Compose**：MVP 阶段单机部署，所有依赖服务容器化
- **Nginx**：静态文件服务，通过 alias 映射项目发布目录，实现 `/projects/{code}/latest/` 访问

---

## 二、整体里程碑

| 阶段 | 内容 | 预估时间 |
|------|------|----------|
| Phase 0 | 项目初始化、基础设施搭建 | 1 周 |
| Phase 1 | P0 核心功能（登录/项目/构建/发布） | 3 周 |
| Phase 2 | P1 扩展功能（文档/提示词/Git导入/二维码） | 2 周 |
| Phase 3 | P2 高级功能（Webhook/截图/多环境） | 后续迭代 |

---

## 三、详细开发计划

### Phase 0：项目初始化（基础设施）

#### 0.1 后端工程初始化
- [x] 使用 Spring Initializr 创建 Maven 工程（Java 17，Spring Boot 3.x）
- [x] 添加核心依赖：Spring Web / Spring Security / MyBatis Plus / MySQL Driver / Lombok / Hutool / Jackson / Validation
- [x] 添加 Redis（Spring Data Redis + Lettuce）、MinIO SDK、Quartz
- [x] 配置 `application.yml`：数据库/Redis/MinIO/JWT 参数
- [x] 配置 `application-dev.yml` 本地开发覆盖配置
- [x] 统一响应结构 `Result<T>`：`{code, message, data}`
- [x] 全局异常处理 `GlobalExceptionHandler`
- [x] 配置 `CorsConfig` 跨域
- [x] 配置 `MybatisPlusConfig`（分页插件）
- [x] 编写 `HealthController` 验证环境

#### 0.2 前端工程初始化
- [x] `npm create vite@latest frontend -- --template vue-ts`
- [x] 安装依赖：element-plus / pinia / vue-router / axios / @vueuse/core
- [x] 安装开发依赖：@types/node / unplugin-auto-import / unplugin-vue-components（按需引入 Element Plus）
- [x] 配置 `vite.config.ts`：路径别名 `@`、开发代理 `/api → localhost:8080`
- [x] 配置 `tsconfig.json`
- [x] 创建 Axios 实例，封装拦截器（Token 注入、统一错误提示、401 跳转登录）
- [x] 创建基础布局组件 `DefaultLayout.vue`（顶部栏 + 侧边菜单 + 内容区）
- [x] 配置路由守卫（未登录跳转 `/login`）

#### 0.3 基础设施（Docker Compose）
- [x] 编写 `deploy/docker-compose.yml`：
  - MySQL 8 容器（端口 3306，挂载 init SQL）
  - Redis 7 容器（端口 6379）
  - MinIO 容器（端口 9000/9001，配置 bucket）
  - Nginx 容器（端口 8090，挂载配置和项目目录）
- [x] 编写 `deploy/mysql/init/01_schema.sql`：所有建表 DDL
- [x] 编写 `deploy/mysql/init/02_data.sql`：初始数据（admin 用户、角色、默认构建模板）
- [x] 编写 `deploy/nginx/nginx.conf`：静态文件服务配置

---

### Phase 1：P0 核心功能

#### 1.1 数据库建表 DDL

按以下顺序创建（依赖关系排序）：

```
sys_user → sys_role → sys_user_role → sys_permission
→ project → project_member
→ source_version
→ build_profile → build_task
→ release_version
→ system_config → operation_log
```

所有表公共字段：
- `id` BIGINT AUTO_INCREMENT PRIMARY KEY
- `created_at` / `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
- 字符集：utf8mb4，排序：utf8mb4_unicode_ci

#### 1.2 认证模块（auth）

**后端**
- [ ] `SysUser` 实体 + Mapper + Service
- [ ] `UserDetailsServiceImpl` 实现 Spring Security 用户加载
- [x] JWT 工具类 `JwtUtils`（生成/解析/刷新）
- [x] `JwtAuthenticationFilter` 过滤器（每次请求校验 Token）
- [x] `SecurityConfig`：白名单配置（`/api/auth/login`）
- [x] `AuthController`：POST /api/auth/login、GET /api/auth/me、POST /api/auth/logout
- [ ] Redis 存储 JWT 黑名单（退出登录使 Token 失效）
- [ ] 操作日志 AOP `OperationLogAspect`（后续所有操作日志通过此切面自动记录）

**前端**
- [x] `views/auth/LoginView.vue`：登录表单
- [x] `stores/userStore.ts`：存储 token、userInfo，登录/退出方法
- [x] 登录成功后跳转 `/projects`

#### 1.3 项目管理模块（project）

**后端**
- [x] `Project` 实体 + Mapper + Service + Controller
- [ ] `ProjectMember` 实体 + Mapper（项目权限控制预留）
- [x] 接口实现：
  - GET /api/projects（分页，支持 name/projectType/status 过滤）
  - POST /api/projects（创建，code 唯一性校验）
  - GET /api/projects/:id
  - PUT /api/projects/:id
  - DELETE /api/projects/:id（软删除）
  - POST /api/projects/:id/archive
  - GET /api/projects/:id/preview-info
- [x] `ProjectType` 枚举：html/vue/react/other
- [x] `ProjectStatus` 枚举：draft/active/archived

**前端**
- [x] `views/project/ProjectListView.vue`：列表、搜索筛选、分页
- [x] `views/project/ProjectDetailView.vue`：详情页 + Tab 容器
- [x] `components/common/ProjectFormDialog.vue`：新建/编辑弹窗
- [x] `stores/projectStore.ts`
- [x] `api/project.ts`：封装所有项目接口

#### 1.4 源码版本模块（source）

**后端**
- [x] `SourceVersion` 实体 + Mapper + Service + Controller
- [x] `SourceType` 枚举：zip/git/html
- [ ] MinIO 文件上传服务 `FileService`（上传、下载 URL 生成）
- [x] 接口实现：
  - POST /api/source-versions/upload（MultipartFile，保存到 MinIO）
  - POST /api/source-versions/git-import（记录 Git 信息，后台异步拉取）
  - POST /api/source-versions/html-create（保存 HTML 内容到 MinIO）
  - GET /api/source-versions?projectId=
  - DELETE /api/source-versions/:id（检查是否有关联构建任务）
- [x] 版本号自动生成：`sv-{projectCode}-{序号}` 格式
- [ ] zip 文件解压到项目源码目录 `/opt/vibeproto/projects/{code}/source/`

**前端**
- [x] `views/project/tabs/SourceVersionTab.vue`
- [x] `components/common/UploadZipDialog.vue`
- [x] `components/common/GitImportDialog.vue`
- [x] `components/common/HtmlCreateDialog.vue`
- [x] `api/sourceVersion.ts`

#### 1.5 构建配置模块（build-profile）

**后端**
- [x] `BuildProfile` 实体 + Mapper + Service + Controller
- [x] 接口实现：
  - GET /api/build-profiles?projectId=
  - POST /api/build-profiles
  - PUT /api/build-profiles/:id
  - DELETE /api/build-profiles/:id
- [x] 内置默认模板数据（HTML/Vue3/React，系统初始化时写入）
- [x] 设为默认时取消其他配置的默认状态（同一项目只一个默认）

**前端**
- [x] `views/project/tabs/BuildProfileTab.vue`
- [x] `components/common/BuildProfileFormDialog.vue`
- [x] `api/buildProfile.ts`

#### 1.6 构建任务模块（build-task）

这是系统最核心的模块，涉及 Docker 容器执行。

**后端**
- [x] `BuildTask` 实体 + Mapper + Service + Controller
- [x] `BuildStatus` 枚举：pending/running/success/failed/canceled
- [ ] `DockerExecutor` 服务：
  - 根据 NodeVersion 选择构建镜像
  - 挂载项目目录到容器 `/workspace`
  - 执行 installCommand + buildCommand
  - 采集容器 stdout/stderr 写入日志文件
  - 设置资源限制（CPU/Memory）
  - 处理超时（`build.timeout.seconds` 配置项）
- [x] 异步执行框架（Quartz Job 或 Spring @Async + ThreadPoolExecutor）：
  - 任务入队时状态为 pending
  - 开始执行时状态改为 running
  - 执行完成根据退出码更新 success/failed
- [ ] 构建产物处理：
  - 校验 outputDir 存在
  - 复制到 `/opt/vibeproto/projects/{code}/releases/v{n}/`
  - 自动生成 `release_version` 记录
  - 更新软链接 `current → releases/v{n}`
- [ ] 日志文件保存到 MinIO（`logs/{projectCode}/{taskNo}.log`）
- [x] 接口实现：
  - POST /api/build-tasks（触发构建，返回 taskId）
  - GET /api/build-tasks?projectId=
  - GET /api/build-tasks/:id
  - GET /api/build-tasks/:id/log（返回日志文本）
  - POST /api/build-tasks/:id/cancel
  - POST /api/build-tasks/:id/retry

**前端**
- [x] `views/build/BuildTaskListView.vue`：全局构建任务列表
- [x] `views/project/tabs/BuildTaskInProjectTab.vue`：项目维度任务列表
- [x] `components/common/BuildLogDrawer.vue`：日志抽屉（轮询刷新）
- [ ] `stores/buildStore.ts`
- [x] `api/buildTask.ts`

#### 1.7 发布版本模块（release）

**后端**
- [x] `ReleaseVersion` 实体 + Mapper + Service + Controller
- [ ] Nginx 配置动态更新或软链接切换（`current` 软链接方案优先）
- [x] 接口实现：
  - GET /api/releases?projectId=
  - GET /api/releases/:id
  - POST /api/releases/:id/set-current（更新软链接 + is_current 字段）
  - POST /api/releases/:id/rollback（与 set-current 逻辑复用）
  - DELETE /api/releases/:id（当前版本不允许直接删除）
- [x] 发布地址格式：`{domain}/projects/{code}/releases/v{n}/`
- [x] latest 地址格式：`{domain}/projects/{code}/latest/`

**前端**
- [x] `views/project/tabs/ReleaseVersionTab.vue`
- [x] `components/common/PreviewInfoCard.vue`：latest 地址 + 二维码
- [x] `stores/releaseStore.ts`
- [x] `api/release.ts`

---

### Phase 2：P1 扩展功能

#### 2.1 需求文档模块（document）
- [x] `ProjectDocument` 实体 + Mapper + Service + Controller
- [x] `DocType` 枚举：prd/page_spec/interaction_spec/tech_note
- [x] 接口：CRUD + 关联版本查询
- [x] 前端：`views/project/tabs/DocumentTab.vue` + 富文本/Markdown 编辑器（推荐 @vueup/vue-quill 或 md-editor-v3）

#### 2.2 提示词资产模块（prompt）
- [ ] `PromptAsset` 实体 + Mapper + Service + Controller
- [ ] `PromptType` 枚举：ui_style/page_gen/code_impl/iteration_task
- [ ] 接口：CRUD
- [ ] 前端：`views/project/tabs/PromptTab.vue` + 一键复制功能

#### 2.3 系统配置模块（system）
- [ ] `SystemConfig` 实体 + Mapper + Service + Controller
- [ ] 配置项：domain.base / deploy.root.path / docker.builder.image / nginx.static.root / build.timeout.seconds
- [ ] 接口：GET /api/system/configs + PUT /api/system/configs
- [ ] 前端：`views/system/SystemSettingsView.vue`

#### 2.4 用户权限模块（user）
- [ ] `SysRole` / `SysUserRole` / `SysPermission` 实体及 Mapper
- [ ] 角色管理接口（CRUD）
- [ ] 用户管理接口（CRUD，密码 BCrypt 加密）
- [ ] 用户角色分配接口
- [ ] 前端：`views/user/UserManageView.vue` + `views/user/RoleManageView.vue`

#### 2.5 Git 导入增强
- [ ] 后台 Git 拉取实现（JGit 库 or 执行 git CLI 命令）
- [ ] 支持 HTTPS 认证 / SSH key 配置
- [ ] 前端 Git 导入弹窗完善

#### 2.6 二维码生成
- [ ] 后端使用 ZXing 生成二维码图片（或前端 qrcode.js）
- [ ] 集成到 `PreviewInfoCard.vue`

---

### Phase 3：P2 高级功能（后续迭代）

- [ ] Gitea/GitLab Webhook 自动触发构建
- [ ] 构建完成后自动截图（Puppeteer/Playwright 容器化）
- [ ] 多环境发布（dev/staging/prod 独立发布路径）
- [ ] 在线源码预览（CodeMirror 集成）
- [ ] AI 编程任务记录关联

---

## 四、关键文件清单

### 数据库 DDL（deploy/mysql/init/01_schema.sql）
包含 14 张表的完整建表语句（见数据库设计文档 2.1-2.14）

### 后端配置（backend/src/main/resources/application.yml）
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vibeproto?useUnicode=true&characterEncoding=utf8mb4
  redis:
    host: localhost
    port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: vibeproto

vibeproto:
  jwt:
    secret: <replace-with-strong-secret>
    expiration: 86400
  deploy:
    root-path: /opt/vibeproto/projects
  build:
    timeout-seconds: 300
    docker-image: node:18-alpine
```

### Docker Compose（deploy/docker-compose.yml）
```yaml
services:
  mysql: ...       # 3306
  redis: ...       # 6379
  minio: ...       # 9000/9001
  nginx: ...       # 8090（静态文件）
  backend: ...     # 8080（后端 API）
  frontend: ...    # 80（生产 Nginx 前端）
```

---

## 五、接口分批开发顺序

### 第一批（P0 核心）
1. `POST /api/auth/login`
2. `GET /api/auth/me`
3. `GET/POST/PUT/DELETE /api/projects`
4. `POST /api/projects/:id/archive`
5. `POST /api/source-versions/upload`
6. `GET /api/source-versions`
7. `GET/POST/PUT/DELETE /api/build-profiles`
8. `POST /api/build-tasks`
9. `GET /api/build-tasks`
10. `GET /api/build-tasks/:id/log`
11. `POST /api/build-tasks/:id/cancel`
12. `POST /api/build-tasks/:id/retry`
13. `GET /api/releases`
14. `POST /api/releases/:id/set-current`
15. `POST /api/releases/:id/rollback`
16. `GET /api/projects/:id/preview-info`

### 第二批（P1 扩展）
17. `GET/POST/PUT/DELETE /api/documents`
18. `GET/POST/PUT/DELETE /api/prompts`
19. `POST /api/source-versions/git-import`
20. `POST /api/source-versions/html-create`
21. `GET/PUT /api/system/configs`
22. 用户/角色管理接口

---

## 六、前端页面优先级

### 第一批（P0）
1. 登录页 `/login`
2. 项目列表页 `/projects`
3. 项目详情页 `/projects/:id`（含 Tab 容器）
   - 源码版本 Tab
   - 构建配置 Tab
   - 构建任务 Tab（项目维度）
   - 发布版本 Tab
4. 全局构建任务页 `/build-tasks`
5. 构建日志抽屉组件

### 第二批（P1）
6. 文档管理 Tab + 编辑页
7. 提示词管理 Tab
8. 系统设置页 `/system`

### 第三批（P2 预留）
9. 用户权限页 `/users` `/roles`
10. Git 导入增强
11. 二维码/快照功能

---

## 七、代码规范约定

### 后端
- 包名：`com.vibeproto.{module}.{layer}`（controller/service/entity/dto/mapper）
- Controller 只做参数校验和响应转换，业务逻辑全在 Service
- Service 接口 + Impl 分离（方便后续单测 Mock）
- 数据库操作尽量通过 MyBatis Plus，复杂查询写 XML（`resources/mapper/`）
- 所有异常通过 `throw new BizException(code, message)` 抛出，全局捕获
- DTO/VO 分离：入参用 XxxRequest，出参用 XxxVO/XxxDTO

### 前端
- API 封装放在 `src/api/` 目录，按模块拆分文件
- Pinia Store 放在 `src/stores/` 目录，严格区分 state/getters/actions
- 通用组件放在 `src/components/common/`，模块专用组件放在对应 views 目录下
- 路由配置集中在 `src/router/index.ts`，使用懒加载
- 统一 API 响应类型 `ApiResponse<T>` 定义在 `src/types/api.ts`

---

## 八、安全要求落地

| 要求 | 实现方式 |
|------|----------|
| 所有接口需鉴权 | `JwtAuthenticationFilter` 全局过滤 |
| 上传文件校验类型 | 后端校验 ContentType + 文件头魔数 |
| 上传文件限制大小 | `spring.servlet.multipart.max-file-size: 100MB` |
| 构建命令白名单 | 构建配置只允许模板内预设命令，禁止任意命令注入 |
| 构建容器资源限制 | Docker run 加 `--cpus=1 --memory=512m` |
| 发布目录访问隔离 | Nginx 配置限制跨项目访问，无目录列表 |
| 敏感配置加密 | JWT Secret / DB Password 通过环境变量注入，不硬编码 |

---

## 九、测试策略

| 层次 | 工具 | 覆盖重点 |
|------|------|----------|
| 后端单元测试 | JUnit 5 + Mockito | Service 层业务逻辑 |
| 后端集成测试 | Spring Boot Test + TestContainers | 核心接口端到端 |
| 前端单元测试 | Vitest + Vue Test Utils | Store / 工具函数 |
| 端到端测试（可选） | Playwright | 登录 → 上传 → 构建 → 发布完整流程 |

---

## 十、验收标准（P0 完成标志）

- [ ] 管理员可登录系统
- [ ] 可创建原型项目
- [ ] 可上传 zip 源码包并生成源码版本记录
- [ ] 可配置构建参数并保存
- [ ] 可触发构建任务并查看实时日志
- [ ] 构建成功后自动生成发布版本
- [ ] 发布版本可通过 `/projects/{code}/latest/` 访问
- [ ] 可切换历史版本（回滚）
- [ ] latest 地址始终指向当前设置的版本
- [ ] 所有关键操作记录到操作日志
