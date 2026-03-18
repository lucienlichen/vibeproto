package com.vibeproto.project.service;

import com.vibeproto.common.model.PageResponse;
import com.vibeproto.project.dto.ProjectCreateRequest;
import com.vibeproto.project.dto.ProjectUpdateRequest;
import com.vibeproto.project.vo.ProjectPreviewInfoVO;
import com.vibeproto.project.vo.ProjectVO;

public interface ProjectService {

    PageResponse<ProjectVO> pageProjects(long pageNum, long pageSize, String name, String projectType, String status);

    ProjectVO createProject(ProjectCreateRequest request, Long operatorId);

    ProjectVO getProject(Long id);

    ProjectVO updateProject(Long id, ProjectUpdateRequest request);

    void deleteProject(Long id);

    ProjectVO archiveProject(Long id);

    ProjectPreviewInfoVO getPreviewInfo(Long id);
}
