package com.vibeproto.build.service;

import com.vibeproto.build.dto.BuildTaskCreateRequest;
import com.vibeproto.build.vo.BuildTaskVO;

import java.util.List;

public interface BuildTaskService {

    BuildTaskVO create(BuildTaskCreateRequest request, Long operatorId);

    List<BuildTaskVO> list(Long projectId);

    BuildTaskVO getById(Long id);

    String getLog(Long id);

    BuildTaskVO cancel(Long id);

    BuildTaskVO retry(Long id, Long operatorId);
}
