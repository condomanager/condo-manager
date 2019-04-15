-- CREDENCIAIS DE SEGURANÇA
INSERT INTO security_credentials(username, password, enabled, expired, locked) VALUES
('admin', 'admin', 1, 0, 0), -- ID 1
('sindico', '1234', 1, 0, 0), -- ID 2
('portaria', '1234', 1, 0, 0), -- ID 3
('morador', '1234', 1, 0, 0); -- ID 4

-- PERFIS DE USUÁRIO
INSERT INTO profile(id, name, email) VALUES
(1, 'Administrador', 'admin@admin.com'),
(2, 'Síndico do condomínio', 'sindico@condominio.com'),
(3, 'Portaria do condomínio', 'portaria@condominio.com'),
(4, 'Morador do condomínio', 'morador@condominio.com');

-- PERFIS (PAPÉIS) DE SEGURANÇA
INSERT INTO security_profile(name) VALUES('ADMIN'), ('MANAGER'), ('CONCIERGE'), ('DWELLER');

-- PRIVILÉGIOS (PERMISSÕES) DE SEGURANÇA
INSERT INTO security_privilege(name) VALUES('MANAGE_RESIDENCE_GROUPS'), ('MANAGE_RESIDENCES'), ('MANAGE_VISITS'), ('MANAGE_WHITE_LIST');

-- RELAÇÕES DE PERFIS E PRIVILÉGIOS;
INSERT INTO security_profile_privileges(security_profile_id, security_privilege_id) VALUES
(2,1), (2,2), -- MANAGER
(3,3), -- CONCIERGE
(4,4); -- DWELLER

-- RELAÇÃO DE USUÁRIOS E PERFIS
INSERT INTO security_credentials_profiles(security_credentials_id, security_profile_id) VALUES
(1,1), -- admin, PERFIL 'ADMIN'
(2,2), (2,4), -- sindico, PERFIL 'MANAGER' E 'DWELLER'
(3,3), -- portaria, PERFIL 'CONCIERGE'
(4,4); -- morador, PERFIL 'DWELLER'