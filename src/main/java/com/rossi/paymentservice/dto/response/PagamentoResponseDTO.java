package com.rossi.paymentservice.dto.response;

import com.rossi.paymentservice.model.enums.Status;

public record PagamentoResponseDTO (Long id,
                                    Long reservaId,
                                    Status status){
}
