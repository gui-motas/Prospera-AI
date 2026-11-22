package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe {@code AnaliseFinanceira} do diagrama de classes — texto gerado pela API de IA.
 * Atributos: id, dataGeracao, tipo, resultado. Operação: consultarResultado().
 *
 * <p>Associações: Usuario solicita 0..* AnaliseFinanceira; MetaFinanceira (0..1) é o contexto
 * de análises de meta; CenarioFinanceiro (0..1) é contextualizado pela análise de cenário.</p>
 */
@Entity
@Table(name = "analises_financeiras")
public class AnaliseFinanceira {

    public static final int TAMANHO_MAXIMO_RESULTADO = 8000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_id")
    private MetaFinanceira meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cenario_id")
    private CenarioFinanceiro cenario;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAnalise tipo;

    @Column(nullable = false, length = TAMANHO_MAXIMO_RESULTADO)
    private String resultado;

    protected AnaliseFinanceira() {
    }

    public AnaliseFinanceira(Usuario usuario, TipoAnalise tipo, MetaFinanceira meta,
                             CenarioFinanceiro cenario, String resultado) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.meta = meta;
        this.cenario = cenario;
        this.dataGeracao = LocalDateTime.now();
        this.resultado = resultado.length() > TAMANHO_MAXIMO_RESULTADO
                ? resultado.substring(0, TAMANHO_MAXIMO_RESULTADO)
                : resultado;
    }

    public String consultarResultado() {
        return resultado;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public MetaFinanceira getMeta() {
        return meta;
    }

    public CenarioFinanceiro getCenario() {
        return cenario;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public TipoAnalise getTipo() {
        return tipo;
    }
}
