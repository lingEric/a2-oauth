package com.hand.oauth.domain.mapper;

import com.hand.oauth.domain.model.CasInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface CasInfoMapper {
    int deleteByPrimaryKey(Integer casId);

    int insert(CasInfo record);

    int insertSelective(CasInfo record);

    CasInfo selectByPrimaryKey(Integer casId);

    int updateByPrimaryKeySelective(CasInfo record);

    int updateByPrimaryKey(CasInfo record);

    //根据租户id查找CasInfo
    CasInfo selectByTenantId(Integer tenantId);
}