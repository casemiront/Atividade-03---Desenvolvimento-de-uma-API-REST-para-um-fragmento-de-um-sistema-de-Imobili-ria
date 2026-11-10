package br.edu.ifma.imobiliaria.service;
import br.edu.ifma.imobiliaria.model.Usuario;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.repository.UsuarioRepository;
import br.edu.ifma.imobiliaria.exception.*;
import br.edu.ifma.imobiliaria.security.ControleAcesso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Sort;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PerfilService perfis;
    private final ClienteService clientes;
    private final PasswordEncoder encoder;
    private final ControleAcesso acesso;
    public UsuarioService(UsuarioRepository repository,PerfilService perfis,ClienteService clientes,
            PasswordEncoder encoder,ControleAcesso acesso) {
        this.repository=repository;this.perfis=perfis;this.clientes=clientes;this.encoder=encoder;this.acesso=acesso;
    }
    public List<UsuarioResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(UsuarioResponse::from).toList();
    }
    public UsuarioResponse buscar(Long id) { return UsuarioResponse.from(entidade(id)); }
    private Usuario entidade(Long id) {
        return repository.findById(id).orElseThrow(()->new RecursoNaoEncontradoException("Usuário não encontrado: "+id));
    }
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse atual() { return UsuarioResponse.from(entidade(acesso.usuario().getIdUsuario())); }
    public UsuarioResponse criar(UsuarioRequest dados) {
        if(repository.existsByLogin(dados.login())) throw new RegistroEmUsoException("Login já cadastrado.");
        Usuario u=new Usuario();u.setLogin(dados.login());
        preencher(u,dados.nome(),dados.ativo(),dados.idCliente(),dados.perfis());
        u.setSenhaHash(hash(dados.senha()));
        return UsuarioResponse.from(repository.saveAndFlush(u));
    }
    public UsuarioResponse atualizar(Long id,UsuarioAtualizacaoRequest dados) {
        Usuario u=entidade(id);
        if(Objects.equals(id,acesso.usuario().getIdUsuario()) && (!dados.ativo() || !dados.perfis().contains("ADMIN")))
            throw new RegistroEmUsoException("O administrador não pode desativar a própria conta nem remover seu próprio perfil ADMIN.");
        preencher(u,dados.nome(),dados.ativo(),dados.idCliente(),dados.perfis());
        return UsuarioResponse.from(repository.saveAndFlush(u));
    }
    public void alterarSenha(Long id,SenhaRequest dados) {
        Usuario u=entidade(id);u.setSenhaHash(hash(dados.senha()));repository.saveAndFlush(u);
    }
    public void excluir(Long id) {
        if(Objects.equals(id,acesso.usuario().getIdUsuario()))
            throw new RegistroEmUsoException("O administrador não pode excluir a própria conta.");
        repository.delete(entidade(id));repository.flush();
    }
    private String hash(String senha) {
        if(senha.getBytes(StandardCharsets.UTF_8).length>72)
            throw new RegraNegocioException("A senha deve ocupar no máximo 72 bytes em UTF-8.");
        return encoder.encode(senha);
    }
    private void preencher(Usuario u,String nome,boolean ativo,Long idCliente,Set<String> nomesPerfis) {
        if(nomesPerfis.contains("CLIENTE") && idCliente==null)
            throw new RegraNegocioException("Usuário com perfil CLIENTE precisa de um cliente vinculado.");
        u.setNome(nome);u.setAtivo(ativo);u.setPerfis(perfis.resolver(nomesPerfis));
        u.setCliente(idCliente==null?null:clientes.buscar(idCliente));
    }
}
