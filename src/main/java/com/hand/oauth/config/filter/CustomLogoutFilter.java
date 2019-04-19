package com.hand.oauth.config.filter;

import com.hand.oauth.domain.dto.UserDetailsVO;
import com.hand.oauth.domain.model.CasInfo;
import com.hand.oauth.domain.service.CasInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLogoutFilter extends GenericFilterBean {
    private RequestMatcher logoutRequestMatcher;
    private SimpleUrlLogoutSuccessHandler urlLogoutSuccessHandler;
    private LogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    //获取casInfo信息，依此来判断当前认证用户的cas地址
    private CasInfoService casInfoService;

    public CustomLogoutFilter(String filterProcessesUrl, String logoutSuccessUrl,CasInfoService casInfoService) {
        this.logoutRequestMatcher = new AntPathRequestMatcher(filterProcessesUrl);
        this.urlLogoutSuccessHandler=new SimpleUrlLogoutSuccessHandler();
        this.urlLogoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl);
        this.casInfoService = casInfoService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (requiresLogout(request, response)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (logger.isDebugEnabled()) {
                logger.debug("Logging out user '" + auth
                        + "' and transferring to logout destination");
            }
            //本地登出
            logoutHandler.logout(request,response,auth);
            if (auth == null) {
                urlLogoutSuccessHandler.onLogoutSuccess(request,response, null);
            }else{
                //判断是否通过cas认证，获取cas信息
                Object details = auth.getDetails();
                if (details == null) {
                    urlLogoutSuccessHandler.onLogoutSuccess(request,response,auth);
                }
                if (details instanceof UserDetails) {
                    Integer tenantId = ((UserDetailsVO) details).getTenant().getTenantId();
                    CasInfo casInfoByTenantId = casInfoService.getCasInfoByTenantId(tenantId);
                    response.sendRedirect(casInfoByTenantId.getCasServiceLogout());
                }else{
                    urlLogoutSuccessHandler.onLogoutSuccess(request,response,auth);
                }
            }
            return;
        }

        filterChain.doFilter(request, response);

    }

    /**
     * 当前请求是否为登出请求
     */
    private boolean requiresLogout(HttpServletRequest request,
                                     HttpServletResponse response) {
        return logoutRequestMatcher.matches(request);
    }

}
