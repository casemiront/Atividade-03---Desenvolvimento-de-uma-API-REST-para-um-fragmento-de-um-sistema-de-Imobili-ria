package br.edu.ifma.imobiliaria.dto;
import br.edu.ifma.imobiliaria.model.Aluguel;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelResponse(Long id, Long idLocacao, LocalDate dtVencimento,
        BigDecimal valorPago, String obs) {
    public static AluguelResponse from(Aluguel a) {
        return new AluguelResponse(a.getId(), a.getLocacao().getId(),
            a.getDtVencimento(), a.getValorPago(), a.getObs());
    }
}
