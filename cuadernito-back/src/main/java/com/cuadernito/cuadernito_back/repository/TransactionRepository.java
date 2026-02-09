package com.cuadernito.cuadernito_back.repository;

import com.cuadernito.cuadernito_back.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.items WHERE t.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.items WHERE t.id = :id AND t.user.id = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    boolean existsByIdAndUserId(Long id, Long userId);
}
