package com.vibeproto.system.service;

import com.vibeproto.system.dto.SystemConfigUpdateRequest;
import com.vibeproto.system.vo.SystemConfigVO;

import java.util.List;

public interface SystemConfigService {
    List<SystemConfigVO> listAll();
    List<SystemConfigVO> batchUpdate(SystemConfigUpdateRequest request);
}
