package com.reminder.financial.api.infrastructure.utils;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;


public final class SpecificationFactory {

    /**
     * Cria uma Specification para uma cláusula LIKE case-insensitive.
     * Retorna uma Specification vazia se o valor for nulo ou vazio.
     */
    public static <T> Specification<T> like(String attribute, String value) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(value)) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get(attribute)), "%" + value.toLowerCase() + "%");
        };
    }

    /**
     * Cria uma Specification para uma cláusula EQUAL.
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> equal(String attribute, Object value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get(attribute), value);
        };
    }

    /**
     * Cria uma Specification para uma cláusula EQUAL em uma entidade associada (Join).
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> equalInJoin(String joinAttribute, String attribute, Object value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            // Evita joins duplicados em consultas complexas
            Join<T, ?> join = root.getJoins().stream()
                    .filter(j -> j.getAttribute().getName().equals(joinAttribute))
                    .findFirst()
                    .orElseGet(() -> root.join(joinAttribute));

            return builder.equal(join.get(attribute), value);
        };
    }

    /**
     * Cria uma Specification para uma cláusula "maior ou igual que" (>=) para um campo do tipo LocalDate.
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> isGreaterThanOrEqualTo(String attribute, LocalDate value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return builder.greaterThanOrEqualTo(root.get(attribute), value);
        };
    }

    /**
     * Cria uma Specification para uma cláusula "menor ou igual que" (<=) para um campo do tipo LocalDate.
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> isLessThanOrEqualTo(String attribute, LocalDate value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return builder.lessThanOrEqualTo(root.get(attribute), value);
        };
    }

    /**
     * [SOBRECARGA] Cria uma Specification para uma cláusula "maior ou igual que" (>=) para um campo do tipo Instant.
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> isGreaterThanOrEqualTo(String attribute, Instant value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return builder.greaterThanOrEqualTo(root.get(attribute), value);
        };
    }

    /**
     * Cria uma Specification para uma cláusula "menor que" (<) para um campo do tipo Instant.
     * Útil para criar intervalos de data/hora "meio-abertos".
     * Retorna uma Specification vazia se o valor for nulo.
     */
    public static <T> Specification<T> isLessThan(String attribute, Instant value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return builder.lessThan(root.get(attribute), value);
        };
    }
}