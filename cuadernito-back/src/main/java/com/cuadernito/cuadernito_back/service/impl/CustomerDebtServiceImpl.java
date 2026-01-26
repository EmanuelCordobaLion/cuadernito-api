package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.CustomerDebtDTO;
import com.cuadernito.cuadernito_back.service.CustomerDebtService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDebtServiceImpl implements CustomerDebtService {

    @Override
    public CustomerDebtDTO createCustomerDebt(CustomerDebtDTO customerDebtDTO, String userEmail) {
        return null;
    }

    @Override
    public CustomerDebtDTO getCustomerDebtById(Long id, String userEmail) {
        return null;
    }

    @Override
    public List<CustomerDebtDTO> getAllCustomerDebtsByUser(String userEmail) {
        return null;
    }

    @Override
    public CustomerDebtDTO updateCustomerDebt(Long id, CustomerDebtDTO customerDebtDTO, String userEmail) {
        return null;
    }

    @Override
    public void deleteCustomerDebt(Long id, String userEmail) {
    }
}
