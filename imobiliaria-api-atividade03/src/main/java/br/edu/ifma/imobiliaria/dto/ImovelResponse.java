package br.edu.ifma.imobiliaria.dto;
import br.edu.ifma.imobiliaria.model.Imovel;
import java.math.BigDecimal;

public record ImovelResponse(Long id, String tipoImovel, String endereco, String cep,
        Integer dormitorios, Integer banheiros, Integer suites, Integer metragem,
        BigDecimal valorAluguelSug, String cidade, BigDecimal valorDiaria, String obs) {
    public static ImovelResponse from(Imovel i) {
        return new ImovelResponse(i.getId(),i.getTipoImovel(),i.getEndereco(),i.getCep(),
            i.getDormitorios(),i.getBanheiros(),i.getSuites(),i.getMetragem(),
            i.getValorAluguelSug(),i.getCidade(),i.getValorDiaria(),i.getObs());
    }
}
