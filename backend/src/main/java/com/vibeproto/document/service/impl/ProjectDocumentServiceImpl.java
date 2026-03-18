package com.vibeproto.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.document.dto.ProjectDocumentCreateRequest;
import com.vibeproto.document.dto.ProjectDocumentUpdateRequest;
import com.vibeproto.document.entity.ProjectDocument;
import com.vibeproto.document.enums.DocType;
import com.vibeproto.document.mapper.ProjectDocumentMapper;
import com.vibeproto.document.service.ProjectDocumentService;
import com.vibeproto.document.vo.ProjectDocumentVO;
import com.vibeproto.project.entity.Project;
import com.vibeproto.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectDocumentServiceImpl implements ProjectDocumentService {

    private final ProjectDocumentMapper projectDocumentMapper;
    private final ProjectMapper projectMapper;

    public ProjectDocumentServiceImpl(ProjectDocumentMapper projectDocumentMapper, ProjectMapper projectMapper) {
        this.projectDocumentMapper = projectDocumentMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<ProjectDocumentVO> list(Long projectId, Long relatedReleaseId) {
        return projectDocumentMapper.selectList(
            new LambdaQueryWrapper<ProjectDocument>()
                .eq(projectId != null, ProjectDocument::getProjectId, projectId)
                .eq(relatedReleaseId != null, ProjectDocument::getRelatedReleaseId, relatedReleaseId)
                .orderByDesc(ProjectDocument::getUpdatedAt)
        ).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public ProjectDocumentVO create(ProjectDocumentCreateRequest request, Long operatorId) {
        requireProject(request.projectId());
        validateDocType(request.docType());

        ProjectDocument document = new ProjectDocument();
        document.setProjectId(request.projectId());
        document.setDocType(request.docType());
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setRelatedReleaseId(request.relatedReleaseId());
        document.setCreatedBy(operatorId);
        projectDocumentMapper.insert(document);
        return toVO(requireDocument(document.getId()));
    }

    @Override
    @Transactional
    public ProjectDocumentVO update(Long id, ProjectDocumentUpdateRequest request) {
        validateDocType(request.docType());
        ProjectDocument document = requireDocument(id);
        document.setDocType(request.docType());
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setRelatedReleaseId(request.relatedReleaseId());
        projectDocumentMapper.updateById(document);
        return toVO(requireDocument(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireDocument(id);
        projectDocumentMapper.deleteById(id);
    }

    private void validateDocType(String docType) {
        try {
            DocType.valueOf(docType);
        } catch (IllegalArgumentException exception) {
            throw new BizException(4012, "文档类型不合法");
        }
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(4040, "项目不存在");
        }
        return project;
    }

    private ProjectDocument requireDocument(Long id) {
        ProjectDocument document = projectDocumentMapper.selectById(id);
        if (document == null) {
            throw new BizException(4045, "文档不存在");
        }
        return document;
    }

    private ProjectDocumentVO toVO(ProjectDocument document) {
        return new ProjectDocumentVO(
            document.getId(),
            document.getProjectId(),
            document.getDocType(),
            document.getTitle(),
            document.getContent(),
            document.getRelatedReleaseId(),
            document.getCreatedBy(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }
}
