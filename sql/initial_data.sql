
-- Not used for now

INSERT INTO `role` VALUES
    (1,'GUEST'),
    (2,'LOCAL_ADMIN'),
    (3,'SUPER_ADMIN');

INSERT INTO `role$permissions` VALUES
    (1,0,'USER_READ_SAME_DOMAIN'),
    (2,0,'USER_READ_SAME_DOMAIN'),
    (2,1,'USER_EDIT_SAME_DOMAIN'),
    (2,2,'USER_CHANGE_ROLE'),
    (3,0,'USER_EDIT_ALL'),
    (3,1,'USER_READ_ALL'),
    (3,2,'USER_CHANGE_ROLE'),
    (3,3,'USER_READ_SAME_DOMAIN'),
    (3,4,'USER_EDIT_SAME_DOMAIN');
