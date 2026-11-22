package br.ucsal.gestaofinanceira.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Barra a tentativa de login antes de verificar a senha, para que um e-mail já bloqueado
 * por excesso de falhas não gaste um BCrypt por requisição.
 */
public class FiltroTentativasLogin extends OncePerRequestFilter {

    private final ContadorTentativasLogin contador;

    public FiltroTentativasLogin(ContadorTentativasLogin contador) {
        this.contador = contador;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // getRequestURI inclui context path; aqui a app roda na raiz, mas o endsWith cobre os dois casos
        String caminho = request.getRequestURI();
        return !("POST".equalsIgnoreCase(request.getMethod())
                && caminho != null && caminho.endsWith("/api/v1/login"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            contador.exigirPermissao(request.getParameter("username"));
        } catch (MuitasTentativasException ex) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erro\":\"" + ex.getMessage() + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
