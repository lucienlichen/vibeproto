package com.vibeproto.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.system.dto.SystemConfigUpdateRequest;
import com.vibeproto.system.entity.SystemConfig;
import com.vibeproto.system.mapper.SystemConfigMapper;
import com.vibeproto.system.service.SystemConfigService;
import com.vibeproto.system.vo.SystemConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    public SystemConfigServiceImpl(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    @Override
    public List<SystemConfigVO> listAll() {
        return systemConfigMapper.selectList(
            new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigKey)
        ).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public List<SystemConfigVO> batchUpdate(SystemConfigUpdateRequest request) {
        for (SystemConfigUpdateRequest.ConfigItem item : request.items()) {
            SystemConfig existing = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, item.configKey())
                    .last("LIMIT 1")
            );
            if (existing != null) {
                existing.setConfigValue(item.configValue());
                systemConfigMapper.updateById(existing);
            } else {
                SystemConfig newConfig = new SystemConfig();
                newConfig.setConfigKey(item.configKey());
                newConfig.setConfigValue(item.configValue());
                systemConfigMapper.insert(newConfig);
            }
        }
        return listAll();
    }

    private SystemConfigVO toVO(SystemConfig config) {
        return new SystemConfigVO(
            config.getId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getDescription(),
            config.getUpdatedAt()
        );
    }
}
