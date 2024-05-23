package org.rubnikovich.bankoperation.controller.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.controller.dto.TransactionDto;
import org.rubnikovich.bankoperation.controller.entity.Transaction;
import org.rubnikovich.bankoperation.controller.entity.User;
import org.rubnikovich.bankoperation.controller.repository.TransactionRepository;
import org.rubnikovich.bankoperation.controller.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public Transaction getById(Long id) {
        return transactionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Collection<TransactionDto> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionDto> transactionsDto = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionsDto.add(toTransactionDto(transaction));
        }
        return transactionsDto;
    }

    public List<TransactionDto> getAllUserTransactions(String login) {
        User user = userRepository.findByLogin(login).orElseThrow();
        Long id = user.getId();
        List<Transaction> transactions = transactionRepository.getAllByRecipientIdOrSenderId(id, id);
        List<TransactionDto> transactionsDto = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionsDto.add(toTransactionDto(transaction));
        }
        return transactionsDto;
    }

    public boolean makeTransaction(TransactionDto transactionDto) {
        User recipient = userRepository.findById(transactionDto.getRecipient())
                .orElseThrow(() -> new NoSuchElementException("Recipient not found"));
        User sender = userRepository.findById(transactionDto.getSender())
                .orElseThrow(() -> new NoSuchElementException("Sender not found"));
        BigDecimal senderMoney = sender.getBalance();
        BigDecimal recipientMoney = recipient.getBalance();
        BigDecimal amount = transactionDto.getAmount();
        BigDecimal result = senderMoney.subtract(amount);
        if (result.compareTo(BigDecimal.ZERO) < 0 ||
                amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        sender.setBalance(senderMoney.subtract(amount));
        recipient.setBalance(recipientMoney.add(amount));
        Transaction transaction = toTransactions(transactionDto);
        transactionRepository.save(transaction);
        userRepository.save(recipient);
        userRepository.save(sender);
        return true;
    }

    private TransactionDto toTransactionDto(Transaction transaction) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmount(transaction.getAmount());
        transactionDto.setDate(transaction.getDate());
        transactionDto.setId(transaction.getId());
        transactionDto.setSender(transaction.getSender().getId()); //or other info
        transactionDto.setRecipient(transaction.getRecipient().getId());
        return transactionDto;
    }

    private Transaction toTransactions(TransactionDto transactionDto) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDate(transactionDto.getDate());
        transaction.setSender(userRepository.findById(transactionDto.getSender()).orElseThrow());
        transaction.setRecipient(userRepository.findById(transactionDto.getRecipient()).orElseThrow());
        return transaction;
    }
}
