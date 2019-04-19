package com.hand.oauth.domain.mapper;

import com.hand.oauth.domain.model.Tenant;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantMapper {
    int deleteByPrimaryKey(Integer tenantId);

    int insert(Tenant record);

    int insertSelective(Tenant record);

    Tenant selectByPrimaryKey(Integer tenantId);

    int updateByPrimaryKeySelective(Tenant record);

    int updateByPrimaryKey(Tenant record);

    Tenant selectByDomain(String domain);
}