package br.edu.ifma.imobiliaria.controller;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service=service; }
    @GetMapping
    public List<UsuarioResponse> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest dados) {
        var criado=service.criar(dados);
        return ResponseEntity.created(URI.create("/api/usuarios/"+criado.id())).body(criado);
    }
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id,@Valid @RequestBody UsuarioAtualizacaoRequest dados) {
        return service.atualizar(id,dados);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);return ResponseEntity.noContent().build();
    }

@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public UsuarioResponse atual() { return service.atual(); }
@PutMapping("/{id}/senha")
public ResponseEntity<Void> alterarSenha(@PathVariable Long id,@Valid @RequestBody SenhaRequest dados) {
    service.alterarSenha(id,dados);return ResponseEntity.noContent().build();
}

}
