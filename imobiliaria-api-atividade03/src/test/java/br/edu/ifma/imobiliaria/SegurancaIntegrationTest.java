package br.edu.ifma.imobiliaria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:seguranca;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class SegurancaIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    String senha(String login) {
        return switch(login) { case "admin" -> "Admin@12345";case "atendente" -> "Atendente@12345";default -> "Cliente@12345"; };
    }
    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request,String login) {
        return request.with(httpBasic(login,senha(login)));
    }
    ObjectNode imovel() {
        return json.createObjectNode().put("tipoImovel","Casa").put("endereco","Rua Teste").put("cep","65000000")
            .put("dormitorios",2).put("banheiros",1).put("suites",0).put("metragem",50)
            .put("valorAluguelSug",1000).put("cidade","São Luís").put("valorDiaria",100);
    }
    ObjectNode locacao(long cliente,long imovel) {
        return json.createObjectNode().put("idInquilino",cliente).put("idImovel",imovel).put("ativo",true)
            .put("dataInicio","2099-02-10").put("dataFim","2099-02-13").put("diaVencimento",10).put("percMulta",2);
    }
    JsonNode criar(String recurso,ObjectNode dados,String login) throws Exception {
        var r=mvc.perform(auth(post("/api/"+recurso),login).contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(r);
    }
    long criarImovel() throws Exception { return criar("imoveis",imovel(),"admin").get("id").asLong(); }
    ObjectNode usuario(String login,String perfil,Long cliente) {
        var d=json.createObjectNode().put("login",login).put("nome","Usuário Teste").put("senha","Teste@12345").put("ativo",true);
        if(cliente!=null)d.put("idCliente",cliente);
        d.putArray("perfis").add(perfil);return d;
    }
    @ParameterizedTest
    @ValueSource(strings={"/api/clientes","/api/imoveis","/api/imoveis/paginados","/api/imoveis/pesquisa","/api/locacoes","/api/alugueis","/api/usuarios","/api/perfis"})
    void todasAsRotasExigemAutenticacao(String rota) throws Exception {
        mvc.perform(get(rota)).andExpect(status().isUnauthorized()).andExpect(header().string("WWW-Authenticate","Basic realm=\"Imobiliaria\""))
            .andExpect(jsonPath("$.status").value(401));
    }
    @Test void credenciaisInvalidasNaoIdentificamSeLoginExiste() throws Exception {
        for(String login:List.of("admin","nao-existe")) {
            mvc.perform(get("/api/imoveis").with(httpBasic(login,"SenhaErrada")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensagem").value("Autenticação necessária ou credenciais inválidas."));
        }
    }
    @ParameterizedTest @ValueSource(strings={"admin","atendente","ana","bruno"})
    void perfisValidosConsultamImoveis(String login) throws Exception {
        mvc.perform(auth(get("/api/imoveis/paginados?size=5"),login)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(5));
        mvc.perform(auth(get("/api/imoveis/pesquisa?tipo=apar&quartos=2"),login)).andExpect(status().isOk());
    }
    @Test void semSessaoNaoReutilizaLoginAnterior() throws Exception {
        mvc.perform(auth(get("/api/imoveis"),"admin")).andExpect(status().isOk()).andExpect(header().doesNotExist("Set-Cookie"));
        mvc.perform(get("/api/imoveis")).andExpect(status().isUnauthorized());
    }
    @ParameterizedTest @ValueSource(strings={"ana","atendente"})
    void naoAdminNaoGerenciaUsuariosNemPerfis(String login) throws Exception {
        for(String recurso:List.of("usuarios","perfis")) {
            for(var req:List.of(get("/api/"+recurso),get("/api/"+recurso+"/1"),
                    post("/api/"+recurso),put("/api/"+recurso+"/1"),delete("/api/"+recurso+"/1"))) {
                mvc.perform(auth(req,login).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
            }
            mvc.perform(auth(put("/api/usuarios/1/senha"),login).contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"Ataque@12345\"}"))
                .andExpect(status().isForbidden());
        }
    }
    @Test void cadastroAnonimoNaoPodeEscolherAdmin() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(usuario("invasor","ADMIN",null).toString()))
            .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("select count(*) from usuarios where login='invasor'",Integer.class)).isZero();
    }
    @Test void clienteNaoCadastraClientesOuImoveis() throws Exception {
        for(String recurso:List.of("clientes","imoveis")) {
            var dados=recurso.equals("imoveis")?imovel():json.createObjectNode()
                .put("nomeCliente","Cliente Teste").put("cpf","11144477735").put("telefone","98999990000")
                .put("email","teste@example.com").put("dtNascimento","1990-01-01");
            mvc.perform(auth(post("/api/"+recurso),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isForbidden());
            mvc.perform(auth(put("/api/"+recurso+"/1"),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isForbidden());
        }
        mvc.perform(auth(get("/api/clientes"),"ana")).andExpect(status().isForbidden());
    }
    @ParameterizedTest @ValueSource(strings={"ana","atendente"})
    void exclusoesRestritasAoAdmin(String login) throws Exception {
        for(String recurso:List.of("clientes","imoveis","locacoes","alugueis"))
            mvc.perform(auth(delete("/api/"+recurso+"/1"),login)).andExpect(status().isForbidden());
    }
    @Test void atendenteCadastraEAtualizaImovel() throws Exception {
        var id=criar("imoveis",imovel(),"atendente").get("id").asLong();
        mvc.perform(auth(put("/api/imoveis/"+id),"atendente").contentType(MediaType.APPLICATION_JSON).content(imovel().put("cidade","Raposa").toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.cidade").value("Raposa"));
    }
    @Test void clienteCriaSuaLocacaoMasNaoDeOutraPessoa() throws Exception {
        long id=criar("locacoes",locacao(1,criarImovel()),"ana").get("id").asLong();
        mvc.perform(auth(get("/api/locacoes/"+id),"ana")).andExpect(status().isOk());
        mvc.perform(auth(post("/api/locacoes"),"ana").contentType(MediaType.APPLICATION_JSON).content(locacao(2,criarImovel()).toString()))
            .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/locacoes/"+id),"bruno")).andExpect(status().isForbidden());
    }
    @Test void listaDoClienteEhFiltradaNoBanco() throws Exception {
        long ana=criar("locacoes",locacao(1,criarImovel()),"admin").get("id").asLong();
        long bruno=criar("locacoes",locacao(2,criarImovel()),"atendente").get("id").asLong();
        mvc.perform(auth(get("/api/locacoes"),"ana")).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(ana));
        mvc.perform(auth(get("/api/locacoes"),"bruno")).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(bruno));
        mvc.perform(auth(get("/api/locacoes"),"atendente")).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(auth(get("/api/locacoes"),"admin")).andExpect(jsonPath("$.length()").value(2));
    }
    @Test void clienteNaoAlteraRegistroAlheioMesmoTrocandoInquilino() throws Exception {
        long imovel=criarImovel();
        long id=criar("locacoes",locacao(2,imovel),"bruno").get("id").asLong();
        mvc.perform(auth(put("/api/locacoes/"+id),"ana").contentType(MediaType.APPLICATION_JSON).content(locacao(1,imovel).toString()))
            .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("select id_inquilino from locacao where id=?",Long.class,id)).isEqualTo(2);
    }
    @Test void clienteAtualizaPropriaLocacaoMasNaoTransfereTitularidade() throws Exception {
        long imovel=criarImovel();var dados=locacao(1,imovel);
        long id=criar("locacoes",dados,"ana").get("id").asLong();
        dados.put("obs","Atualização da Ana");
        mvc.perform(auth(put("/api/locacoes/"+id),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.obs").value("Atualização da Ana"));
        dados.put("idInquilino",2);
        mvc.perform(auth(put("/api/locacoes/"+id),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString()))
            .andExpect(status().isForbidden());
    }
    @Test void protecaoIncluiAlugueisDaLocacao() throws Exception {
        long locacao=criar("locacoes",locacao(2,criarImovel()),"admin").get("id").asLong();
        var dados=json.createObjectNode().put("idLocacao",locacao).put("dtVencimento","2099-02-10").put("valorPago",100);
        long id=criar("alugueis",dados,"atendente").get("id").asLong();
        mvc.perform(auth(get("/api/alugueis/"+id),"ana")).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/alugueis"),"ana")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(auth(get("/api/alugueis/"+id),"bruno")).andExpect(status().isOk());
        mvc.perform(auth(post("/api/alugueis"),"bruno").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isForbidden());
        mvc.perform(auth(put("/api/alugueis/"+id),"bruno").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isForbidden());
    }
    @Test void dtoNaoPermiteIdTotalOuPerfisExtras() throws Exception {
        mvc.perform(auth(post("/api/imoveis"),"admin").contentType(MediaType.APPLICATION_JSON).content(imovel().put("id",999).toString()))
            .andExpect(status().isBadRequest());
        var dados=locacao(1,criarImovel()).put("valorAluguel",1);
        mvc.perform(auth(post("/api/locacoes"),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest());
        dados.remove("valorAluguel");dados.putArray("perfis").add("ADMIN");
        mvc.perform(auth(post("/api/locacoes"),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest());
    }
    @Test void dadosPessoaisDesnecessariosNaoSaemNoDto() throws Exception {
        mvc.perform(auth(get("/api/clientes/1"),"admin")).andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").doesNotExist()).andExpect(jsonPath("$.dtNascimento").doesNotExist())
            .andExpect(jsonPath("$.nomeCliente").value("Ana Exemplo"));
    }
    @Test void senhasDeExemploEstaoEmBcrypt() {
        var rows=jdbc.queryForList("select login,senha_hash from usuarios");
        assertThat(rows).hasSize(4);
        for(var row:rows) {
            String hash=(String)row.get("SENHA_HASH"),login=(String)row.get("LOGIN");
            assertThat(hash).startsWith("$2a$10$").hasSize(60).isNotEqualTo(senha(login));
            assertThat(encoder.matches(senha(login),hash)).isTrue();
        }
    }
    @Test void novoUsuarioPersisteHashESemSenhaNaResposta() throws Exception {
        var r=criar("usuarios",usuario("novocliente","CLIENTE",1L),"admin");
        assertThat(r.has("senha")).isFalse();assertThat(r.has("senhaHash")).isFalse();
        String hash=jdbc.queryForObject("select senha_hash from usuarios where login='novocliente'",String.class);
        assertThat(encoder.matches("Teste@12345",hash)).isTrue();
        mvc.perform(get("/api/imoveis").with(httpBasic("novocliente","Teste@12345"))).andExpect(status().isOk());
        for(String caminho:List.of("/api/usuarios","/api/usuarios/"+r.get("id").asLong(),"/api/usuarios/me")) {
            String body=mvc.perform(auth(get(caminho),"admin")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(body).doesNotContain("senha","$2a$","Teste@12345","Admin@12345");
        }
    }
    @Test void umUsuarioPodeTerMaisDeUmPerfil() throws Exception {
        var dados=usuario("multiplo","ATENDENTE",1L);dados.withArray("perfis").add("CLIENTE");
        var r=criar("usuarios",dados,"admin");assertThat(r.get("perfis").size()).isEqualTo(2);
        mvc.perform(get("/api/clientes").with(httpBasic("multiplo","Teste@12345"))).andExpect(status().isOk());
    }
    @Test void usuarioClienteSemVinculoEhRejeitado() throws Exception {
        mvc.perform(auth(post("/api/usuarios"),"admin").contentType(MediaType.APPLICATION_JSON).content(usuario("semvinculo","CLIENTE",null).toString()))
            .andExpect(status().isBadRequest());
    }
    @Test void validaUsuarioSenhaPerfisELoginDuplicado() throws Exception {
        var dados=usuario("admin","ADMIN",null);
        mvc.perform(auth(post("/api/usuarios"),"admin").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isConflict());
        dados.put("login","novo").put("senha","curta");
        mvc.perform(auth(post("/api/usuarios"),"admin").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.senha").exists());
        dados.put("senha","Teste@12345");dados.putArray("perfis").add("NAO_CADASTRADO");
        mvc.perform(auth(post("/api/usuarios"),"admin").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest());
        dados.putArray("perfis");
        mvc.perform(auth(post("/api/usuarios"),"admin").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest());
    }
    @Test void senhaNovaRevogaAntigaENaoEhRetornada() throws Exception {
        long id=criar("usuarios",usuario("trocasenha","ATENDENTE",null),"admin").get("id").asLong();
        mvc.perform(auth(put("/api/usuarios/"+id+"/senha"),"admin").contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"NovaSenha@12345\"}"))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/api/imoveis").with(httpBasic("trocasenha","Teste@12345"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/imoveis").with(httpBasic("trocasenha","NovaSenha@12345"))).andExpect(status().isOk());
    }
    @Test void desativacaoERevogacaoDePerfilValemNaProximaRequisicao() throws Exception {
        long id=criar("usuarios",usuario("revogar","ATENDENTE",1L),"admin").get("id").asLong();
        var d=json.createObjectNode().put("nome","Revogado").put("ativo",true).put("idCliente",1);
        d.putArray("perfis").add("CLIENTE");
        mvc.perform(auth(put("/api/usuarios/"+id),"admin").contentType(MediaType.APPLICATION_JSON).content(d.toString())).andExpect(status().isOk());
        mvc.perform(get("/api/clientes").with(httpBasic("revogar","Teste@12345"))).andExpect(status().isForbidden());
        d.put("ativo",false);
        mvc.perform(auth(put("/api/usuarios/"+id),"admin").contentType(MediaType.APPLICATION_JSON).content(d.toString())).andExpect(status().isOk());
        mvc.perform(get("/api/imoveis").with(httpBasic("revogar","Teste@12345"))).andExpect(status().isUnauthorized());
    }
    @Test void usuarioExcluidoNaoAutentica() throws Exception {
        long id=criar("usuarios",usuario("excluir","ATENDENTE",null),"admin").get("id").asLong();
        mvc.perform(auth(delete("/api/usuarios/"+id),"admin")).andExpect(status().isNoContent());
        mvc.perform(get("/api/imoveis").with(httpBasic("excluir","Teste@12345"))).andExpect(status().isUnauthorized());
    }
    @Test void adminNaoRemoveProprioAcesso() throws Exception {
        mvc.perform(auth(delete("/api/usuarios/1"),"admin")).andExpect(status().isConflict());
        var d=json.createObjectNode().put("nome","Admin").put("ativo",false);d.putArray("perfis").add("ADMIN");
        mvc.perform(auth(put("/api/usuarios/1"),"admin").contentType(MediaType.APPLICATION_JSON).content(d.toString())).andExpect(status().isConflict());
    }
    @Test void crudPerfisComProtecaoDosPadroes() throws Exception {
        var d=json.createObjectNode().put("nome","AUDITOR").put("descricao","Perfil adicional sem permissões implícitas");
        long id=criar("perfis",d,"admin").get("id").asLong();
        mvc.perform(auth(get("/api/perfis/"+id),"admin")).andExpect(status().isOk());
        d.put("descricao","Descrição atualizada");
        mvc.perform(auth(put("/api/perfis/"+id),"admin").contentType(MediaType.APPLICATION_JSON).content(d.toString())).andExpect(status().isOk());
        mvc.perform(auth(delete("/api/perfis/"+id),"admin")).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/perfis/1"),"admin")).andExpect(status().isConflict());
        d.put("nome","OUTRO");
        mvc.perform(auth(put("/api/perfis/1"),"admin").contentType(MediaType.APPLICATION_JSON).content(d.toString())).andExpect(status().isConflict());
    }
    @Test void perfilVinculadoEClienteVinculadoNaoPodemSerExcluidos() throws Exception {
        var d=json.createObjectNode().put("nome","AUXILIAR").put("descricao","Sem permissões implícitas");
        long id=criar("perfis",d,"admin").get("id").asLong();
        criar("usuarios",usuario("auxiliar","AUXILIAR",null),"admin");
        mvc.perform(auth(delete("/api/perfis/"+id),"admin")).andExpect(status().isConflict());
        mvc.perform(auth(delete("/api/clientes/1"),"admin")).andExpect(status().isConflict());
        mvc.perform(get("/api/imoveis").with(httpBasic("auxiliar","Teste@12345"))).andExpect(status().isForbidden());
    }
    @Test void dtoERegrasAnterioresMantidosParaCliente() throws Exception {
        long imovel=criarImovel();var dados=locacao(1,imovel);
        var r=criar("locacoes",dados,"ana");
        assertThat(r.get("valorAluguel").decimalValue()).isEqualByComparingTo("300");
        assertThat(r.has("inquilino")).isFalse();assertThat(r.has("imovel")).isFalse();
        mvc.perform(auth(post("/api/locacoes"),"bruno").contentType(MediaType.APPLICATION_JSON).content(locacao(2,imovel).toString())).andExpect(status().isConflict());
        dados.put("dataFim","2099-02-10");
        mvc.perform(auth(post("/api/locacoes"),"ana").contentType(MediaType.APPLICATION_JSON).content(dados.toString())).andExpect(status().isBadRequest());
    }
    @Test void meRetornaSomenteIdentidadeAtualSemSenha() throws Exception {
        mvc.perform(auth(get("/api/usuarios/me"),"ana")).andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("ana")).andExpect(jsonPath("$.idCliente").value(1))
            .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.senhaHash").doesNotExist());
    }
}
