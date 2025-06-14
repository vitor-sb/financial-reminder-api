package com.project.manager.finance.management.domains.models.reminder.enums;

import java.time.LocalDate;

public enum RecurrenceEnum {
    UNICA,
    DIARIA,
    SEMANAL,
    QUINZENAL,
    MENSAL,
    TRIMESTRAL,
    SEMESTRAL,
    ANUAL;

    public LocalDate getNextDate(LocalDate currentDate) {
        if (this == UNICA) {
            return null;
        }
        return switch (this) {
            case DIARIA -> currentDate.plusDays(1);
            case SEMANAL -> currentDate.plusWeeks(1);
            case QUINZENAL -> currentDate.plusWeeks(2);
            case MENSAL -> currentDate.plusMonths(1);
            case TRIMESTRAL -> currentDate.plusMonths(3);
            case SEMESTRAL -> currentDate.plusMonths(6);
            case ANUAL -> currentDate.plusYears(1);
            default -> null;
        };
    }
}
