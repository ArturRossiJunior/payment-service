package com.rossi.paymentservice.controller;

import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.dto.response.PagamentoResponseDTO;
import com.rossi.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> processarPagamento(@Valid @RequestBody PagamentoRequestDTO pagamentoRequestDTO) {
        PagamentoResponseDTO pagamento = paymentService.processarPagamento(pagamentoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }
}
