package br.edu.ifma.imobiliaria.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelRequest(
    @NotNull @Positive Long idLocacao,
    @NotNull LocalDate dtVencimento,
    @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal valorPago,
    @Size(max=2000) String obs
) { }
