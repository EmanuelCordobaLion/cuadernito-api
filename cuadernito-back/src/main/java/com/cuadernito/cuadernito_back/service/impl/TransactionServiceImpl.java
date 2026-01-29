package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.TransactionDTO;
import com.cuadernito.cuadernito_back.entity.Category;
import com.cuadernito.cuadernito_back.entity.CustomerDebt;
import com.cuadernito.cuadernito_back.entity.Transaction;
import com.cuadernito.cuadernito_back.entity.Transaction.TransactionType;
import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.exception.BadRequestException;
import com.cuadernito.cuadernito_back.exception.ResourceNotFoundException;
import com.cuadernito.cuadernito_back.mapper.TransactionMapper;
import com.cuadernito.cuadernito_back.repository.CategoryRepository;
import com.cuadernito.cuadernito_back.repository.CustomerDebtRepository;
import com.cuadernito.cuadernito_back.repository.TransactionRepository;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import com.cuadernito.cuadernito_back.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerDebtRepository customerDebtRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionDTO transactionDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validateAmount(transactionDTO.getAmount());
        TransactionType type = parseAndValidateType(transactionDTO.getType());
        Category category = getCategoryOwnedByUser(transactionDTO.getCategoryId(), user.getId());
        CustomerDebt customerDebt = null;
        if (transactionDTO.getCustomerDebtId() != null) {
            customerDebt = customerDebtRepository.findByIdAndUserId(transactionDTO.getCustomerDebtId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));
        }

        LocalDateTime date = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .amount(transactionDTO.getAmount())
                .description(transactionDTO.getDescription())
                .type(type)
                .date(date)
                .category(category)
                .user(user)
                .customerDebt(customerDebt)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

        return transactionMapper.toDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactionsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return transactionRepository.findByUserId(user.getId()).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

        if (transactionDTO.getAmount() != null) {
            validateAmount(transactionDTO.getAmount());
            transaction.setAmount(transactionDTO.getAmount());
        }
        if (transactionDTO.getDescription() != null) {
            transaction.setDescription(transactionDTO.getDescription());
        }
        if (transactionDTO.getType() != null) {
            transaction.setType(parseAndValidateType(transactionDTO.getType()));
        }
        if (transactionDTO.getDate() != null) {
            transaction.setDate(transactionDTO.getDate());
        }
        if (transactionDTO.getCategoryId() != null) {
            Category category = getCategoryOwnedByUser(transactionDTO.getCategoryId(), user.getId());
            transaction.setCategory(category);
        }
        if (transactionDTO.getCustomerDebtId() != null) {
            CustomerDebt customerDebt = customerDebtRepository.findByIdAndUserId(transactionDTO.getCustomerDebtId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));
            transaction.setCustomerDebt(customerDebt);
        }

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean exists = transactionRepository.existsByIdAndUserId(id, user.getId());
        if (!exists) {
            throw new ResourceNotFoundException("Transacción no encontrada");
        }
        transactionRepository.deleteById(id);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("El monto es obligatorio");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor que cero");
        }
    }

    private TransactionType parseAndValidateType(String type) {
        if (type == null || type.isBlank()) {
            throw new BadRequestException("El tipo de transacción es obligatorio (INGRESO o GASTO)");
        }
        try {
            return TransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de transacción no válido. Use INGRESO o GASTO");
        }
    }

    private Category getCategoryOwnedByUser(Long categoryId, Long userId) {
        if (categoryId == null) {
            throw new BadRequestException("La categoría es obligatoria");
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }
}
