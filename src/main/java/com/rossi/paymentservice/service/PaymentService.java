package com.rossi.paymentservice.service;

import com.rossi.paymentservice.config.RabbitMQConfig;
import com.rossi.paymentservice.dto.ConfirmacaoMessageDTO;
import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.dto.response.PagamentoResponseDTO;
import com.rossi.paymentservice.model.enums.Metodo;
import com.rossi.paymentservice.model.Pagamento;
import com.rossi.paymentservice.model.enums.Status;
import com.rossi.paymentservice.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal LIMITE_APROVACAO_CARTAO = new BigDecimal("30");

    private final PagamentoRepository pagamentoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PagamentoResponseDTO processarPagamento(PagamentoRequestDTO request) {
        Status statusCalculado;

        if (request.metodo() == Metodo.PIX) {
            statusCalculado = Status.APROVADO;
        } else if (request.valor().compareTo(LIMITE_APROVACAO_CARTAO) > 0) {
            statusCalculado = Status.RECUSADO;
        } else {
            statusCalculado = Status.APROVADO;
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setReservaId(request.reservaId());
        pagamento.setValor(request.valor());
        pagamento.setMetodo(request.metodo());
        pagamento.setStatus(statusCalculado);

        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);

        ConfirmacaoMessageDTO confirmacao = new ConfirmacaoMessageDTO(
                pagamentoSalvo.getReservaId(),
                pagamentoSalvo.getStatus());

        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_RESERVAS, confirmacao);

        return converterParaResponseDTO(pagamentoSalvo);
    }

    private PagamentoResponseDTO converterParaResponseDTO(Pagamento pagamento) {
        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getReservaId(),
                pagamento.getStatus());
    }
}
