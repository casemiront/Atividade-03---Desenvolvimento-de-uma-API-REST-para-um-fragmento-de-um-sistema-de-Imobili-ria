# API Imobiliária — Atividade 3

Continuação da API de clientes, imóveis, locações e aluguéis. Java 21, Spring Boot 3.5.16, Spring Data JPA, Bean Validation, Spring Security, H2 em arquivo e Flyway. HTTP Basic, DTOs de entrada/saída e autorização por perfil e titularidade da locação.

## Executar

Abra o `pom.xml` como projeto Maven e execute `ImobiliariaApplication` com JDK 21. Alternativamente, na pasta do projeto:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar .\target\imobiliaria-api-3.0.0.jar
```

Endereço: `http://localhost:8080`. O Maven precisa de Internet no primeiro uso. Banco: `dados/imobiliaria-atividade03.mv.db`, relativo ao diretório de execução; use a raiz do projeto. A aplicação escuta somente em loopback por padrão.

## Autenticação de demonstração

No Postman, selecione **Authorization > Basic Auth**. Não há endpoint de login nem token JWT: envie usuário e senha em cada requisição.

| Login | Senha de demonstração | Perfil | Cliente vinculado |
|---|---|---|---|
| admin | Admin@12345 | ADMIN | Nenhum |
| atendente | Atendente@12345 | ATENDENTE | Nenhum |
| ana | Cliente@12345 | CLIENTE | Ana Exemplo |
| bruno | Cliente@12345 | CLIENTE | Bruno Exemplo |

Essas contas são exclusivamente didáticas. A migration contém hashes BCrypt, não senhas em texto. Antes de uso real, substitua as credenciais e configure HTTPS: Base64 do HTTP Basic não é criptografia.

## Permissões adotadas

| Operação | ADMIN | ATENDENTE | CLIENTE |
|---|---|---|---|
| Consultar imóveis, paginação e pesquisa | Sim | Sim | Sim |
| Criar/alterar imóveis | Sim | Sim | Não |
| Consultar/criar/alterar clientes | Sim | Sim | Não |
| Consultar/criar/alterar locações | Todas | Todas | Somente próprias |
| Consultar aluguéis | Todos | Todos | Das próprias locações |
| Criar/alterar aluguéis | Sim | Sim | Não |
| Excluir clientes, imóveis, locações e aluguéis | Sim, respeitando vínculos | Não | Não |
| Gerenciar usuários, senhas e perfis | Sim | Não | Não |
| Consultar a própria identidade (`/api/usuarios/me`) | Sim | Sim | Sim |

A consulta de todas as locações pelo ATENDENTE é uma decisão de atendimento operacional. O CLIENTE não pode criar em nome de outro, transferir titularidade nem acessar outra locação por ID. A lista é filtrada no repositório. A checagem de titularidade está no serviço, além das anotações de perfil nos controllers. Aluguéis seguem a titularidade da locação.

Um usuário pode ter vários perfis; as permissões se somam. ADMIN ou ATENDENTE mantêm sua abrangência operacional quando combinados com CLIENTE. Perfis adicionais não recebem permissões de negócio automaticamente. Não há cadastro público de usuários; somente ADMIN atribui perfis. Uma conta com CLIENTE exige `idCliente` existente. Mais de uma conta pode apontar para o mesmo cadastro de cliente, por decisão administrativa.

## Recursos e DTOs

CRUD em `/api/clientes`, `/api/imoveis`, `/api/locacoes`, `/api/alugueis`, `/api/usuarios` e `/api/perfis`; consultas e alterações individuais usam `/{id}`. Troca de senha administrativa: `PUT /api/usuarios/{id}/senha` com `{"senha":"NovaSenha@12345"}`.

- Paginação: `/api/imoveis/paginados?page=0&size=5`.
- Query by Example: `/api/imoveis/pesquisa?tipo=apar&cidade=luí&quartos=2`.
- Ordenação: `ordenar=id|tipoImovel|cidade|dormitorios|valorDiaria`, `direcao=asc|desc`.
- Tipo/cidade: correspondência parcial, ignorando maiúsculas/minúsculas; acentos são significativos. Quartos corresponde a `dormitorios`. Os filtros se combinam por E.
- Controllers recebem Request DTOs e devolvem Response DTOs, inclusive nas páginas. Mapeamento manual.
- `ClienteResponse` omite CPF e nascimento; `UsuarioResponse` nunca expõe senha/hash. IDs de relacionamentos necessários permanecem nos DTOs.
- Criações não aceitam ID gerado pelo banco; locações não aceitam total enviado pelo consumidor. Campos desconhecidos retornam 400.
- Senhas: 8–64 caracteres e no máximo 72 bytes UTF-8. BCrypt com custo 10. Recebidas apenas na criação/troca.

## Regras preservadas

`ativo=true` confirma a locação; `false` cancela/inativa. Apenas confirmadas ocupam o imóvel. Intervalo `[dataInicio, dataFim)`: saída posterior à entrada, podendo coincidir com a próxima entrada. Total = diária × quantidade de diárias, calculado com BigDecimal em `valorAluguel`. `valorAluguelSug` é legado e não entra no cálculo.

A diária contratada é preservada ao editar observações/cancelar; mudar imóvel/período usa a diária atual. Bloqueio pessimista do imóvel serializa a verificação de disponibilidade e a gravação. Sobreposição retorna 409. Imóveis com locações confirmadas ativas/futuras não podem ser excluídos; histórico vinculado também impede exclusão física. Remova aluguéis antes de locações. Não há cascata. Cliente ligado a usuário também não pode ser excluído.

Aluguéis não aceitam valor negativo nem repetição de locação + vencimento. ADMIN não pode excluir/desativar a própria conta ou remover seu próprio perfil ADMIN. Perfis obrigatórios não podem ser renomeados/excluídos; perfis vinculados não podem ser excluídos.

## Segurança e migrations

`SecurityConfiguration` habilita HTTP Basic, sessões stateless e `@EnableMethodSecurity`. `CustomUserDetailsService` carrega credenciais/perfis persistidos. `@PreAuthorize` define permissões; `ControleAcesso` confere o cliente associado ao principal autenticado. Credenciais inválidas/ausentes: 401; falta de permissão: 403; entrada inválida: 400; ausência: 404; conflito: 409.

CSRF está desabilitado para esta API JSON utilizada pelo Postman, sem formulário nem autenticação por cookie. Revise essa decisão se adicionar navegador com credenciais automáticas. O exemplo não inclui TLS, rate limiting ou recuperação de senha.

Migrations em `src/main/resources/db/migration`: V1–V3 preservadas; V4 cria usuários, perfis e associação muitos-para-muitos; V5 insere quatro usuários fictícios com BCrypt. Hibernate usa `ddl-auto=validate`; Flyway cria/evolui o esquema. A etapa 3 usa um arquivo de banco próprio.

## Referência

Segurança baseada no [exemplo da disciplina](https://github.com/jcpinheiro/frete-api-2025/tree/main/frete_v12_seguranca_methodsecurity), adaptado para perfis relacionais, DTOs, titularidade e cadastro de usuários restrito ao ADMIN. O H2 foi mantido das etapas anteriores; Postman é o cliente HTTP, não o banco.
