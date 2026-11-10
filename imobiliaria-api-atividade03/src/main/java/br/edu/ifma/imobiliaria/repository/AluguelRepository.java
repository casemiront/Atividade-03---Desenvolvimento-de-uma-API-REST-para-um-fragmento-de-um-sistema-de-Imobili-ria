package br.edu.ifma.imobiliaria.repository;
import br.edu.ifma.imobiliaria.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {
    java.util.List<Aluguel> findByLocacaoInquilinoIdOrderById(Long id);
    boolean existsByLocacaoId(Long id);
    boolean existsByLocacaoIdAndDtVencimentoAndIdNot(Long id, java.time.LocalDate data, Long ignorarId);
}
