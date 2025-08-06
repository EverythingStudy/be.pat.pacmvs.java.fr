DROP TABLE if EXISTS tb_species_wax_code_template;
CREATE TABLE `tb_species_wax_code_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `species_id` varchar(100) DEFAULT NULL COMMENT '种属ID',
  `wax_code` varchar(255)  DEFAULT NULL COMMENT '蜡块编号',
  `organ_name` varchar(255) DEFAULT NULL COMMENT '脏器名称',
  `organ_en` varchar(255) DEFAULT NULL COMMENT '英文名称',
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