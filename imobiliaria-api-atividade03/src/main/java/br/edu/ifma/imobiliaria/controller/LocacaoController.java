package br.edu.ifma.imobiliaria.controller;


import br.edu.ifma.imobiliaria.model.Locacao;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.LocacaoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ATENDENTE','CLIENTE')")
@RequestMapping("/api/locacoes")
public class LocacaoController {
    private final LocacaoService service;
    public LocacaoController(LocacaoService service) { this.service = service; }

    @GetMapping
    public List<LocacaoResponse> listar() { return service.listar().stream().map(LocacaoResponse::from).toList(); }

    @GetMapping("/{id}")
    public LocacaoResponse buscar(@PathVariable Long id) { return LocacaoResponse.from(service.buscar(id)); }

    @PostMapping
    public ResponseEntity<LocacaoResponse> criar(@Valid @RequestBody LocacaoRequest dados) {
        Locacao criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/locacoes/" + criado.getId())).body(LocacaoResponse.from(criado));
    }

    @PutMapping("/{id}")
    public LocacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody LocacaoRequest dados) {
        return LocacaoResponse.from(service.atualizar(id, dados));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
