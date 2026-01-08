package com.estoquefx.updater.core;

public class AppInfo {
    public static final String NOME_APP =
            "SistemaEstoqueFX";
    public static final String VERSAO   =
            "1.2.3";
    public static final String BUG_REPORT_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSd_phUkuqlleT4CsKnvZPnEruQDdZK7qeCkvGU3HXa8D6ruWw/viewform?usp=dialog";
    public static final String RELEASES_URL =
            "https://github.com/Abysswalkerrr/estoque_releases/releases";
    public static final String UPDATE_URL =
            "https://api.github.com/repos/Abysswalkerrr/estoque_releases/releases/latest";

    public static String novidades = """
            1.2.2
            Correção na lógica de coloração da tabela.
            
            1.2.1
            Menu imprimir adicionado ao invés de deixar a função na aba arquivo.
            
            1.2.0
            Função de impressão/salvar como pdf implementada.
            
            1.1.8
            Correções minoritárias
            
            1.1.7
            Prevenção de falhas na criação de produtos.
            
            1.1.6
            Correção da coloração na tabela e reestruturação dos arquivos.
            
            1.1.5
            Correção de formatação da aba novidades e reformulação do bug report.
            
            1.1.4
            Mudanças agora aparecem no popup de atualização, função de relatar falhas adicionada e página para ver versões anteriores adicionada.
           
            1.1.3
            Correções da barra de download e instalador.
           
            1.1.2
            Aba de novidades de versão implementada.
           
            1.1.1
            Barra de progresso e lógica de atualização implementada.
           
            1.1.0
            Autocomplete lançado.
           
            1.0.4
            Formatação das tabelas aprimorada.
           
            1.0.3
            Correções ao atualizador.
           
            1.0.2
            Atualizador implementado.
           
            1.0.1
            Descrição implementada.
           """;
}
