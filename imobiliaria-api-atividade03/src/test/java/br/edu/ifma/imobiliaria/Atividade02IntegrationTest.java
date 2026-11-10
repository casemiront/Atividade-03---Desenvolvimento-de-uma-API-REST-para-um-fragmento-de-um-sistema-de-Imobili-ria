package br.edu.ifma.imobiliaria;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
@org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
class Atividade02IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    ObjectNode dadosImovel() {
        return json.createObjectNode().put("tipoImovel","Casa").put("endereco","Rua de Teste")
            .put("cep","65000000").put("dormitorios",2).put("banheiros",1).put("suites",0)
            .put("metragem",80).put("valorAluguelSug",1800).put("cidade","Cidade Teste")
            .put("valorDiaria",125.50).put("obs","Teste");
    }
    long criar(String recurso, ObjectNode dados) throws Exception {
        String r=mvc.perform(post("/api/"+recurso).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).get("id").asLong();
    }
    ObjectNode locacao(long imovel, String inicio, String fim) {
        return json.createObjectNode().put("idImovel",imovel).put("idInquilino",1).put("ativo",true)
            .put("dataInicio",inicio).put("dataFim",fim).put("diaVencimento",10).put("percMulta",2);
    }
    ObjectNode aluguel(long locacao) {
        return json.createObjectNode().put("idLocacao",locacao).put("dtVencimento","2099-01-10")
            .put("valorPago",0).put("obs","Pendente");
    }
    long imovel() throws Exception { return criar("imoveis",dadosImovel()); }
    void conflito(ObjectNode dados) throws Exception {
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("indisponível")));
    }
    @Test void paginaDadosPreviamenteCadastrados() throws Exception {
        mvc.perform(get("/api/imoveis/paginados?page=0&size=5"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.totalElements").value(12))
            .andExpect(jsonPath("$.totalPages").value(3)).andExpect(jsonPath("$.first").value(true));
        mvc.perform(get("/api/imoveis/paginados?page=2&size=5"))
            .andExpect(jsonPath("$.content.length()").value(2)).andExpect(jsonPath("$.last").value(true));
    }
    @Test void paginaForaDoResultadoRetornaVazia() throws Exception {
        mvc.perform(get("/api/imoveis/paginados?page=99&size=5"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }
    @Test void qbeCombinaFiltrosParciaisIgnorandoCaixa() throws Exception {
        mvc.perform(get("/api/imoveis/pesquisa").param("tipo","aPaR").param("cidade","LUÍ").param("quartos","2"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].cidade").value("São Luís"));
    }
    @Test void qbeSemFiltrosOuComTextoVazioNaoFiltraValoresNulos() throws Exception {
        mvc.perform(get("/api/imoveis/pesquisa").param("cidade","  "))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(12));
    }
    @Test void qbeQuartosZeroEhFiltroValido() throws Exception {
        mvc.perform(get("/api/imoveis/pesquisa?quartos=0"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test void qbeSemResultadoEh200() throws Exception {
        mvc.perform(get("/api/imoveis/pesquisa?cidade=NenhumaCidade"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test void ordenaPeloValorDaDiaria() throws Exception {
        mvc.perform(get("/api/imoveis/paginados?ordenar=valorDiaria&direcao=desc&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].valorDiaria").value(350));
    }
    @Test void parametrosInvalidosSao400() throws Exception {
        for(String query:new String[]{"page=-1","size=0","size=101","page=abc","quartos=-1","quartos=abc","ordenar=inexistente","direcao=errada"}) {
            mvc.perform(get("/api/imoveis/pesquisa?"+query)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
        }
    }
    @Test void calculaTresDiariasComBigDecimal() throws Exception {
        long id=criar("locacoes",locacao(imovel(),"2099-01-10","2099-01-13"));
        mvc.perform(get("/api/locacoes/"+id)).andExpect(jsonPath("$.quantidadeDiarias").value(3))
            .andExpect(jsonPath("$.valorDiariaAplicada").value(125.50))
            .andExpect(jsonPath("$.valorAluguel").value(376.50));
    }
    @Test void calculaUmaDiariaEPrazoEntreMeses() throws Exception {
        long imovel=imovel();
        long id=criar("locacoes",locacao(imovel,"2099-01-31","2099-02-01"));
        mvc.perform(get("/api/locacoes/"+id)).andExpect(jsonPath("$.quantidadeDiarias").value(1))
            .andExpect(jsonPath("$.valorAluguel").value(125.50));
    }
    @Test void naoAceitaValorTotalEnviadoPeloCliente() throws Exception {
        ObjectNode dados=locacao(imovel(),"2099-01-10","2099-01-13").put("valorAluguel",1);
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isBadRequest());
        dados.remove("valorAluguel"); dados.put("valorTotal",1);
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isBadRequest());
    }
    @Test void datasIguaisNaoGeramLocacao() throws Exception {
        ObjectNode dados=locacao(imovel(),"2099-01-10","2099-01-10");
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").value("A data final deve ser posterior à data inicial."));
    }
    @Test void impedeTodasAsFormasDeSobreposicao() throws Exception {
        long imovel=imovel(); criar("locacoes",locacao(imovel,"2099-01-10","2099-01-20"));
        for(String[] periodo:new String[][]{{"2099-01-10","2099-01-20"},{"2099-01-11","2099-01-19"},
                {"2099-01-09","2099-01-21"},{"2099-01-09","2099-01-11"},{"2099-01-19","2099-01-21"}}) {
            conflito(locacao(imovel,periodo[0],periodo[1]));
        }
    }
    @Test void aceitaPeriodosConsecutivosEImoveisDiferentes() throws Exception {
        long imovel=imovel(); criar("locacoes",locacao(imovel,"2099-01-10","2099-01-20"));
        criar("locacoes",locacao(imovel,"2099-01-01","2099-01-10"));
        criar("locacoes",locacao(imovel,"2099-01-20","2099-01-21"));
        criar("locacoes",locacao(imovel(),"2099-01-10","2099-01-20"));
    }
    @Test void canceladaNaoBloqueiaMasReativacaoConflitanteFalha() throws Exception {
        long imovel=imovel(); ObjectNode dados=locacao(imovel,"2099-01-10","2099-01-20").put("ativo",false);
        long cancelada=criar("locacoes",dados);
        criar("locacoes",locacao(imovel,"2099-01-10","2099-01-20"));
        dados.put("ativo",true);
        mvc.perform(put("/api/locacoes/"+cancelada).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/locacoes/"+cancelada)).andExpect(jsonPath("$.ativo").value(false));
    }
    @Test void atualizarIgnoraProprioIdERecalculaPeriodo() throws Exception {
        ObjectNode dados=locacao(imovel(),"2099-01-10","2099-01-13"); long id=criar("locacoes",dados);
        mvc.perform(put("/api/locacoes/"+id).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isOk());
        dados.put("dataFim","2099-01-14");
        mvc.perform(put("/api/locacoes/"+id).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorAluguel").value(502));
    }
    @Test void mudancaDeImovelTambemVerificaConflito() throws Exception {
        long um=imovel(),dois=imovel();
        criar("locacoes",locacao(um,"2099-01-10","2099-01-20"));
        ObjectNode dados=locacao(dois,"2099-01-10","2099-01-20"); long id=criar("locacoes",dados);
        dados.put("idImovel",um);
        mvc.perform(put("/api/locacoes/"+id).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isConflict());
    }
    @Test void diariaContratadaNaoMudaAoEditarObservacao() throws Exception {
        long imovel=imovel(); ObjectNode dados=locacao(imovel,"2099-01-10","2099-01-13");
        long id=criar("locacoes",dados);
        mvc.perform(put("/api/imoveis/"+imovel).contentType(MediaType.APPLICATION_JSON)
            .content(dadosImovel().put("valorDiaria",200).toString())).andExpect(status().isOk());
        dados.put("obs","Atualização");
        mvc.perform(put("/api/locacoes/"+id).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorAluguel").value(376.50));
    }
    @Test void bloqueiaExclusaoAtivaEFutura() throws Exception {
        LocalDate hoje=LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        for(int dias:new int[]{-1,10}) {
            long imovel=imovel(); criar("locacoes",locacao(imovel,hoje.plusDays(dias).toString(),hoje.plusDays(dias+5).toString()));
            mvc.perform(delete("/api/imoveis/"+imovel)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("ativa ou futura")));
        }
    }
    @Test void preservaHistoricoEPermiteExcluirDepoisDosVinculos() throws Exception {
        long imovel=imovel(); long locacao=criar("locacoes",locacao(imovel,"2020-01-10","2020-01-13"));
        mvc.perform(delete("/api/imoveis/"+imovel)).andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("histórico")));
        mvc.perform(delete("/api/locacoes/"+locacao)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/imoveis/"+imovel)).andExpect(status().isNoContent());
    }
    @Test void crudAlugueisEProtecaoDaLocacao() throws Exception {
        long locacao=criar("locacoes",locacao(imovel(),"2099-01-10","2099-01-13"));
        ObjectNode dados=aluguel(locacao); long id=criar("alugueis",dados);
        mvc.perform(get("/api/alugueis")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/alugueis/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.idLocacao").value(locacao));
        dados.put("valorPago",376.50);
        mvc.perform(put("/api/alugueis/"+id).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorPago").value(376.50));
        mvc.perform(delete("/api/locacoes/"+locacao)).andExpect(status().isConflict());
        mvc.perform(delete("/api/alugueis/"+id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/alugueis/"+id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/locacoes/"+locacao)).andExpect(status().isNoContent());
    }
    @Test void rejeitaAluguelDuplicadoENegativo() throws Exception {
        long locacao=criar("locacoes",locacao(imovel(),"2099-01-10","2099-01-13"));
        ObjectNode dados=aluguel(locacao); criar("alugueis",dados);
        mvc.perform(post("/api/alugueis").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isConflict());
        dados.put("valorPago",-1);
        mvc.perform(post("/api/alugueis").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valorPago").exists());
    }
    @Test void rejeitaAluguelComLocacaoInexistente() throws Exception {
        mvc.perform(post("/api/alugueis").contentType(MediaType.APPLICATION_JSON).content(aluguel(999999).toString()))
            .andExpect(status().isNotFound());
    }
    @Test void validaCidadeEDiaria() throws Exception {
        ObjectNode dados=dadosImovel().put("cidade","").put("valorDiaria",0);
        mvc.perform(post("/api/imoveis").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cidade").exists())
            .andExpect(jsonPath("$.campos.valorDiaria").exists());
    }
    @Test void migrationsNovasAplicadas() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"success\"=TRUE AND \"version\" IS NOT NULL",Integer.class)).isEqualTo(5);
    }
}
