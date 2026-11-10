package br.edu.ifma.imobiliaria.repository;
import br.edu.ifma.imobiliaria.model.Usuario;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    @EntityGraph(attributePaths={"perfis","cliente"})
    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByPerfisId(Long id);
    boolean existsByClienteId(Long id);
}
