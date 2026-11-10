package br.edu.ifma.imobiliaria.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(Instant timestamp, int status, String mensagem, String caminho, Map<String,String> campos) { }
    private ResponseEntity<Erro> resposta(HttpStatus status, String mensagem, HttpServletRequest req, Map<String,String> campos) {
        return ResponseEntity.status(status).body(new Erro(Instant.now(), status.value(), mensagem, req.getRequestURI(), campos));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validar(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String,String> campos = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.merge(e.getField(), e.getDefaultMessage(), (a,b) -> a + "; " + b));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", req, campos);
    }
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Erro> acessoNegado(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação.", req, Map.of());
    }
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Erro> regra(RegraNegocioException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> conflito(DataIntegrityViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Conflito: registro duplicado ou vinculado a outro cadastro.", req, Map.of());
    }
    @ExceptionHandler(RegistroEmUsoException.class)
    public ResponseEntity<Erro> emUso(RegistroEmUsoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    public ResponseEntity<Erro> concorrencia(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Registro em atualização. Tente novamente.", req, Map.of());
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Erro> formato(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Formato inválido. Confira JSON, tipos, campos e datas (AAAA-MM-DD).", req, Map.of());
    }
}
