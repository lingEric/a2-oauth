package com.hand.oauth.domain.mapper;

import com.hand.oauth.domain.dto.UserDetailsVO;
import com.hand.oauth.domain.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    int deleteByPrimaryKey(Integer userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    UserDetailsVO selectByUsername(String username);

    UserDetailsVO selectByPhone(String phone);

    UserDetailsVO selectByEmail(String email);
}