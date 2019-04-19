package com.hand.oauth.domain.service;

import com.hand.oauth.domain.model.Tenant;
import org.springframework.stereotype.Service;

@Service
public interface TenantService {
    Tenant selectByDomain(String domain);
}
