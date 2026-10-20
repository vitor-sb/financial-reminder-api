package com.reminder.financial.api.adapters.mappers;

import com.reminder.financial.api.adapters.dtos.paymentreminder.*;
import com.reminder.financial.api.application.dtos.paymentreminder.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderMapper {

    public PaymentReminderResponse toResponse(PaymentReminderResult result) {
        return new PaymentReminderResponse(
                result.title(),
                result.description(),
                result.date(),
                result.recurrence(),
                result.method(),
                result.value(),
                result.installmentsNumber()
        );
    }

    public CreatePaymentReminderCommand toCommand(@Valid CreatePaymentReminderRequest request) {
        return new CreatePaymentReminderCommand(
                request.title(),
                request.description(),
                request.date(),
                request.recurrence(),
                request.method(),
                request.value(),
                request.installmentsNumber()
        );
    }

    public UpdatePaymentReminderCommand toCommand(@Valid UpdatePaymentReminderRequest request) {
        return new UpdatePaymentReminderCommand(
                request.title(),
                request.description(),
                request.method(),
                request.value()
        );
    }

    public GetPaymentReminderQuery toQuery(String id, String name) {
        return new GetPaymentReminderQuery(
                id, name
        );
    }

    public MonthlyBillResponse toResponse(ListMonthlyBillsResult result) {
        return new MonthlyBillResponse(
                result.transactionId(),
                result.reminderId(),
                result.title(),
                result.dueDate(),
                result.value(),
                result.method(),
                result.status(),
                result.installmentNumber()
        );
    }

    public MonthlyBillsSummaryResponse toSummaryResponse(MonthlyBillsSummaryResult result) {
        Page<MonthlyBillResponse> billsResponsePage = result.bills().map(this::toResponse);

        var totals = new MonthlyBillsSummaryResponse.Totals(
                result.totalPaid(),
                result.totalPending(),
                result.totalOverdue()
        );

        return new MonthlyBillsSummaryResponse(billsResponsePage, totals);
    }

    public ListPaymentRemindersResponse toListPaymentRemindersResponse(ListPaymentRemindersResult result) {
        return new ListPaymentRemindersResponse(
                result.id(),
                result.title(),
                result.description(),
                result.baseDate(),
                result.recurrence(),
                result.method(),
                result.baseValue(),
                result.installmentsNumber()
        );
    }

    public ListPaymentRemindersFilterQuery toListTransactionsFilterQuery(ListPaymentRemindersFilterRequest request) {
        if (request == null)
            return new ListPaymentRemindersFilterQuery(null, null, null, null, null, null, null);

        return new ListPaymentRemindersFilterQuery(
                request.reminderId(),
                request.title(),
                request.dueDateFrom(),
                request.dueDateUntil(),
                request.recurrence(),
                request.method(),
                request.enabled()
        );
    }
}