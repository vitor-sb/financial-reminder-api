# PRD: Adapters in/out e porta para Redis — Financial Reminder API

> **Origem:** observação de que `adapters` não segue o padrão `in`/`out` do validation-flow-engine (2026-10-20). Continua `prd-refactor-solid-clean-arch.md` (R01–R14, concluído).
> **Natureza:** refatoração. Nenhuma funcionalidade nova, nenhum contrato HTTP, schema ou `openapi.yaml` alterado.

## Estado atual (verificado no código)

- `adapters/` tem só entrada HTTP: `controllers`, `dtos`, `exception`, `mappers`.
- A saída está em `infrastructure/`: `persistence` (implementações de busca por Specification), `security` (`TokenProvider`, `BcryptPasswordHasher`, `SpringCredentialsAuthenticator`, `SecurityFilter`, `LoginIpRateLimitFilter`, `SecurityConfiguration`, `AuditorAwareConfig`), `filters` (`HttpTracingFilter`), `config`, `utils`.
- `domains/services/LoginAttemptService` usa `StringRedisTemplate` e script Lua direto: o domínio depende de Spring Data Redis.

## Estrutura alvo

```
adapters/in/web/{controllers,dtos,mappers,exception,filter}
adapters/out/persistence     (buscas por Specification, SpecificationFactory, PageConversion)
adapters/out/security        (TokenProvider, BcryptPasswordHasher, SpringCredentialsAuthenticator)
adapters/out/cache           (RedisLoginAttemptStore)
infrastructure/config        (só configuração de beans: Jpa, Redis, Security, AuditorAware)
```

## Regras para todas as stories

- [ ] `mvn test` verde, sem remover nem enfraquecer testes existentes
- [ ] Não enfraquecer nenhuma proteção SEC01–SEC12 nem os testes em `src/test/.../integration`
- [ ] `openapi.yaml` sem diferença; sem migration nova
- [ ] Movimentação de pacotes sem mudança de comportamento (apenas `package`/`import`, e `git mv` para preservar histórico)

## User Stories

#### US-A01: Entrada HTTP em `adapters/in/web`
**Description:** Como mantenedor, quero tudo que é entrada HTTP sob `adapters/in/web`.

**Acceptance Criteria:**
- [ ] `adapters/controllers|dtos|mappers|exception` movidos para `adapters/in/web/...`
- [ ] `HttpTracingFilter`, `SecurityFilter` e `LoginIpRateLimitFilter` movidos para `adapters/in/web/filter`
- [ ] Pacotes de testes espelham os de produção
- [ ] Nenhum pacote antigo vazio sobra

#### US-A02: Saída em `adapters/out`
**Description:** Como mantenedor, quero implementações de portas sob `adapters/out`.

**Acceptance Criteria:**
- [ ] `infrastructure/persistence` e `infrastructure/utils/SpecificationFactory` movidos para `adapters/out/persistence`
- [ ] `TokenProvider`, `BcryptPasswordHasher` e `SpringCredentialsAuthenticator` movidos para `adapters/out/security`
- [ ] `infrastructure` fica só com configuração (`JpaConfiguration`, `RedisConfiguration`, `SecurityConfiguration`, `AuditorAwareConfig`) e constantes
- [ ] `domains/repositories/jpa` (Spring Data) permanece onde está; fora de escopo

#### US-A03: Porta `LoginAttemptStore` e adaptador Redis
**Description:** Como mantenedor, quero o domínio sem Spring Data Redis.

**Acceptance Criteria:**
- [ ] Interface `LoginAttemptStore` em `application` com operações de contador com janela (incrementar com TTL, ler, definir bloqueio com TTL, consultar bloqueio, apagar), sem tipos do Redis
- [ ] `RedisLoginAttemptStore` em `adapters/out/cache` com `StringRedisTemplate`, script Lua `INCR + EXPIRE` atômico, chaves `failKey/blockKey/ipKey`
- [ ] Regras (limites, janela, duração do bloqueio, decisão de bloquear) ficam fora do adapter, em `LoginAttemptService`, sem import de `org.springframework.data.redis`
- [ ] Fake em memória da porta para testes; `LoginIpLimitTest`, `LoginUniformResponseTest` e `LoginAttemptServiceTest` passam sem `@MockBean` de Redis e sem enfraquecer asserções
- [ ] Teste do adaptador cobrindo atomicidade de incremento com TTL (pode usar mock do template)

#### US-A04: ArchUnit para o novo desenho
**Description:** Como mantenedor, quero travar o novo desenho no build.

**Acceptance Criteria:**
- [ ] `adapters.in` e `adapters.out` não dependem um do outro
- [ ] `adapters` dependem só de `application`, `domains` e bibliotecas; `application` e `domains` não dependem de `adapters` nem `infrastructure`
- [ ] `domains` e `application` sem import de `org.springframework.data.redis`
- [ ] Regras antigas ajustadas, nunca removidas sem equivalente

## Non-Goals

- Separar entidade JPA de modelo de domínio.
- Mover `domains/repositories/jpa`.
- Mudar regras de limite de login.

## Ordem

A01 → A02 → A03 → A04.
