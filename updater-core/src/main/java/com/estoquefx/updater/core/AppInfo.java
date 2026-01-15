package com.estoquefx.updater.core;

public class AppInfo {
    public static final String NOME_APP =
            "SistemaEstoqueFX";
    public static final String VERSAO   =
            "1.6.1";
    public static final String VERSAO_CHANNEL =
            "stable";
    public static String UPDATE_CHANNEL = "stable";

    public static final String BUG_REPORT_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSd_phUkuqlleT4CsKnvZPnEruQDdZK7qeCkvGU3HXa8D6ruWw/viewform?usp=dialog";

    public static final String UPDATE_URL = getUpdateUrl();

    public static final String RELEASES_URL =
            "https://api.github.com/repos/Abysswalkerrr/estoque_releases/releases";

    public static final String LATEST_RELEASES_URL =
            "https://api.github.com/repos/abysswalkerrr/estoque_releases/releases/latest";

    private static String getUpdateUrl() {
        if (isBeta()) {
            return RELEASES_URL;
        } else {
            return LATEST_RELEASES_URL;
        }
    }

    public static boolean isBeta() {return "beta".equalsIgnoreCase(UPDATE_CHANNEL);}

    public static void setUpdateChannel(String updateChannel) {
        if (updateChannel.equalsIgnoreCase("beta") || updateChannel.equalsIgnoreCase("stable")) {
            UPDATE_CHANNEL = updateChannel;
        }
    }

    public static String novidades = """
            1.7.0
            Importação de arquivos .csv(excel) implementada.
            
            1.6.1
            Correção de falhas em potencial.
            
            1.6.0
            Melhoria no exportarCSV e opção de abrir pasta de dados.
            
            1.5.2
            Agora é possível pesquisar "urgente"(indiferente se for maiúsculo ou minúsculo) para filtrar os produtos com estoque baixo.
            
            1.5.1
            Correções minoritárias.
            
            1.5.0
            Coluna de última alteração adicionada e uso de versões experimentais permitido.
            
            1.4.2
            Correção de falhas relacionadas a ausência de arquivos.
            
            1.4.1
            Melhoria da função de avisar atualizações.
            
            1.4.0
            Saldo total implementado.
            
            1.3.3
            Adicionada a opção de ignorar o pedido de atualizar.
            
            1.3.2
            Correção de erros.
            
            1.3.1
            Correção da lógica de última atualização.
            
            1.3.0
            Texto mostrando quando foi a última contagem implementado.
            
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
