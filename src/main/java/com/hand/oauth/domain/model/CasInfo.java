package com.hand.oauth.domain.model;

import java.io.Serializable;

public class CasInfo implements Serializable {
    private Integer casId;

    private Integer tenantId;

    private String casServer;

    private String casServerLogin;

    private String casServerLogout;

    private String casService;

    private String casServiceLogout;

    public Integer getCasId() {
        return casId;
    }

    public void setCasId(Integer casId) {
        this.casId = casId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getCasServer() {
        return casServer;
    }

    public void setCasServer(String casServer) {
        this.casServer = casServer == null ? null : casServer.trim();
    }

    public String getCasServerLogin() {
        return casServerLogin;
    }

    public void setCasServerLogin(String casServerLogin) {
        this.casServerLogin = casServerLogin == null ? null : casServerLogin.trim();
    }

    public String getCasServerLogout() {
        return casServerLogout;
    }

    public void setCasServerLogout(String casServerLogout) {
        this.casServerLogout = casServerLogout == null ? null : casServerLogout.trim();
    }

    public String getCasService() {
        return casService;
    }

    public void setCasService(String casService) {
        this.casService = casService == null ? null : casService.trim();
    }

    public String getCasServiceLogout() {
        return casServiceLogout;
    }

    public void setCasServiceLogout(String casServiceLogout) {
        this.casServiceLogout = casServiceLogout == null ? null : casServiceLogout.trim();
    }
}