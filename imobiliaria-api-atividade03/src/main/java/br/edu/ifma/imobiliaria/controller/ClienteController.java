package br.edu.ifma.imobiliaria.controller;


import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping
    public List<ClienteResponse> listar() { return service.listar().stream().map(ClienteResponse::from).toList(); }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) { return ClienteResponse.from(service.buscar(id)); }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest dados) {
        var criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/clientes/" + criado.getId())).body(ClienteResponse.from(criado));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest dados) {
        return ClienteResponse.from(service.atualizar(id, dados));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
