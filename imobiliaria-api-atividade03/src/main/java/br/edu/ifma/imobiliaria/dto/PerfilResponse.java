package br.edu.ifma.imobiliaria.dto;
import br.edu.ifma.imobiliaria.model.Perfil;
public record PerfilResponse(Long id,String nome,String descricao) {
    public static PerfilResponse from(Perfil p) { return new PerfilResponse(p.getId(),p.getNome(),p.getDescricao()); }
}
