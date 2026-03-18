1. 架构目标

本系统用于统一管理 vibecoding 产生的前端原型项目，要求满足以下技术目标：

支持多种前端项目纳管；

支持快速构建与静态部署；

支持版本化发布和回滚；

支持需求与提示词资产管理；

具备后续扩展 Git 集成和自动化流水线能力。

2. 总体架构

系统采用前后端分离架构，由以下部分组成：

前端管理端；

后端业务服务；

MySQL 数据库；

Redis 缓存与任务辅助；

MinIO 文件存储；

Docker 构建执行环境；

Nginx 静态发布服务；

项目源码与构建产物文件目录。

总体逻辑如下：

用户在前端管理端创建项目、上传代码、配置构建；

后端记录项目信息并生成构建任务；

构建任务在 Docker 容器中执行；

构建产物输出到部署目录；

Nginx 对外提供静态访问；

MySQL 存储元数据；

MinIO 存储源码包、日志、快照等文件。

3. 技术栈
3.1 前端

Vue 3

TypeScript

Vite

Element Plus

Pinia

Vue Router

Axios

选择原因：

适合后台管理系统；

AI 工具生成速度快；

与常见企业级 B/S 系统风格一致。

3.2 后端

Spring Boot 3

Spring Security

MyBatis Plus

Quartz 或 Spring Task

Lombok

Hutool

Jackson

选择原因：

易于搭建标准权限与管理后台；

易于扩展构建调度、项目管理、版本管理能力。

3.3 数据层

MySQL 8.x

Redis

MinIO

用途：

MySQL：业务数据；

Redis：缓存、任务锁、队列辅助；

MinIO：上传包、日志、截图、构建文件存储。

3.4 部署层

Docker

Docker Compose

Nginx

Node 构建镜像

4. 架构分层
4.1 表现层

前端页面：

登录页

项目列表页

项目详情页

源码版本页

构建配置页

构建任务页

发布版本页

文档管理页

提示词管理页

系统设置页

4.2 业务层

后端按模块拆分：

auth：认证授权

project：项目管理

source：源码版本管理

build：构建配置与构建任务

release：发布管理

document：需求文档管理

prompt：提示词资产管理

system：系统配置管理

file：文件上传下载管理

4.3 基础设施层

文件存储服务

Docker 执行器

Nginx 发布器

Git 拉取器

日志采集器

快照生成器

5. 核心技术设计
5.1 项目存储设计

每个项目在服务器有独立目录，例如：

/opt/vibeproto/projects/{projectCode}/
  repo/
  source/
  builds/
  releases/
  temp/

目录职责：

repo：Git 工作区或源码镜像；

source：上传的 zip、源码快照；

builds：临时构建目录；

releases：历史发布版本目录；

temp：临时文件。

5.2 构建执行设计

构建任务不直接在宿主机执行，而是在 Docker 容器中执行。

基本流程：

后端创建构建任务；

后端准备源码目录；

后端调用 Docker 执行器；

容器挂载项目构建目录；

执行安装命令和构建命令；

校验输出目录；

将产物复制到 releases；

更新 latest 映射。

优点：

环境隔离；

避免污染宿主机；

易支持多 Node 版本。

5.3 发布设计

每次发布成功后生成版本目录，例如：

/opt/vibeproto/projects/demo/releases/v1/
/opt/vibeproto/projects/demo/releases/v2/

latest 通过以下方式之一指向当前版本：

软链接；

Nginx alias 映射；

数据库记录 + 动态路由。

建议优先使用软链接方式：

/opt/vibeproto/projects/demo/current -> /opt/vibeproto/projects/demo/releases/v2/

Nginx 暴露：

/projects/demo/latest/

/projects/demo/releases/v1/

5.4 版本设计

版本分为两层：

源码版本

代表一次导入或提交的代码快照。
来源可以是 zip / Git / HTML 内容。

发布版本

代表一次构建成功后形成的可访问站点版本。

关系：

一个源码版本可对应多个构建任务；

一个成功构建任务产生一个发布版本；

latest 指向某一个发布版本。

5.5 构建模板设计

构建模板是复用能力，示例：

HTML 模板

install command：空

build command：空

output dir：source root

Vue3 + Vite 模板

install command：npm install

build command：npm run build

output dir：dist

React + Vite 模板

install command：npm install

build command：npm run build

output dir：dist

5.6 文档与提示词资产设计

需求文档和提示词不作为附件散存，应作为业务实体管理。

目标：

支持结构化保存；

支持版本化；

支持与项目、源码版本、发布版本关联；

支持复制导出给 AI 工具。

6. 接口架构原则

后端 API 使用 RESTful 风格，统一约定：

/api/auth/*

/api/projects/*

/api/source-versions/*

/api/build-profiles/*

/api/build-tasks/*

/api/releases/*

/api/documents/*

/api/prompts/*

/api/system/*

统一返回结构：

{
  "code": 200,
  "message": "success",
  "data": {}
}
7. 权限架构

采用 RBAC 模型：

用户

角色

权限

菜单

按项目的数据权限

控制范围：

菜单权限

按钮权限

API 权限

项目数据访问权限

8. 日志与审计

日志分四类：

登录日志；

操作日志；

构建日志；

系统异常日志。

重点要求：

构建日志可追溯；

发布、回滚、删除等关键操作必须记录。

9. 部署架构建议
9.1 单机 MVP 架构

使用 Docker Compose 部署：

前端

后端

MySQL

Redis

MinIO

Nginx

适合首版快速落地。

9.2 后续扩展架构

可扩展为：

管理服务

构建服务

文件存储服务

独立 Nginx 发布层

10. 安全要求

上传文件校验类型和大小；

限制可执行命令来源，仅允许白名单模板；

构建容器资源限制；

敏感配置加密存储；

发布目录禁止越权访问；

所有接口需鉴权。

11. 可扩展设计预留

对接 Gitea/GitLab；

Webhook 自动构建；

多环境发布；

项目分享链接；

AI 编程任务记录；

发布前自动截图；

在线源码预览。