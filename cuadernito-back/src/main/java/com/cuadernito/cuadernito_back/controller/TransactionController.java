package com.cuadernito.cuadernito_back.controller;

import com.cuadernito.cuadernito_back.dto.CreateTransactionRequest;
import com.cuadernito.cuadernito_back.dto.TransactionDTO;
import com.cuadernito.cuadernito_back.dto.UpdateTransactionRequest;
import com.cuadernito.cuadernito_back.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transacciones", description = "Endpoints para gestionar transacciones (ingresos y gastos)")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Crear transacción", description = "Crea una nueva transacción. Solo envíe los campos que debe llenar; id, userId, createdAt se devuelven en el response.")
    public ResponseEntity<TransactionDTO> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        TransactionDTO dto = TransactionDTO.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .type(request.getType())
                .date(request.getDate())
                .categoryId(request.getCategoryId())
                .esFiado(request.getEsFiado())
                .customerDebtId(request.getCustomerDebtId())
                .debtAmount(request.getDebtAmount())
                .customerFirstName(request.getCustomerFirstName())
                .customerLastName(request.getCustomerLastName())
                .customerPhone(request.getCustomerPhone())
                .customerDocumentNumber(request.getCustomerDocumentNumber())
                .build();
        TransactionDTO created = transactionService.createTransaction(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transacción por ID", description = "Obtiene una transacción específica del usuario autenticado")
    public ResponseEntity<TransactionDTO> getTransactionById(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        TransactionDTO transaction = transactionService.getTransactionById(id, email);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    @Operation(summary = "Listar transacciones", description = "Obtiene todas las transacciones del usuario autenticado")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        List<TransactionDTO> transactions = transactionService.getAllTransactionsByUser(email);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar transacción", description = "Actualización parcial. Solo envíe los campos que desea cambiar.")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        TransactionDTO dto = TransactionDTO.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .type(request.getType())
                .date(request.getDate())
                .categoryId(request.getCategoryId())
                .esFiado(request.getEsFiado())
                .customerDebtId(request.getCustomerDebtId())
                .debtAmount(request.getDebtAmount())
                .customerFirstName(request.getCustomerFirstName())
                .customerLastName(request.getCustomerLastName())
                .customerPhone(request.getCustomerPhone())
                .customerDocumentNumber(request.getCustomerDocumentNumber())
                .build();
        TransactionDTO updated = transactionService.updateTransaction(id, dto, email);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar transacción", description = "Elimina una transacción del usuario autenticado")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        transactionService.deleteTransaction(id, email);
        return ResponseEntity.noContent().build();
    }
}
