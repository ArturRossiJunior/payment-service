package com.rossi.paymentservice.dto;

import com.rossi.paymentservice.model.enums.Metodo;

import java.math.BigDecimal;

public record PagamentoMessageDTO(Long reservaId,
                                  BigDecimal valor,
                                  Metodo metodo,
                                  Long usuarioId) {
}
