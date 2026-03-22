package com.vibeproto.source.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.file.service.FileStorageService;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import com.vibeproto.source.dto.GitImportRequest;
import com.vibeproto.source.dto.HtmlCreateRequest;
import com.vibeproto.source.entity.SourceVersion;
import com.vibeproto.source.enums.SourceType;
import com.vibeproto.source.mapper.SourceVersionMapper;
import com.vibeproto.source.service.SourceVersionService;
import com.vibeproto.source.vo.SourceVersionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SourceVersionServiceImpl implements SourceVersionService {

    private final SourceVersionMapper sourceVersionMapper;
    private final ProjectMapper projectMapper;
    private final FileStorageService fileStorageService;

    public SourceVersionServiceImpl(SourceVersionMapper sourceVersionMapper,
                                    ProjectMapper projectMapper,
                                    FileStorageService fileStorageService) {
        this.sourceVersionMapper = sourceVersionMapper;
        this.projectMapper = projectMapper;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public SourceVersionVO uploadZip(Long projectId, String remark, MultipartFile file, Long operatorId) throws IOException {
        Project project = requireProject(projectId);
        validateZip(file);

        String versionNo = nextVersionNo(project);
        String filename = fileStorageService.uniqueFilename(file.getOriginalFilename(), ".zip");
        String relativePath = fileStorageService.store(file, "source/" + project.getCode(), filename);

        SourceVersion sourceVersion = new SourceVersion();
        sourceVersion.setProjectId(projectId);
        sourceVersion.setVersionNo(versionNo);
        sourceVersion.setSourceType(SourceType.zip.name());
        sourceVersion.setSourceName(file.getOriginalFilename());
        sourceVersion.setFilePath(relativePath);
        sourceVersion.setRemark(remark);
        sourceVersion.setCreatedBy(operatorId);
        sourceVersionMapper.insert(sourceVersion);
        return toVO(sourceVersion);
    }

    @Override
    @Transactional
    public SourceVersionVO createFromHtml(HtmlCreateRequest request, Long operatorId) throws IOException {
        Project project = requireProject(request.projectId());
        String versionNo = nextVersionNo(project);
        String filename = fileStorageService.uniqueFilename(request.sourceName(), ".html");
        String relativePath = fileStorageService.storeText(request.htmlContent(), "html/" + project.getCode(), filename);

        SourceVersion sourceVersion = new SourceVersion();
        sourceVersion.setProjectId(project.getId());
        sourceVersion.setVersionNo(versionNo);
        sourceVersion.setSourceType(SourceType.html.name());
        sourceVersion.setSourceName(request.sourceName());
        sourceVersion.setHtmlContentPath(relativePath);
        sourceVersion.setRemark(request.remark());
        sourceVersion.setCreatedBy(operatorId);
        sourceVersionMapper.insert(sourceVersion);
        return toVO(sourceVersion);
    }

    @Override
    @Transactional
    public SourceVersionVO createFromGit(GitImportRequest request, Long operatorId) {
        Project project = requireProject(request.projectId());
        String versionNo = nextVersionNo(project);

        // 1. Clone the git repo to a temp directory
        Path tempDir = null;
        Path zipFile = null;
        try {
            tempDir = Files.createTempDirectory("vibeproto-git-");
            Path cloneDir = tempDir.resolve("repo");

            List<String> cmd = new java.util.ArrayList<>(List.of(
                "git", "clone", "--depth", "1"
            ));
            if (StringUtils.hasText(request.gitBranch())) {
                cmd.add("--branch");
                cmd.add(request.gitBranch());
            }
            cmd.add(request.gitUrl());
            cmd.add(cloneDir.toString());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BizException(5001, "Git 克隆超时（120秒）");
            }
            if (process.exitValue() != 0) {
                throw new BizException(5002, "Git 克隆失败: " + output);
            }

            // If commitHash specified, checkout it
            if (StringUtils.hasText(request.commitHash())) {
                ProcessBuilder checkout = new ProcessBuilder("git", "checkout", request.commitHash());
                checkout.directory(cloneDir.toFile());
                checkout.redirectErrorStream(true);
                Process cp = checkout.start();
                cp.waitFor(30, TimeUnit.SECONDS);
            }

            // 2. Zip the cloned content (excluding .git directory)
            zipFile = tempDir.resolve("source.zip");
            zipDirectory(cloneDir, zipFile);

            // 3. Store the zip via fileStorageService
            String filename = fileStorageService.uniqueFilename("git-source.zip", ".zip");
            String relativePath = fileStorageService.storeFile(zipFile, "source/" + project.getCode(), filename);

            // 4. Save source version record
            SourceVersion sourceVersion = new SourceVersion();
            sourceVersion.setProjectId(project.getId());
            sourceVersion.setVersionNo(versionNo);
            sourceVersion.setSourceType(SourceType.git.name());
            sourceVersion.setSourceName(request.gitUrl());
            sourceVersion.setFilePath(relativePath);
            sourceVersion.setGitUrl(request.gitUrl());
            sourceVersion.setGitBranch(request.gitBranch());
            sourceVersion.setCommitHash(request.commitHash());
            sourceVersion.setRemark(request.remark());
            sourceVersion.setCreatedBy(operatorId);
            sourceVersionMapper.insert(sourceVersion);
            return toVO(sourceVersion);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(5003, "Git 导入失败: " + e.getMessage());
        } finally {
            // Cleanup temp files
            if (tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new OutputStream() {
            private final OutputStream delegate = Files.newOutputStream(zipFile);
            @Override public void write(int b) throws IOException { delegate.write(b); }
            @Override public void write(byte[] b, int off, int len) throws IOException { delegate.write(b, off, len); }
            @Override public void close() throws IOException { delegate.close(); }
        })) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && dir.getFileName().toString().equals(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourceDir.relativize(file).toString();
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
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

    @Override
    public List<SourceVersionVO> listByProjectId(Long projectId) {
        requireProject(projectId);
        return sourceVersionMapper.selectList(
            new LambdaQueryWrapper<SourceVersion>()
                .eq(SourceVersion::getProjectId, projectId)
                .orderByDesc(SourceVersion::getCreatedAt)
        ).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SourceVersion sourceVersion = sourceVersionMapper.selectById(id);
        if (sourceVersion == null) {
            throw new BizException(4041, "源码版本不存在");
        }
        sourceVersionMapper.deleteById(id);
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private void validateZip(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(4005, "请上传 zip 文件");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.toLowerCase().endsWith(".zip")) {
            throw new BizException(4006, "仅支持上传 zip 文件");
        }
    }

    private String nextVersionNo(Project project) {
        long count = sourceVersionMapper.selectCount(
            new LambdaQueryWrapper<SourceVersion>().eq(SourceVersion::getProjectId, project.getId())
        );
        return "sv-%s-%d".formatted(project.getCode(), count + 1);
    }

    private SourceVersionVO toVO(SourceVersion sourceVersion) {
        return new SourceVersionVO(
            sourceVersion.getId(),
            sourceVersion.getProjectId(),
            sourceVersion.getVersionNo(),
            sourceVersion.getSourceType(),
            sourceVersion.getSourceName(),
            sourceVersion.getFilePath(),
            sourceVersion.getGitUrl(),
            sourceVersion.getGitBranch(),
            sourceVersion.getCommitHash(),
            sourceVersion.getHtmlContentPath(),
            sourceVersion.getRemark(),
            sourceVersion.getCreatedBy(),
            sourceVersion.getCreatedAt()
        );
    }
}
