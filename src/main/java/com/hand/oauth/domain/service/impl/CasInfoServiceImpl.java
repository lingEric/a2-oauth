package com.hand.oauth.domain.service.impl;

import com.hand.oauth.domain.mapper.CasInfoMapper;
import com.hand.oauth.domain.model.CasInfo;
import com.hand.oauth.domain.service.CasInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CasInfoServiceImpl implements CasInfoService {
    @Autowired
    private CasInfoMapper casInfoMapper;

    //根据租户id查找CasInfo
    public CasInfo getCasInfoByTenantId(int tenantId) {
        return casInfoMapper.selectByTenantId(tenantId);
    }

}
