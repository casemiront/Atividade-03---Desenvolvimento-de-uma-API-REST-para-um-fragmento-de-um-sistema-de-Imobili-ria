package br.edu.ifma.imobiliaria.repository;
import br.edu.ifma.imobiliaria.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil,Long> {
    Optional<Perfil> findByNome(String nome);
    boolean existsByNome(String nome);
}
