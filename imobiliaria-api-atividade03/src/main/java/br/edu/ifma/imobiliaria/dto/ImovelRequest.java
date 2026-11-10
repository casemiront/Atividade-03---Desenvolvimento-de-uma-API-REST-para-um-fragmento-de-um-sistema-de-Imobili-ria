package br.edu.ifma.imobiliaria.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ImovelRequest(
    @NotBlank @Size(max=50) String tipoImovel,
    @NotBlank @Size(max=255) String endereco,
    @NotBlank @Pattern(regexp="[0-9]{8}", message="Informe 8 dígitos, sem pontuação") String cep,
    @NotNull @PositiveOrZero Integer dormitorios,
    @NotNull @PositiveOrZero Integer banheiros,
    @NotNull @PositiveOrZero Integer suites,
    @NotNull @Positive Integer metragem,
    @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal valorAluguelSug,
    @NotBlank @Size(max=100) String cidade,
    @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal valorDiaria,
    @Size(max=2000) String obs
) { }
