package com.vibeproto.build.task;

import com.vibeproto.build.entity.BuildProfile;
import com.vibeproto.build.entity.BuildTask;
import com.vibeproto.build.enums.BuildStatus;
import com.vibeproto.build.mapper.BuildProfileMapper;
import com.vibeproto.build.mapper.BuildTaskMapper;
import com.vibeproto.common.config.properties.DeployProperties;
import com.vibeproto.common.config.properties.StorageProperties;
import com.vibeproto.file.service.FileStorageService;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import com.vibeproto.release.service.ReleaseVersionService;
import com.vibeproto.source.entity.SourceVersion;
import com.vibeproto.source.mapper.SourceVersionMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class BuildTaskExecutor {

    private final BuildTaskMapper buildTaskMapper;
    private final BuildProfileMapper buildProfileMapper;
    private final ProjectMapper projectMapper;
    private final SourceVersionMapper sourceVersionMapper;
    private final FileStorageService fileStorageService;
    private final ReleaseVersionService releaseVersionService;
    private final StorageProperties storageProperties;
    private final DeployProperties deployProperties;

    public BuildTaskExecutor(BuildTaskMapper buildTaskMapper,
                             BuildProfileMapper buildProfileMapper,
                             ProjectMapper projectMapper,
                             SourceVersionMapper sourceVersionMapper,
                             FileStorageService fileStorageService,
                             ReleaseVersionService releaseVersionService,
                             StorageProperties storageProperties,
                             DeployProperties deployProperties) {
        this.buildTaskMapper = buildTaskMapper;
        this.buildProfileMapper = buildProfileMapper;
        this.projectMapper = projectMapper;
        this.sourceVersionMapper = sourceVersionMapper;
        this.fileStorageService = fileStorageService;
        this.releaseVersionService = releaseVersionService;
        this.storageProperties = storageProperties;
        this.deployProperties = deployProperties;
    }

    @Async("buildTaskPool")
    public void execute(Long taskId) {
        BuildTask task = buildTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        Project project = projectMapper.selectById(task.getProjectId());
        SourceVersion sourceVersion = sourceVersionMapper.selectById(task.getSourceVersionId());
        BuildProfile buildProfile = buildProfileMapper.selectById(task.getBuildProfileId());

        LocalDateTime start = LocalDateTime.now();
        task.setStatus(BuildStatus.running.name());
        task.setStartTime(start);
        buildTaskMapper.updateById(task);

        try {
            BuildTask latest = buildTaskMapper.selectById(taskId);
            if (latest == null || BuildStatus.canceled.name().equals(latest.getStatus())) {
                writeLog(project, task, "构建任务已取消\n");
                return;
            }

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("开始构建\n");
            logBuilder.append("项目: ").append(project.getName()).append('\n');
            logBuilder.append("源码版本: ").append(sourceVersion.getVersionNo()).append('\n');
            logBuilder.append("构建配置: ").append(buildProfile.getProfileName()).append('\n');
            logBuilder.append("源码类型: ").append(sourceVersion.getSourceType()).append('\n');

            String sourceType = sourceVersion.getSourceType();
            long releaseCount = countReleases(task.getProjectId());
            String versionNo = "v" + (releaseCount + 1);
            Path releaseDir = Path.of(deployProperties.rootPath(), project.getCode(), "releases", versionNo);
            Files.createDirectories(releaseDir);

            if (sourceVersion.getFilePath() != null && ("zip".equals(sourceType) || "git".equals(sourceType))) {
                Path zipFile = Path.of(storageProperties.rootPath()).resolve(sourceVersion.getFilePath());
                if (Files.exists(zipFile)) {
                    logBuilder.append("解压源码包: ").append(zipFile.getFileName()).append('\n');
                    extractZip(zipFile, releaseDir, buildProfile.getOutputDir());
                    logBuilder.append("源码解压完成\n");
                } else {
                    logBuilder.append("警告: 源码文件不存在，创建占位页面\n");
                    writePlaceholder(releaseDir, project.getName(), versionNo);
                }
            } else if ("html".equals(sourceType) && sourceVersion.getHtmlContentPath() != null) {
                Path htmlFile = Path.of(storageProperties.rootPath()).resolve(sourceVersion.getHtmlContentPath());
                if (Files.exists(htmlFile)) {
                    logBuilder.append("复制 HTML 源码\n");
                    Files.copy(htmlFile, releaseDir.resolve("index.html"), StandardCopyOption.REPLACE_EXISTING);
                    logBuilder.append("HTML 复制完成\n");
                } else {
                    logBuilder.append("警告: HTML 文件不存在，创建占位页面\n");
                    writePlaceholder(releaseDir, project.getName(), versionNo);
                }
            } else {
                // git or unknown — create placeholder
                logBuilder.append("TODO: Git/Vue/React 构建暂不支持自动执行，创建占位页面\n");
                writePlaceholder(releaseDir, project.getName(), versionNo);
            }

            fixAbsolutePaths(releaseDir, project.getCode(), versionNo);
            logBuilder.append("构建目录: ").append(releaseDir).append('\n');
            logBuilder.append("构建完成\n");

            String logPath = writeLog(project, task, logBuilder.toString());
            BuildTask successTask = buildTaskMapper.selectById(taskId);
            successTask.setStatus(BuildStatus.success.name());
            successTask.setLogPath(logPath);
            successTask.setEndTime(LocalDateTime.now());
            successTask.setDurationMs(Duration.between(start, successTask.getEndTime()).toMillis());
            buildTaskMapper.updateById(successTask);
            releaseVersionService.createFromBuildTask(taskId);
        } catch (Exception exception) {
            String errMsg = exception.getMessage();
            try {
                String logPath = writeLog(project, task, "构建失败\n错误信息: " + errMsg + "\n");
                task.setLogPath(logPath);
                buildTaskMapper.updateById(task);
            } catch (Exception ignored) {
            }
            markFailed(taskId, start, errMsg);
        }
    }

    private void extractZip(Path zipFile, Path targetDir, String outputDir) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            // detect if there is a single top-level directory wrapping everything
            String commonPrefix = detectCommonPrefix(zip);

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (commonPrefix != null && entryName.startsWith(commonPrefix)) {
                    entryName = entryName.substring(commonPrefix.length());
                }
                if (entryName.isEmpty()) {
                    continue;
                }
                Path entryPath = targetDir.resolve(entryName).normalize();
                if (!entryPath.startsWith(targetDir)) {
                    continue; // zip slip protection
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream is = zip.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(entryPath)) {
                        is.transferTo(os);
                    }
                }
            }
        }

        // if outputDir is specified and exists inside extracted content, move its contents up
        if (outputDir != null && !outputDir.isBlank() && !".".equals(outputDir)) {
            Path buildOutput = targetDir.resolve(outputDir);
            if (Files.isDirectory(buildOutput)) {
                moveDirectoryContents(buildOutput, targetDir);
                deleteDirectory(buildOutput);
            }
        }
    }

    private String detectCommonPrefix(ZipFile zip) {
        String prefix = null;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            int slash = name.indexOf('/');
            if (slash < 0) return null; // file at root level, no common prefix
            String candidate = name.substring(0, slash + 1);
            if (prefix == null) {
                prefix = candidate;
            } else if (!prefix.equals(candidate)) {
                return null;
            }
        }
        return prefix;
    }

    private void moveDirectoryContents(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                Path dest = target.resolve(relative);
                Files.createDirectories(dest.getParent());
                Files.move(file, dest, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void fixAbsolutePaths(Path releaseDir, String projectCode, String versionNo) throws IOException {
        Path indexHtml = releaseDir.resolve("index.html");
        if (!Files.exists(indexHtml)) {
            return;
        }
        String content = Files.readString(indexHtml);
        String basePath = "/projects/" + projectCode + "/releases/" + versionNo + "/";

        // Inject <base> tag so Vue Router's createWebHistory() uses the correct base
        String fixed = content.contains("<head>")
            ? content.replace("<head>", "<head>\n  <base href=\"" + basePath + "\">")
            : content;

        // Fix absolute /assets/ paths to relative (belt-and-suspenders)
        fixed = fixed
            .replace("=\"/assets/", "=\"assets/")
            .replace("='/assets/", "='assets/");

        if (!fixed.equals(content)) {
            Files.writeString(indexHtml, fixed);
        }
    }

    private void writePlaceholder(Path releaseDir, String projectName, String versionNo) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html lang="zh">
            <head><meta charset="UTF-8"><title>%s</title></head>
            <body>
            <h1>%s</h1>
            <p>版本: %s</p>
            <p>此占位页面由 VibeProto 自动生成。</p>
            </body>
            </html>
            """.formatted(projectName, projectName, versionNo);
        Files.writeString(releaseDir.resolve("index.html"), html);
    }

    private long countReleases(Long projectId) {
        // Delegate to release service for count consistency
        return releaseVersionService.countByProjectId(projectId);
    }

    private void markFailed(Long taskId, LocalDateTime start, String message) {
        BuildTask failedTask = buildTaskMapper.selectById(taskId);
        if (failedTask == null) {
            return;
        }
        failedTask.setStatus(BuildStatus.failed.name());
        failedTask.setErrorMessage(message);
        failedTask.setEndTime(LocalDateTime.now());
        failedTask.setDurationMs(Duration.between(start, failedTask.getEndTime()).toMillis());
        buildTaskMapper.updateById(failedTask);
    }

    private String writeLog(Project project, BuildTask task, String content) throws IOException {
        String filename = task.getTaskNo() + ".log";
        return fileStorageService.storeText(content, "logs/" + project.getCode(), filename);
    }
}
