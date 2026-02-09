package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.TransactionDTO;
import com.cuadernito.cuadernito_back.dto.TransactionItemDTO;
import com.cuadernito.cuadernito_back.entity.Category;
import com.cuadernito.cuadernito_back.entity.CustomerDebt;
import com.cuadernito.cuadernito_back.entity.CustomerDebt.DebtStatus;
import com.cuadernito.cuadernito_back.entity.Transaction;
import com.cuadernito.cuadernito_back.entity.Transaction.TransactionType;
import com.cuadernito.cuadernito_back.entity.TransactionItem;
import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.exception.BadRequestException;
import com.cuadernito.cuadernito_back.exception.ResourceNotFoundException;
import com.cuadernito.cuadernito_back.mapper.TransactionMapper;
import com.cuadernito.cuadernito_back.repository.CategoryRepository;
import com.cuadernito.cuadernito_back.repository.CustomerDebtRepository;
import com.cuadernito.cuadernito_back.repository.TransactionItemRepository;
import com.cuadernito.cuadernito_back.repository.TransactionRepository;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import com.cuadernito.cuadernito_back.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private TransactionItemRepository transactionItemRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionDTO transactionDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<TransactionItemDTO> itemsDTO = transactionDTO.getItems();
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            throw new BadRequestException("La transacción debe tener al menos un item");
        }
        if (itemsDTO.size() < 1) {
            throw new BadRequestException("La transacción debe tener al menos un item");
        }

        BigDecimal totalAmount = itemsDTO.stream()
                .map(item -> {
                    if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BadRequestException("Todos los items deben tener un monto mayor que cero");
                    }
                    return item.getAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TransactionType type = (transactionDTO.getType() == null || transactionDTO.getType().isBlank())
                ? TransactionType.INGRESO
                : parseAndValidateType(transactionDTO.getType());

        LocalDateTime date = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();

        boolean esFiado = Boolean.TRUE.equals(transactionDTO.getEsFiado());
        CustomerDebt customerDebt = null;
        BigDecimal debtAmount = null;

        if (esFiado) {
            debtAmount = (transactionDTO.getDebtAmount() != null && transactionDTO.getDebtAmount().compareTo(BigDecimal.ZERO) > 0)
                    ? transactionDTO.getDebtAmount()
                    : totalAmount;
            validateDebtAmount(debtAmount, totalAmount);

            if (isValidExistingDebtId(transactionDTO.getCustomerDebtId())) {
                customerDebt = customerDebtRepository.findByIdAndUserId(transactionDTO.getCustomerDebtId(), user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));
                addToDebt(customerDebt, debtAmount);
            } else {
                validateNewCustomerForFiado(transactionDTO);
                String doc = validateDocumentNumber(transactionDTO.getCustomerDocumentNumber());
                Optional<CustomerDebt> existing = customerDebtRepository.findByUserIdAndDocumentNumber(user.getId(), doc);
                if (existing.isPresent()) {
                    customerDebt = existing.get();
                    addToDebt(customerDebt, debtAmount);
                } else {
                    customerDebt = createNewCustomerDebt(user,
                            transactionDTO.getCustomerFirstName().trim(),
                            transactionDTO.getCustomerLastName().trim(),
                            transactionDTO.getCustomerPhone().trim(),
                            doc,
                            debtAmount);
                }
            }
        }

        Transaction transaction = Transaction.builder()
                .amount(totalAmount)
                .description(transactionDTO.getDescription())
                .type(type)
                .date(date)
                .user(user)
                .customerDebt(customerDebt)
                .debtAmount(debtAmount)
                .items(new ArrayList<>())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        for (TransactionItemDTO itemDTO : itemsDTO) {
            Category category = getCategoryOwnedByUser(itemDTO.getCategoryId(), user.getId());
            TransactionItem item = TransactionItem.builder()
                    .transaction(saved)
                    .category(category)
                    .amount(itemDTO.getAmount())
                    .build();
            saved.getItems().add(transactionItemRepository.save(item));
        }

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

        BigDecimal debtAmountAnterior = transaction.getDebtAmount();
        CustomerDebt deudaAnterior = transaction.getCustomerDebt();

        if (transactionDTO.getDescription() != null) {
            transaction.setDescription(transactionDTO.getDescription());
        }
        if (transactionDTO.getType() != null) {
            transaction.setType(parseAndValidateType(transactionDTO.getType()));
        }
        if (transactionDTO.getDate() != null) {
            transaction.setDate(transactionDTO.getDate());
        }

        if (transactionDTO.getItems() != null || (transactionDTO.getRemoveItemIds() != null && !transactionDTO.getRemoveItemIds().isEmpty())) {
            updateTransactionItems(transaction, transactionDTO.getItems(), transactionDTO.getRemoveItemIds(), user.getId());
            BigDecimal nuevoAmount = transaction.getItems().stream()
                    .map(TransactionItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            transaction.setAmount(nuevoAmount);
        }

        Boolean esFiadoEnRequest = transactionDTO.getEsFiado();
        if (esFiadoEnRequest != null) {
            if (deudaAnterior != null && debtAmountAnterior != null) {
                subtractFromDebt(deudaAnterior, debtAmountAnterior);
            }
            transaction.setCustomerDebt(null);
            transaction.setDebtAmount(null);

            if (esFiadoEnRequest) {
                BigDecimal nuevoDebtAmount = (transactionDTO.getDebtAmount() != null && transactionDTO.getDebtAmount().compareTo(BigDecimal.ZERO) > 0)
                        ? transactionDTO.getDebtAmount()
                        : transaction.getAmount();
                validateDebtAmount(nuevoDebtAmount, transaction.getAmount());

                CustomerDebt nuevaDeuda;
                if (isValidExistingDebtId(transactionDTO.getCustomerDebtId())) {
                    nuevaDeuda = customerDebtRepository.findByIdAndUserId(transactionDTO.getCustomerDebtId(), user.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Deuda del cliente no encontrada"));
                    addToDebt(nuevaDeuda, nuevoDebtAmount);
                } else if (transactionDTO.getCustomerDocumentNumber() != null && !transactionDTO.getCustomerDocumentNumber().trim().isEmpty()) {
                    validateNewCustomerForFiado(transactionDTO);
                    String doc = validateDocumentNumber(transactionDTO.getCustomerDocumentNumber());
                    Optional<CustomerDebt> existing = customerDebtRepository.findByUserIdAndDocumentNumber(user.getId(), doc);
                    if (existing.isPresent()) {
                        nuevaDeuda = existing.get();
                        addToDebt(nuevaDeuda, nuevoDebtAmount);
                    } else {
                        nuevaDeuda = createNewCustomerDebt(user,
                                transactionDTO.getCustomerFirstName().trim(),
                                transactionDTO.getCustomerLastName().trim(),
                                transactionDTO.getCustomerPhone().trim(),
                                doc,
                                nuevoDebtAmount);
                    }
                } else {
                    throw new BadRequestException("Para marcar como fiado debe indicar customerDebtId o los datos del cliente (nombre, apellido, teléfono, número de documento)");
                }
                transaction.setCustomerDebt(nuevaDeuda);
                transaction.setDebtAmount(nuevoDebtAmount);
            }
        } else if ((transactionDTO.getItems() != null || (transactionDTO.getRemoveItemIds() != null && !transactionDTO.getRemoveItemIds().isEmpty())) 
                && transaction.getCustomerDebt() != null && transaction.getDebtAmount() != null) {
            BigDecimal nuevoAmount = transaction.getAmount();
            BigDecimal nuevoDebtAmount = transaction.getDebtAmount().min(nuevoAmount);
            
            if (nuevoDebtAmount.compareTo(debtAmountAnterior) != 0) {
                subtractFromDebt(transaction.getCustomerDebt(), debtAmountAnterior);
                addToDebt(transaction.getCustomerDebt(), nuevoDebtAmount);
                transaction.setDebtAmount(nuevoDebtAmount);
            }
        }

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toDTO(updated);
    }

    private void updateTransactionItems(Transaction transaction, List<TransactionItemDTO> itemsDTO, List<Long> removeItemIds, Long userId) {
        if (removeItemIds != null && !removeItemIds.isEmpty()) {
            for (Long itemId : removeItemIds) {
                TransactionItem item = transaction.getItems().stream()
                        .filter(i -> i.getId().equals(itemId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado: " + itemId));
                transaction.getItems().remove(item);
                transactionItemRepository.delete(item);
            }
        }

        if (itemsDTO != null && !itemsDTO.isEmpty()) {
            for (TransactionItemDTO itemDTO : itemsDTO) {
                if (itemDTO.getAmount() == null || itemDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException("Todos los items deben tener un monto mayor que cero");
                }
                Category category = getCategoryOwnedByUser(itemDTO.getCategoryId(), userId);

                if (itemDTO.getId() != null) {
                    TransactionItem item = transaction.getItems().stream()
                            .filter(i -> i.getId().equals(itemDTO.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado: " + itemDTO.getId()));
                    item.setCategory(category);
                    item.setAmount(itemDTO.getAmount());
                    transactionItemRepository.save(item);
                } else {
                    TransactionItem item = TransactionItem.builder()
                            .transaction(transaction)
                            .category(category)
                            .amount(itemDTO.getAmount())
                            .build();
                    TransactionItem saved = transactionItemRepository.save(item);
                    transaction.getItems().add(saved);
                }
            }
        }

        if (transaction.getItems().isEmpty()) {
            throw new BadRequestException("La transacción debe tener al menos un item. Si desea eliminar todos los items, elimine la transacción completa.");
        }
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

        if (transaction.getCustomerDebt() != null && transaction.getDebtAmount() != null) {
            subtractFromDebt(transaction.getCustomerDebt(), transaction.getDebtAmount());
        }
        transactionRepository.delete(transaction);
    }

    private void validateDebtAmount(BigDecimal debtAmount, BigDecimal transactionAmount) {
        if (debtAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El monto fiado no puede ser negativo");
        }
        if (debtAmount.compareTo(transactionAmount != null ? transactionAmount : BigDecimal.ZERO) > 0) {
            throw new BadRequestException("El monto fiado no puede superar el monto de la transacción");
        }
    }

    private void validateNewCustomerForFiado(TransactionDTO dto) {
        if (dto.getCustomerFirstName() == null || dto.getCustomerFirstName().trim().isEmpty()) {
            throw new BadRequestException("El nombre del cliente es obligatorio cuando es fiado (cliente nuevo)");
        }
        if (dto.getCustomerLastName() == null || dto.getCustomerLastName().trim().isEmpty()) {
            throw new BadRequestException("El apellido del cliente es obligatorio cuando es fiado (cliente nuevo)");
        }
        if (dto.getCustomerPhone() == null || dto.getCustomerPhone().trim().isEmpty()) {
            throw new BadRequestException("El teléfono del cliente es obligatorio cuando es fiado (cliente nuevo)");
        }
        if (dto.getCustomerDocumentNumber() == null || dto.getCustomerDocumentNumber().trim().isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio cuando es fiado (cliente nuevo)");
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

    private TransactionType parseAndValidateType(String type) {
        try {
            return TransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de transacción no válido. Use INGRESO o GASTO");
        }
    }

    private boolean isValidExistingDebtId(Long customerDebtId) {
        return customerDebtId != null && customerDebtId > 0;
    }

    private Category getCategoryOwnedByUser(Long categoryId, Long userId) {
        if (categoryId == null) {
            throw new BadRequestException("La categoría es obligatoria");
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    private CustomerDebt createNewCustomerDebt(User user, String firstName, String lastName, String phone, String documentNumber, BigDecimal totalAmount) {
        CustomerDebt debt = CustomerDebt.builder()
                .documentNumber(documentNumber)
                .customerFirstName(firstName)
                .customerLastName(lastName)
                .customerPhone(phone)
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(totalAmount)
                .status(DebtStatus.PENDIENTE)
                .user(user)
                .build();
        return customerDebtRepository.save(debt);
    }

    private void addToDebt(CustomerDebt debt, BigDecimal amount) {
        debt.setTotalAmount(debt.getTotalAmount().add(amount));
        debt.setRemainingAmount(debt.getRemainingAmount().add(amount));
        debt.setStatus(computeDebtStatus(debt.getPaidAmount(), debt.getTotalAmount()));
        customerDebtRepository.save(debt);
    }

    private void subtractFromDebt(CustomerDebt debt, BigDecimal amount) {
        BigDecimal newTotal = debt.getTotalAmount().subtract(amount).max(BigDecimal.ZERO);
        debt.setTotalAmount(newTotal);
        BigDecimal paid = debt.getPaidAmount().min(newTotal);
        debt.setPaidAmount(paid);
        debt.setRemainingAmount(newTotal.subtract(paid));
        debt.setStatus(computeDebtStatus(paid, newTotal));
        customerDebtRepository.save(debt);
    }

    private DebtStatus computeDebtStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return DebtStatus.PAGADO;
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return DebtStatus.PENDIENTE;
        }
        if (paidAmount.compareTo(totalAmount) >= 0) {
            return DebtStatus.PAGADO;
        }
        return DebtStatus.PARCIAL;
    }
}
