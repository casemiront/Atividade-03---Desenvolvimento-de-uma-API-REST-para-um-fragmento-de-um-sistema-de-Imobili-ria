package br.edu.ifma.imobiliaria.service;
import br.edu.ifma.imobiliaria.model.Perfil;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.repository.*;
import br.edu.ifma.imobiliaria.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import java.util.*;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class PerfilService {
    private static final Set<String> PADRAO=Set.of("ADMIN","ATENDENTE","CLIENTE");
    private final PerfilRepository repository;
    private final UsuarioRepository usuarios;
    public PerfilService(PerfilRepository repository,UsuarioRepository usuarios) {
        this.repository=repository; this.usuarios=usuarios;
    }
    public List<PerfilResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(PerfilResponse::from).toList();
    }
    public PerfilResponse buscar(Long id) { return PerfilResponse.from(entidade(id)); }
    private Perfil entidade(Long id) {
        return repository.findById(id).orElseThrow(()->new RecursoNaoEncontradoException("Perfil não encontrado: "+id));
    }
    public Set<Perfil> resolver(Set<String> nomes) {
        Set<Perfil> result=new HashSet<>();
        for(String nome:nomes) result.add(repository.findByNome(nome)
            .orElseThrow(()->new RegraNegocioException("Perfil não cadastrado: "+nome)));
        return result;
    }
    public PerfilResponse criar(PerfilRequest dados) {
        if(repository.existsByNome(dados.nome())) throw new RegistroEmUsoException("Perfil já cadastrado.");
        Perfil p=new Perfil();p.setNome(dados.nome());p.setDescricao(dados.descricao());
        return PerfilResponse.from(repository.saveAndFlush(p));
    }
    public PerfilResponse atualizar(Long id,PerfilRequest dados) {
        Perfil p=entidade(id);
        if(!p.getNome().equals(dados.nome())) {
            if(PADRAO.contains(p.getNome()) || usuarios.existsByPerfisId(id))
                throw new RegistroEmUsoException("Não é permitido renomear perfil padrão ou vinculado a usuários.");
            if(repository.existsByNome(dados.nome())) throw new RegistroEmUsoException("Perfil já cadastrado.");
        }
        p.setNome(dados.nome());p.setDescricao(dados.descricao());
        return PerfilResponse.from(repository.saveAndFlush(p));
    }
    public void excluir(Long id) {
        Perfil p=entidade(id);
        if(PADRAO.contains(p.getNome()) || usuarios.existsByPerfisId(id))
            throw new RegistroEmUsoException("Não é permitido excluir perfil padrão ou vinculado a usuários.");
        repository.delete(p);repository.flush();
    }
}
