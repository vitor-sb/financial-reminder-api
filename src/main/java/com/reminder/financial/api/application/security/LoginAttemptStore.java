package com.reminder.financial.api.application.security;

import java.time.Duration;

/** Contadores de falha de login com janela (TTL). Implementações devem incrementar e definir o TTL atomicamente. */
public interface LoginAttemptStore {

    /** Incrementa o contador de falhas do e-mail e renova o TTL; retorna o novo valor. */
    long incrementFailures(String email, Duration window);

    long failures(String email);

    void block(String email, long untilEpochMillis, Duration ttl);

    /** Instante (epoch ms) até o qual o e-mail está bloqueado; 0 se não há bloqueio. */
    long blockUntil(String email);

    /** Apaga contador e bloqueio do e-mail. */
    void clear(String email);

    long incrementIpFailures(String ip, Duration window);

    long ipFailures(String ip);

    /** TTL restante do contador do IP em segundos; valor &lt; 1 se inexistente. */
    long ipTtlSeconds(String ip);
}
