INSERT INTO `pathmedics`.`sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`,
                                     `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`,
                                     `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`,
                                     `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`)
VALUES (696, '阅片列表', '阅片列表(en)', 1, 'matrix', 'topic-readFilm/archived-matrix/index', 695, NULL, 0, 'C', 1, '0',
        'matrix', '#', 1, '2025-03-19 15:47:19', 1, '2025-03-19 15:47:56', '', NULL, '0', '0', '0', '0');
INSERT INTO `pathmedics`.`sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`,
                                     `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`,
                                     `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`,
                                     `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`)
VALUES (695, '已归档项目', '已归档项目(en)', 4, 'archivedTopic', 'topic-readFilm/archived-topic/index', 651, NULL, 0,
        'C', 1, '0', 'archivedTopic', '#', 5, '2025-03-19 15:02:22', 5, '2025-03-19 15:03:44', '', NULL, '0', '0', '0',
        '0');
INSERT INTO `pathmedics`.`sys_menu` (`menu_id`, `menu_name`, `menu_name_en`, `order_num`, `path`, `component`,
                                     `parent_id`, `query`, `is_cache`, `menu_type`, `is_frame`, `status`, `perms`,
                                     `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`,
                                     `is_functional_modules`, `visible`, `full_width`, `no_fit`, `no_header`)
VALUES (694, '归档', '归档(en)', 11, '', NULL, 652, NULL, 1, 'F', 1, '0', 'readFilmCreate:list:archived', ' ', 1,
        '2025-03-20 16:02:04', NULL, NULL, '', NULL, '0', '0', '0', '0');
INSERT INTO `pathmedics`.`sys_role` (`role_id`, `role_name`, `role_name_en`, `role_key`, `role_sort`, `role_level`,
                                     `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
                                     `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (17, '档案管理员', NULL, 'archived', 'R25', 0, '1', 1, 1, '0', '0', 1, '2025-03-19 15:05:27', 5,
        '2025-03-26 17:20:37', NULL);

INSERT INTO `pathmedics`.`sys_role_menu` (`role_id`, `menu_id`)
VALUES (17, 695);
INSERT INTO `pathmedics`.`sys_role_menu` (`role_id`, `menu_id`)
VALUES (17, 696);

INSERT INTO `pathmedics`.`sys_role_menu` (`role_id`, `menu_id`)
VALUES (17, 651);

