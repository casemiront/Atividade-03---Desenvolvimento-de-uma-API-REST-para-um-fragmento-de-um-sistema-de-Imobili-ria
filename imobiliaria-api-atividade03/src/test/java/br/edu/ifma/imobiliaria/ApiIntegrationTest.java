package br.edu.ifma.imobiliaria;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
@org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    ObjectNode cliente() throws Exception {
        return (ObjectNode) json.readTree("""
          {"nomeCliente":"Cliente Demonstração","cpf":"52998224725","telefone":"98999990000",
           "email":"cliente@example.com","dtNascimento":"1998-04-15"}
        """);
    }
    ObjectNode imovel() throws Exception {
        return (ObjectNode) json.readTree("""
          {"tipoImovel":"Apartamento","endereco":"Rua de Exemplo, 100","cep":"65000000",
           "dormitorios":2,"banheiros":2,"suites":1,"metragem":70,"valorAluguelSug":1500.00,"cidade":"São Luís","valorDiaria":150.00,"obs":"Exemplo"}
        """);
    }
    long criar(String recurso, ObjectNode corpo) throws Exception {
        String resposta = mvc.perform(post("/api/"+recurso).contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asLong();
    }
    ObjectNode locacao() throws Exception {
        ObjectNode corpo = (ObjectNode) json.readTree("""
          {"ativo":true,"dataInicio":"2026-10-01","dataFim":"2027-09-30",
           "diaVencimento":10,"percMulta":2.00,"obs":"Contrato de exemplo"}
        """);
        corpo.put("idInquilino",criar("clientes",cliente()));
        corpo.put("idImovel",criar("imoveis",imovel()));
        return corpo;
    }
    void crud(String recurso, ObjectNode corpo, String campo, String valor) throws Exception {
        long id = criar(recurso,corpo);
        String url = "/api/"+recurso+"/"+id;
        mvc.perform(get("/api/"+recurso)).andExpect(status().isOk()).andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem((int) id)));
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        corpo.put(campo,valor);
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$."+campo).value(valor));
        mvc.perform(get(url)).andExpect(jsonPath("$."+campo).value(valor));
        mvc.perform(delete(url)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(url)).andExpect(status().isNotFound());
    }
    @Test void crudClientes() throws Exception { crud("clientes",cliente(),"nomeCliente","Cliente Atualizado"); }
    @Test void crudImoveis() throws Exception { crud("imoveis",imovel(),"endereco","Rua Nova, 200"); }
    @Test void crudLocacoes() throws Exception { crud("locacoes",locacao(),"obs","Contrato atualizado"); }
    @Test void migrationFoiAplicada() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"success\"=TRUE AND \"version\"='1'", Integer.class)).isEqualTo(1);
    }
    @Test void rejeitaClienteInvalido() throws Exception {
        ObjectNode corpo=cliente().put("nomeCliente","").put("cpf","11111111111").put("email","invalido").put("dtNascimento","2999-01-01");
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nomeCliente").exists())
            .andExpect(jsonPath("$.campos.cpf").exists()).andExpect(jsonPath("$.campos.email").exists())
            .andExpect(jsonPath("$.campos.dtNascimento").exists());
    }
    @Test void rejeitaCpfDuplicado() throws Exception {
        criar("clientes",cliente());
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cliente().toString())).andExpect(status().isConflict());
    }
    @Test void rejeitaImovelInvalido() throws Exception {
        ObjectNode corpo=imovel().put("valorAluguelSug",-1).put("metragem",0).put("cep","abc");
        mvc.perform(post("/api/imoveis").contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valorAluguelSug").exists())
            .andExpect(jsonPath("$.campos.metragem").exists()).andExpect(jsonPath("$.campos.cep").exists());
    }
    @Test void rejeitaDatasInvertidas() throws Exception {
        ObjectNode corpo=locacao().put("dataFim","2020-01-01");
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").value("A data final deve ser posterior à data inicial."));
    }
    @Test void rejeitaImovelInexistente() throws Exception {
        ObjectNode corpo=locacao().put("idImovel",999999);
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(corpo.toString())).andExpect(status().isNotFound());
    }
    @Test void rejeitaInquilinoInexistente() throws Exception {
        ObjectNode corpo=locacao().put("idInquilino",999999);
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(corpo.toString())).andExpect(status().isNotFound());
    }
    @Test void rejeitaVencimentoEMultaInvalidos() throws Exception {
        ObjectNode corpo=locacao().put("diaVencimento",32).put("percMulta",101);
        mvc.perform(post("/api/locacoes").contentType(MediaType.APPLICATION_JSON).content(corpo.toString()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.diaVencimento").exists())
            .andExpect(jsonPath("$.campos.percMulta").exists());
    }
    @Test void impedeExcluirClienteVinculado() throws Exception {
        ObjectNode corpo=locacao(); criar("locacoes",corpo);
        mvc.perform(delete("/api/clientes/"+corpo.get("idInquilino").asLong())).andExpect(status().isConflict());
    }
    @Test void impedeExcluirImovelVinculado() throws Exception {
        ObjectNode corpo=locacao(); criar("locacoes",corpo);
        mvc.perform(delete("/api/imoveis/"+corpo.get("idImovel").asLong())).andExpect(status().isConflict());
    }
    @Test void rejeitaJsonETipoInvalido() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{quebrado")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/clientes/abc")).andExpect(status().isBadRequest());
    }
    @Test void atualizarAusenteNaoCriaRegistro() throws Exception {
        mvc.perform(put("/api/clientes/999999").contentType(MediaType.APPLICATION_JSON).content(cliente().toString())).andExpect(status().isNotFound());
        mvc.perform(put("/api/imoveis/999999").contentType(MediaType.APPLICATION_JSON).content(imovel().toString())).andExpect(status().isNotFound());
        mvc.perform(put("/api/locacoes/999999").contentType(MediaType.APPLICATION_JSON).content(locacao().toString())).andExpect(status().isNotFound());
    }
    @Test void rejeitaCamposObrigatoriosAusentes() throws Exception {
        for(String recurso: new String[]{"clientes","imoveis","locacoes","alugueis"}) {
            mvc.perform(post("/api/"+recurso).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isMap());
        }
    }
}
