package com.vibeproto.prompt.service;

import com.vibeproto.prompt.dto.PromptAssetCreateRequest;
import com.vibeproto.prompt.dto.PromptAssetUpdateRequest;
import com.vibeproto.prompt.vo.PromptAssetVO;

import java.util.List;

public interface PromptAssetService {
    List<PromptAssetVO> list(Long projectId);
    PromptAssetVO getById(Long id);
    PromptAssetVO create(PromptAssetCreateRequest request, Long operatorId);
    PromptAssetVO update(Long id, PromptAssetUpdateRequest request);
    void delete(Long id);
}
