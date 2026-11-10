package br.edu.ifma.imobiliaria.security;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
public class ControleAcesso {
    private Authentication atual() { return SecurityContextHolder.getContext().getAuthentication(); }
    public boolean equipe() {
        var a=atual();
        return a!=null && a.isAuthenticated() && a.getAuthorities().stream()
            .anyMatch(p -> p.getAuthority().equals("ROLE_ADMIN") || p.getAuthority().equals("ROLE_ATENDENTE"));
    }
    public UsuarioAutenticado usuario() {
        var a=atual();
        if (a==null || !(a.getPrincipal() instanceof UsuarioAutenticado usuario))
            throw new AccessDeniedException("Acesso não permitido.");
        return usuario;
    }
    public Long clienteId() {
        var u=usuario();
        boolean cliente=u.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals("ROLE_CLIENTE"));
        if (!cliente || u.getIdCliente()==null) throw new AccessDeniedException("Usuário sem cliente vinculado.");
        return u.getIdCliente();
    }
    public void exigirProprioCliente(Long idCliente) {
        if (!equipe() && !Objects.equals(clienteId(),idCliente))
            throw new AccessDeniedException("Você só pode acessar as próprias locações.");
    }
}
