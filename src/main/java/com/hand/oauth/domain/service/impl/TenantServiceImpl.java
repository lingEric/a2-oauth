package com.hand.oauth.domain.service.impl;

import com.hand.oauth.domain.mapper.TenantMapper;
import com.hand.oauth.domain.model.Tenant;
import com.hand.oauth.domain.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl implements TenantService {
    @Autowired
    private TenantMapper tenantMapper;

    public Tenant selectByDomain(String domain){
        return tenantMapper.selectByDomain(domain);
    }
}
