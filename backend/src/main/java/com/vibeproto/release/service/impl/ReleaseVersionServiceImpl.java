package com.vibeproto.release.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.build.entity.BuildTask;
import com.vibeproto.build.enums.BuildStatus;
import com.vibeproto.build.mapper.BuildTaskMapper;
import com.vibeproto.common.config.properties.DeployProperties;
import com.vibeproto.common.config.properties.SystemProperties;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import com.vibeproto.release.entity.ReleaseVersion;
import com.vibeproto.release.mapper.ReleaseVersionMapper;
import com.vibeproto.release.service.ReleaseVersionService;
import com.vibeproto.release.vo.ReleaseVersionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ReleaseVersionServiceImpl implements ReleaseVersionService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseVersionServiceImpl.class);

    private final ReleaseVersionMapper releaseVersionMapper;
    private final BuildTaskMapper buildTaskMapper;
    private final ProjectMapper projectMapper;
    private final SystemProperties systemProperties;
    private final DeployProperties deployProperties;

    public ReleaseVersionServiceImpl(ReleaseVersionMapper releaseVersionMapper,
                                     BuildTaskMapper buildTaskMapper,
                                     ProjectMapper projectMapper,
                                     SystemProperties systemProperties,
                                     DeployProperties deployProperties) {
        this.releaseVersionMapper = releaseVersionMapper;
        this.buildTaskMapper = buildTaskMapper;
        this.projectMapper = projectMapper;
        this.systemProperties = systemProperties;
        this.deployProperties = deployProperties;
    }

    @Override
    public List<ReleaseVersionVO> list(Long projectId) {
        return releaseVersionMapper.selectList(
            new LambdaQueryWrapper<ReleaseVersion>()
                .eq(projectId != null, ReleaseVersion::getProjectId, projectId)
                .orderByDesc(ReleaseVersion::getCreatedAt)
        ).stream().map(this::toVO).toList();
    }

    @Override
    public ReleaseVersionVO getById(Long id) {
        return toVO(requireRelease(id));
    }

    @Override
    @Transactional
    public ReleaseVersionVO setCurrent(Long id) {
        ReleaseVersion releaseVersion = requireRelease(id);
        clearCurrent(releaseVersion.getProjectId());
        releaseVersion.setIsCurrent(1);
        releaseVersionMapper.updateById(releaseVersion);

        Project project = requireProject(releaseVersion.getProjectId());
        project.setCurrentReleaseId(releaseVersion.getId());
        projectMapper.updateById(project);

        updateSymlink(project.getCode(), releaseVersion.getVersionNo());

        return toVO(requireRelease(id));
    }

    @Override
    @Transactional
    public ReleaseVersionVO rollback(Long id) {
        return setCurrent(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ReleaseVersion releaseVersion = requireRelease(id);
        if (releaseVersion.getIsCurrent() != null && releaseVersion.getIsCurrent() == 1) {
            throw new BizException(4010, "当前版本不允许删除");
        }
        releaseVersionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public ReleaseVersionVO createFromBuildTask(Long buildTaskId) {
        BuildTask buildTask = requireBuildTask(buildTaskId);
        if (!BuildStatus.success.name().equals(buildTask.getStatus())) {
            throw new BizException(4011, "仅成功构建任务可生成发布版本");
        }

        ReleaseVersion existed = releaseVersionMapper.selectOne(
            new LambdaQueryWrapper<ReleaseVersion>().eq(ReleaseVersion::getBuildTaskId, buildTaskId).last("LIMIT 1")
        );
        if (existed != null) {
            return toVO(existed);
        }

        Project project = requireProject(buildTask.getProjectId());
        long count = releaseVersionMapper.selectCount(
            new LambdaQueryWrapper<ReleaseVersion>().eq(ReleaseVersion::getProjectId, buildTask.getProjectId())
        );
        String versionNo = "v" + (count + 1);
        String releasePath = "%s/%s/releases/%s/".formatted(deployProperties.rootPath().replaceAll("/+$", ""), project.getCode(), versionNo);
        String previewUrl = "%s/projects/%s/releases/%s/".formatted(
            systemProperties.basePreviewUrl().replaceAll("/+$", ""),
            project.getCode(),
            versionNo
        );

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setProjectId(buildTask.getProjectId());
        releaseVersion.setSourceVersionId(buildTask.getSourceVersionId());
        releaseVersion.setBuildTaskId(buildTask.getId());
        releaseVersion.setVersionNo(versionNo);
        releaseVersion.setReleasePath(releasePath);
        releaseVersion.setPreviewUrl(previewUrl);
        releaseVersion.setIsCurrent(count == 0 ? 1 : 0);
        releaseVersion.setReleasedBy(buildTask.getTriggeredBy());
        releaseVersionMapper.insert(releaseVersion);

        if (count == 0) {
            project.setCurrentReleaseId(releaseVersion.getId());
            projectMapper.updateById(project);
            updateSymlink(project.getCode(), versionNo);
        }
        return toVO(releaseVersion);
    }

    @Override
    public long countByProjectId(Long projectId) {
        return releaseVersionMapper.selectCount(
            new LambdaQueryWrapper<ReleaseVersion>().eq(ReleaseVersion::getProjectId, projectId)
        );
    }

    private void updateSymlink(String projectCode, String versionNo) {
        try {
            Path currentLink = Path.of(deployProperties.rootPath(), projectCode, "current");
            Path targetDir = Path.of(deployProperties.rootPath(), projectCode, "releases", versionNo);
            Files.deleteIfExists(currentLink);
            if (Files.isDirectory(targetDir)) {
                Files.createSymbolicLink(currentLink, targetDir);
            }
        } catch (IOException e) {
            log.warn("创建软链接失败: {}", e.getMessage());
        }
    }

    private void clearCurrent(Long projectId) {
        releaseVersionMapper.selectList(
            new LambdaQueryWrapper<ReleaseVersion>().eq(ReleaseVersion::getProjectId, projectId)
        ).forEach(item -> {
            if (item.getIsCurrent() != null && item.getIsCurrent() == 1) {
                item.setIsCurrent(0);
                releaseVersionMapper.updateById(item);
            }
        });
    }

    private ReleaseVersion requireRelease(Long id) {
        ReleaseVersion releaseVersion = releaseVersionMapper.selectById(id);
        if (releaseVersion == null) {
            throw new BizException(4044, "发布版本不存在");
        }
        return releaseVersion;
    }

    private BuildTask requireBuildTask(Long id) {
        BuildTask buildTask = buildTaskMapper.selectById(id);
        if (buildTask == null) {
            throw new BizException(4043, "构建任务不存在");
        }
        return buildTask;
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private ReleaseVersionVO toVO(ReleaseVersion releaseVersion) {
        return new ReleaseVersionVO(
            releaseVersion.getId(),
            releaseVersion.getProjectId(),
            releaseVersion.getSourceVersionId(),
            releaseVersion.getBuildTaskId(),
            releaseVersion.getVersionNo(),
            releaseVersion.getReleasePath(),
            releaseVersion.getPreviewUrl(),
            releaseVersion.getIsCurrent(),
            releaseVersion.getReleasedBy(),
            releaseVersion.getCreatedAt()
        );
    }
}
