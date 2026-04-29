package com.rossi.paymentservice.service;

import com.rossi.paymentservice.config.RabbitMQConfig;
import com.rossi.paymentservice.dto.ConfirmacaoMessageDTO;
import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.dto.response.PagamentoResponseDTO;
import com.rossi.paymentservice.model.Pagamento;
import com.rossi.paymentservice.model.enums.Metodo;
import com.rossi.paymentservice.model.enums.Status;
import com.rossi.paymentservice.repository.PagamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — Testes Unitários")
class PaymentServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Pagamento pagamentoSalvo(Long reservaId, BigDecimal valor, Metodo metodo, Status status) {
        Pagamento p = new Pagamento();
        p.setId(1L);
        p.setReservaId(reservaId);
        p.setValor(valor);
        p.setMetodo(metodo);
        p.setStatus(status);
        return p;
    }

    // -----------------------------------------------------------------------
    // Cenário 1: PIX → sempre APROVADO, independente do valor
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento via PIX deve sempre ser APROVADO")
    void pagamentoPixDeveSerAprovado() {
        PagamentoRequestDTO request = new PagamentoRequestDTO(10L, new BigDecimal("999.99"), Metodo.PIX);
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo(10L, new BigDecimal("999.99"), Metodo.PIX, Status.APROVADO));

        PagamentoResponseDTO response = paymentService.processarPagamento(request);

        assertThat(response.status()).isEqualTo(Status.APROVADO);

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.APROVADO);
    }

    // -----------------------------------------------------------------------
    // Cenário 2: CARTÃO com valor ≤ R$30 → APROVADO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento via CARTÃO com valor igual ao limite deve ser APROVADO")
    void pagamentoCartaoValorNoLimiteDeveSerAprovado() {
        // Arrange — exatamente no limite: 30.00
        PagamentoRequestDTO request = new PagamentoRequestDTO(20L, new BigDecimal("30.00"), Metodo.CARTAO);
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo(20L, new BigDecimal("30.00"), Metodo.CARTAO, Status.APROVADO));

        PagamentoResponseDTO response = paymentService.processarPagamento(request);

        assertThat(response.status()).isEqualTo(Status.APROVADO);
    }

    @Test
    @DisplayName("Pagamento via CARTÃO com valor abaixo do limite deve ser APROVADO")
    void pagamentoCartaoAbaixoLimiteDeveSerAprovado() {
        PagamentoRequestDTO request = new PagamentoRequestDTO(21L, new BigDecimal("29.99"), Metodo.CARTAO);
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo(21L, new BigDecimal("29.99"), Metodo.CARTAO, Status.APROVADO));

        PagamentoResponseDTO response = paymentService.processarPagamento(request);

        assertThat(response.status()).isEqualTo(Status.APROVADO);
    }

    // -----------------------------------------------------------------------
    // Cenário 3: CARTÃO com valor > R$30 → RECUSADO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento via CARTÃO com valor acima do limite deve ser RECUSADO")
    void pagamentoCartaoAcimaLimiteDeveSerRecusado() {
        PagamentoRequestDTO request = new PagamentoRequestDTO(30L, new BigDecimal("30.01"), Metodo.CARTAO);
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo(30L, new BigDecimal("30.01"), Metodo.CARTAO, Status.RECUSADO));

        PagamentoResponseDTO response = paymentService.processarPagamento(request);

        assertThat(response.status()).isEqualTo(Status.RECUSADO);

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.RECUSADO);
    }

    // -----------------------------------------------------------------------
    // Cenário 4: Confirmação publicada na fila de reservas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve publicar ConfirmacaoMessageDTO na fila de reservas após processar pagamento")
    void devePublicarConfirmacaoNaFilaDeReservas() {
        PagamentoRequestDTO request = new PagamentoRequestDTO(50L, new BigDecimal("20.00"), Metodo.PIX);
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo(50L, new BigDecimal("20.00"), Metodo.PIX, Status.APROVADO));

        paymentService.processarPagamento(request);

        ArgumentCaptor<ConfirmacaoMessageDTO> msgCaptor = ArgumentCaptor.forClass(ConfirmacaoMessageDTO.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.FILA_RESERVAS), msgCaptor.capture());
        assertThat(msgCaptor.getValue().reservaId()).isEqualTo(50L);
        assertThat(msgCaptor.getValue().status()).isEqualTo(Status.APROVADO);
    }

    // -----------------------------------------------------------------------
    // Cenário 5: Response tem os campos corretos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Response deve conter id, reservaId e status do pagamento salvo")
    void responsDeveConterCamposCorretos() {
        PagamentoRequestDTO request = new PagamentoRequestDTO(7L, new BigDecimal("15.00"), Metodo.CARTAO);
        Pagamento salvo = pagamentoSalvo(7L, new BigDecimal("15.00"), Metodo.CARTAO, Status.APROVADO);
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(salvo);

        PagamentoResponseDTO response = paymentService.processarPagamento(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.reservaId()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo(Status.APROVADO);
    }
}
