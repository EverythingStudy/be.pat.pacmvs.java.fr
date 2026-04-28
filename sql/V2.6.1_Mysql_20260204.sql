DELETE FROM tb_organ_tag WHERE `organ_tag_code` = '7E' AND species_id = '1';
DELETE FROM tb_organ WHERE `organ_code` = '7E' AND species_id = '1';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '5D' AND species_id = '1';
UPDATE tb_organ_tag SET organ_recognition = 1, fine_contour = 1, screening_difference = 0, structured_analysis = 1 WHERE organ_tag_code = '7B' AND species_id = '1';