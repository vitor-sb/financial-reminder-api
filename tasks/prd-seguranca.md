# PRD: Correções de segurança — Financial Reminder API

> **Origem:** auditoria de segurança de 2026-10-20 (somente leitura de código; nada foi explorado em execução).
> **Natureza:** correção. Roda **antes** de `prd-refactor-solid-clean-arch.md` e de `prd-financial-reminder-v1.md`.
> **Stack:** Java 17, Spring Boot, Spring Security, JWT (auth0), Redis, PostgreSQL, Liquibase.

---

## 1. Introdução

A API tem autenticação JWT, BCrypt e verificação de posse (IDOR) nos use cases. A auditoria encontrou, porém, falhas no fluxo de contas e de login que permitem escalada de privilégio, reativação de contas alheias e bloqueio de contas por terceiros.

### Achados verificados no código

- `POST /users` é público e `CreateUserRequest` recebe `role` (`OWNER`, `ADMIN`, `MEMBER`, `USER`) que `CreateUserUseCaseImpl` repassa ao `User`. `/admin/**` exige `ADMIN_PERMISSION`, que `OWNER` e `ADMIN` possuem.
- `POST /users/me/enable` é público e recebe só o e-mail (`EnableUserRequest`); `ToggleSelfUserAccountUseCaseImpl` reativa a conta e todos os lembretes.
- `LoginAttemptService` usa só o e-mail como chave. `checkAndRecordRequestRate` grava o timestamp **antes** de validar a senha; o intervalo mínimo é 15 s e o bloqueio cresce a cada falha. Leitura e escrita no Redis não são atômicas.
- `AuthenticationService.loadUserByUsername` lança `NotFoundException` (404 "User not found.") para e-mail inexistente e outra exceção para conta desativada/excluída; senha errada devolve 401. Respostas distinguem os casos.
- `CreateUserRequest.password` só tem `@NotBlank`. `value`, `paymentValue` e `installmentsNumber` aceitam zero/negativo; `PayTransactionRequest` não tem `@Valid`.
- Swagger/OpenAPI sempre públicos (`PUBLIC_SWAGGER_ENDPOINTS`). `docker-compose.yaml` publica 5432 e 6379 em todas as interfaces. `k8s/postgres.yaml` e `k8s/redis.yaml` definem senhas como `value:` literal.
- Respostas de posse diferem (409/401), e `PayTransactionUseCaseImpl` checa status antes da posse.
- `SecurityFilter.recoverToken` usa `replace("Bearer ", "")`; o token não tem `jti`.

## 2. Objetivos

- Nenhum cliente anônimo consegue obter papel acima de `USER` nem reativar conta alheia.
- O login não permite travar a conta de terceiros nem descobrir se um e-mail existe.
- Entradas financeiras inválidas são rejeitadas na borda.
- Superfície de rede e documentação restritas fora de desenvolvimento.
- Matriz de autorização coberta por testes automatizados.

## 3. Regras para todas as stories

- [ ] `mvn test` verde, sem remover nem enfraquecer testes existentes
- [ ] Toda story que muda a API atualiza `src/main/resources/openapi.yaml`
- [ ] Cada correção vem com um teste que **falha** no código atual e passa depois (teste de regressão)
- [ ] Nenhum segredo, e-mail ou token em log; mensagens de erro de autenticação são genéricas
- [ ] Sem migration, exceto onde a story disser
- [ ] Segredos nunca entram em arquivos versionados (`.env` e `k8s` usam referência a Secret)

## 4. User Stories (em ordem de prioridade)

### Fase 1 — Crítico e alto

#### US-SEC01: Cadastro público não aceita `role`
**Description:** Como dono do sistema, quero que o cadastro público crie sempre um usuário comum, para que ninguém se torne administrador por conta própria.

**Acceptance Criteria:**
- [ ] `CreateUserRequest` deixa de ter o campo `role`; `CreateUserCommand` e `UserMapper` ajustados
- [ ] `CreateUserUseCaseImpl` cria o usuário com `RoleEnum.USER` fixo
- [ ] Corpo com `role` é ignorado (não altera o papel) ou rejeitado com 400 — escolher um e documentar no OpenAPI
- [ ] O usuário `OWNER` continua sendo criado apenas pelo bootstrap (`APPLICATION_OWNER_*`); nenhum caminho HTTP público cria `OWNER`/`ADMIN`
- [ ] Teste de integração: `POST /users` com `"role":"ADMIN"` cria usuário `USER` e `GET /admin/users/list` com o token dele responde 403

#### US-SEC02: Reativação de conta exige credenciais
**Description:** Como usuário, quero reativar minha conta provando que sou eu, para que terceiros não reativem contas alheias.

**Acceptance Criteria:**
- [ ] `EnableUserRequest` passa a exigir `email` e `password`; a senha é validada contra o hash antes de reativar
- [ ] Credencial inválida, e-mail inexistente e conta excluída devolvem a mesma resposta (401 genérico, mesmo corpo)
- [ ] Mesmo limite de tentativas do login (US-SEC03a/b) se aplica ao endpoint
- [ ] A reativação continua reativando os lembretes do usuário, como hoje
- [ ] Teste: e-mail correto com senha errada → 401 e conta continua desativada; e-mail inexistente → mesma resposta do caso anterior

#### US-SEC03a: Limite de login só conta falhas e é atômico
**Description:** Como usuário legítimo, quero que o limite de login considere apenas tentativas falhas, para que um terceiro não consiga me trancar do lado de fora.

**Acceptance Criteria:**
- [ ] Remover o throttle de 15 s gravado antes da autenticação (`checkAndRecordRequestRate`)
- [ ] Contador de falhas incrementado só quando a autenticação falha, com `INCR` + `EXPIRE` atômicos no Redis; sucesso zera o contador
- [ ] Bloqueio temporário (tempo crescente com teto, ex.: máx. 30 min) após `MAX_ATTEMPTS` falhas
- [ ] Login correto durante o bloqueio continua negado, mas o corpo/status é o mesmo do bloqueio por falha (429 com `Retry-After`)
- [ ] Teste unitário: 4 falhas + 1 sucesso zera o contador; teste de concorrência simples com 20 chamadas paralelas incrementa exatamente 20

#### US-SEC03b: Limite de login também por IP
**Description:** Como operador, quero limitar tentativas por origem, para que um único cliente não teste senhas de muitos e-mails nem trave contas em massa.

**Acceptance Criteria:**
- [ ] Segunda chave no Redis por IP do cliente (janela e teto configuráveis em `application.yaml`)
- [ ] IP obtido de `request.getRemoteAddr()`; uso de `X-Forwarded-For` só com `server.forward-headers-strategy` habilitado explicitamente por propriedade (desligado por padrão)
- [ ] Excedido o teto, responde 429 sem consultar o banco
- [ ] Teste: 1 IP com N tentativas em e-mails diferentes é limitado; outro IP não é afetado

#### US-SEC04: Respostas de login uniformes
**Description:** Como atacante hipotético, não devo conseguir descobrir quais e-mails existem nem o estado da conta pelas respostas de login.

**Acceptance Criteria:**
- [ ] `AuthenticationService.loadUserByUsername` lança `UsernameNotFoundException` para usuário inexistente, desativado ou excluído
- [ ] `AuthenticationException` (inclui `BadCredentialsException`) responde sempre 401 com a mesma mensagem ("Invalid email or password") — hoje `ApplicationExceptionHandler` devolve 403
- [ ] Senha errada, e-mail inexistente e conta desativada produzem status, corpo e (aproximadamente) tempo equivalentes; usar hash dummy para igualar o custo quando o usuário não existe
- [ ] Teste de integração compara os três casos (status e corpo idênticos)
- [ ] `openapi.yaml` atualizado para 401 em `/auth/login`

### Fase 2 — Média

#### US-SEC05: Política de senha
**Acceptance Criteria:**
- [ ] `CreateUserRequest.password` e a senha da reativação: `@Size(min = 10, max = 72)` e diferente do e-mail
- [ ] Mensagem de validação não ecoa a senha
- [ ] `openapi.yaml` documenta `minLength`/`maxLength`
- [ ] Teste: senha de 1 caractere e de 100 caracteres → 400

#### US-SEC06: Validar valores financeiros na borda
**Acceptance Criteria:**
- [ ] `CreatePaymentReminderRequest.value`, `PayTransactionRequest.paymentValue` e `UpdatePaymentTransactionRequest.paymentValue`: `@Positive` e `@Digits(integer = 12, fraction = 2)`
- [ ] `installmentsNumber`: `@Min(1)` e `@Max(600)`
- [ ] `PayTransactionRequest` recebido com `@Valid` no controller
- [ ] Teste: valor 0, negativo, com 3 casas decimais e parcelas 0/-1 → 400
- [ ] `openapi.yaml` com `minimum`/`maximum` correspondentes

#### US-SEC07: Swagger/OpenAPI só fora de produção
**Acceptance Criteria:**
- [ ] `springdoc.swagger-ui.enabled` e `api-docs.enabled` controlados por propriedade, desligados por padrão e ligados no perfil de desenvolvimento
- [ ] `SecurityConfiguration` libera `/swagger-ui/**` e `/v3/api-docs/**` apenas quando a propriedade estiver ligada; caso contrário exigem autenticação
- [ ] Teste: com a propriedade desligada, `GET /swagger-ui/index.html` anônimo → 401

#### US-SEC08: Rede e segredos de infraestrutura
**Acceptance Criteria:**
- [ ] `docker-compose.yaml`: portas 5432 e 6379 publicadas em `127.0.0.1` (ou removidas)
- [ ] `k8s/postgres.yaml` e `k8s/redis.yaml`: senhas lidas de `secretKeyRef` do Secret `financial-env`; nenhum valor de senha literal nos manifestos
- [ ] `k8s/deploy.sh` cria/atualiza o Secret a partir do `.env`, sem imprimir valores
- [ ] `grep -rn -E "PASSWORD|SECRET" k8s/` não mostra valor literal

### Fase 3 — Baixa

#### US-SEC09: Respostas de posse uniformes
**Acceptance Criteria:**
- [ ] Recurso de outro usuário responde 404 (mesmo texto de "não encontrado") em get, update, delete, enable/disable de reminder e em pay/update de transação
- [ ] `PayTransactionUseCaseImpl` verifica a posse **antes** de status/ativo
- [ ] Teste: usuário A acessando recurso do usuário B recebe a mesma resposta que um id inexistente
- [ ] `openapi.yaml` sem 409/401 de posse nessas rotas

#### US-SEC10: Endurecer o parse e o conteúdo do JWT
**Acceptance Criteria:**
- [ ] `SecurityFilter.recoverToken` exige o prefixo exato `Bearer ` e rejeita o restante (sem `replace`)
- [ ] Token passa a incluir `jti`, `iat` e `nbf`
- [ ] Teste: header `Authorization: <token>` sem prefixo → 401 em rota protegida
- [ ] Validação continua exigindo issuer e expiração

#### US-SEC11: Varredura de dependências
**Acceptance Criteria:**
- [ ] Profile Maven `security` com `org.owasp:dependency-check-maven`, `failBuildOnCVSS` = 7
- [ ] Comando documentado em `tasks/` ou no README: `mvn -Psecurity verify`
- [ ] Relatório inicial executado e achados High/Critical registrados em `scripts/ralph/progress.txt`

#### US-SEC12: Matriz de autorização automatizada
**Acceptance Criteria:**
- [ ] Teste parametrizado: para cada rota, anônimo → 401, `USER` → 200/204 nas rotas próprias e 403 em `/admin/**`, `ADMIN` → 200 em `/admin/**`
- [ ] Teste de acesso cruzado entre dois usuários para todas as rotas com id (reminders e transactions)
- [ ] Teste falha se uma rota nova for criada sem constar na matriz

## 5. Non-Goals

- Revogação de token (lista de bloqueio) e refresh token.
- Trocar `sub` do JWT de e-mail para id de usuário.
- Endpoint administrativo para criar `ADMIN` (hoje só o bootstrap cria `OWNER`).
- MFA, recuperação de senha por e-mail e confirmação de e-mail.
- Mensagem genérica no erro 500, remoção de e-mail dos logs e `traceId` (já em `prd-refactor-solid-clean-arch.md`, US-R01 a R03).

## 6. Considerações

- US-SEC01 e US-SEC02 são correções pequenas e podem ir sozinhas em um hotfix.
- US-SEC03a e US-SEC04 alteram o mesmo trecho de `LoginUseCaseImpl`/`ApplicationExceptionHandler`; fazer na ordem do PRD.
- Após esta branch, a US-R01 do refactor ainda vale para as demais exceções, mas o mapeamento de `AuthenticationException` → 401 já estará feito.
