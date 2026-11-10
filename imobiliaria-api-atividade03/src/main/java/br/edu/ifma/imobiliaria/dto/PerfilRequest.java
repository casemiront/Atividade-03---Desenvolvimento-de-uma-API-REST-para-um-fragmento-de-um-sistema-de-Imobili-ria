package br.edu.ifma.imobiliaria.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record PerfilRequest(
    @NotBlank @Pattern(regexp="[A-Z][A-Z_]{2,39}") String nome,
    @NotBlank @Size(max=200) String descricao
) { }
