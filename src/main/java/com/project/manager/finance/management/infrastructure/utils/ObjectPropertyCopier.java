package com.project.manager.finance.management.infrastructure.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public final class ObjectPropertyCopier {

    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null)
            throw new IllegalArgumentException("Source object cannot be null");

        if (target == null)
            throw new IllegalArgumentException("Target object cannot be null");

        BeanUtils.copyProperties(source, target, getNullOrBlankPropertyNames(source));
    }

    /**
     * Inspeciona um objeto de origem (source) e retorna um array de
     * nomes de propriedades que são nulas ou, no caso de Strings, que estão
     * vazias ou contêm apenas espaços em branco.
     *
     * @param source O objeto de origem a ser inspecionado.
     * @return Um array de Strings com os nomes das propriedades a serem ignoradas na cópia.
     */
    private static String[] getNullOrBlankPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());

            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }

            else if (srcValue instanceof String && !StringUtils.hasText((String) srcValue)) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }
}
