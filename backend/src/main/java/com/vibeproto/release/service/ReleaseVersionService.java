package com.vibeproto.release.service;

import com.vibeproto.release.vo.ReleaseVersionVO;

import java.util.List;

public interface ReleaseVersionService {

    List<ReleaseVersionVO> list(Long projectId);

    ReleaseVersionVO getById(Long id);

    ReleaseVersionVO setCurrent(Long id);

    ReleaseVersionVO rollback(Long id);

    void delete(Long id);

    ReleaseVersionVO createFromBuildTask(Long buildTaskId);

    long countByProjectId(Long projectId);
}
