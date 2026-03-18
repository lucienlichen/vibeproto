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
import java.util.List;

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

        SourceVersion sourceVersion = new SourceVersion();
        sourceVersion.setProjectId(project.getId());
        sourceVersion.setVersionNo(versionNo);
        sourceVersion.setSourceType(SourceType.git.name());
        sourceVersion.setSourceName(project.getName() + " Git 导入");
        sourceVersion.setGitUrl(request.gitUrl());
        sourceVersion.setGitBranch(request.gitBranch());
        sourceVersion.setCommitHash(request.commitHash());
        sourceVersion.setRemark(request.remark());
        sourceVersion.setCreatedBy(operatorId);
        sourceVersionMapper.insert(sourceVersion);
        return toVO(sourceVersion);
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
