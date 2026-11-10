package br.edu.ifma.imobiliaria.service;


import br.edu.ifma.imobiliaria.model.Cliente;
import br.edu.ifma.imobiliaria.repository.ClienteRepository;
import br.edu.ifma.imobiliaria.repository.LocacaoRepository;
import br.edu.ifma.imobiliaria.dto.ClienteRequest;
import br.edu.ifma.imobiliaria.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
@Transactional
public class ClienteService {
    private final ClienteRepository repository;
    private final LocacaoRepository locacoes;
    private final br.edu.ifma.imobiliaria.repository.UsuarioRepository usuarios;

    public ClienteService(ClienteRepository repository, LocacaoRepository locacoes, br.edu.ifma.imobiliaria.repository.UsuarioRepository usuarios) {
        this.repository = repository;
        this.locacoes = locacoes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly=true)
    public List<Cliente> listar() { return repository.findAll(Sort.by("id")); }

    @Transactional(readOnly=true)
    public Cliente buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }

    public Cliente criar(ClienteRequest dados) {
        Cliente entidade = new Cliente();
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public Cliente atualizar(Long id, ClienteRequest dados) {
        Cliente entidade = buscar(id);
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public void excluir(Long id) {
        Cliente cliente = buscar(id);
        if (usuarios.existsByClienteId(id)) {
            throw new RegistroEmUsoException("Cliente vinculado a um usuário. Remova ou altere o vínculo primeiro.");
        }
        if (locacoes.existsByInquilinoId(id)) {
            throw new RegistroEmUsoException("Cliente vinculado a uma locação. Exclua a locação primeiro.");
        }
        repository.delete(cliente);
        repository.flush();
    }

    private void preencher(Cliente entidade, ClienteRequest dados) {
        entidade.setNomeCliente(dados.nomeCliente());
        entidade.setCpf(dados.cpf());
        entidade.setTelefone(dados.telefone());
        entidade.setEmail(dados.email());
        entidade.setDtNascimento(dados.dtNascimento());
    }
}
