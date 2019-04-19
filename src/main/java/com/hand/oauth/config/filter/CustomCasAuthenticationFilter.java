package com.hand.oauth.config.filter;

import com.hand.oauth.domain.model.CasInfo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomCasAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final static String endpoint = "/login/cas";

    private UserDetailsService userDetailsService;

    public CustomCasAuthenticationFilter(String defaultFilterProcessesUrl, UserDetailsService userDetailsService) {
        super(defaultFilterProcessesUrl);
        this.userDetailsService = userDetailsService;
    }

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public CustomCasAuthenticationFilter() {
        super(new AntPathRequestMatcher(endpoint));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (!requiresAuthentication(req, res)) {
            chain.doFilter(request, response);
            return;
        }
        String ticket = obtainArtifact(req);
        //开始校验ticket
        try {
            CasInfo casInfo = (CasInfo) req.getSession().getAttribute("casInfoByTenantId");
            if (StringUtils.hasText(casInfo.getCasServer())) {
                //获取当前项目地址
                String service;
                int port = request.getServerPort();
                if (port != 80) {
                    service = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + endpoint;
                } else {
                    service = request.getScheme() + "://" + request.getServerName() + endpoint;
                }
                //开始校验ticket
                Assertion validateResult = getTicketValidator(casInfo.getCasServer()).validate(ticket, service);
                //根据校验结果，获取用户详细信息
                UserDetails userDetails = null;
                try {
                    userDetails = userDetailsService.loadUserByUsername(validateResult.getPrincipal().getName());
                    if (this.logger.isDebugEnabled()) {
                        logger.debug("userDetailsServiceImpl is loading username:"+validateResult.getPrincipal().getName());
                    }
                } catch (UsernameNotFoundException e) {
                    unsuccessfulAuthentication(req, res, e);
                }
                //手动封装authentication对象
                assert userDetails != null;
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(validateResult.getPrincipal(), ticket, userDetails.getAuthorities());
                authentication.setDetails(userDetails);
                successfulAuthentication(req,res,chain,authentication);


            } else {
                unsuccessfulAuthentication(req, res, new BadCredentialsException("bad credential:ticket校验失败"));
            }
        } catch (TicketValidationException e) {
            //ticket校验失败
            unsuccessfulAuthentication(req, res, new BadCredentialsException(e.getMessage()));
        }
//        chain.doFilter(request, response);
    }

    /**
     * 不做任何操作，实际用户认证在doFilter方法内完成，可以在此方法中对session进行自定义操作
     */
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        return null;
    }

    /**
     * 从HttpServletRequest请求中获取ticket
     */
    private String obtainArtifact(HttpServletRequest request) {
        String artifactParameter = "ticket";
        return request.getParameter(artifactParameter);
    }

    /**
     * 获取Cas30ServiceTicketValidator，暂时没有实现代理凭据
     */
    private TicketValidator getTicketValidator(String casServerUrlPrefix) {
        return new Cas30ServiceTicketValidator(casServerUrlPrefix);
    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }

        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Authentication request failed: " + failed.toString(), failed);
            this.logger.debug("Updated SecurityContextHolder to contain null Authentication");
            this.logger.debug("Delegating to authentication failure handler " + this.failureHandler);
        }

        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

}
