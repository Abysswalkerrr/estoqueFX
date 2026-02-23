package com.estoquefx.updater.core;

public class AppInfo {
    public static final String NOME_APP =
            "SistemaEstoqueFX";
    public static final String VERSAO   =
            "3.3.4.0";

    public static final String BUG_REPORT_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSd_phUkuqlleT4CsKnvZPnEruQDdZK7qeCkvGU3HXa8D6ruWw/viewform?usp=dialog";

    public static final String SUGGESTIONS_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSdbPIgxiZlIa5eCqGVBFEs1BI2vEEjZvDG-zoP6pcg59yVlFw/viewform";

    public static final String UPDATE_URL = getUpdateUrl();

    public static final String RELEASES_URL =
            "https://api.github.com/repos/Abysswalkerrr/estoque_releases/releases";

    public static final String LATEST_RELEASES_URL =
            "https://api.github.com/repos/abysswalkerrr/estoque_releases/releases/latest";

    private static String getUpdateUrl() {
        return LATEST_RELEASES_URL;
    }

    public static String novidades = """
            3.3.4.0
            Ícones adicionados.\n
            3.3.3.0
            Botões de atualizar reimaginados.\n
            3.3.2.0
            Descrição funcional na aba de patrimônio.\n
            3.3.1.0
            Botões recolocados na tabela principal.\n
            3.3.0.0
            Aba de controle de patrimônios adicionada.\n
            3.2.0.1
            Troca da descrição por localização.\n
            3.2.0.0
            Salvamento automático ao fechar o app implementado.\n
            3.1.1.0
            Opção de mostrar/esconder senha implementada.\n
            3.1.0.x
            Reestruturações e otimizações de código(resultando em menores consumos de memória e CPU).\n
            3.1.0.0
            Lançada a aba de histórico de movimentações.\n
            3.0.4
            Agora o app cria uma pasta pra cada estoque, permitindo mais flexibilidade na gestão.\n
            3.0.0.3
            Correção de um erro que fazia com que a opção salvar estoque, quando usada dentro do trocar estoque, acabava deletando tudo.\n
            3.0.0.2
            Registro de contas corrigido.\n
            3.0.0.1
            Correções minoritárias\n
            3.0.0.0
            Implementação de login e salvar na nuvem. Pode ser que seja exigida conexão à internet para usar o programa.\n
            2.0.0
            Aba de relatórios implementada, sendo composta de estatísticas, gráficos e tabelas a respeito do estoque.\n
            1.8.1
            Adição de um buscador exclusivo para categorias.\n
            1.8.0
            Adição de abas(não implementadas) e novas opções de filtragem.\n
            1.7.1
            Correções na lógica de importação dos arquivos CSV.\n
            1.7.0
            Importação de arquivos .csv(excel) implementada.\n
            1.6.1
            Correção de falhas em potencial.\n
            1.6.0
            Melhoria no exportarCSV e opção de abrir pasta de dados.\n
            1.5.2
            Agora é possível pesquisar "urgente"(indiferente se for maiúsculo ou minúsculo) para filtrar os produtos com estoque baixo.\n
            1.5.1
            Correções minoritárias.\n
            1.5.0
            Coluna de última alteração adicionada e uso de versões experimentais permitido.\n
            1.4.2
            Correção de falhas relacionadas a ausência de arquivos.\n
            1.4.1
            Melhoria da função de avisar atualizações.\n
            1.4.0
            Saldo total implementado.\n
            1.3.3
            Adicionada a opção de ignorar o pedido de atualizar.\n
            1.3.2
            Correção de erros.\n
            1.3.1
            Correção da lógica de última atualização.\n
            1.3.0
            Texto mostrando quando foi a última contagem implementado.\n
            1.2.2
            Correção na lógica de coloração da tabela.\n
            1.2.1
            Menu imprimir adicionado ao invés de deixar a função na aba arquivo.\n
            1.2.0
            Função de impressão/salvar como pdf implementada.\n
            1.1.8
            Correções minoritárias\n
            1.1.7
            Prevenção de falhas na criação de produtos.\n
            1.1.6
            Correção da coloração na tabela e reestruturação dos arquivos.\n
            1.1.5
            Correção de formatação da aba novidades e reformulação do bug report.\n
            1.1.4
            Mudanças agora aparecem no popup de atualização, função de relatar falhas adicionada e página para ver versões anteriores adicionada.\n
            1.1.3
            Correções da barra de download e instalador.\n
            1.1.2
            Aba de novidades de versão implementada.\n
            1.1.1
            Barra de progresso e lógica de atualização implementada.\n
            1.1.0
            Autocomplete lançado.\n
            1.0.4
            Formatação das tabelas aprimorada.\n
            1.0.3
            Correções ao atualizador.\n
            1.0.2
            Atualizador implementado.\n
            1.0.1
            Descrição implementada.""";
}
