package br.edu.ifma.imobiliaria.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record UsuarioAtualizacaoRequest(
    @NotBlank @Size(max=100) String nome,
    @NotNull Boolean ativo,
    @Positive Long idCliente,
    @NotEmpty Set<@NotBlank @Size(max=40) String> perfis
) { }
