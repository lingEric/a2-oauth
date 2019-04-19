# spring security集成cas



源码地址在文章末尾，转载请注明出处，谢谢。

## 0.配置本地ssl连接

操作记录如下：

```
=====================1.创建证书文件thekeystore ，并导出为thekeystore.crt
cd C:\Users\23570\keystore

C:\Users\23570\keystore>keytool -genkey -keyalg RSA -alias thekeystore -keystore thekeystore
输入密钥库口令:changeit
再次输入新口令:changeit
您的名字与姓氏是什么?
  [Unknown]:  localhost
您的组织单位名称是什么?
  [Unknown]:  localhost
您的组织名称是什么?
  [Unknown]:
您所在的城市或区域名称是什么?
  [Unknown]:
您所在的省/市/自治区名称是什么?
  [Unknown]:
该单位的双字母国家/地区代码是什么?
  [Unknown]:
CN=localhost, OU=localhost, O=Unknown, L=Unknown, ST=Unknown, C=Unknown是否正确?
  [否]:  y

输入 <thekeystore> 的密钥口令
        (如果和密钥库口令相同, 按回车):

Warning:
JKS 密钥库使用专用格式。建议使用 "keytool -importkeystore -srckeystore thekeystore -destkeystore thekeystore -deststoretype pkcs12" 迁移到行业标准格式 PKCS12。

C:\Users\23570\keystore>keytool -export -alias thekeystore -file thekeystore.crt -keystore thekeystore
输入密钥库口令:
存储在文件 <thekeystore.crt> 中的证书

Warning:
JKS 密钥库使用专用格式。建议使用 "keytool -importkeystore -srckeystore thekeystore -destkeystore thekeystore -deststoretype pkcs12" 迁移到行业标准格式 PKCS12。


======================2.把证书文件导入到本地证书库中，注意切换JRE相应目录
切换为【管理员身份】运行以下命令：

C:\Users\23570\keystore>keytool -import -alias thekeystore -storepass changeit -file thekeystore.crt -keystore "C:\Program Files\Java\jdk1.8.0_191\jre\lib\security\cacerts"
所有者: CN=localhost, OU=localhost, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
发布者: CN=localhost, OU=localhost, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
序列号: 657eb9ce
有效期为 Fri Mar 29 11:50:08 CST 2019 至 Thu Jun 27 11:50:08 CST 2019
证书指纹:
         MD5:  8D:3C:78:E9:8A:44:77:3F:C2:8B:20:95:C7:6C:91:8F
         SHA1: 69:F3:46:C4:03:95:E1:D0:E6:9D:8B:72:F4:EB:ED:13:8B:9A:6A:38
         SHA256: 79:D1:F8:B2:1B:E3:AF:D4:4F:35:CB:6B:C8:84:3F:85:21:13:0F:96:4A:B5:E5:4C:47:11:44:21:8F:F3:2D:83
签名算法名称: SHA256withRSA
主体公共密钥算法: 2048 位 RSA 密钥
版本: 3

扩展:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: B0 38 1D 00 56 65 EE 98   7C 35 58 04 B5 2E C0 A0  .8..Ve...5X.....
0010: D5 C2 C5 B5                                        ....
]
]

是否信任此证书? [否]:  y
证书已添加到密钥库中

=========================3.配置tomcat/conf/server.xml中的ssl连接

<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="200" SSLEnabled="true" scheme="https"
           secure="true" clientAuth="false" sslProtocol="TLS"
           keystoreFile="C:\Users\23570\keystore\thekeystore"
           keystorePass="changeit"/>
		   
==========================4.其他命令参考
删除JRE中指定别名的证书
keytool -delete -alias cas.server.com -keystore "C:\Program Files\Java\jdk1.8.0_191\jre\lib\security\cacerts"

查看JRE中指定别名的证书
keytool -list -v -keystore "C:\Program Files\Java\jdk1.8.0_191\jre\lib\security\cacerts" -alias cas.server.com		   

```



## 1.cas服务搭建

```shell
git clone --branch 5.3 https://github.com/apereo/cas-overlay-template.git cas-server
```

注意：

这里选用cas server 5.3版本，使用maven构建

### 1.使用数据库账号密码登录cas

导入依赖

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-jdbc</artifactId>
    <version>${cas.version}</version>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.47</version>
</dependency>
```

配置查询

```properties
#这里是配置用户表单登录时用户名字段为username
cas.authn.jdbc.query[0].sql=select password from oauth_account left join oauth_user on oauth_account.user_id=oauth_user.user_id where oauth_user.username=?;
cas.authn.jdbc.query[0].fieldPassword=password
cas.authn.jdbc.query[0].fieldExpired=expired
cas.authn.jdbc.query[0].fieldDisabled=disabled

cas.authn.jdbc.query[0].dialect=org.hibernate.dialect.MySQLDialect
cas.authn.jdbc.query[0].driverClass=com.mysql.jdbc.Driver
cas.authn.jdbc.query[0].url=jdbc:mysql://127.0.0.1:3306/srm-aurora2?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false
cas.authn.jdbc.query[0].user=root
cas.authn.jdbc.query[0].password=root

#默认不加密
#cas.authn.jdbc.query[0].passwordEncoder.type=NONE

#默认加密策略，通过encodingAlgorithm来指定算法，默认NONE不加密
cas.authn.jdbc.query[0].passwordEncoder.type=DEFAULT
cas.authn.jdbc.query[0].passwordEncoder.characterEncoding=UTF-8
cas.authn.jdbc.query[0].passwordEncoder.encodingAlgorithm=MD5

#配置用户表单登录时用户名字段为phone
cas.authn.jdbc.query[1].sql=select password from oauth_account left join oauth_user on oauth_account.user_id=oauth_user.user_id where oauth_user.phone=?;
cas.authn.jdbc.query[1].fieldPassword=password
cas.authn.jdbc.query[1].fieldExpired=expired
cas.authn.jdbc.query[1].fieldDisabled=disabled

cas.authn.jdbc.query[1].dialect=org.hibernate.dialect.MySQLDialect
cas.authn.jdbc.query[1].driverClass=com.mysql.jdbc.Driver
cas.authn.jdbc.query[1].url=jdbc:mysql://127.0.0.1:3306/srm-aurora2?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false
cas.authn.jdbc.query[1].user=root
cas.authn.jdbc.query[1].password=root

#默认不加密
#cas.authn.jdbc.query[0].passwordEncoder.type=NONE

#默认加密策略，通过encodingAlgorithm来指定算法，默认NONE不加密
cas.authn.jdbc.query[1].passwordEncoder.type=DEFAULT
cas.authn.jdbc.query[1].passwordEncoder.characterEncoding=UTF-8
cas.authn.jdbc.query[1].passwordEncoder.encodingAlgorithm=MD5
```

数据库脚本

```sql
/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50722
 Source Host           : localhost:3306
 Source Schema         : srm-aurora2

 Target Server Type    : MySQL
 Target Server Version : 50722
 File Encoding         : 65001

 Date: 19/04/2019 14:40:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oauth_account
-- ----------------------------
DROP TABLE IF EXISTS `oauth_account`;
CREATE TABLE `oauth_account`  (
  `account_id` int(255) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(255) NULL DEFAULT NULL,
  `user_id` int(255) NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`account_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_account
-- ----------------------------
INSERT INTO `oauth_account` VALUES (1, 1, 1, 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `oauth_account` VALUES (2, 2, 2, 'e10adc3949ba59abbe56e057f20f883e');

-- ----------------------------
-- Table structure for oauth_cas_info
-- ----------------------------
DROP TABLE IF EXISTS `oauth_cas_info`;
CREATE TABLE `oauth_cas_info`  (
  `cas_id` int(255) NOT NULL,
  `tenant_id` int(255) NULL DEFAULT NULL,
  `cas_server` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cas_server_login` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cas_server_logout` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cas_service` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cas_service_logout` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`cas_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_cas_info
-- ----------------------------
INSERT INTO `oauth_cas_info` VALUES (1, 2, 'https://localhost:8443/cas', 'https://localhost:8443/cas/login?service=http%3A%2F%2Flocalhost%3A8083%2Flogin%2Fcas', 'https://localhost:8443/cas/logout', 'http://localhost:8083/login/cas', 'https://localhost:8443/cas/logout?service=http://localhost:8083/logout/success');
INSERT INTO `oauth_cas_info` VALUES (2, 3, 'https://localhost:9443/sso', 'https://localhost:9443/sso/login?service=http%3A%2F%2Flocalhost%3A8083%2Flogin%2Fcas', 'https://localhost:9443/sso/logout', 'http://localhost:8083/login/cas', 'https://localhost:9443/sso/logout?service=http://localhost:8083/logout/success');

-- ----------------------------
-- Table structure for oauth_tenant
-- ----------------------------
DROP TABLE IF EXISTS `oauth_tenant`;
CREATE TABLE `oauth_tenant`  (
  `tenant_id` int(255) NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `login_provider` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `login_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_tenant
-- ----------------------------
INSERT INTO `oauth_tenant` VALUES (1, 'http://localhost:8084/', 'a租户', 'oauth', 'form');
INSERT INTO `oauth_tenant` VALUES (2, 'http://localhost:8085/', 'b租户', 'cas', 'wechat');
INSERT INTO `oauth_tenant` VALUES (3, 'http://localhost:8086/', 'c租户', 'cas', 'form');

-- ----------------------------
-- Table structure for oauth_user
-- ----------------------------
DROP TABLE IF EXISTS `oauth_user`;
CREATE TABLE `oauth_user`  (
  `user_id` int(255) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_user
-- ----------------------------
INSERT INTO `oauth_user` VALUES (1, '22304', '15797656200', 'donglin.ling@hand-china.com');
INSERT INTO `oauth_user` VALUES (2, 'admin', '15797656201', 'ericling666@gmail.com');

SET FOREIGN_KEY_CHECKS = 1;

```

发布cas server，访问：

https://localhost:8443/cas/login

测试账号和密码，admin:123456

### 2.CAS客户端服务注册

这里演示通过json文件注册服务，实际项目中，可以配置成从数据库中注册

1. 添加json支持依赖

   ```xml
   <!--json服务注册-->
   <dependency>
       <groupId>org.apereo.cas</groupId>
       <artifactId>cas-server-support-json-service-registry</artifactId>
       <version>${cas.version}</version>
   </dependency>
   ```

2. 添加json服务注册文件

   ```json
   {
     "@class" : "org.apereo.cas.services.RegexRegisteredService",
     "serviceId" : "^(https|http|imaps)://.*",
     "name" : "HTTPS and HTTP and IMAPS",
     "id" : 10000001,
     "description" : "This service definition authorizes all application urls that support HTTPS and HTTP and IMAPS protocols.",
     "evaluationOrder" : 10000,
     "attributeReleasePolicy": {
       "@class": "org.apereo.cas.services.ReturnAllAttributeReleasePolicy"
     },
     "proxyPolicy": {
       "@class": "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
       "pattern": "^(https|http)?://.*"
     }
   }
   ```

   注意文件目录和文件名格式：

   目录：resources/services/{xxx}-{id}.json

   xxx表示可以随意配置，后面-{id}，这里的id需要和文件中的id一致。

   作为演示，这个json注册文件，没有限制域名，也就是说所有的服务都可以注册成功。

3. 开启json服务注册

   ```properties
   
   ##
   # 开启json服务注册
   #
   cas.serviceRegistry.initFromJson=true
   ```

   

以上就是配置json服务注册的过程。

### 3.其它常用配置

```properties

##
# 登出后允许跳转到指定页面
#
cas.logout.followServiceRedirects=true

# 设置service ticket的行为
# cas.ticket.st.maxLength=20
# cas.ticket.st.numberOfUses=1
cas.ticket.st.timeToKillInSeconds=120

# 设置proxy ticket的行为
cas.ticket.pt.timeToKillInSeconds=120
# cas.ticket.pt.numberOfUses=1
```

配置说明：

1. 配置cas服务登出时，是否跳转到各个子服务的登出页面，默认false【即默认情况下，子服务点击登出，用户统一跳转到cas的登出页面】，子服务登出时访问cas登出端点，并带上service。

   示例：`https://localhost:8443/cas/logout?service=http://localhost:8083/logout/success`

   这样配置，cas注销session之后，会重定向到service。

   这个字段可以配置，默认是service。配置如下：

   ```properties
   cas.logout.redirectParameter=service
   ```

2. 配置service ticket的失效时间，我这里配置这个选项，是为了方便后面debug调试，实际生产中，不必配置这个选项。

更多常用配置项，请查看官网链接：<https://apereo.github.io/cas/5.3.x/installation/Configuration-Properties.html>



## 2.spring security和cas集成

### 1.依赖和其他配置

1. 核心依赖

   ```xml
   <!--security-cas集成-->
   <dependency>
       <groupId>org.springframework.security</groupId>
       <artifactId>spring-security-cas</artifactId>
   </dependency>
   
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
   ```

2. application.yml配置

   ```yml
   # 我这里是为了方便调试
   logging.level.org.springframework.security: debug
   logging.level.web: debug
   ```

### 2.配置登录端点

1. spring security开启表单登陆

   ```java
   @Override
       protected void configure(HttpSecurity http) throws Exception {
       	http.formLogin().loginPage("/login");
   	}
   ```

   这个配置，会开启用户表单登录，并且配置登录端点为`/login`

2. 配置登录端点响应逻辑

   ```java
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
   ```

   我这里的登陆逻辑实现了：用户从第三方网站【平台的租户】跳转到这个网站时，根据跳转过来的请求头【referer】获取这个租户的域名，再从数据库中查找这个域名对应的租户信息和登录逻辑。

   这里的租户信息有一个关键字段是：`loginProvider`，有两种情况`cas`，`oauth`

   1. `cas`:租户有自己的cas单点登录系统，平台需要和租户的cas集成
   2. `oauth`：租户没有cas，使用平台统一的表单登陆

   具体的登录流程分析，在最后详细介绍，这里不过多讲解。

### 3.配置CAS的ticket校验以及登录响应

1. 自定义AuthenticationFilter

   因为我的需求是，每个租户有自己的cas系统，所以每个cas地址不一样，不可能使用官方的`CasAuthenticationFilter` 。具体原因是，官方的`CasAuthenticationFilter`在应用程序启动时，资源匹配器就已经初始化好了，它只会对特定的cas地址发送ticket校验请求。而要做到可配置，就只能自己实现这个逻辑，并且可配置的对相应cas server地址发出ticket校验请求。

   ```java
   
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
   
   ```

2. 把自定义的`CustomCasAuthenticationFilter`添加到spring security的过滤器链中

   ```java
   @Qualifier("userDetailsServiceImpl")
   @Autowired
   private UserDetailsService userDetailsService;
   
   private final static String endpoint = "/login/cas";
   
   @Override
   protected void configure(HttpSecurity http) throws Exception {
   	http.addFilterAt(new CustomCasAuthenticationFilter(endpoint, userDetailsService), UsernamePasswordAuthenticationFilter.class);
   }
   ```

   

### 4.配置单点登出

1. 自定义实现`LogoutFilter`

   ```java
   
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
   
   ```

2. 把`CustomLogoutFilter`添加到spring security的过滤器链中

   ```java
   @Override
   protected void configure(HttpSecurity http) throws Exception {
   	http.addFilterAt(new CustomLogoutFilter("/logout", "/logout/success", casInfoService), LogoutFilter.class);
   }
   ```

   

### 5.流程分析

#### 1.表单登陆流程分析

目前有5个服务

cas server,tenant-a,tenant-b,tenant-c,a2-oauth

租户a，b，c就是一个超链接而已，为了模拟三个租户的域名，所以弄了三个租户。

这三个域名分别是：

`<http://localhost:8084/>` , `<http://localhost:8085/>` , `<http://localhost:8086/>`

数据库中，对这3个租户的配置如下：

![](http://ww1.sinaimg.cn/large/006edVQGgy1g27zywkcz7j30cy02o3ye.jpg)

其中b和c租户是配置了cas登录的。

cas server发布了两个，都开了SSL链接，分别是：

https://localhost:8443/cas ，https://localhost:9443/sso

我们先测试表单登录。启动租户a，访问链接http://localhost:8084 ,这个页面只有一个超链接，点击超链接，访问

`http://localhost:8083/oauth/authorize?client_id=youku&response_type=token&redirect_uri=http://localhost:8081/youku/qq/redirect`

查看日志：

```
//前面经过spring security的一堆过滤器链，都没有匹配到
FrameworkEndpointHandlerMapping : Mapped to public org.springframework.web.servlet.ModelAndView org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint.authorize(java.util.Map<java.lang.String, java.lang.Object>,java.util.Map<java.lang.String, java.lang.String>,org.springframework.web.bind.support.SessionStatus,java.security.Principal)

//用户未认证，无法授权，抛出异常，ExceptionTranslationFilter对异常处理，跳转到配置的authentication //entry point，这里的authentication entry point，就是我之前配置的/login端点
2019-04-19 16:01:14.608 DEBUG 21568 --- [nio-8083-exec-1] o.s.web.servlet.DispatcherServlet        : Failed to complete request: org.springframework.security.authentication.InsufficientAuthenticationException: User must be authenticated with Spring Security before authorization can be completed.
2019-04-19 16:01:14.611 DEBUG 21568 --- [nio-8083-exec-1] o.s.s.w.a.ExceptionTranslationFilter     : Authentication exception occurred; redirecting to authentication entry point

org.springframework.security.authentication.InsufficientAuthenticationException: User must be authenticated with Spring Security before authorization can be completed.
```

![](http://ww1.sinaimg.cn/large/006edVQGgy1g280cpeufpj311y0jvwju.jpg)

可以看到，已经进入到了controller里面。

```java
final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
        Object attribute = session.getAttribute(SAVED_REQUEST);
```

这段代码的作用是为了拿到，之前发起的请求。那么这个请求是什么时候被保存的呢？

我们知道抛出异常之后，ExceptionTranslationFilter对异常进行处理，检测到用户没有登录，所以才跳转到authentication entry point，所以，猜想应该是这里保存了最开始的请求信息。

以下是ExceptionTranslationFilter的核心代码：

```java
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    this.handleSpringSecurityException(request, response, chain, (RuntimeException)ase);
}

private void handleSpringSecurityException(HttpServletRequest request, HttpServletResponse response, FilterChain chain, RuntimeException exception) throws IOException, ServletException {
        if (exception instanceof AuthenticationException) {
            this.logger.debug("Authentication exception occurred; redirecting to authentication entry point", exception);
            this.sendStartAuthentication(request, response, chain, (AuthenticationException)exception);
        } else if (exception instanceof AccessDeniedException) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!this.authenticationTrustResolver.isAnonymous(authentication) && !this.authenticationTrustResolver.isRememberMe(authentication)) {
                this.logger.debug("Access is denied (user is not anonymous); delegating to AccessDeniedHandler", exception);
                this.accessDeniedHandler.handle(request, response, (AccessDeniedException)exception);
            } else {
                this.logger.debug("Access is denied (user is " + (this.authenticationTrustResolver.isAnonymous(authentication) ? "anonymous" : "not fully authenticated") + "); redirecting to authentication entry point", exception);
                this.sendStartAuthentication(request, response, chain, new InsufficientAuthenticationException(this.messages.getMessage("ExceptionTranslationFilter.insufficientAuthentication", "Full authentication is required to access this resource")));
            }
        }

    }
```

这里对异常的处理，其实，核心就只有两个方法：

1. `this.accessDeniedHandler.handle(request, response, (AccessDeniedException)exception);` ，这种情况下，用户已经登陆了，但是权限不够，所以交给accessDeniedHandler进行处理，一般来讲，如果没有进行特殊的配置，会返回一个403错误和异常信息【不再跳转到authentication entry point，因为用户已经登陆了】，这里不深究。

2. `this.sendStartAuthentication(request, response, chain, (AuthenticationException)exception);` ,这个方法核心代码如下：

   ```java
   protected void sendStartAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, AuthenticationException reason) throws ServletException, IOException {
           SecurityContextHolder.getContext().setAuthentication((Authentication)null);
       	//就是在这里保存的这次请求的所有信息，包括请求头，请求路径，参数，cookie等详细信息。所以，后面跳转到/login端点时，我在controller里面可以拿出来。
           this.requestCache.saveRequest(request, response);
           this.logger.debug("Calling Authentication entry point.");
       	//这里就是发起用户认证了，根据我的配置，它就会跳转到/login
           this.authenticationEntryPoint.commence(request, response, reason);
       }
   ```



再回到前面的controller登录逻辑，往下走：

```java
@GetMapping("/login")
public String loginJump(HttpSession session) {
    final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    Object attribute = session.getAttribute(SAVED_REQUEST);
    // 默认情况下，用户直接访问/login时，没有SAVED_REQUEST
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
            //获取到数据库中配置的租户信息
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
                        //因为我在数据库中配置的是oauth，所以，最后响应login视图
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
```

用户跳转到登陆页面

![](http://ww1.sinaimg.cn/large/006edVQGgy1g280pr0ws7j30gq0bz3yn.jpg)

输入用户名密码，点击登陆，进入`UsernamePasswordAuthenticationFilter` ，开始尝试认证用户

```java
public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		if (postOnly && !request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException(
					"Authentication method not supported: " + request.getMethod());
		}

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				username, password);

		// Allow subclasses to set the "details" property
		setDetails(request, authRequest);

		return this.getAuthenticationManager().authenticate(authRequest);
	}
```

最终会调用AuthenticationManager接口的authenticate方法，而`AuthenticationManager`委托一堆的AuthenticationProvider来进行认证。后面的流程，不再赘述，不在本篇文章的讨论范畴。

用户认证成功后，调用`successfulAuthentication(request, response, chain, authResult);` 其实，这个方法里面核心代码就是`successHandler.onAuthenticationSuccess(request, response, authResult);`

AuthenticationSuccessHandler有很多实现类，我们也可以自定义实现AuthenticationSuccessHandler。最常用的实现是，`SavedRequestAwareAuthenticationSuccessHandler` ，看一下它里面的核心代码：

```java
@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		SavedRequest savedRequest = requestCache.getRequest(request, response);

		if (savedRequest == null) {
			super.onAuthenticationSuccess(request, response, authentication);

			return;
		}
		String targetUrlParameter = getTargetUrlParameter();
		if (isAlwaysUseDefaultTargetUrl()
				|| (targetUrlParameter != null && StringUtils.hasText(request
						.getParameter(targetUrlParameter)))) {
			requestCache.removeRequest(request, response);
			super.onAuthenticationSuccess(request, response, authentication);

			return;
		}

		clearAuthenticationAttributes(request);

		// Use the DefaultSavedRequest URL
		String targetUrl = savedRequest.getRedirectUrl();
		logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
```

其实，这个方法，就是获取到之前保存的请求信息，然后再重定向到之前的请求。

#### 2.CAS登录流程分析

这次，我们访问租户b，这个租户，配置了cas登录。

访问租户b：<http://localhost:8085/> ，这个页面里，也就是一个超链接，点击超链接，访问

http://localhost:8083/oauth/authorize?client_id=iqiyi&response_type=token&redirect_uri=http://localhost:8081/iqiyi/qq/redirect

前面的流程还是一样的，经过spring security的过滤器链，都没有匹配到，在最后DispatcherServlet抛出异常，然后ExceptionTranslationFilter对异常处理，跳转到/login端点，然后拿出配置在数据库中的casInfo，跳转到

https://localhost:8443/cas/login?service=http%3A%2F%2Flocalhost%3A8083%2Flogin%2Fcas

![](http://ww1.sinaimg.cn/large/006edVQGgy1g281gblfkgj311y0jvwjp.jpg)

输入用户名密码，cas成功认证用户之后，生成TGT

```
=============================================================
WHO: admin
WHAT: Supplied credentials: [admin]
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Fri Apr 19 16:51:01 CST 2019
CLIENT IP ADDRESS: 0:0:0:0:0:0:0:1
SERVER IP ADDRESS: 0:0:0:0:0:0:0:1
=============================================================

>
2019-04-19 16:51:01,300 INFO [org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager] - <Audit trail record BEGIN
=============================================================
WHO: admin
WHAT: TGT-**************************GHfz0lUJQE-8fkKJgyv8WXNE5FYLBqb7zfWGfNoKwDZ0AjqA-DESKTOP-GDU9JII
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Fri Apr 19 16:51:01 CST 2019
CLIENT IP ADDRESS: 0:0:0:0:0:0:0:1
SERVER IP ADDRESS: 0:0:0:0:0:0:0:1
=============================================================

>
2019-04-19 16:51:01,307 INFO [org.apereo.cas.DefaultCentralAuthenticationService] - <Granted ticket [ST-35-Mf1v9Z2qVVVKlWeTgyc-Hlzh2xY-DESKTOP-GDU9JII] for service [http://localhost:8083/login/cas] and principal [admin]>
2019-04-19 16:51:01,308 INFO [org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager] - <Audit trail record BEGIN
=============================================================
WHO: admin
WHAT: ST-35-Mf1v9Z2qVVVKlWeTgyc-Hlzh2xY-DESKTOP-GDU9JII for http://localhost:8083/login/cas
ACTION: SERVICE_TICKET_CREATED
APPLICATION: CAS
WHEN: Fri Apr 19 16:51:01 CST 2019
CLIENT IP ADDRESS: 0:0:0:0:0:0:0:1
SERVER IP ADDRESS: 0:0:0:0:0:0:0:1
=============================================================
```

然后跳转到service地址，也就是

localhost:8083/login/cas ,并带上为这个service生成的service ticket，所以最后的请求地址为：

```http
http://localhost:8083/login/cas?ticket=ST-35-Mf1v9Z2qVVVKlWeTgyc-Hlzh2xY-DESKTOP-GDU9JII
```

而这个端点`/login/cas`会被我配置的自定义CustomCasAuthenticationFilter拦截

![](http://ww1.sinaimg.cn/large/006edVQGgy1g281qb9jqnj311y0jvjwt.jpg)



```java
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
```

校验成功之后，我的逻辑是，手动加载用户信息，然后把当前认证信息Authentication放到SecurityContextHolder中。

```java
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

```



#### 3.单点登出流程分析

用户发送`/logout`请求，被我自定义的`CustomLogoutFilter`拦截

![](http://ww1.sinaimg.cn/large/006edVQGgy1g281vaf77xj311y0jv0y7.jpg)

之后的逻辑是，先从本地登出，然后判断之前是否是从cas认证的，如果是，再获取cas信息，然后把cas也登出了。这里判断登陆用户的认证方式，我想了很久，最后的实现思路如下：

之前通过cas登录时，我手动的添加登陆用户的认证方式到Authentication中。代码如下：

```java
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

//就是这里做了文章
authentication.setDetails(userDetails);
successfulAuthentication(req,res,chain,authentication);
```



然后，登出时，拿到这个信息，进行登出操作。因为，我在userdetails中封装了这个信息，所以可以拿到。

```java
public class UserDetailsVO implements UserDetails {
    //user
    private Integer userId;

    private String username;

    private String phone;

    private String email;

    //tenant
    private Tenant tenant;

    //account
    private Integer accountId;

    private String password;
    //省略setter和getter
}
```



源码地址：<https://github.com/lingEric/a2-oauth>