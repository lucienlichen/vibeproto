1. 设计原则

先满足 MVP；

表结构清晰，避免过度设计；

源码版本与发布版本分离；

文档和提示词资产可关联版本；

后续可平滑扩展 Git 与自动化能力。

2. 数据库表设计
2.1 用户表 sys_user

字段：

id

username

password

nickname

email

phone

status

created_at

updated_at

说明：

系统登录用户；

后续支持角色关联。

2.2 角色表 sys_role

字段：

id

role_code

role_name

status

created_at

updated_at

2.3 用户角色关联表 sys_user_role

字段：

id

user_id

role_id

2.4 权限表 sys_permission

字段：

id

permission_code

permission_name

type

path

parent_id

sort_no

2.5 项目表 project

字段：

id

name

code

description

project_type

status

default_build_profile_id

current_release_id

owner_id

created_by

created_at

updated_at

说明：

code 用于生成访问路径；

project_type 取值：html/vue/react/other；

status 取值：draft/active/archived。

2.6 项目成员表 project_member

字段：

id

project_id

user_id

role_in_project

created_at

说明：

项目维度的成员权限控制；

role_in_project 可取 owner/editor/viewer。

2.7 源码版本表 source_version

字段：

id

project_id

version_no

source_type

source_name

file_path

git_url

git_branch

commit_hash

html_content_path

remark

created_by

created_at

说明：

source_type：zip/git/html；

file_path：上传 zip 路径；

html_content_path：在线 HTML 文件路径。

2.8 构建配置表 build_profile

字段：

id

project_id

profile_name

node_version

install_command

build_command

output_dir

env_json

is_default

enabled

created_by

created_at

updated_at

说明：

env_json 用于存储环境变量；

output_dir 指构建产物目录。

2.9 构建任务表 build_task

字段：

id

project_id

source_version_id

build_profile_id

task_no

status

log_path

start_time

end_time

duration_ms

error_message

triggered_by

created_at

说明：

status：pending/running/success/failed/canceled；

log_path 指向构建日志文件。

2.10 发布版本表 release_version

字段：

id

project_id

source_version_id

build_task_id

release_no

release_name

deploy_path

access_url

snapshot_path

is_current

remark

created_by

created_at

说明：

release_no 如 v1/v2/v3；

access_url 为外部访问地址；

is_current 表示是否为 latest。

2.11 文档表 project_document

字段：

id

project_id

title

doc_type

content

related_source_version_id

related_release_id

version_no

created_by

created_at

updated_at

说明：

doc_type：prd/page_spec/interaction_spec/tech_note。

2.12 提示词资产表 prompt_asset

字段：

id

project_id

title

prompt_type

content

related_source_version_id

related_release_id

created_by

created_at

updated_at

说明：

prompt_type：ui_style/page_gen/code_impl/iteration_task。

2.13 系统配置表 system_config

字段：

id

config_key

config_value

remark

updated_at

示例：

domain.base

deploy.root.path

docker.builder.image

nginx.static.root

build.timeout.seconds

2.14 操作日志表 operation_log

字段：

id

user_id

module_name

operation_type

target_id

content

created_at

3. 表关系说明

一个项目有多个源码版本；

一个项目有多个构建配置；

一个源码版本可触发多个构建任务；

一个成功构建任务对应一个发布版本；

一个项目同时拥有多个文档和提示词资产；

项目表 current_release_id 指向当前展示版本。

4. 接口设计
4.1 认证接口
登录

POST /api/auth/login

请求：

{
  "username": "admin",
  "password": "123456"
}

响应：

{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
获取当前用户

GET /api/auth/me

退出登录

POST /api/auth/logout

4.2 项目管理接口
分页查询项目

GET /api/projects

参数：

pageNum

pageSize

name

projectType

status

创建项目

POST /api/projects

请求：

{
  "name": "隐患排查原型",
  "code": "inspection-demo",
  "description": "用于演示隐患排查系统原型",
  "projectType": "vue",
  "defaultBuildProfileId": 1
}
获取项目详情

GET /api/projects/{id}

更新项目

PUT /api/projects/{id}

删除项目

DELETE /api/projects/{id}

归档项目

POST /api/projects/{id}/archive

4.3 源码版本接口
上传 zip 创建源码版本

POST /api/source-versions/upload

表单字段：

projectId

file

remark

Git 导入源码版本

POST /api/source-versions/git-import

请求：

{
  "projectId": 1,
  "gitUrl": "https://xxx.git",
  "gitBranch": "main",
  "remark": "首次导入"
}
在线创建 HTML 源码版本

POST /api/source-versions/html-create

请求：

{
  "projectId": 1,
  "htmlContent": "<html>...</html>",
  "remark": "单页原型"
}
查询源码版本列表

GET /api/source-versions?projectId=1

删除源码版本

DELETE /api/source-versions/{id}

4.4 构建配置接口
查询构建配置列表

GET /api/build-profiles?projectId=1

创建构建配置

POST /api/build-profiles

请求：

{
  "projectId": 1,
  "profileName": "Vue3默认构建",
  "nodeVersion": "18",
  "installCommand": "npm install",
  "buildCommand": "npm run build",
  "outputDir": "dist",
  "envJson": "{}",
  "isDefault": true,
  "enabled": true
}
修改构建配置

PUT /api/build-profiles/{id}

删除构建配置

DELETE /api/build-profiles/{id}

4.5 构建任务接口
发起构建任务

POST /api/build-tasks

请求：

{
  "projectId": 1,
  "sourceVersionId": 10,
  "buildProfileId": 3,
  "remark": "发布首页优化版"
}
查询构建任务列表

GET /api/build-tasks?projectId=1

查询构建任务详情

GET /api/build-tasks/{id}

获取构建日志

GET /api/build-tasks/{id}/log

取消构建任务

POST /api/build-tasks/{id}/cancel

重试构建任务

POST /api/build-tasks/{id}/retry

4.6 发布版本接口
查询发布版本列表

GET /api/releases?projectId=1

获取发布版本详情

GET /api/releases/{id}

设为当前版本

POST /api/releases/{id}/set-current

删除发布版本

DELETE /api/releases/{id}

回滚到历史版本

POST /api/releases/{id}/rollback

说明：

MVP 中 set-current 与 rollback 可复用同一逻辑。

4.7 文档管理接口
查询文档列表

GET /api/documents?projectId=1

创建文档

POST /api/documents

请求：

{
  "projectId": 1,
  "title": "项目PRD",
  "docType": "prd",
  "content": "xxxx",
  "relatedSourceVersionId": null,
  "relatedReleaseId": null
}
更新文档

PUT /api/documents/{id}

删除文档

DELETE /api/documents/{id}

4.8 提示词接口
查询提示词列表

GET /api/prompts?projectId=1

创建提示词

POST /api/prompts

请求：

{
  "projectId": 1,
  "title": "首页实现提示词",
  "promptType": "code_impl",
  "content": "请基于Vue3实现...",
  "relatedReleaseId": 2
}
更新提示词

PUT /api/prompts/{id}

删除提示词

DELETE /api/prompts/{id}

4.9 系统配置接口
查询系统配置

GET /api/system/configs

更新系统配置

PUT /api/system/configs

4.10 预览辅助接口
获取项目预览信息

GET /api/projects/{id}/preview-info

响应示例：

{
  "code": 200,
  "message": "success",
  "data": {
    "latestUrl": "https://proto.xxx.com/projects/inspection-demo/latest/",
    "currentReleaseNo": "v3",
    "historyUrls": [
      {
        "releaseNo": "v1",
        "url": "https://proto.xxx.com/projects/inspection-demo/releases/v1/"
      }
    ]
  }
}
5. MVP 开发优先级
第一批接口

auth

projects

source-versions/upload

build-profiles

build-tasks

releases

第二批接口

documents

prompts

preview-info

第三批接口

git-import

html-create

system-config

6. 返回码规范建议

200：成功

400：参数错误

401：未登录

403：无权限

404：资源不存在

500：系统异常