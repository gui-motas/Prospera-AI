package br.ucsal.gestaofinanceira.seguranca;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;

/**
 * Política mínima de senha: 8 caracteres, com letras e números.
 * A senha é armazenada apenas como hash BCrypt (RNF-003).
 */
public final class ValidadorSenha {

    public static final int TAMANHO_MINIMO = 8;

    private ValidadorSenha() {
    }

    public static void validar(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres.");
        }
        boolean temLetra = senha.chars().anyMatch(Character::isLetter);
        boolean temNumero = senha.chars().anyMatch(Character::isDigit);
        if (!temLetra || !temNumero) {
            throw new RegraNegocioException("A senha deve conter letras e números.");
        }
    }
}
