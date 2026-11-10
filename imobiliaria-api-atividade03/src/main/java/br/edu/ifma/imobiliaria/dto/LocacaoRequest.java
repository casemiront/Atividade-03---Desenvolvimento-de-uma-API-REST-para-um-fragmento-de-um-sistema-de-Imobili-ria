package br.edu.ifma.imobiliaria.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LocacaoRequest(
    @NotNull @Positive Long idImovel,
    @NotNull @Positive Long idInquilino,
    @NotNull Boolean ativo,
    @NotNull LocalDate dataFim,
    @NotNull LocalDate dataInicio,
    @NotNull @Min(1) @Max(31) Integer diaVencimento,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer=3, fraction=2) BigDecimal percMulta,
    @Size(max=2000) String obs
) { }
