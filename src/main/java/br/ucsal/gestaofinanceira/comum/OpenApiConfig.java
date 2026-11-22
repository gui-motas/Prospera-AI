package br.ucsal.gestaofinanceira.comum;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação da API: http://localhost:8090/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacaoApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API — Plataforma de Gestão Financeira Pessoal com Planejamento Inteligente de Metas")
                        .description("API REST da plataforma especificada na ERS da disciplina de Engenharia de Software (UCSAL). "
                                + "Autenticação por sessão: faça POST /api/v1/login (username, password) e use o cookie JSESSIONID.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("sessao"))
                .components(new Components().addSecuritySchemes("sessao",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")));
    }
}
