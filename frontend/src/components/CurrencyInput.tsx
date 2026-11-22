import React from 'react';

interface CurrencyInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    value: string | number;
    onValueChange: (value: string) => void; // Retorna o valor "limpo" (ex: "10.50") para o estado
}

export function CurrencyInput({ value, onValueChange, className, ...props }: CurrencyInputProps) {

    const formatDisplay = (val: string | number) => {
        if (!val) return '';
        const numberVal = typeof val === 'string' ? parseFloat(val) : val;
        if (isNaN(numberVal)) return '';

        // Formata para o padrão brasileiro (R$ X.XXX,XX)
        return numberVal.toLocaleString('pt-BR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        // 1. Remove tudo que não é dígito
        let rawValue = e.target.value.replace(/\D/g, '');

        // 2. Remove zeros à esquerda excessivos
        rawValue = rawValue.replace(/^0+/, '');

        // 3. Se estiver vazio, zera
        if (!rawValue) {
            onValueChange('');
            return;
        }

        // 4. Garante que tenha pelo menos 3 dígitos para lógica funcionar visualmente se for pequeno (opcional, mas ajuda)
        // Mas a lógica principal é dividir por 100
        const floatValue = parseFloat(rawValue) / 100;

        // 5. Devolve o valor em formato de string numérica padrão JS (ponto flutuante) para o estado do pai
        // Ex: Se digitou "1000", vira 10.00. Convertemos para string para manter compatibilidade com os estados existentes
        onValueChange(floatValue.toString());
    };

    return (
        <input
            {...props}
            type="text" // Importante: type text para podermos controlar a formatação visual
            inputMode="numeric" // Abre teclado numérico no celular
            className={className}
            value={formatDisplay(value)} // O que o usuário VÊ
            onChange={handleChange}     // O que acontece ao digitar
        />
    );
}