package br.edu.ifma.imobiliaria.security;
import br.edu.ifma.imobiliaria.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    public CustomUserDetailsService(UsuarioRepository usuarios) { this.usuarios=usuarios; }
    @Override
    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String login) {
        var usuario=usuarios.findByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
        return new UsuarioAutenticado(usuario);
    }
}
