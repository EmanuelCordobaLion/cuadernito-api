package com.cuadernito.cuadernito_back.repository;

import com.cuadernito.cuadernito_back.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
    List<TransactionItem> findByTransactionId(Long transactionId);
    void deleteByTransactionId(Long transactionId);
}
