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

 Date: 19/04/2019 17:13:04
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
