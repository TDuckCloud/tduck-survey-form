/*
 Navicat Premium Data Transfer

 Source Server         : tduck
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : 212.64.73.120:3310
 Source Schema         : tduck-v4

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 06/04/2023 21:10:42
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ac_user
-- ----------------------------
DROP TABLE IF EXISTS `ac_user`;
CREATE TABLE `ac_user`
(
    `id`                 bigint unsigned NOT NULL AUTO_INCREMENT,
    `name`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT '' COMMENT '姓名',
    `avatar`             varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像',
    `gender`             tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别0未知 1男2女',
    `email`              varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '邮箱',
    `phone_number`       varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT '手机号',
    `password`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '密码',
    `reg_channel`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin                 DEFAULT NULL COMMENT '注册渠道',
    `last_login_channel` tinyint                                                                DEFAULT NULL COMMENT '最后登录渠道',
    `last_login_time`    datetime                                                               DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT '最后登录Ip',
    `password_type`      tinyint(1) DEFAULT '0' COMMENT '密码类型',
    `need_change_password` tinyint(1) DEFAULT '0' COMMENT '是否需要修改密码 0否 1是',
    `deleted`            tinyint(1) DEFAULT '0' COMMENT '状态',
    `create_time`        datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15672 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户';
INSERT INTO ac_user (`id`, `name`, `avatar`, `gender`, `email`, `phone_number`, `password`, `reg_channel`,
                     `last_login_channel`, `last_login_time`, `last_login_ip`, `password_type`, `need_change_password`, `deleted`,
                     `create_time`, `update_time`)
VALUES (1, 'admin', '', 1, 'admin@tduckcloud.com', NULL, '$2a$10$FgOTdkh3qVLE9DNgD4XzDu2PCJB3QtnGbriBPaMhMKTVWM9XYsiIm',
        '1', 2, '2023-04-06 09:35:22', '172.17.0.1', 1, 1, 0, '2021-06-13 13:49:25', '2023-04-06 09:35:22');

-- ----------------------------
-- Table structure for ac_user_authorize
-- ----------------------------
DROP TABLE IF EXISTS `ac_user_authorize`;
CREATE TABLE `ac_user_authorize`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `type`        tinyint(1) NOT NULL COMMENT '第三方平台类型',
    `app_id`      varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '平台AppId',
    `open_id`     varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '平台OpenId',
    `user_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '平台用户名',
    `user_id`     bigint                                                                 DEFAULT NULL COMMENT '用户Id',
    `user_info`   json                                                                   DEFAULT NULL COMMENT '平台用户信息',
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `open_id` (`open_id`,`type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='第三方用户授权信息';

-- ----------------------------
-- Table structure for ac_user_token
-- ----------------------------
DROP TABLE IF EXISTS `ac_user_token`;
CREATE TABLE `ac_user_token`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `type`        int                                                           NOT NULL DEFAULT '0' COMMENT '类型',
    `user_id`     bigint                                                        NOT NULL COMMENT '用户Id',
    `token`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'token',
    `expire_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    constraint token unique (token)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户';

-- ----------------------------
-- Table structure for fm_form_template
-- ----------------------------
DROP TABLE IF EXISTS `fm_form_template`;
CREATE TABLE `fm_form_template`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '模板Id',
    `form_key`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '模板唯一标识',
    `cover_img`   varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL COMMENT '封面图',
    `name`        text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '模板名称',
    `description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '模板I描述',
    `category_id` int                                                          NOT NULL COMMENT '模板类型',
    `scheme`      json                                                                  DEFAULT NULL COMMENT '模板定义',
    `status`      tinyint                                                      NOT NULL DEFAULT '0' COMMENT '状态',
    `update_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3613 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单模板';

-- ----------------------------
-- Table structure for fm_form_template_category
-- ----------------------------
DROP TABLE IF EXISTS `fm_form_template_category`;
CREATE TABLE `fm_form_template_category`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `name`        varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分类名称',
    `sort`        int      DEFAULT NULL COMMENT '排序',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='项目模板分类';

-- ----------------------------
-- Table structure for fm_form_theme
-- ----------------------------
DROP TABLE IF EXISTS `fm_form_theme`;
CREATE TABLE `fm_form_theme`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT,
    `name`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主题名称',
    `style`          bigint                                                        NOT NULL COMMENT '主题风格',
    `head_img_url`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '头部图片',
    `background_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '背景图片',
    `theme_color`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '主题颜色代码',
    `update_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=236 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='项目主题外观模板';

-- ----------------------------
-- Table structure for fm_form_theme_category
-- ----------------------------
DROP TABLE IF EXISTS `fm_form_theme_category`;
CREATE TABLE `fm_form_theme_category`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `name`        varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '主题名称',
    `sort`        int      DEFAULT NULL COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单主题分类';

-- ----------------------------
-- Table structure for fm_user_form
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form`;
CREATE TABLE `fm_user_form`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `form_key`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单唯一标识',
    `source_id`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL COMMENT '来源Id',
    `source_type` tinyint                                                               DEFAULT NULL COMMENT '来源类型',
    `name`        text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单名称',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '表单描述',
    `user_id`     bigint                                                       NOT NULL COMMENT '用户ID',
    `type`        varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          DEFAULT NULL COMMENT '表单类型',
    `status`      tinyint                                                      NOT NULL DEFAULT '0' COMMENT '状态',
    `is_deleted`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    `is_folder`   tinyint unsigned DEFAULT '0' COMMENT '是文件夹',
    `folder_id`   bigint                                                                DEFAULT '0' COMMENT '文件夹Id',
    `update_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `code` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8467 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户表单';

-- ----------------------------
-- Table structure for fm_user_form_auth
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_auth`;
CREATE TABLE `fm_user_form_auth`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT,
    `form_key`      varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '项目key',
    `auth_group_id` bigint   DEFAULT NULL COMMENT '授权组id',
    `user_id_list`  json     DEFAULT NULL COMMENT '用户Id列表',
    `role_id_list`  json     DEFAULT NULL COMMENT '角色Id列表',
    `dept_id_list`  json     DEFAULT NULL COMMENT '部门Id列表',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `project_key` (`form_key`,`auth_group_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=159 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单授权对象';

-- ----------------------------
-- Table structure for fm_user_form_data
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_data`;
CREATE TABLE `fm_user_form_data`
(
    `id`                bigint                                                        NOT NULL AUTO_INCREMENT,
    `form_key`          varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单key',
    `serial_number`     int                                                           DEFAULT NULL COMMENT '序号',
    `original_data`     json                                                          DEFAULT NULL COMMENT '填写结果',
    `submit_ua`         json                                                          DEFAULT NULL COMMENT '提交ua',
    `submit_os`         varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '提交系统',
    `submit_browser`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '提交浏览器',
    `submit_request_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '请求ip',
    `submit_address`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '提交地址',
    `complete_time`     int                                                           DEFAULT NULL COMMENT '完成时间 毫秒',
    `wx_open_id`        varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信openId',
    `wx_user_info`      json                                                          DEFAULT NULL COMMENT '微信用户信息',
    `ext_value`         varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '扩展字段记录来源等',
    `create_time`       datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建人',
    `update_time`       datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`         varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                 `project_key` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=211738 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单收集数据结果';

-- ----------------------------
-- Table structure for fm_user_form_item
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_item`;
CREATE TABLE `fm_user_form_item`
(
    `id`              bigint                                                        NOT NULL AUTO_INCREMENT,
    `form_key`        varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '项目key',
    `form_item_id`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '表单项Id',
    `type`            varchar(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '表单项类型 ',
    `label`           text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单项标题',
    `is_display_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '展示类型组件',
    `is_hide_type`    tinyint(1) NOT NULL DEFAULT '0' COMMENT '隐藏类型组件',
    `is_special_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '特殊处理类型',
    `show_label`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否显示标签',
    `default_value`   varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL COMMENT '表单项默认值',
    `required`        tinyint(1) NOT NULL COMMENT '是否必填',
    `placeholder`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          DEFAULT NULL COMMENT '输入型提示文字',
    `sort`            bigint                                                                 DEFAULT '0' COMMENT '排序',
    `span`            int                                                           NOT NULL DEFAULT '24' COMMENT '栅格宽度',
    `scheme`          json                                                                   DEFAULT NULL COMMENT '表表单原始JSON',
    `reg_list`        json                                                                   DEFAULT NULL COMMENT '正则表达式 ',
    `update_time`     datetime                                                               DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`     datetime                                                               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY               `project_key` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=59044 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单项';

-- ----------------------------
-- Table structure for fm_user_form_logic
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_logic`;
CREATE TABLE `fm_user_form_logic`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '逻辑Id',
    `form_key`    varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单key',
    `scheme`      json     DEFAULT NULL COMMENT '逻辑定义',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `project_key` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1707 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='项目逻辑';

-- ----------------------------
-- Table structure for fm_user_form_setting
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_setting`;
CREATE TABLE `fm_user_form_setting`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `form_key`    varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单key',
    `settings`    json     DEFAULT NULL COMMENT '设置内容 ',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `project_key` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=491 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='表单设置表';

-- ----------------------------
-- Table structure for fm_user_form_theme
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_theme`;
CREATE TABLE `fm_user_form_theme`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT,
    `form_key`         varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单key',
    `submit_btn_text`  varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '提交按钮文字',
    `logo_img`         varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'logo图片',
    `logo_position`    varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT 'logo位置',
    `background_color` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '背景颜色',
    `background_img`   varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '背景图片',
    `show_title`       tinyint(1) DEFAULT '1' COMMENT '是否显示标题',
    `show_describe`    tinyint(1) DEFAULT '1' COMMENT '是否显示描述语',
    `theme_color`      varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  DEFAULT NULL COMMENT '主题颜色\r\n',
    `show_number`      tinyint(1) DEFAULT '0' COMMENT '显示序号',
    `show_submit_btn`  tinyint(1) DEFAULT '1' COMMENT '显示提交按钮',
    `head_img_url`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头部图片',
    `update_time`      datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`      datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `project_key` (`form_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1772 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='项目表单项';

-- ----------------------------
-- Table structure for fm_user_form_view_count
-- ----------------------------
DROP TABLE IF EXISTS `fm_user_form_view_count`;
CREATE TABLE `fm_user_form_view_count`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `form_key`    varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '表单唯一标识',
    `count`       int                                                          NOT NULL DEFAULT '0',
    `update_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `form_key` (`form_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户表单查看次数';

-- ----------------------------
-- Table structure for sys_env_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_env_config`;
CREATE TABLE `sys_env_config`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '参数主键',
    `env_key`     varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '配置key',
    `env_value`   json                                                          NOT NULL COMMENT '参数键值',
    `update_time` datetime                                                               DEFAULT NULL COMMENT '更新时间',
    `create_time` datetime                                                               DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='系统环境配置表';

INSERT INTO sys_env_config (`id`, `env_key`, `env_value`, `update_time`, `create_time`)
VALUES (9, 'systemInfoConfig', '{
  \"webBaseUrl\": \"\", \"openWxMpLogin\": false}', '2023-04-04 14:33:29', '2023-04-06 21:19:21');
INSERT INTO sys_env_config (`id`, `env_key`, `env_value`, `update_time`, `create_time`)
VALUES (14, 'fileEnvConfig', '{
  \"ossType\": \"LOCAL\"}', '2023-03-26 14:34:38', '2023-04-04 22:48:43');
-- ----------------------------
-- Table structure for wx_mp_user
-- ----------------------------
DROP TABLE IF EXISTS `wx_mp_user`;
CREATE TABLE `wx_mp_user`
(
    `id`           int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键',
    `appid`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公众号AppId',
    `nickname`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
    `sex`          tinyint(1) DEFAULT NULL COMMENT '性别',
    `head_img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像',
    `union_id`     varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
    `open_id`      varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `country`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家',
    `province`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '省',
    `city`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '城市',
    `is_subscribe` tinyint(1) DEFAULT '1' COMMENT '是否关注',
    `user_id`      bigint                                                        DEFAULT NULL COMMENT '用户Id',
    `update_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY            `wx_union_id` (`head_img_url`(191)) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15651 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='微信公众号用户 ';

DROP TABLE IF EXISTS `webhook_config`;
CREATE TABLE `webhook_config`
(
    `id`            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `hook_name`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT NULL COMMENT 'Webhook配置名称',
    `source_type`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '配置的来源类型',
    `source_id`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源Id',
    `url`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Webhook的URL地址',
    `request_type`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Webhook请求类型，如POST、GET等',
    `enabled`       tinyint(1) NOT NULL COMMENT '是否启用',
    `other_options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '其他Webhook配置，例如请求头等。以JSON格式存储',
    `create_time`   timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY             `source_type` (`source_type`,`source_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Webhook配置表';

DROP TABLE IF EXISTS `webhook_event`;
CREATE TABLE `webhook_event`
(
    `id`                bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `webhook_config_id` bigint                                                        NOT NULL COMMENT '关联的Webhook配置ID',
    `source_id`         varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源的数据Id',
    `event_type`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Webhook事件类型',
    `event_data`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Webhook事件数据',
    `status`            varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Webhook事件状态，如pending、success、failed等',
    `retry_times`       int                                                           NOT NULL DEFAULT '0' COMMENT 'Webhook事件重试次数',
    `last_error`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Webhook事件最后一次错误信息',
    `create_time`       timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Webhook事件表';

SET
FOREIGN_KEY_CHECKS = 1;
