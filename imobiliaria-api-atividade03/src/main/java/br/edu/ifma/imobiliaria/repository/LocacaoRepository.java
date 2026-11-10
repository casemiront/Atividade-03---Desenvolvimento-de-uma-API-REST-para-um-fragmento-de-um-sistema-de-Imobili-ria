package br.edu.ifma.imobiliaria.repository;

import br.edu.ifma.imobiliaria.model.Locacao;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
    java.util.List<Locacao> findByInquilinoIdOrderById(Long id);
    boolean existsByInquilinoId(Long id);
    boolean existsByImovelId(Long id);
    boolean existsByImovelIdAndAtivoTrueAndDataFimAfter(Long id, LocalDate hoje);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Locacao l where l.id = :id")
    Optional<Locacao> buscarParaAtualizacao(@Param("id") Long id);

    @Query("""
        select count(l) > 0 from Locacao l
        where l.imovel.id = :imovelId and l.ativo = true
          and l.dataInicio < :fim and l.dataFim > :inicio
          and (:ignorarId is null or l.id <> :ignorarId)
        """)
    boolean existeConflito(@Param("imovelId") Long imovelId,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
            @Param("ignorarId") Long ignorarId);
}
