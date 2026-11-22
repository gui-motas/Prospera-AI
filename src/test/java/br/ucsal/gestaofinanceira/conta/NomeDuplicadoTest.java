package br.ucsal.gestaofinanceira.conta;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A verificação "já existe?" seguida de insert tem corrida: duas requisições iguais podem
 * passar pela checagem antes de qualquer uma gravar. A constraint do banco fecha a corrida
 * (o serviço traduz o erro para a mesma mensagem amigável).
 */
@SpringBootTest
@Transactional
class NomeDuplicadoTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaFinanceiraRepository contaRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    @Test
    void bancoRecusaDuasContasComOMesmoNomeParaOMesmoUsuario() {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Ana", "ana.duplicada@email.com", "hash", PerfilUsuario.USUARIO));
        contaRepository.save(new ContaFinanceira(usuario, "Banco", TipoConta.CONTA_CORRENTE, BigDecimal.ZERO));

        // simula a segunda requisição da corrida: passa pela checagem, mas o banco recusa
        assertThrows(DataIntegrityViolationException.class,
                () -> contaRepository.saveAndFlush(
                        new ContaFinanceira(usuario, "Banco", TipoConta.POUPANCA, BigDecimal.ZERO)));
    }

    @Test
    void bancoRecusaMesmaCategoriaComMesmoNomeETipo() {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Bia", "bia.duplicada@email.com", "hash", PerfilUsuario.USUARIO));
        categoriaRepository.save(new Categoria(usuario, "Lazer", TipoMovimentacao.DESPESA));

        assertThrows(DataIntegrityViolationException.class,
                () -> categoriaRepository.saveAndFlush(new Categoria(usuario, "Lazer", TipoMovimentacao.DESPESA)));
    }

    @Test
    void mesmaCategoriaComTipoDiferenteEhPermitida() {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Caio", "caio.duplicada@email.com", "hash", PerfilUsuario.USUARIO));
        categoriaRepository.save(new Categoria(usuario, "Outros", TipoMovimentacao.DESPESA));

        // a chave inclui o tipo, então receita pode repetir o nome
        categoriaRepository.saveAndFlush(new Categoria(usuario, "Outros", TipoMovimentacao.RECEITA));
    }
}
