package com.vibeproto.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.common.model.PageResponse;
import com.vibeproto.project.dto.ProjectCreateRequest;
import com.vibeproto.project.dto.ProjectUpdateRequest;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.enums.ProjectStatus;
import com.vibeproto.project.enums.ProjectType;
import com.vibeproto.project.mapper.ProjectMapper;
import com.vibeproto.project.service.ProjectService;
import com.vibeproto.project.vo.ProjectPreviewInfoVO;
import com.vibeproto.project.vo.ProjectVO;
import com.vibeproto.release.entity.ReleaseVersion;
import com.vibeproto.release.mapper.ReleaseVersionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ReleaseVersionMapper releaseVersionMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper,
                              ReleaseVersionMapper releaseVersionMapper) {
        this.projectMapper = projectMapper;
        this.releaseVersionMapper = releaseVersionMapper;
    }

    @Override
    public PageResponse<ProjectVO> pageProjects(long pageNum, long pageSize, String name, String projectType, String status) {
        validateProjectType(projectType);
        validateStatus(status);

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
            .like(StringUtils.hasText(name), Project::getName, name)
            .eq(StringUtils.hasText(projectType), Project::getProjectType, projectType)
            .eq(StringUtils.hasText(status), Project::getStatus, status)
            .orderByDesc(Project::getUpdatedAt);

        Page<Project> page = projectMapper.selectPage(Page.of(pageNum, pageSize), wrapper);
        List<ProjectVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    @Transactional
    public ProjectVO createProject(ProjectCreateRequest request, Long operatorId) {
        validateProjectType(request.projectType());
        checkCodeUnique(request.code(), null);

        Project project = new Project();
        project.setName(request.name());
        project.setCode(request.code());
        project.setDescription(request.description());
        project.setProjectType(request.projectType());
        project.setDefaultBuildProfileId(request.defaultBuildProfileId());
        project.setStatus(ProjectStatus.draft.name());
        project.setOwnerId(operatorId);
        project.setCreatedBy(operatorId);
        projectMapper.insert(project);
        return getProject(project.getId());
    }

    @Override
    public ProjectVO getProject(Long id) {
        return toVO(requireProject(id));
    }

    @Override
    @Transactional
    public ProjectVO updateProject(Long id, ProjectUpdateRequest request) {
        validateProjectType(request.projectType());
        validateStatus(request.status());

        Project project = requireProject(id);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setProjectType(request.projectType());
        project.setDefaultBuildProfileId(request.defaultBuildProfileId());
        project.setStatus(request.status());
        projectMapper.updateById(project);
        return getProject(id);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = requireProject(id);
        projectMapper.deleteById(project.getId());
    }

    @Override
    @Transactional
    public ProjectVO archiveProject(Long id) {
        Project project = requireProject(id);
        project.setStatus(ProjectStatus.archived.name());
        projectMapper.updateById(project);
        return getProject(id);
    }

    @Override
    public ProjectPreviewInfoVO getPreviewInfo(Long id) {
        Project project = requireProject(id);
        ReleaseVersion currentRelease = project.getCurrentReleaseId() == null ? null : releaseVersionMapper.selectById(project.getCurrentReleaseId());
        String latestUrl = currentRelease == null ? null : currentRelease.getPreviewUrl();
        return new ProjectPreviewInfoVO(project.getId(), project.getCode(), latestUrl, latestUrl);
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private void checkCodeUnique(String code, Long id) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
            .eq(Project::getCode, code)
            .ne(id != null, Project::getId, id)
            .last("LIMIT 1");
        if (projectMapper.selectOne(wrapper) != null) {
            throw new BizException(4002, "项目标识已存在");
        }
    }

    private void validateProjectType(String projectType) {
        if (!StringUtils.hasText(projectType)) {
            return;
        }
        try {
            ProjectType.valueOf(projectType);
        } catch (IllegalArgumentException exception) {
            throw new BizException(4003, "项目类型不合法");
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        try {
            ProjectStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new BizException(4004, "项目状态不合法");
        }
    }

    private ProjectVO toVO(Project project) {
        ReleaseVersion currentRelease = project.getCurrentReleaseId() == null ? null : releaseVersionMapper.selectById(project.getCurrentReleaseId());
        return new ProjectVO(
            project.getId(),
            project.getName(),
            project.getCode(),
            project.getDescription(),
            project.getProjectType(),
            project.getStatus(),
            project.getDefaultBuildProfileId(),
            project.getCurrentReleaseId(),
            project.getOwnerId(),
            project.getCreatedBy(),
            currentRelease == null ? "-" : currentRelease.getVersionNo(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}
