-- CREDENCIAIS DE SEGURANÇA
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('admin', 'admin', 1, 0, 0);
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('sindico', '1234', 1, 0, 0);
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('portaria', '1234', 1, 0, 0);
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES('morador', '1234', 1, 0, 0);

-- PERFIS (PAPÉIS) DE SEGURANÇA
INSERT INTO security_profile(name) VALUES('ADMIN'), ('MANAGER'), ('CONCIERGE'), ('DWELLER');

-- PRIVILÉGIOS (PERMISSÕES) DE SEGURANÇA
INSERT INTO security_privilege(name) VALUES('MANAGE_RESIDENCE_GROUPS'), ('MANAGE_RESIDENCES'), ('MANAGE_VISITS'), ('MANAGE_WHITE_LIST');

-- RELAÇÕES DE PERFIS E PRIVILÉGIOS;
INSERT INTO security_profile_privileges(fk_security_profile, fk_security_privilege)
VALUES(2,1), (2,2), -- MANAGER
(3,3), -- CONCIERGE
(4,4); -- DWELLER

-- RELAÇÃO DE USUÁRIOS E PERFIS
INSERT INTO security_credentials_profiles(fk_security_credentials, fk_security_profile)
VALUES(1,1), -- admin, PERFIL 'ADMIN'
(2,2), (2,4), -- sindico, PERFIL 'MANAGER' E 'DWELLER'
(3,3), -- portaria, PERFIL 'CONCIERGE'
(4,4); -- morador, PERFIL 'DWELLER'