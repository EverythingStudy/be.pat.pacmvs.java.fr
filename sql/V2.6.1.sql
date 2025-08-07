DROP TABLE if EXISTS tb_species_wax_code_template;
CREATE TABLE `tb_species_wax_code_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `species_id` varchar(100) DEFAULT NULL COMMENT '种属ID',
  `wax_code` varchar(255)  DEFAULT NULL COMMENT '蜡块编号',
  `organ_name` varchar(255) DEFAULT NULL COMMENT '脏器名称',
  `organ_en` varchar(255) DEFAULT NULL COMMENT '英文名称',
  `block_count` int NOT NULL DEFAULT '0' COMMENT '取材块数',
  `sex_flag` char(1)  DEFAULT 'N' COMMENT '性别（M；F；N）',
  `organ_code` varchar(255) DEFAULT NULL COMMENT '脏器编码',
  `abbreviation` varchar(255) DEFAULT NULL COMMENT '脏器缩写',
  `create_by` bigint DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_species_id` (`species_id`) USING BTREE
) COMMENT = '种属蜡块模板表';

DROP TABLE if EXISTS fr_production;
CREATE TABLE `fr_production` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `special_id` bigint NOT NULL DEFAULT '0' COMMENT '专题ID',
  `template_id` bigint NOT NULL DEFAULT '0' COMMENT '种属蜡块模板表ID',
  `species_id` varchar(100) DEFAULT NULL COMMENT '种属ID',
  `wax_code` varchar(255)  DEFAULT NULL COMMENT '蜡块编号',
  `organ_name` varchar(255) DEFAULT NULL COMMENT '脏器名称',
  `organ_en` varchar(255) DEFAULT NULL COMMENT '英文名称',
  `block_count` int NOT NULL DEFAULT '0' COMMENT '取材块数',
  `sex_flag` char(1)  DEFAULT 'N' COMMENT '性别（M；F；N）',
  `organ_code` varchar(255) DEFAULT NULL COMMENT '脏器编码',
  `abbreviation` varchar(255) DEFAULT NULL COMMENT '脏器缩写',
  `organization_id` bigint NOT NULL DEFAULT '0' COMMENT '机构ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_special_id` (`species_id`) USING BTREE,
  KEY `idx_species_id` (`species_id`) USING BTREE
) COMMENT = '专题制片信息';

ALTER TABLE `fr_slide`
ADD COLUMN `ai_status` int NOT NULL DEFAULT 0 COMMENT 'AI分析状态：0-未分析；1-脏器识别中；2-脏器识别异常' AFTER `viewers`;

-- 以下sql林亚提供
CREATE TABLE `fr_single_slide`
(
    `single_id`            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '单脏器切片id',
    `slide_id`             bigint                                                        NOT NULL COMMENT '切片id',
    `thumb_url`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单脏器图片缩略图地址',
    `category_id`          bigint                                                        NOT NULL COMMENT '单脏器类型',
    `forecast_status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci               DEFAULT '0' COMMENT '结构化状态 0未预测、1预测成功、2预测失败、3预测中',
    `diagnosis_status`     char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci               DEFAULT '0' COMMENT '人工诊断状态 0：未诊断；1：已诊断',
    `create_time`          datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `description`          varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '单切片描述',
    `abnormal_status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci               DEFAULT '0' COMMENT '异常状态 0：默认值 ；1：未见异常',
    `abnormal_create_by`   bigint                                                                 DEFAULT NULL COMMENT '未见异常创建人',
    `abnormal_create_time` datetime                                                               DEFAULT NULL COMMENT '未见异常创建时间',
    `area`                 varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '精细轮廓总面积',
    `ai_status_fine`       int                                                                    DEFAULT '0' COMMENT '精轮廓状态：0未预测、1预测成功、2预测失败、3预测中',
    `fine_contour_time`    bigint                                                                 DEFAULT NULL COMMENT '精轮廓总时间',
    `structure_time`       bigint                                                                 DEFAULT NULL COMMENT '结构化总时间',
    `start_time`           datetime                                                               DEFAULT NULL COMMENT 'ai算法开始时间',
    `perimeter`            varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL,
    `task_id`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '任务id',
    PRIMARY KEY (`single_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=756 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='单脏器切片表';

CREATE TABLE `fr_json_file`
(
    `file_id`        bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `task_id`        bigint NOT NULL COMMENT '任务ID',
    `file_url`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件路径',
    `structure_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结构名称',
    `status`         int                                                           DEFAULT NULL COMMENT '状态(0未进行解析、1解析中、2解析成功、3解析失败)',
    `times`          int                                                           DEFAULT NULL COMMENT '执行次数（第几次）',
    `start_time`     datetime                                                      DEFAULT NULL COMMENT '开始时间',
    `end_time`       datetime                                                      DEFAULT NULL COMMENT '结束时间',
    `create_time`    datetime                                                      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`file_id`, `task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ai预测json文件表';

CREATE TABLE `fr_json_task`
(
    `task_id`         bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `slide_id`        bigint                                                        DEFAULT NULL COMMENT '切片ID',
    `special_id`      int                                                           DEFAULT NULL COMMENT '专题ID',
    `image_id`        int                                                           DEFAULT NULL COMMENT '图像ID',
    `single_id`       bigint                                                        DEFAULT NULL COMMENT '单脏器切片id',
    `organization_id` bigint                                                        DEFAULT '0' COMMENT '机构ID',
    `category_id`     bigint                                                        DEFAULT NULL COMMENT '脏器标签ID',
    `algorithm_code`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '算法ID',
    `code`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '算法返回状态',
    `msg`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '算法返回msg',
    `data`            text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '算法返回数据',
    `status`          int                                                           DEFAULT NULL COMMENT '0未进行解析、1解析中、2解析成功、3解析失败',
    `times`           int                                                           DEFAULT NULL COMMENT '执行次数（第几次）',
    `start_time`      datetime                                                      DEFAULT NULL COMMENT '开始时间',
    `end_time`        datetime                                                      DEFAULT NULL COMMENT '结束时间',
    `create_time`     datetime                                                      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='json解析任务表';

CREATE TABLE `fr_ai_forecast`
(
    `forecast_id`                int NOT NULL AUTO_INCREMENT,
    `quantitative_indicators`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定量指标',
    `quantitative_indicators_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定量指标英文',
    `results`                    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '预测结果',
    `forecast_range`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '范围',
    `create_by`                  bigint                                                        DEFAULT NULL COMMENT '创建者',
    `create_time`                datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `single_slide_id`            bigint                                                        DEFAULT NULL COMMENT '单脏器切片id',
    `unit`                       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
    `struct_type`                char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT ' 结构指标类别0：产品呈现指标1：算法输出指标',
    PRIMARY KEY (`forecast_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4536 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='量化指标表';


ALTER TABLE `fr_json_file`
    ADD COLUMN `structure_id` varchar(16) NULL COMMENT '结构Id' AFTER `file_url`,
    ADD COLUMN `ai_status` int NULL COMMENT 'AI识别状态 0成功 1失败' AFTER `structure_name`;

CREATE TABLE `fr_contour_json`
(
    `contour_json_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `slide_id`        bigint                                                        DEFAULT NULL COMMENT '切片id',
    `tile_name`       varchar(255)                                                  DEFAULT NULL COMMENT '瓦片名称',
    `structure_size`  int                                                           DEFAULT NULL COMMENT '结构大小',
    `create_by`       bigint                                                        DEFAULT NULL COMMENT '创建者',
    `create_time`     datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `single_slide_id` bigint                                                        DEFAULT NULL COMMENT '单切片id',
    `middle`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '中结构json文件',
    `small`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '小结构json文件',
    `middle_small`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `big`             varchar(255)                                                  DEFAULT NULL COMMENT '大结构json文件',
    PRIMARY KEY (`contour_json_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1024859 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;