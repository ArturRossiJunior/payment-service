package com.rossi.paymentservice.dto;

import com.rossi.paymentservice.model.enums.Status;

public record ConfirmacaoMessageDTO(Long reservaId,
        Status status) {
}
