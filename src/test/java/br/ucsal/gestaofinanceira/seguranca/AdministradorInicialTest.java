package br.ucsal.gestaofinanceira.seguranca;

import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ADMIN_PASSWORD inválida é erro de configuração; não pode derrubar a aplicação no boot. */
class AdministradorInicialTest {

    private final UsuarioRepository repositorio = mock(UsuarioRepository.class);

    private AdministradorInicial com(String email, String senha) {
        when(repositorio.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        return new AdministradorInicial(repositorio, new BCryptPasswordEncoder(), email, senha);
    }

    @Test
    void senhaFracaNaoDerrubaOInicioENaoCriaAdmin() {
        AdministradorInicial inicial = com("admin@email.com", "admin");

        assertDoesNotThrow(() -> inicial.run(null));
        verify(repositorio, never()).save(any());
    }

    @Test
    void senhaValidaCriaAdmin() {
        com("admin@email.com", "Senha12345").run(null);

        verify(repositorio).save(any());
    }

    @Test
    void semConfiguracaoNaoFazNada() {
        com("", "").run(null);

        verify(repositorio, never()).save(any());
    }
}
