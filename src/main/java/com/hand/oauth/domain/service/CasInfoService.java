package com.hand.oauth.domain.service;

import com.hand.oauth.domain.model.CasInfo;
import org.springframework.stereotype.Service;

@Service
public interface CasInfoService {
    CasInfo getCasInfoByTenantId(int tenantId);
}
