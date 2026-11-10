package br.edu.ifma.imobiliaria;

import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.*;
import br.edu.ifma.imobiliaria.exception.RegistroEmUsoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:concorrencia;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
@org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
class DisponibilidadeConcorrenciaTest {
    @Autowired LocacaoService locacoes;
    @Autowired ImovelService imoveis;

    @Test void duasReservasSimultaneasNaoPodemConfirmarMesmoPeriodo() throws Exception {
        var imovel = imoveis.criar(new ImovelRequest("Casa", "Rua Concorrência", "65000000",
            2, 1, 0, 80, new BigDecimal("2000"), "São Luís", new BigDecimal("100"), null));
        var dados = new LocacaoRequest(imovel.getId(), 1L, true, LocalDate.of(2099,1,13),
            LocalDate.of(2099,1,10), 10, new BigDecimal("2"), null);
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        Callable<Integer> reserva = () -> {
            prontas.countDown();
            if (!iniciar.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Tempo de espera excedido");
            try {
                var contexto=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "admin-teste", "", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
                org.springframework.security.core.context.SecurityContextHolder.setContext(contexto);
                locacoes.criar(dados); return 201;
            }
            catch (RegistroEmUsoException ex) { return 409; }
            finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
        };
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> primeira = pool.submit(reserva);
            Future<Integer> segunda = pool.submit(reserva);
            assertThat(prontas.await(10, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();
            assertThat(List.of(primeira.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(201, 409);
            assertThat(locacoes.listar().stream().filter(l -> l.getImovel().getId().equals(imovel.getId())).count())
                .isEqualTo(1);
        }
    }
}
