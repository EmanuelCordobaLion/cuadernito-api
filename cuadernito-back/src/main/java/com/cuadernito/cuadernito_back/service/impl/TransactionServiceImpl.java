package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.TransactionDTO;
import com.cuadernito.cuadernito_back.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public TransactionDTO createTransaction(TransactionDTO transactionDTO, String userEmail) {
        return null;
    }

    @Override
    public TransactionDTO getTransactionById(Long id, String userEmail) {
        return null;
    }

    @Override
    public List<TransactionDTO> getAllTransactionsByUser(String userEmail) {
        return null;
    }

    @Override
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, String userEmail) {
        return null;
    }

    @Override
    public void deleteTransaction(Long id, String userEmail) {
    }
}
