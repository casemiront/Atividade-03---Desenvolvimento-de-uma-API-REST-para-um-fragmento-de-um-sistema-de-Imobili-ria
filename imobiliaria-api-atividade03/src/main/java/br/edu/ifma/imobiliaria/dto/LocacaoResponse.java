package br.edu.ifma.imobiliaria.dto;

import br.edu.ifma.imobiliaria.model.Locacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LocacaoResponse(Long id, Long idImovel, Long idInquilino, Boolean ativo,
        LocalDate dataFim, LocalDate dataInicio, Integer diaVencimento,
        BigDecimal percMulta, BigDecimal valorAluguel, String obs,
        long quantidadeDiarias, BigDecimal valorDiariaAplicada) {
    public static LocacaoResponse from(Locacao l) {
        return new LocacaoResponse(l.getId(), l.getImovel().getId(), l.getInquilino().getId(),
            l.getAtivo(), l.getDataFim(), l.getDataInicio(), l.getDiaVencimento(),
            l.getPercMulta(), l.getValorAluguel(), l.getObs(),
            ChronoUnit.DAYS.between(l.getDataInicio(), l.getDataFim()), l.getValorDiariaAplicada());
    }
}
