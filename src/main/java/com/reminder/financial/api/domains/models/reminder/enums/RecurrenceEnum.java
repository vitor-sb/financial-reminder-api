package com.reminder.financial.api.domains.models.reminder.enums;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public enum RecurrenceEnum {
    UNICA {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return null;
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return 1;
        }
    },
    DIARIA {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusDays(1);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.DAYS.between(baseDate, futureDate);
        }
    },
    SEMANAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusWeeks(1);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.WEEKS.between(baseDate, futureDate);
        }
    },
    QUINZENAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusWeeks(2);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.WEEKS.between(baseDate, futureDate) / 2;
        }
    },
    MENSAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusMonths(1);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.MONTHS.between(baseDate, futureDate);
        }
    },
    TRIMESTRAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusMonths(3);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.MONTHS.between(baseDate, futureDate) / 3;
        }
    },
    SEMESTRAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusMonths(6);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.MONTHS.between(baseDate, futureDate) / 6;
        }
    },
    ANUAL {
        @Override
        public LocalDate getNextDate(LocalDate currentDate) {
            return currentDate.plusYears(1);
        }

        @Override
        public long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate) {
            return ChronoUnit.YEARS.between(baseDate, futureDate);
        }
    };

    public abstract LocalDate getNextDate(LocalDate currentDate);

    public abstract long calculateInstallmentsBetween(LocalDate baseDate, LocalDate futureDate);

    public LocalDate calculateSimulationStartDate(LocalDate baseDate, YearMonth targetMonth) {
        if (YearMonth.from(baseDate).isAfter(targetMonth)) {
            return baseDate;
        }

        LocalDate currentDate = baseDate;

        while (YearMonth.from(currentDate).isBefore(targetMonth)) {
            LocalDate nextDate = this.getNextDate(currentDate);
            if (nextDate == null) break;

            if (ChronoUnit.MONTHS.between(YearMonth.from(currentDate), targetMonth) > 1) {
                long estimatedSteps = this.calculateInstallmentsBetween(currentDate, targetMonth.atDay(1));
                if (estimatedSteps > 1) {
                    for (int i = 0; i < estimatedSteps -1; i++) {
                        currentDate = this.getNextDate(currentDate);
                    }
                }
            }
            currentDate = nextDate;
        }

        return currentDate;
    }
}