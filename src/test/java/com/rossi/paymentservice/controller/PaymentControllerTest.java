package com.rossi.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rossi.paymentservice.dto.request.PagamentoRequestDTO;
import com.rossi.paymentservice.dto.response.PagamentoResponseDTO;
import com.rossi.paymentservice.model.enums.Metodo;
import com.rossi.paymentservice.model.enums.Status;
import com.rossi.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController — Testes de Slice Web")
class PaymentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private static final String URL = "/api/pagamentos";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    // -----------------------------------------------------------------------
    // Cenário 1: Requisição válida → 201 Created
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/pagamentos com dados válidos deve retornar 201 Created")
    void deveRetornar201QuandoRequisicaoValida() throws Exception {
        PagamentoRequestDTO request = new PagamentoRequestDTO(10L, new BigDecimal("25.00"), Metodo.PIX);
        PagamentoResponseDTO responseEsperado = new PagamentoResponseDTO(1L, 10L, Status.APROVADO);
        when(paymentService.processarPagamento(any(PagamentoRequestDTO.class))).thenReturn(responseEsperado);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reservaId").value(10))
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    // -----------------------------------------------------------------------
    // Cenário 2: valor nulo → 400 Bad Request
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/pagamentos com valor nulo deve retornar 400 Bad Request")
    void deveRetornar400QuandoValorNulo() throws Exception {
        String bodyInvalido = """
                {
                    "reservaId": 10,
                    "metodo": "PIX"
                }
                """;

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Cenário 3: valor negativo → 400 Bad Request
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/pagamentos com valor negativo deve retornar 400 Bad Request")
    void deveRetornar400QuandoValorNegativo() throws Exception {
        PagamentoRequestDTO request = new PagamentoRequestDTO(10L, new BigDecimal("-5.00"), Metodo.CARTAO);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Cenário 4: reservaId nulo → 400 Bad Request
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/pagamentos com reservaId nulo deve retornar 400 Bad Request")
    void deveRetornar400QuandoReservaIdNulo() throws Exception {
        String bodyInvalido = """
                {
                    "valor": 25.00,
                    "metodo": "CARTAO"
                }
                """;

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }
}
