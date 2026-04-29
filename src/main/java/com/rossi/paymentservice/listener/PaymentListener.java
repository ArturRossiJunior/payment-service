package com.rossi.paymentservice.listener;

import com.rossi.paymentservice.config.RabbitMQConfig;
import com.rossi.paymentservice.dto.PagamentoMessageDTO;
import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.FILA_PAGAMENTOS)
    public void processarPagamento(PagamentoMessageDTO mensagem) {
        log.info("Processando pagamento da reserva: {} | Usuário: {}", mensagem.reservaId(), mensagem.usuarioId());

        PagamentoRequestDTO request = new PagamentoRequestDTO(
                mensagem.reservaId(),
                mensagem.valor(),
                mensagem.metodo()
        );

        paymentService.processarPagamento(request);
        log.info("Pagamento da reserva {} processado com sucesso!", mensagem.reservaId());
    }
}