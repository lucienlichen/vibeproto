package com.vibeproto.build.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.build.dto.BuildTaskCreateRequest;
import com.vibeproto.build.entity.BuildProfile;
import com.vibeproto.build.entity.BuildTask;
import com.vibeproto.build.enums.BuildStatus;
import com.vibeproto.build.mapper.BuildProfileMapper;
import com.vibeproto.build.mapper.BuildTaskMapper;
import com.vibeproto.build.service.BuildTaskService;
import com.vibeproto.build.task.BuildTaskExecutor;
import com.vibeproto.build.vo.BuildTaskVO;
import com.vibeproto.common.config.properties.StorageProperties;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import com.vibeproto.source.entity.SourceVersion;
import com.vibeproto.source.mapper.SourceVersionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BuildTaskServiceImpl implements BuildTaskService {

    private static final DateTimeFormatter TASK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BuildTaskMapper buildTaskMapper;
    private final ProjectMapper projectMapper;
    private final SourceVersionMapper sourceVersionMapper;
    private final BuildProfileMapper buildProfileMapper;
    private final BuildTaskExecutor buildTaskExecutor;
    private final StorageProperties storageProperties;

    public BuildTaskServiceImpl(BuildTaskMapper buildTaskMapper,
                                ProjectMapper projectMapper,
                                SourceVersionMapper sourceVersionMapper,
                                BuildProfileMapper buildProfileMapper,
                                BuildTaskExecutor buildTaskExecutor,
                                StorageProperties storageProperties) {
        this.buildTaskMapper = buildTaskMapper;
        this.projectMapper = projectMapper;
        this.sourceVersionMapper = sourceVersionMapper;
        this.buildProfileMapper = buildProfileMapper;
        this.buildTaskExecutor = buildTaskExecutor;
        this.storageProperties = storageProperties;
    }

    @Override
    @Transactional
    public BuildTaskVO create(BuildTaskCreateRequest request, Long operatorId) {
        requireProject(request.projectId());
        requireSourceVersion(request.sourceVersionId(), request.projectId());
        requireBuildProfile(request.buildProfileId(), request.projectId());

        BuildTask buildTask = new BuildTask();
        buildTask.setProjectId(request.projectId());
        buildTask.setSourceVersionId(request.sourceVersionId());
        buildTask.setBuildProfileId(request.buildProfileId());
        buildTask.setTaskNo("bt-" + LocalDateTime.now().format(TASK_NO_FORMATTER));
        buildTask.setStatus(BuildStatus.pending.name());
        buildTask.setTriggeredBy(operatorId);
        buildTaskMapper.insert(buildTask);
        Long taskId = buildTask.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                buildTaskExecutor.execute(taskId);
            }
        });
        return toVO(requireTask(taskId));
    }

    @Override
    public List<BuildTaskVO> list(Long projectId) {
        return buildTaskMapper.selectList(
            new LambdaQueryWrapper<BuildTask>()
                .eq(projectId != null, BuildTask::getProjectId, projectId)
                .orderByDesc(BuildTask::getCreatedAt)
        ).stream().map(this::toVO).toList();
    }

    @Override
    public BuildTaskVO getById(Long id) {
        return toVO(requireTask(id));
    }

    @Override
    public String getLog(Long id) {
        BuildTask buildTask = requireTask(id);
        if (buildTask.getLogPath() == null) {
            return "";
        }
        Path path = Path.of(storageProperties.rootPath()).resolve(buildTask.getLogPath()).normalize();
        try {
            return Files.exists(path) ? Files.readString(path) : "";
        } catch (IOException exception) {
            throw new BizException(5001, "读取构建日志失败");
        }
    }

    @Override
    @Transactional
    public BuildTaskVO cancel(Long id) {
        BuildTask buildTask = requireTask(id);
        if (BuildStatus.success.name().equals(buildTask.getStatus()) || BuildStatus.failed.name().equals(buildTask.getStatus())) {
            throw new BizException(4008, "当前任务状态不允许取消");
        }
        buildTask.setStatus(BuildStatus.canceled.name());
        buildTask.setEndTime(LocalDateTime.now());
        buildTaskMapper.updateById(buildTask);
        return toVO(requireTask(id));
    }

    @Override
    @Transactional
    public BuildTaskVO retry(Long id, Long operatorId) {
        BuildTask original = requireTask(id);
        return create(new BuildTaskCreateRequest(original.getProjectId(), original.getSourceVersionId(), original.getBuildProfileId()), operatorId);
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private SourceVersion requireSourceVersion(Long id, Long projectId) {
        SourceVersion sourceVersion = sourceVersionMapper.selectById(id);
        if (sourceVersion == null || !projectId.equals(sourceVersion.getProjectId())) {
            throw new BizException(4041, "源码版本不存在");
        }
        return sourceVersion;
    }

    private BuildProfile requireBuildProfile(Long id, Long projectId) {
        BuildProfile buildProfile = buildProfileMapper.selectById(id);
        if (buildProfile == null) {
            throw new BizException(4042, "构建配置不存在");
        }
        if (buildProfile.getProjectId() != null && !projectId.equals(buildProfile.getProjectId())) {
            throw new BizException(4009, "构建配置不属于当前项目");
        }
        return buildProfile;
    }

    private BuildTask requireTask(Long id) {
        BuildTask buildTask = buildTaskMapper.selectById(id);
        if (buildTask == null) {
            throw new BizException(4043, "构建任务不存在");
        }
        return buildTask;
    }

    private BuildTaskVO toVO(BuildTask buildTask) {
        var project = projectMapper.selectById(buildTask.getProjectId());
        var sourceVersion = sourceVersionMapper.selectById(buildTask.getSourceVersionId());
        var buildProfile = buildProfileMapper.selectById(buildTask.getBuildProfileId());
        return new BuildTaskVO(
            buildTask.getId(),
            buildTask.getProjectId(),
            buildTask.getSourceVersionId(),
            buildTask.getBuildProfileId(),
            buildTask.getTaskNo(),
            buildTask.getStatus(),
            buildTask.getLogPath(),
            buildTask.getStartTime(),
            buildTask.getEndTime(),
            buildTask.getDurationMs(),
            buildTask.getErrorMessage(),
            buildTask.getTriggeredBy(),
            buildTask.getCreatedAt(),
            project != null ? project.getName() : null,
            sourceVersion != null ? sourceVersion.getVersionNo() : null,
            buildProfile != null ? buildProfile.getProfileName() : null
        );
    }
}
