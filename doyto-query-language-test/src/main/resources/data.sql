
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid) VALUES ('f0rb', '17778888881', 'f0rb@163.com', '测试1', '123456', '高级', true);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid) VALUES ('user2', '17778888882', 'test2@qq.com', '测试2', '123456', '普通', true);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, memo, valid) VALUES ('user3', '17778888883', 'test3@qq.com', '测试3', '123456', '普通', 'memo', true);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid) VALUES ('user4', '17778888884', 'test4@qq.com', '测试4', '123456', '普通', true);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid) VALUES ('user5', '17778888885', 'test5@qq.com', '测试5', '123456', '普通', true);

INSERT INTO t_menu_01 (id, platform, parent_id, menu_name, memo, valid) VALUES (1, '01', 0, 'root', 'root menu', true);
INSERT INTO t_menu_01 (id, platform, parent_id, menu_name, memo, valid) VALUES (2, '01', 1, 'first', 'first menu', true);
INSERT INTO t_menu (id, platform, parent_id, menu_name, memo, valid) VALUES (1, '02', 0, 'root', 'root menu', true);

INSERT INTO t_role (role_name, role_code) VALUES ('admin', 'ADMIN');
INSERT INTO t_role (role_name, role_code) VALUES ('vip', 'VIP');
INSERT INTO t_role (role_name, role_code, valid) VALUES ('vip2', 'VIP2', false);
INSERT INTO t_role (role_name, role_code) VALUES ('vip3', 'VIP3');

INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 2);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (3, 3);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (4, 2);

INSERT INTO t_perm (perm_name, valid) VALUES ('user:get', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:list', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:update', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:delete', true);

INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 2);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 3);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 4);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 2);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (3, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (4, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (5, 1);