package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.CustomerDebtDTO;
import com.cuadernito.cuadernito_back.entity.CustomerDebt;
import com.cuadernito.cuadernito_back.entity.CustomerDebt.DebtStatus;
import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.exception.BadRequestException;
import com.cuadernito.cuadernito_back.exception.ResourceNotFoundException;
import com.cuadernito.cuadernito_back.mapper.CustomerDebtMapper;
import com.cuadernito.cuadernito_back.repository.CustomerDebtRepository;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import com.cuadernito.cuadernito_back.service.CustomerDebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerDebtServiceImpl implements CustomerDebtService {

    @Autowired
    private CustomerDebtRepository customerDebtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerDebtMapper customerDebtMapper;

    @Override
    @Transactional
    public CustomerDebtDTO createCustomerDebt(CustomerDebtDTO customerDebtDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validateCustomerNames(customerDebtDTO.getCustomerFirstName(), customerDebtDTO.getCustomerLastName());
        validateCustomerPhone(customerDebtDTO.getCustomerPhone());
        String documentNumber = validateDocumentNumber(customerDebtDTO.getDocumentNumber());
        BigDecimal totalAmount = validateTotalAmount(customerDebtDTO.getTotalAmount());
        BigDecimal paidAmount = customerDebtDTO.getPaidAmount() != null ? customerDebtDTO.getPaidAmount() : BigDecimal.ZERO;
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El monto pagado no puede ser negativo");
        }
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new BadRequestException("El monto pagado no puede superar el monto total");
        }

        BigDecimal remainingAmount = totalAmount.subtract(paidAmount);
        DebtStatus status = computeStatus(paidAmount, totalAmount);

        CustomerDebt customerDebt = CustomerDebt.builder()
                .documentNumber(documentNumber)
                .customerFirstName(customerDebtDTO.getCustomerFirstName().trim())
                .customerLastName(customerDebtDTO.getCustomerLastName().trim())
                .customerPhone(customerDebtDTO.getCustomerPhone().trim())
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .status(status)
                .user(user)
                .build();

        CustomerDebt saved = customerDebtRepository.save(customerDebt);
        return customerDebtMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDebtDTO getCustomerDebtById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CustomerDebt customerDebt = customerDebtRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));

        return customerDebtMapper.toDTO(customerDebt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDebtDTO> getAllCustomerDebtsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return customerDebtRepository.findByUserId(user.getId()).stream()
                .map(customerDebtMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerDebtDTO updateCustomerDebt(Long id, CustomerDebtDTO customerDebtDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CustomerDebt customerDebt = customerDebtRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));

        if (customerDebtDTO.getCustomerFirstName() != null) {
            if (customerDebtDTO.getCustomerFirstName().trim().isEmpty()) {
                throw new BadRequestException("El nombre del cliente no puede estar vacío");
            }
            customerDebt.setCustomerFirstName(customerDebtDTO.getCustomerFirstName().trim());
        }
        if (customerDebtDTO.getCustomerLastName() != null) {
            if (customerDebtDTO.getCustomerLastName().trim().isEmpty()) {
                throw new BadRequestException("El apellido del cliente no puede estar vacío");
            }
            customerDebt.setCustomerLastName(customerDebtDTO.getCustomerLastName().trim());
        }
        if (customerDebtDTO.getCustomerPhone() != null) {
            validateCustomerPhone(customerDebtDTO.getCustomerPhone());
            customerDebt.setCustomerPhone(customerDebtDTO.getCustomerPhone().trim());
        }
        if (customerDebtDTO.getDocumentNumber() != null) {
            String doc = validateDocumentNumber(customerDebtDTO.getDocumentNumber());
            Optional<CustomerDebt> existing = customerDebtRepository.findByUserIdAndDocumentNumber(user.getId(), doc);
            if (existing.isPresent() && !existing.get().getId().equals(customerDebt.getId())) {
                throw new BadRequestException("Ya existe una deuda con ese número de documento");
            }
            customerDebt.setDocumentNumber(doc);
        }
        if (customerDebtDTO.getTotalAmount() != null) {
            BigDecimal newTotal = validateTotalAmount(customerDebtDTO.getTotalAmount());
            customerDebt.setTotalAmount(newTotal);
            recalculateAmountsAndStatus(customerDebt);
        }
        if (customerDebtDTO.getPaidAmount() != null) {
            BigDecimal paid = customerDebtDTO.getPaidAmount();
            if (paid.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("El monto pagado no puede ser negativo");
            }
            if (paid.compareTo(customerDebt.getTotalAmount()) > 0) {
                throw new BadRequestException("El monto pagado no puede superar el monto total");
            }
            customerDebt.setPaidAmount(paid);
            recalculateAmountsAndStatus(customerDebt);
        }

        CustomerDebt updated = customerDebtRepository.save(customerDebt);
        return customerDebtMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public CustomerDebtDTO registerPayment(Long id, BigDecimal amount, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CustomerDebt customerDebt = customerDebtRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago debe ser mayor que cero");
        }

        BigDecimal newPaidAmount = customerDebt.getPaidAmount().add(amount);
        if (newPaidAmount.compareTo(customerDebt.getTotalAmount()) > 0) {
            newPaidAmount = customerDebt.getTotalAmount();
        }
        customerDebt.setPaidAmount(newPaidAmount);
        customerDebt.setRemainingAmount(customerDebt.getTotalAmount().subtract(newPaidAmount));
        customerDebt.setStatus(computeStatus(newPaidAmount, customerDebt.getTotalAmount()));

        CustomerDebt updated = customerDebtRepository.save(customerDebt);
        return customerDebtMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCustomerDebt(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean exists = customerDebtRepository.existsByIdAndUserId(id, user.getId());
        if (!exists) {
            throw new ResourceNotFoundException("Deuda del cliente no encontrada");
        }
        customerDebtRepository.deleteById(id);
    }

    private void validateCustomerNames(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del cliente es obligatorio");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new BadRequestException("El apellido del cliente es obligatorio");
        }
    }

    private void validateCustomerPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("El teléfono del cliente es obligatorio");
        }
        if (phone.trim().length() > 20) {
            throw new BadRequestException("El teléfono no puede superar 20 caracteres");
        }
    }

    private String validateDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }
        String doc = documentNumber.trim();
        if (!doc.matches("\\d+")) {
            throw new BadRequestException("El número de documento debe contener solo dígitos");
        }
        if (doc.length() > 50) {
            throw new BadRequestException("El número de documento no puede superar 50 caracteres");
        }
        return doc;
    }

    private BigDecimal validateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new BadRequestException("El monto total es obligatorio");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto total debe ser mayor que cero");
        }
        return totalAmount;
    }

    private DebtStatus computeStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return DebtStatus.PENDIENTE;
        }
        if (paidAmount.compareTo(totalAmount) >= 0) {
            return DebtStatus.PAGADO;
        }
        return DebtStatus.PARCIAL;
    }

    private void recalculateAmountsAndStatus(CustomerDebt customerDebt) {
        BigDecimal total = customerDebt.getTotalAmount();
        BigDecimal paid = customerDebt.getPaidAmount();
        if (paid.compareTo(total) > 0) {
            paid = total;
            customerDebt.setPaidAmount(paid);
        }
        customerDebt.setRemainingAmount(total.subtract(paid));
        customerDebt.setStatus(computeStatus(paid, total));
    }
}
