package com.rossi.paymentservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_PAGAMENTOS = "pagamentos.fila";
    public static final String FILA_RESERVAS   = "reservas.fila";

    @Bean
    public Queue filaPagamentos() {
        return new Queue(FILA_PAGAMENTOS, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}