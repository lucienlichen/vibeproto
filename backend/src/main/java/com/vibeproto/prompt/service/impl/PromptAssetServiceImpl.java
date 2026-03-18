package com.vibeproto.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.prompt.dto.PromptAssetCreateRequest;
import com.vibeproto.prompt.dto.PromptAssetUpdateRequest;
import com.vibeproto.prompt.entity.PromptAsset;
import com.vibeproto.prompt.enums.PromptType;
import com.vibeproto.prompt.mapper.PromptAssetMapper;
import com.vibeproto.prompt.service.PromptAssetService;
import com.vibeproto.prompt.vo.PromptAssetVO;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromptAssetServiceImpl implements PromptAssetService {

    private final PromptAssetMapper promptAssetMapper;
    private final ProjectMapper projectMapper;

    public PromptAssetServiceImpl(PromptAssetMapper promptAssetMapper, ProjectMapper projectMapper) {
        this.promptAssetMapper = promptAssetMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<PromptAssetVO> list(Long projectId) {
        return promptAssetMapper.selectList(
            new LambdaQueryWrapper<PromptAsset>()
                .eq(projectId != null, PromptAsset::getProjectId, projectId)
                .orderByDesc(PromptAsset::getUpdatedAt)
        ).stream().map(this::toVO).toList();
    }

    @Override
    public PromptAssetVO getById(Long id) {
        return toVO(requirePrompt(id));
    }

    @Override
    @Transactional
    public PromptAssetVO create(PromptAssetCreateRequest request, Long operatorId) {
        requireProject(request.projectId());
        validatePromptType(request.promptType());

        PromptAsset asset = new PromptAsset();
        asset.setProjectId(request.projectId());
        asset.setTitle(request.title());
        asset.setPromptType(request.promptType());
        asset.setContent(request.content());
        asset.setRelatedSourceVersionId(request.relatedSourceVersionId());
        asset.setRelatedReleaseId(request.relatedReleaseId());
        asset.setCreatedBy(operatorId);
        promptAssetMapper.insert(asset);
        return toVO(requirePrompt(asset.getId()));
    }

    @Override
    @Transactional
    public PromptAssetVO update(Long id, PromptAssetUpdateRequest request) {
        validatePromptType(request.promptType());
        PromptAsset asset = requirePrompt(id);
        asset.setTitle(request.title());
        asset.setPromptType(request.promptType());
        asset.setContent(request.content());
        asset.setRelatedSourceVersionId(request.relatedSourceVersionId());
        asset.setRelatedReleaseId(request.relatedReleaseId());
        promptAssetMapper.updateById(asset);
        return toVO(requirePrompt(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requirePrompt(id);
        promptAssetMapper.deleteById(id);
    }

    private void validatePromptType(String promptType) {
        try {
            PromptType.valueOf(promptType);
        } catch (IllegalArgumentException e) {
            throw new BizException(4012, "提示词类型不合法");
        }
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private PromptAsset requirePrompt(Long id) {
        PromptAsset asset = promptAssetMapper.selectById(id);
        if (asset == null) {
            throw new BizException(4046, "提示词资产不存在");
        }
        return asset;
    }

    private PromptAssetVO toVO(PromptAsset asset) {
        return new PromptAssetVO(
            asset.getId(),
            asset.getProjectId(),
            asset.getTitle(),
            asset.getPromptType(),
            asset.getContent(),
            asset.getRelatedSourceVersionId(),
            asset.getRelatedReleaseId(),
            asset.getCreatedBy(),
            asset.getCreatedAt(),
            asset.getUpdatedAt()
        );
    }
}
