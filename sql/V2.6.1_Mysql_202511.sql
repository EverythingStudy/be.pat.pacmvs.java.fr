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

