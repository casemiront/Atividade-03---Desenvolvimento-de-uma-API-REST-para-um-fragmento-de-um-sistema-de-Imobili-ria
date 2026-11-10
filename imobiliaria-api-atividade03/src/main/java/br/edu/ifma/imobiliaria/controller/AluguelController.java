package br.edu.ifma.imobiliaria.controller;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.model.Aluguel;
import br.edu.ifma.imobiliaria.service.AluguelService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ATENDENTE','CLIENTE')")
@RequestMapping("/api/alugueis")
public class AluguelController {
    private final AluguelService service;
    public AluguelController(AluguelService service) { this.service = service; }
    @GetMapping
    public List<AluguelResponse> listar() {
        return service.listar().stream().map(AluguelResponse::from).toList();
    }
    @GetMapping("/{id}")
    public AluguelResponse buscar(@PathVariable Long id) { return AluguelResponse.from(service.buscar(id)); }
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    @PostMapping
    public ResponseEntity<AluguelResponse> criar(@Valid @RequestBody AluguelRequest dados) {
        Aluguel criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/alugueis/" + criado.getId())).body(AluguelResponse.from(criado));
    }
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    @PutMapping("/{id}")
    public AluguelResponse atualizar(@PathVariable Long id, @Valid @RequestBody AluguelRequest dados) {
        return AluguelResponse.from(service.atualizar(id, dados));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
