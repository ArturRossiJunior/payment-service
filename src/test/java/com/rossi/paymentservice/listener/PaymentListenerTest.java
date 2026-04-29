package com.rossi.paymentservice.listener;

import com.rossi.paymentservice.dto.PagamentoMessageDTO;
import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.model.enums.Metodo;
import com.rossi.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentListener — Testes Unitários")
class PaymentListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentListener paymentListener;

    // -----------------------------------------------------------------------
    // Cenário 1: Mensagem recebida deve ser convertida e delegada ao service
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve converter PagamentoMessageDTO e chamar paymentService.processarPagamento()")
    void deveConverterMensagemEChamarService() {
        PagamentoMessageDTO mensagem = new PagamentoMessageDTO(
                42L,
                new BigDecimal("20.00"),
                Metodo.PIX,
                7L);

        paymentListener.processarPagamento(mensagem);

        verify(paymentService).processarPagamento(any(PagamentoRequestDTO.class));
    }

    // -----------------------------------------------------------------------
    // Cenário 2: Os campos do DTO de request devem refletir a mensagem
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("O PagamentoRequestDTO deve conter os dados mapeados corretamente da mensagem")
    void deveMappearCamposCorretamenteDaMensagem() {
        PagamentoMessageDTO mensagem = new PagamentoMessageDTO(
                99L,
                new BigDecimal("50.00"),
                Metodo.CARTAO,
                5L);

        paymentListener.processarPagamento(mensagem);

        ArgumentCaptor<PagamentoRequestDTO> captor = ArgumentCaptor.forClass(PagamentoRequestDTO.class);
        verify(paymentService).processarPagamento(captor.capture());

        PagamentoRequestDTO requestCapturado = captor.getValue();
        assertThat(requestCapturado.reservaId()).isEqualTo(99L);
        assertThat(requestCapturado.valor()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(requestCapturado.metodo()).isEqualTo(Metodo.CARTAO);
    }
}
