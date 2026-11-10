package br.edu.ifma.imobiliaria.dto;
import br.edu.ifma.imobiliaria.model.Cliente;

public record ClienteResponse(Long id, String nomeCliente, String telefone, String email) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNomeCliente(), c.getTelefone(), c.getEmail());
    }
}
