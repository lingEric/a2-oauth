package com.hand.oauth.config;

import com.hand.oauth.domain.model.CasInfo;
import com.hand.oauth.domain.model.Tenant;
import com.hand.oauth.domain.service.CasInfoService;
import com.hand.oauth.domain.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class LoginEndpointConfig {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CasInfoService casInfoService;

    @GetMapping("/login")
    public String loginJump(HttpSession session) {
        final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
        Object attribute = session.getAttribute(SAVED_REQUEST);
        if (attribute == null) {
            //默认跳转到登陆页面
            return "login";
        }
        if (attribute instanceof DefaultSavedRequest) {
            DefaultSavedRequest savedRequest = (DefaultSavedRequest) attribute;
            List<String> referer = savedRequest.getHeaderValues("referer");
            if (referer.size() == 1) {
                //有referer请求头
                String domain = referer.get(0);
                Tenant tenant = tenantService.selectByDomain(domain);
                if (tenant == null) {
                    return "login";
                } else {
                    String loginProvider = tenant.getLoginProvider();
                    switch (loginProvider) {
                        case "cas":
                            //获取cas地址
                            CasInfo casInfoByTenantId = casInfoService.getCasInfoByTenantId(tenant.getTenantId());
                            String casServerLogin = casInfoByTenantId.getCasServerLogin();
                            session.setAttribute("casInfoByTenantId",casInfoByTenantId);
                            return "redirect:" + casServerLogin;
                        case "oauth":
                            return "login";
                        default:
                            return "login";

                    }
                }

            } else {
                return "login";
            }
        }
        return "login";
    }
}
