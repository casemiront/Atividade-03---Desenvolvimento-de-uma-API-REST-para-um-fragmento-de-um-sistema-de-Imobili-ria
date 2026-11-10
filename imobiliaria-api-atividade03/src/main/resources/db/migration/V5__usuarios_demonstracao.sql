-- Contas fictícias para demonstração local. Somente hashes BCrypt são persistidos.
INSERT INTO usuarios(login,nome,senha_hash,ativo,id_cliente) VALUES ('admin','Administrador de demonstração','$2a$10$eKXk2dQT7pM2rM5gULilEOXNPWazSSXla1HXAIunv2a4ZR4DS7mPu',TRUE,NULL);
INSERT INTO usuario_perfis(id_usuario,id_perfil) SELECT u.id,p.id FROM usuarios u,perfis p WHERE u.login='admin' AND p.nome='ADMIN';
INSERT INTO usuarios(login,nome,senha_hash,ativo,id_cliente) VALUES ('atendente','Atendente de demonstração','$2a$10$D5vk9s6vuWb4KIbpkVsM7.CwnqfCAuz1B70vnpvH1SL5gPLH/I4H2',TRUE,NULL);
INSERT INTO usuario_perfis(id_usuario,id_perfil) SELECT u.id,p.id FROM usuarios u,perfis p WHERE u.login='atendente' AND p.nome='ATENDENTE';
INSERT INTO usuarios(login,nome,senha_hash,ativo,id_cliente) VALUES ('ana','Ana Exemplo','$2a$10$eHCt0k7lEKRO5vw0LioajOrMe6MJBuMeB.ZB6UCNZ4O5SR8M51AgS',TRUE,(SELECT id FROM clientes WHERE cpf='11144477735'));
INSERT INTO usuario_perfis(id_usuario,id_perfil) SELECT u.id,p.id FROM usuarios u,perfis p WHERE u.login='ana' AND p.nome='CLIENTE';
INSERT INTO usuarios(login,nome,senha_hash,ativo,id_cliente) VALUES ('bruno','Bruno Exemplo','$2a$10$FrpF9zbmHVPZVJL862dmvOoKZLz5T4jap0yWy5mZbdsBDnIIJCFd2',TRUE,(SELECT id FROM clientes WHERE cpf='12345678909'));
INSERT INTO usuario_perfis(id_usuario,id_perfil) SELECT u.id,p.id FROM usuarios u,perfis p WHERE u.login='bruno' AND p.nome='CLIENTE';
