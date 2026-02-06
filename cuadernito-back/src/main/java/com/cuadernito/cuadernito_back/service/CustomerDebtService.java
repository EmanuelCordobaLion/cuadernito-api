package com.cuadernito.cuadernito_back.service;

import com.cuadernito.cuadernito_back.dto.CustomerDebtDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerDebtService {
    CustomerDebtDTO createCustomerDebt(CustomerDebtDTO customerDebtDTO, String userEmail);
    CustomerDebtDTO getCustomerDebtById(Long id, String userEmail);
    List<CustomerDebtDTO> getAllCustomerDebtsByUser(String userEmail);
    CustomerDebtDTO updateCustomerDebt(Long id, CustomerDebtDTO customerDebtDTO, String userEmail);
    CustomerDebtDTO registerPayment(Long id, BigDecimal amount, String userEmail);
    void deleteCustomerDebt(Long id, String userEmail);
}
