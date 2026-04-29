CREATE TABLE pagamentos (id BIGSERIAL PRIMARY KEY,
                         reserva_id BIGINT NOT NULL,
                         valor DECIMAL(10, 2) NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         metodo VARCHAR(20) NOT NULL
);