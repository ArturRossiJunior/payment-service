package com.rossi.paymentservice.dto.request;

import com.rossi.paymentservice.model.enums.Metodo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PagamentoRequestDTO (@NotNull Long reservaId,
                                   @NotNull @Positive BigDecimal valor,
                                   @NotNull Metodo metodo){

}
