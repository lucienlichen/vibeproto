package com.vibeproto.build.service;

import com.vibeproto.build.dto.BuildProfileCreateRequest;
import com.vibeproto.build.dto.BuildProfileUpdateRequest;
import com.vibeproto.build.vo.BuildProfileVO;

import java.util.List;

public interface BuildProfileService {

    List<BuildProfileVO> listByProjectId(Long projectId);

    BuildProfileVO create(BuildProfileCreateRequest request, Long operatorId);

    BuildProfileVO update(Long id, BuildProfileUpdateRequest request);

    void delete(Long id);
}
