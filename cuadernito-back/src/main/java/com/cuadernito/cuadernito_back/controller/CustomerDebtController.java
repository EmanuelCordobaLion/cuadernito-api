package com.cuadernito.cuadernito_back.controller;

import com.cuadernito.cuadernito_back.dto.CustomerDebtDTO;
import com.cuadernito.cuadernito_back.dto.RegisterPaymentRequest;
import com.cuadernito.cuadernito_back.service.CustomerDebtService;
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
@RequestMapping("/api/v1/customer-debts")
@Tag(name = "Deudas de Clientes", description = "Endpoints para gestionar deudas de clientes (fiados)")
public class CustomerDebtController {

    @Autowired
    private CustomerDebtService customerDebtService;

    @PostMapping
    @Operation(summary = "Crear deuda de cliente", description = "Registra una nueva deuda de cliente (fiado)")
    public ResponseEntity<CustomerDebtDTO> createCustomerDebt(
            @Valid @RequestBody CustomerDebtDTO customerDebtDTO,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CustomerDebtDTO created = customerDebtService.createCustomerDebt(customerDebtDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener deuda por ID", description = "Obtiene una deuda específica del usuario autenticado")
    public ResponseEntity<CustomerDebtDTO> getCustomerDebtById(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CustomerDebtDTO customerDebt = customerDebtService.getCustomerDebtById(id, email);
        return ResponseEntity.ok(customerDebt);
    }

    @GetMapping
    @Operation(summary = "Listar deudas", description = "Obtiene todas las deudas de clientes del usuario autenticado")
    public ResponseEntity<List<CustomerDebtDTO>> getAllCustomerDebts(
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        List<CustomerDebtDTO> customerDebts = customerDebtService.getAllCustomerDebtsByUser(email);
        return ResponseEntity.ok(customerDebts);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar deuda", description = "Actualiza una deuda existente del usuario autenticado")
    public ResponseEntity<CustomerDebtDTO> updateCustomerDebt(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDebtDTO customerDebtDTO,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CustomerDebtDTO updated = customerDebtService.updateCustomerDebt(id, customerDebtDTO, email);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Registrar pago", description = "Registra un pago sobre la deuda (hace decrecer lo que debe el cliente)")
    public ResponseEntity<CustomerDebtDTO> registerPayment(
            @PathVariable Long id,
            @Valid @RequestBody RegisterPaymentRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CustomerDebtDTO updated = customerDebtService.registerPayment(id, request.getAmount(), email);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar deuda", description = "Elimina una deuda del usuario autenticado")
    public ResponseEntity<Void> deleteCustomerDebt(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        customerDebtService.deleteCustomerDebt(id, email);
        return ResponseEntity.noContent().build();
    }
}
