package br.edu.ifma.imobiliaria.controller;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@PreAuthorize("hasRole('ADMIN')")
public class PerfilController {
    private final PerfilService service;
    public PerfilController(PerfilService service) { this.service=service; }
    @GetMapping
    public List<PerfilResponse> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public PerfilResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest dados) {
        var criado=service.criar(dados);
        return ResponseEntity.created(URI.create("/api/perfis/"+criado.id())).body(criado);
    }
    @PutMapping("/{id}")
    public PerfilResponse atualizar(@PathVariable Long id,@Valid @RequestBody PerfilRequest dados) {
        return service.atualizar(id,dados);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);return ResponseEntity.noContent().build();
    }

}
