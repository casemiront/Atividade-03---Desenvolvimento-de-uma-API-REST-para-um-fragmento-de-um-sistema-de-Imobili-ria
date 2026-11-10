package br.edu.ifma.imobiliaria.security;
import br.edu.ifma.imobiliaria.model.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UsuarioAutenticado extends User {
    private final Long idUsuario;
    private final Long idCliente;
    public UsuarioAutenticado(Usuario u) {
        super(u.getLogin(),u.getSenhaHash(),u.isAtivo(),true,true,true,
            u.getPerfis().stream().map(p -> new SimpleGrantedAuthority("ROLE_"+p.getNome())).toList());
        idUsuario=u.getId();
        idCliente=u.getCliente()==null ? null : u.getCliente().getId();
    }
    public Long getIdUsuario() { return idUsuario; }
    public Long getIdCliente() { return idCliente; }
}
