package br.edu.ifma.imobiliaria.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record SenhaRequest(
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(min=8,max=64) String senha
) { }
