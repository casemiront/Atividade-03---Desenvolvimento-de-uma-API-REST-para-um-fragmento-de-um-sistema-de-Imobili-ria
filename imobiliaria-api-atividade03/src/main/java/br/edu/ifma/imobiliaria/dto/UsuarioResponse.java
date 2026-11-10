package br.edu.ifma.imobiliaria.dto;
import br.edu.ifma.imobiliaria.model.Usuario;
import java.util.List;
public record UsuarioResponse(Long id,String login,String nome,boolean ativo,Long idCliente,List<String> perfis) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(),u.getLogin(),u.getNome(),u.isAtivo(),
            u.getCliente()==null?null:u.getCliente().getId(),u.getPerfis().stream().map(p->p.getNome()).sorted().toList());
    }
}
