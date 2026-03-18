SET NAMES utf8mb4;
USE vibeproto;

-- password: Admin@123
INSERT INTO sys_user (username, password, nickname, email, status)
VALUES ('admin', '$2b$10$5lDEWjMQuphpolqMsRUvJu0Hxpk0X1c3CA654hV6aK.l1G/U3Skke', '系统管理员', 'admin@vibeproto.local', 'active')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), email = VALUES(email), status = VALUES(status);

INSERT INTO sys_role (role_code, role_name, status)
VALUES
  ('SUPER_ADMIN', '超级管理员', 'active'),
  ('PRODUCT_MANAGER', '产品经理', 'active'),
  ('VIEWER', '访客', 'active')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), status = VALUES(status);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'SUPER_ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO build_profile (project_id, profile_name, node_version, install_command, build_command, output_dir, env_json, is_default, enabled)
VALUES
  (NULL, 'HTML 默认模板', 'node:18-alpine', 'npm install', 'npm run build', 'dist', JSON_OBJECT(), 1, 1),
  (NULL, 'Vue3 默认模板', 'node:18-alpine', 'npm install', 'npm run build', 'dist', JSON_OBJECT(), 0, 1),
  (NULL, 'React 默认模板', 'node:18-alpine', 'npm install', 'npm run build', 'dist', JSON_OBJECT(), 0, 1)
ON DUPLICATE KEY UPDATE
  node_version = VALUES(node_version),
  install_command = VALUES(install_command),
  build_command = VALUES(build_command),
  output_dir = VALUES(output_dir),
  enabled = VALUES(enabled);

INSERT INTO system_config (config_key, config_value, description)
VALUES
  ('system.base-preview-url', 'http://localhost:8090', '原型预览服务的基础 URL，用于拼接版本访问地址'),
  ('storage.root-path', '/tmp/vibeproto-storage', '上传文件的本地存储根目录'),
  ('deploy.root-path', '/Users/lucien/.vibeproto/projects', '构建产物的部署根目录'),
  ('build.timeout-seconds', '300', '构建任务超时时间（秒）'),
  ('build.docker-image', 'node:18-alpine', 'Node.js 构建使用的 Docker 镜像')
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  description = VALUES(description);
