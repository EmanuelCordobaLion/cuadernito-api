package com.cuadernito.cuadernito_back.service;

import com.cuadernito.cuadernito_back.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    TransactionDTO createTransaction(TransactionDTO transactionDTO, String userEmail);
    TransactionDTO getTransactionById(Long id, String userEmail);
    List<TransactionDTO> getAllTransactionsByUser(String userEmail);
    TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, String userEmail);
    void deleteTransaction(Long id, String userEmail);
}
