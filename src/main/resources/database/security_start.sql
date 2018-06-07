-- SELECT * FROM security_credentials;
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('admin', 'admin', 1, 0, 0);
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('manager', 'manager', 1, 0, 0);
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('employee', 'employee', 1, 0, 0);

-- SELECT * FROM security_authentication;

-- SELECT * FROM security_profile;
INSERT INTO security_profile(name) VALUES('ADMIN'), ('MANAGER'), ('COMMISION'), ('EMPLOYEE'), ('RESIDENT');

-- SELECT * FROM security_privilege;
INSERT INTO security_privilege(name) VALUES('MANAGE_BUILDINGS'), ('MANAGE_RESIDENCES'), ('MANAGE_EMPLOYEES'), ('MANAGE_USERS');
INSERT INTO security_privilege(name) VALUES('PAINT_BUILDINGS');

-- SELECT * FROM security_profile_privileges;
INSERT INTO security_profile_privileges(fk_security_profile, fk_security_privilege)
VALUES(1,1), (1,2), (1,3), (1,4),
(2,3), (2,4),
(3,3);
INSERT INTO security_profile_privileges(fk_security_profile, fk_security_privilege) VALUES(4,5);

-- SELECT * FROM security_credentials_profiles;
INSERT INTO security_credentials_profiles(fk_security_credentials, fk_security_profile) VALUES(1,1), (2,2), (3,4);