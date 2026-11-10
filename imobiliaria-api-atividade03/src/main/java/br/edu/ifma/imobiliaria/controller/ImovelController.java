package br.edu.ifma.imobiliaria.controller;


import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.ImovelService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ATENDENTE','CLIENTE')")
@RequestMapping("/api/imoveis")
public class ImovelController {
    private final ImovelService service;
    public ImovelController(ImovelService service) { this.service = service; }

    @GetMapping
    public List<ImovelResponse> listar() { return service.listar().stream().map(ImovelResponse::from).toList(); }

    @GetMapping({"/paginados", "/pesquisa"})
    public PaginaResponse<ImovelResponse> pesquisar(
            @RequestParam(required=false) String tipo,
            @RequestParam(required=false) String cidade,
            @RequestParam(required=false) Integer quartos,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size,
            @RequestParam(defaultValue="id") String ordenar,
            @RequestParam(defaultValue="asc") String direcao) {
        return PaginaResponse.from(service.pesquisar(tipo, cidade, quartos, page, size, ordenar, direcao).map(ImovelResponse::from));
    }

    @GetMapping("/{id}")
    public ImovelResponse buscar(@PathVariable Long id) { return ImovelResponse.from(service.buscar(id)); }

    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    @PostMapping
    public ResponseEntity<ImovelResponse> criar(@Valid @RequestBody ImovelRequest dados) {
        var criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/imoveis/" + criado.getId())).body(ImovelResponse.from(criado));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    @PutMapping("/{id}")
    public ImovelResponse atualizar(@PathVariable Long id, @Valid @RequestBody ImovelRequest dados) {
        return ImovelResponse.from(service.atualizar(id, dados));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
