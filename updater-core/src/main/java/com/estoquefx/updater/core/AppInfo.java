package com.estoquefx.updater.core;

public class AppInfo {
    public static final String NOME_APP =
            "SistemaEstoqueFX";
    public static final String VERSAO   =
            "1.8.0";
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
            1.8.0
            Adição de abas(não implementadas) e novas opções de filtragem.
            \s
            1.7.1
            Correções na lógica de importação dos arquivos CSV.
           \s
            1.7.0
            Importação de arquivos .csv(excel) implementada.
           \s
            1.6.1
            Correção de falhas em potencial.
           \s
            1.6.0
            Melhoria no exportarCSV e opção de abrir pasta de dados.
           \s
            1.5.2
            Agora é possível pesquisar "urgente"(indiferente se for maiúsculo ou minúsculo) para filtrar os produtos com estoque baixo.
           \s
            1.5.1
            Correções minoritárias.
           \s
            1.5.0
            Coluna de última alteração adicionada e uso de versões experimentais permitido.
           \s
            1.4.2
            Correção de falhas relacionadas a ausência de arquivos.
           \s
            1.4.1
            Melhoria da função de avisar atualizações.
           \s
            1.4.0
            Saldo total implementado.
           \s
            1.3.3
            Adicionada a opção de ignorar o pedido de atualizar.
           \s
            1.3.2
            Correção de erros.
           \s
            1.3.1
            Correção da lógica de última atualização.
           \s
            1.3.0
            Texto mostrando quando foi a última contagem implementado.
           \s
            1.2.2
            Correção na lógica de coloração da tabela.
           \s
            1.2.1
            Menu imprimir adicionado ao invés de deixar a função na aba arquivo.
           \s
            1.2.0
            Função de impressão/salvar como pdf implementada.
           \s
            1.1.8
            Correções minoritárias
           \s
            1.1.7
            Prevenção de falhas na criação de produtos.
           \s
            1.1.6
            Correção da coloração na tabela e reestruturação dos arquivos.
           \s
            1.1.5
            Correção de formatação da aba novidades e reformulação do bug report.
           \s
            1.1.4
            Mudanças agora aparecem no popup de atualização, função de relatar falhas adicionada e página para ver versões anteriores adicionada.
          \s
            1.1.3
            Correções da barra de download e instalador.
          \s
            1.1.2
            Aba de novidades de versão implementada.
          \s
            1.1.1
            Barra de progresso e lógica de atualização implementada.
          \s
            1.1.0
            Autocomplete lançado.
          \s
            1.0.4
            Formatação das tabelas aprimorada.
          \s
            1.0.3
            Correções ao atualizador.
          \s
            1.0.2
            Atualizador implementado.
          \s
            1.0.1
            Descrição implementada.""";
}
