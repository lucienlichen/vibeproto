package com.vibeproto.build.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.build.dto.BuildProfileCreateRequest;
import com.vibeproto.build.dto.BuildProfileUpdateRequest;
import com.vibeproto.build.entity.BuildProfile;
import com.vibeproto.build.mapper.BuildProfileMapper;
import com.vibeproto.build.service.BuildProfileService;
import com.vibeproto.build.vo.BuildProfileVO;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuildProfileServiceImpl implements BuildProfileService {

    private final BuildProfileMapper buildProfileMapper;
    private final ProjectMapper projectMapper;

    public BuildProfileServiceImpl(BuildProfileMapper buildProfileMapper, ProjectMapper projectMapper) {
        this.buildProfileMapper = buildProfileMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<BuildProfileVO> listByProjectId(Long projectId) {
        requireProject(projectId);
        return buildProfileMapper.selectList(
            new LambdaQueryWrapper<BuildProfile>()
                .and(wrapper -> wrapper.eq(BuildProfile::getProjectId, projectId).or().isNull(BuildProfile::getProjectId))
                .orderByDesc(BuildProfile::getIsDefault)
                .orderByAsc(BuildProfile::getId)
        ).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public BuildProfileVO create(BuildProfileCreateRequest request, Long operatorId) {
        requireProject(request.projectId());
        BuildProfile buildProfile = new BuildProfile();
        buildProfile.setProjectId(request.projectId());
        buildProfile.setProfileName(request.profileName());
        buildProfile.setNodeVersion(request.nodeVersion());
        buildProfile.setInstallCommand(request.installCommand());
        buildProfile.setBuildCommand(request.buildCommand());
        buildProfile.setOutputDir(request.outputDir());
        buildProfile.setEnvJson(request.envJson());
        buildProfile.setIsDefault(request.isDefault());
        buildProfile.setEnabled(request.enabled());
        buildProfile.setCreatedBy(operatorId);
        if (request.isDefault() == 1) {
            clearProjectDefault(request.projectId());
        }
        buildProfileMapper.insert(buildProfile);
        return toVO(requireProfile(buildProfile.getId()));
    }

    @Override
    @Transactional
    public BuildProfileVO update(Long id, BuildProfileUpdateRequest request) {
        BuildProfile buildProfile = requireProfile(id);
        buildProfile.setProfileName(request.profileName());
        buildProfile.setNodeVersion(request.nodeVersion());
        buildProfile.setInstallCommand(request.installCommand());
        buildProfile.setBuildCommand(request.buildCommand());
        buildProfile.setOutputDir(request.outputDir());
        buildProfile.setEnvJson(request.envJson());
        buildProfile.setEnabled(request.enabled());
        buildProfile.setIsDefault(request.isDefault());
        if (request.isDefault() == 1 && buildProfile.getProjectId() != null) {
            clearProjectDefault(buildProfile.getProjectId());
        }
        buildProfileMapper.updateById(buildProfile);
        return toVO(requireProfile(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BuildProfile buildProfile = requireProfile(id);
        if (buildProfile.getProjectId() == null) {
            throw new BizException(4007, "系统默认模板不允许删除");
        }
        buildProfileMapper.deleteById(id);
    }

    private void clearProjectDefault(Long projectId) {
        buildProfileMapper.selectList(
            new LambdaQueryWrapper<BuildProfile>().eq(BuildProfile::getProjectId, projectId)
        ).forEach(item -> {
            if (item.getIsDefault() != null && item.getIsDefault() == 1) {
                item.setIsDefault(0);
                buildProfileMapper.updateById(item);
            }
        });
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private BuildProfile requireProfile(Long id) {
        BuildProfile buildProfile = buildProfileMapper.selectById(id);
        if (buildProfile == null) {
            throw new BizException(4042, "构建配置不存在");
        }
        return buildProfile;
    }

    private BuildProfileVO toVO(BuildProfile buildProfile) {
        return new BuildProfileVO(
            buildProfile.getId(),
            buildProfile.getProjectId(),
            buildProfile.getProfileName(),
            buildProfile.getNodeVersion(),
            buildProfile.getInstallCommand(),
            buildProfile.getBuildCommand(),
            buildProfile.getOutputDir(),
            buildProfile.getEnvJson(),
            buildProfile.getIsDefault(),
            buildProfile.getEnabled(),
            buildProfile.getCreatedBy(),
            buildProfile.getCreatedAt(),
            buildProfile.getUpdatedAt()
        );
    }
}
