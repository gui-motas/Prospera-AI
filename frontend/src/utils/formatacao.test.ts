import { describe, expect, it } from 'vitest';
import { formatarData, formatarMes, paraIso, periodoDoMes } from './formatacao';
import { getPasswordScore, isPasswordValid } from './password';

describe('formatacao', () => {
  it('formata datas ISO sem deslocamento de fuso horário', () => {
    expect(formatarData('2026-11-01')).toBe('01/11/2026');
    expect(formatarData(null)).toBe('—');
  });

  it('formata o mês abreviado', () => {
    expect(formatarMes('2026-11')).toBe('nov/26');
  });

  it('calcula o período do mês', () => {
    expect(periodoDoMes(new Date(2026, 1, 10))).toEqual({ inicio: '2026-02-01', fim: '2026-02-28' });
    expect(paraIso(new Date(2026, 0, 5))).toBe('2026-01-05');
  });
});

describe('senha', () => {
  it('segue a regra mínima do backend (8 caracteres, letras e números)', () => {
    expect(isPasswordValid('abc12345')).toBe(true);
    // exatamente o que ValidadorSenha exige: só letras e números já basta
    expect(isPasswordValid('senha12345')).toBe(true);
    expect(isPasswordValid('abcdefgh')).toBe(false);
    expect(isPasswordValid('1234567')).toBe(false);
    expect(isPasswordValid('12345678')).toBe(false);
  });

  it('pontua a força da senha', () => {
    expect(getPasswordScore('')).toBe(0);
    expect(getPasswordScore('Abcdef12!')).toBe(4);
  });

  it('a pontuação é só um indicador: uma senha válida pode ter nota baixa', () => {
    // este era o conflito: o medidor pedia símbolo e maiúscula, mas o backend aceita
    expect(isPasswordValid('senha12345')).toBe(true);
    expect(getPasswordScore('senha12345')).toBeLessThan(3);
  });
});
