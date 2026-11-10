package br.edu.ifma.imobiliaria.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record UsuarioRequest(
    @NotBlank @Pattern(regexp="[a-z0-9._-]{3,60}",message="Use 3 a 60 letras minúsculas, números, ponto, hífen ou sublinhado") String login,
    @NotBlank @Size(max=100) String nome,
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(min=8,max=64) String senha,
    @NotNull Boolean ativo,
    @Positive Long idCliente,
    @NotEmpty Set<@NotBlank @Size(max=40) String> perfis
) { }
