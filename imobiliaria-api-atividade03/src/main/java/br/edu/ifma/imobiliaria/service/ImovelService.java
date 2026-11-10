package br.edu.ifma.imobiliaria.service;

import br.edu.ifma.imobiliaria.model.Imovel;
import br.edu.ifma.imobiliaria.repository.*;
import br.edu.ifma.imobiliaria.dto.ImovelRequest;
import br.edu.ifma.imobiliaria.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ImovelService {
    private final ImovelRepository repository;
    private final LocacaoRepository locacoes;

    public ImovelService(ImovelRepository repository, LocacaoRepository locacoes) {
        this.repository = repository;
        this.locacoes = locacoes;
    }

    @Transactional(readOnly=true)
    public List<Imovel> listar() { return repository.findAll(Sort.by("id")); }

    @Transactional(readOnly=true)
    public Page<Imovel> pesquisar(String tipo, String cidade, Integer quartos,
            int pagina, int tamanho, String ordenar, String direcao) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new RegraNegocioException("Página deve ser >= 0 e tamanho deve estar entre 1 e 100.");
        }
        if (quartos != null && quartos < 0) {
            throw new RegraNegocioException("A quantidade de quartos não pode ser negativa.");
        }
        if (!Set.of("id", "tipoImovel", "cidade", "dormitorios", "valorDiaria").contains(ordenar)
                || !(direcao.equalsIgnoreCase("asc") || direcao.equalsIgnoreCase("desc"))) {
            throw new RegraNegocioException("Ordenação inválida. Use id, tipoImovel, cidade, dormitorios ou valorDiaria; direção asc ou desc.");
        }
        Imovel exemplo = new Imovel();
        exemplo.setTipoImovel(textoOpcional(tipo));
        exemplo.setCidade(textoOpcional(cidade));
        exemplo.setDormitorios(quartos);
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues()
            .withMatcher("tipoImovel", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withMatcher("cidade", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Sort sort = Sort.by(Sort.Direction.fromString(direcao), ordenar);
        if (!ordenar.equals("id")) sort = sort.and(Sort.by("id"));
        return repository.findAll(Example.of(exemplo, matcher), PageRequest.of(pagina, tamanho, sort));
    }

    private String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    @Transactional(readOnly=true)
    public Imovel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Imóvel não encontrado: " + id));
    }

    public Imovel buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Imóvel não encontrado: " + id));
    }

    public Imovel criar(ImovelRequest dados) {
        Imovel entidade = new Imovel();
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public Imovel atualizar(Long id, ImovelRequest dados) {
        Imovel entidade = buscarParaAtualizacao(id);
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public void excluir(Long id) {
        Imovel imovel = buscarParaAtualizacao(id);
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        if (locacoes.existsByImovelIdAndAtivoTrueAndDataFimAfter(id, hoje)) {
            throw new RegistroEmUsoException("Não é possível excluir: o imóvel possui locação confirmada ativa ou futura.");
        }
        if (locacoes.existsByImovelId(id)) {
            throw new RegistroEmUsoException("O imóvel possui histórico de locações. Exclua os aluguéis e as locações vinculadas antes de excluir o imóvel.");
        }
        repository.delete(imovel);
        repository.flush();
    }

    private void preencher(Imovel entidade, ImovelRequest dados) {
        entidade.setTipoImovel(dados.tipoImovel());
        entidade.setEndereco(dados.endereco());
        entidade.setCep(dados.cep());
        entidade.setDormitorios(dados.dormitorios());
        entidade.setBanheiros(dados.banheiros());
        entidade.setSuites(dados.suites());
        entidade.setMetragem(dados.metragem());
        entidade.setValorAluguelSug(dados.valorAluguelSug());
        entidade.setCidade(dados.cidade());
        entidade.setValorDiaria(dados.valorDiaria());
        entidade.setObs(dados.obs());
    }
}
