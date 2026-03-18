package com.vibeproto.document.service;

import com.vibeproto.document.dto.ProjectDocumentCreateRequest;
import com.vibeproto.document.dto.ProjectDocumentUpdateRequest;
import com.vibeproto.document.vo.ProjectDocumentVO;

import java.util.List;

public interface ProjectDocumentService {

    List<ProjectDocumentVO> list(Long projectId, Long relatedReleaseId);

    ProjectDocumentVO create(ProjectDocumentCreateRequest request, Long operatorId);

    ProjectDocumentVO update(Long id, ProjectDocumentUpdateRequest request);

    void delete(Long id);
}
