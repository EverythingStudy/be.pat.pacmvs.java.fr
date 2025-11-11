-- 2025-11-10修改
-- tb_organ_tag四个算法状态初始化
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '02';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '07';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '0F';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '51';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 1, structured_analysis = 1 WHERE organ_tag_code = '5E';
-- 修改标签名称
UPDATE tb_organ_tag SET organ_en = 'Bone with bone marrow, femur and tibia',organ_name = '骨和骨髓，股骨与胫骨' WHERE organ_tag_code = '7E' AND species_id = '1';
UPDATE tb_organ SET name = '骨和骨髓，股骨与胫骨',name_en = 'Bone with bone marrow, femur and tibia' WHERE organ_code = '7E' AND species_id = '1';
-- 新增标签
INSERT INTO tb_organ_tag (species_id, organ_tag_code, organ_en, organ_name, abbreviation, rgb, chromatic_value, organization_id, create_by, update_by) VALUES ( "1",'50','Bone with bone marrow, femur','骨和骨髓，股骨','FE','rgb(145, 200, 200)','#91C8C8',1,1,1);
INSERT INTO tb_organ (organ_code, name, name_en, species_id, organization_id) VALUES ('50','骨和骨髓，股骨','Bone with bone marrow, femur',"1",1);
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '50';

-- 更新tb_structure_tag_set中已存在的记录
UPDATE tb_structure_tag_set a
JOIN tb_organ b
ON a.organ_code = b.organ_code
AND a.species_id = b.species_id
AND a.organization_id = b.organization_id
AND a.type = 0 AND b.organ_code = '7E'
SET a.structure_tag_set_name = b.name, a.structure_tag_set_name_en = b.name_en;
-- 插入tb_organ中不存在于tb_structure_tag_set的记录
INSERT INTO tb_structure_tag_set (structure_tag_set_name, structure_tag_set_name_en, species_id, organ_code, organization_id, create_by,update_by)
SELECT a.name, a.name_en, a.species_id, a.organ_code, a.organization_id, 1, 1
FROM tb_organ a
LEFT JOIN tb_structure_tag_set b
ON a.organ_code = b.organ_code
AND a.species_id = b.species_id
AND a.organization_id = b.organization_id
AND b.type = 0
WHERE b.structure_tag_set_id IS NULL;
-- 更新结构标签名称
UPDATE tb_structure_tag a
JOIN tb_structure_tag_set b
ON a.structure_tag_set_id = b.structure_tag_set_id AND a.type = 0 AND b.type = 0 AND b.organ_code = '7E'
JOIN tb_species c
ON b.species_id = c.species_id AND b.organization_id = c.organization_id
JOIN tb_structure d
ON a.structure_id = d.structure_id AND a.organization_id = d.organization_id AND b.organ_code = d.organ_code AND b.species_id = d.species_id
SET a.structure_tag_name = CONCAT(c.name, b.structure_tag_set_name, d.name);

--2025-11-05 新增sql
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (712, '列表', 'list', 7, '', NULL, 5, NULL, 1, 'F', 1, '0', 'system:user:list', '#', 5, '2025-11-03 14:05:32', 5, '2025-11-03 14:05:52', '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (713, '列表', 'list', 6, '', NULL, 6, NULL, 1, 'F', 1, '0', 'system:role:list', ' ', 5, '2025-11-03 14:06:50', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (714, '列表', 'list', 6, '', NULL, 8, NULL, 1, 'F', 1, '0', 'system:organization:list', ' ', 5, '2025-11-03 14:08:08', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (715, '列表', 'list', 1, '', NULL, 7, NULL, 1, 'F', 1, '0', 'system:log:list', ' ', 5, '2025-11-03 14:09:05', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (716, '列表', 'list', 8, '', NULL, 57, NULL, 1, 'F', 1, '0', 'section:slices:list', ' ', 5, '2025-11-03 14:10:13', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (717, '列表', 'list', 6, '', NULL, 32, NULL, 1, 'F', 1, '0', 'project:pathology:list', ' ', 5, '2025-11-03 14:11:40', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (718, '列表', 'list', 1, '', NULL, 703, NULL, 1, 'F', 1, '0', 'project:organLabel:list', ' ', 5, '2025-11-03 14:12:50', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (719, '列表', 'list', 15, '', NULL, 652, NULL, 1, 'F', 1, '0', 'readFilmCreate:list', ' ', 5, '2025-11-03 14:13:41', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (720, '列表', 'list', 6, '', NULL, 654, NULL, 1, 'F', 1, '0', 'readFilmCreate:sliceConfig:list', ' ', 5, '2025-11-03 14:15:55', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (721, '列表', 'list', 3, '', NULL, 663, NULL, 1, 'F', 1, '0', 'readFilmCreate:users:list', ' ', 5, '2025-11-03 14:16:53', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (722, '列表', 'list', 3, '', NULL, 709, NULL, 1, 'F', 1, '0', 'readFilmCreate:production:list', ' ', 5, '2025-11-03 14:17:44', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO  `sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`, `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`) VALUES (723, '列表', 'list', 3, '', NULL, 670, NULL, 1, 'F', 1, '0', 'topicRecycleBin:list', ' ', 5, '2025-11-03 14:21:02', NULL, NULL, '', NULL, '0', '0', '0', '0');


update sys_menu set status='1' where menu_id=59;
update sys_menu set status='1' where menu_id=61;
update sys_menu set status='1' where menu_id=62;
update sys_menu set status='1' where menu_id=656;
update sys_menu set status='1' where menu_id=684;
update sys_menu set status='1' where menu_id=685;
update sys_menu set status='1' where menu_id=694;
update sys_menu set status='1' where menu_id=667;
update sys_menu set status='1' where menu_id=668;
update sys_menu set status='1' where menu_id=706;
update sys_menu set status='1' where menu_id=669;
update sys_menu set menu_name='新增项目',menu_name_en='新增项目(en)' where menu_id=655;
