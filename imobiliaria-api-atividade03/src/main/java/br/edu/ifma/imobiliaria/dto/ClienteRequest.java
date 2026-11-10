package br.edu.ifma.imobiliaria.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteRequest(
    @NotBlank @Size(max=255) String nomeCliente,
    @NotBlank @Pattern(regexp="[0-9]{11}", message="Informe 11 dígitos, sem pontuação") @CPF(message="CPF inválido") String cpf,
    @NotBlank @Pattern(regexp="[0-9]{10,12}", message="Informe de 10 a 12 dígitos") String telefone,
    @NotBlank @Email @Size(max=100) String email,
    @NotNull @Past LocalDate dtNascimento
) { }
