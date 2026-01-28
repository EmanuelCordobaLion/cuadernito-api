package com.cuadernito.cuadernito_back.repository;

import com.cuadernito.cuadernito_back.entity.CustomerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, Long> {
    List<CustomerDebt> findByUserId(Long userId);
    Optional<CustomerDebt> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
