package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyBillsPaginatorTest {

    private final List<ListMonthlyBillsResult> bills = IntStream.range(0, 5)
            .mapToObj(i -> new ListMonthlyBillsResult(null, String.valueOf(i), "t", LocalDate.now(), BigDecimal.ONE, null, TransactionStatusEnum.PENDENTE, null))
            .toList();

    @Test
    void returnsRequestedSlice() {
        var page = MonthlyBillsPaginator.paginate(bills, PageRequest.of(1, 2));
        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
    }

    @Test
    void pageBeyondEndIsEmpty() {
        var page = MonthlyBillsPaginator.paginate(bills, PageRequest.of(5, 2));
        assertTrue(page.getContent().isEmpty());
        assertEquals(5, page.getTotalElements());
    }
}
