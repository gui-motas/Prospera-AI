package br.ucsal.gestaofinanceira.seguranca;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Autenticação por sessão (RF-002) e controle de acesso (RNF-004, RN-008):
 * todas as rotas exigem login, exceto cadastro, login, health check e documentação.
 * A área administrativa exige o perfil Administrador.
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    @Value("${app.cors.allowed-origins}")
    private String origensPermitidas;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // API JSON consumida por SPA com cookie de sessão: o CSRF é barrado pelo CORS
                // (origens exatas), não por token. Nunca libere origem curinga com credenciais.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/cadastro/**", "/api/v1/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                escreverJson(res, HttpStatus.UNAUTHORIZED, "Faça login para continuar."))
                        .accessDeniedHandler((req, res, e) ->
                                escreverJson(res, HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.")))
                .formLogin(form -> form
                        // a tela de login é do frontend; aqui só existe o endpoint que recebe as credenciais
                        .loginPage("/api/v1/login")
                        .loginProcessingUrl("/api/v1/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                        .failureHandler((req, res, e) -> {
                            String mensagem;
                            if (e instanceof LockedException) {
                                mensagem = "Conta bloqueada pelo administrador.";
                            } else if (e instanceof DisabledException) {
                                mensagem = "Confirme seu e-mail antes de entrar.";
                            } else {
                                mensagem = "E-mail ou senha incorretos.";
                            }
                            escreverJson(res, HttpStatus.UNAUTHORIZED, mensagem);
                        }))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origens = Arrays.stream(origensPermitidas.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // Origem curinga com credenciais deixaria qualquer site chamar a API com o cookie do usuário
        // (o CSRF está desligado), então a aplicação recusa subir nessa configuração.
        boolean curinga = origens.stream().anyMatch(o -> o.equals("*") || o.endsWith("://*"));
        if (curinga) {
            throw new IllegalStateException(
                    "APP_CORS_ALLOWED_ORIGINS não pode conter origem curinga (*): liste as origens exatas "
                            + "do frontend. Com cookie de sessão, curinga permitiria CSRF.");
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void escreverJson(HttpServletResponse res, HttpStatus status, String mensagem) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
