package br.ucsal.gestaofinanceira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Plataforma de Gestão Financeira Pessoal com Planejamento Inteligente de Metas.
 *
 * <p>Organização dos pacotes (espelha o Diagrama de Classes da ERS):</p>
 * <ul>
 *   <li>{@code usuario}, {@code cadastro}, {@code seguranca} — Usuario, cadastro e autenticação (RF-001, RF-002)</li>
 *   <li>{@code conta} — ContaFinanceira (RF-003)</li>
 *   <li>{@code categoria} — Categoria (RF-004, RN-003)</li>
 *   <li>{@code movimentacao} — Movimentacao (RF-005, RF-006, RN-002)</li>
 *   <li>{@code orcamento} — Orcamento / limites de gastos (RN-005)</li>
 *   <li>{@code meta} — MetaFinanceira (RF-08, RF-09, RN-006, RN-007)</li>
 *   <li>{@code financeiro} — ServicoFinanceiro e painel (RF-007, RN-004, RN-012)</li>
 *   <li>{@code planejamento} — CenarioFinanceiro, AnaliseFinanceira e ServicoIA (RF-010, RN-008..RN-011, RNF-09)</li>
 *   <li>{@code admin} — funções do perfil Administrador</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync
public class GestaoFinanceiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoFinanceiraApplication.class, args);
    }
}
