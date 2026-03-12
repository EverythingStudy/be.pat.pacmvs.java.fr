ALTER TABLE fr_production ADD COLUMN `wax_code_id` bigint NULL COMMENT '种属蜡块模板表ID' AFTER `special_id`;
ALTER TABLE fr_production
DROP INDEX `idx_special_id`,
ADD INDEX `idx_special_id`(`special_id` ASC) USING BTREE;

ALTER TABLE `pathmedics`.`tb_species_wax_code_template`
    ADD COLUMN `algorithm_method` varchar(255) NULL COMMENT '对应算法接口脏器编码：只记录不同的' AFTER `abbreviation`;