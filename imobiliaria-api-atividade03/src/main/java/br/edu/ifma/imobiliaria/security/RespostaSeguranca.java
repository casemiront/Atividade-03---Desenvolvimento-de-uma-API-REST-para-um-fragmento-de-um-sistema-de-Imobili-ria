package br.edu.ifma.imobiliaria.security;
import br.edu.ifma.imobiliaria.exception.ApiExceptionHandler.Erro;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class RespostaSeguranca implements AuthenticationEntryPoint,AccessDeniedHandler {
    private final ObjectMapper json;
    public RespostaSeguranca(ObjectMapper json) { this.json=json; }
    @Override
    public void commence(HttpServletRequest req,HttpServletResponse res,AuthenticationException ex) throws IOException {
        res.setHeader("WWW-Authenticate","Basic realm=\"Imobiliaria\"");
        escrever(req,res,401,"Autenticação necessária ou credenciais inválidas.");
    }
    @Override
    public void handle(HttpServletRequest req,HttpServletResponse res,AccessDeniedException ex) throws IOException {
        escrever(req,res,403,"Você não tem permissão para esta operação.");
    }
    private void escrever(HttpServletRequest req,HttpServletResponse res,int status,String mensagem) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(),new Erro(Instant.now(),status,mensagem,req.getRequestURI(),Map.of()));
    }
}
