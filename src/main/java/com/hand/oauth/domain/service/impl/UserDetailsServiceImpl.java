package com.hand.oauth.domain.service.impl;

import com.hand.oauth.domain.dto.UserDetailsVO;
import com.hand.oauth.domain.mapper.UserMapper;
import com.hand.oauth.domain.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements CustomUserDetailsService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户表单登录时，可以输入工号，手机号，邮箱登录
     * 查询用户是否存在时，依此判断相应字段是否存在记录，如果都不存在，则抛出异常
     * @param username 用户表单登录时，输入的值
     * @return 封装的userDetails对象，实现了userDetails，并封装了租户和用户详细信息
     * @throws UsernameNotFoundException 用户输入的账号不存在
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //用户输入username
        UserDetailsVO userDetailsVO = userMapper.selectByUsername(username);
        if (userDetailsVO != null) {
            return userDetailsVO;
        } else {
            //用户输入phone
            userDetailsVO = userMapper.selectByPhone(username);
            if (userDetailsVO != null) {
                return userDetailsVO;
            } else {
                //用户输入email
                userDetailsVO = userMapper.selectByEmail(username);
                if (userDetailsVO != null) {
                    return userDetailsVO;
                }else{
                    //UsernameNotFoundException
                    throw new UsernameNotFoundException("username is not founded...");
                }

            }
        }
    }
}
