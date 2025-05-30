package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import com.project.manager.finance.management.domains.repositories.jpa.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PaymentTransactionService {
    @Autowired
    private PaymentTransactionRepository transactionRepository;

    public void createPaymentTransactionByReminder(PaymentReminder reminder) {
        PaymentTransaction transaction = new PaymentTransaction(
                reminder,
                reminder.getBaseDate(),
                reminder.getMethod(),
                reminder.getInstallmentsNumber(),
                TransactionStatusEnum.PENDENTE
        );

        transactionRepository.save(transaction);
    }

    public List<PaymentTransaction> listPaymentTransactionsByReminderId(String reminderId) {
        return transactionRepository.findByReminderIdAndAuditDeletedFalse(reminderId);
    }
}
