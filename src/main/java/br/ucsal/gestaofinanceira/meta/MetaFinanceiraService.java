package br.ucsal.gestaofinanceira.meta;

import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.meta.MetaDtos.AporteRequest;
import br.ucsal.gestaofinanceira.meta.MetaDtos.MetaRequest;
import br.ucsal.gestaofinanceira.meta.MetaDtos.MetaResponse;
import br.ucsal.gestaofinanceira.meta.MetaDtos.OperacaoAporte;
import br.ucsal.gestaofinanceira.planejamento.AnaliseFinanceiraRepository;
import br.ucsal.gestaofinanceira.planejamento.CenarioFinanceiroRepository;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-08 – Cadastro de metas financeiras e RF-09 – Acompanhamento de metas. */
@Service
public class MetaFinanceiraService {

    private final MetaFinanceiraRepository metaRepository;
    private final CenarioFinanceiroRepository cenarioRepository;
    private final AnaliseFinanceiraRepository analiseRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    public MetaFinanceiraService(MetaFinanceiraRepository metaRepository,
                                 CenarioFinanceiroRepository cenarioRepository,
                                 AnaliseFinanceiraRepository analiseRepository,
                                 UsuarioAutenticado usuarioAutenticado) {
        this.metaRepository = metaRepository;
        this.cenarioRepository = cenarioRepository;
        this.analiseRepository = analiseRepository;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /** Lista as metas atualizando o status (ex.: meta que passou do prazo vira ATRASADA). */
    @Transactional
    public List<MetaResponse> listar() {
        LocalDate hoje = LocalDate.now();
        List<MetaFinanceira> metas = metaRepository.findByUsuarioIdOrderByPrazoAsc(usuarioAutenticado.obterId());
        metas.forEach(m -> m.atualizarStatus(hoje));
        return metas.stream().map(m -> MetaResponse.de(m, hoje)).toList();
    }

    @Transactional
    public MetaResponse criar(MetaRequest req) {
        Usuario usuario = usuarioAutenticado.obter();
        LocalDate hoje = LocalDate.now();
        if (!req.prazo().isAfter(hoje)) {
            throw new RegraNegocioException("O prazo da meta deve ser uma data futura.");
        }
        MetaFinanceira meta = new MetaFinanceira(usuario, req.nome(), req.valorDesejado(), req.prazo(), hoje);
        return MetaResponse.de(metaRepository.save(meta), hoje);
    }

    /** Edição feita pelo usuário — inclusive para adotar um novo prazo sugerido em um cenário (RN-011). */
    @Transactional
    public MetaResponse editar(UUID id, MetaRequest req) {
        LocalDate hoje = LocalDate.now();
        MetaFinanceira meta = buscarDoUsuario(id, usuarioAutenticado.obterId());
        meta.editar(req.nome(), req.valorDesejado(), req.prazo(), hoje);
        return MetaResponse.de(meta, hoje);
    }

    @Transactional
    public MetaResponse registrarAporte(UUID id, AporteRequest req) {
        LocalDate hoje = LocalDate.now();
        MetaFinanceira meta = buscarDoUsuario(id, usuarioAutenticado.obterId());
        meta.atualizarValorAcumulado(
                req.operacao() == OperacaoAporte.RESGATE ? req.valor().negate() : req.valor(), hoje);
        return MetaResponse.de(meta, hoje);
    }

    /** Exclui a meta junto com seus cenários e análises. */
    @Transactional
    public void excluir(UUID id) {
        MetaFinanceira meta = buscarDoUsuario(id, usuarioAutenticado.obterId());
        analiseRepository.deleteByCenarioMetaId(meta.getId());
        analiseRepository.deleteByMetaId(meta.getId());
        cenarioRepository.deleteByMetaId(meta.getId());
        metaRepository.delete(meta);
    }

    /** Busca garantindo a propriedade dos dados (RN-001). */
    public MetaFinanceira buscarDoUsuario(UUID id, UUID usuarioId) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta não encontrada."));
    }
}
