package br.edu.ifma.imobiliaria.repository;

import br.edu.ifma.imobiliaria.model.Imovel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Imovel i where i.id = :id")
    Optional<Imovel> buscarParaAtualizacao(@Param("id") Long id);
}
