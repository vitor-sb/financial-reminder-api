package com.project.manager.finance.management.infrastructure.persistence;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SpecificationFactory {

    /**
     * Cria uma Specification para uma cláusula LIKE case-insensitive.
     * Retorna uma Specification vazia se o valor for nulo ou vazio.
     * @param attribute O nome do atributo na entidade.
     * @param value O valor a ser comparado.
     * @return Specification<T>
     */
    public static <T> Specification<T> like(String attribute, String value) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(value)) {
                return builder.conjunction(); // Retorna uma cláusula vazia (sempre true)
            }
            return builder.like(builder.lower(root.get(attribute)), "%" + value.toLowerCase() + "%");
        };
    }

    /**
     * Cria uma Specification para uma cláusula EQUAL.
     * Retorna uma Specification vazia se o valor for nulo.
     * @param attribute O nome do atributo na entidade.
     * @param value O valor a ser comparado.
     * @return Specification<T>
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
     * @param joinAttribute O nome do atributo para o Join.
     * @param attribute O nome do atributo na entidade associada.
     * @param value O valor a ser comparado.
     * @return Specification<T>
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
}