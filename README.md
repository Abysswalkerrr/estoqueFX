# 📦 EstoqueFX

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)

**Sistema profissional de gestão de estoque para empresas**

Desenvolvido com JavaFX e integração em nuvem com Supabase

[Funcionalidades](#-funcionalidades) • [Instalação](#-instalação) • [Como Usar](#-como-usar) • [Tecnologias](#-tecnologias)

</div>

---

## ✨ Funcionalidades

### 🏪 Gestão de Estoque
- ✅ **Múltiplos estoques** - Gerencie diferentes estoques para diferentes locais/empresas
- ✅ **Cadastro de produtos** - Nome, categoria, código, quantidade, valor unitário
- ✅ **Entrada e saída** - Controle completo de movimentações
- ✅ **Estoque mínimo** - Configure alertas automáticos quando produtos estão acabando
- ✅ **Categorização** - Organize produtos por categorias personalizadas

### 📊 Relatórios e Dashboard
- 📈 **Dashboard visual** - Gráficos de pizza mostrando distribuição de valor por categoria
- 📉 **Produtos urgentes** - Lista de produtos abaixo do estoque mínimo
- 💰 **Valor total** - Cálculo automático do saldo total do estoque
- 📑 **Exportação CSV** - Exporte seus dados para planilhas

### ☁️ Sincronização em Nuvem
- 🔐 **Login seguro** - Sistema de autenticação via Supabase
- 🔄 **Sincronização automática** - Seus dados salvos na nuvem em tempo real
- 💾 **Backup local** - Dados salvos localmente como segurança
- 🌐 **Acesso de qualquer lugar** - Login em qualquer computador

### 🔧 Recursos Avançados
- 🔄 **Auto-atualização** - Sistema detecta e instala atualizações automaticamente
- 📥 **Importação CSV** - Importe dados de planilhas existentes
- 🖨️ **Impressão** - Imprima listagens de produtos
- 🔍 **Busca avançada** - Pesquise por nome, código ou categoria
- 🎨 **Interface intuitiva** - Design limpo e fácil de usar

---

## 🚀 Instalação

1. Acesse a [página de releases](https://github.com/Abysswalkerrr/estoque_releases/releases)
2. Baixe o instalador mais recente (`.msi` para Windows)
3. Execute o instalador e siga as instruções
4. Pronto! O sistema está instalado

---

## 💻 Como Usar

### Primeiro Acesso

1. **Tela de Login/Registro**
   - Se tiver conta: faça login
   - Caso contrário: registre-se ou use modo offline
   <p align="center">
      <img width="1919" height="1017" alt="image" src="https://github.com/user-attachments/assets/e1a6acf5-dc0f-4001-9413-016b47acf10e" />
   </p>

2. **Criar Estoque**
   - Clique em "Criar Novo Estoque"
   - Digite um nome (ex: "Loja Centro", "Depósito Sul")
   <p align="center">
   <img width="622" height="531" alt="image" src="https://github.com/user-attachments/assets/9f49ea96-a78c-40de-b8fc-1ff3d3a30d28" />
   </p>

3. **Adicionar Produtos**
   - Abra o menu "Estoque"
   - Clique no botão "Criar Produto"
   - Preencha:
     - Nome do produto
     - Categoria
     - Quantidade mínima (para alertas)
     - Valor unitário
     - Quantidade em estoque
    <p align="center">
    <img width="1919" height="1012" alt="image" src="https://github.com/user-attachments/assets/3a0d1a71-1621-41f0-9617-5dc0eeaf6c01" />
    
    <img width="1243" height="606" alt="image" src="https://github.com/user-attachments/assets/46a2ade8-05d0-4162-baba-c9d59ac96a5d" />
    </p>


### Operações Diárias

#### Registrar Entrada de Produtos
```
Menu → Entrada → Selecione produto → Informe quantidade
```

#### Registrar Saída de Produtos
```
Menu → Saída → Selecione produto → Informe quantidade
```

#### Ver Produtos Urgentes
```
Dashboard → Veja o número de produtos urgentes
ou
Marque a checkbox "Apenas Urgentes" na lista
```

#### Exportar Relatório
```
Menu → Exportar CSV → Escolha local para salvar
```

### Trocar de Estoque

```
Menu → Trocar Estoque → Selecione outro estoque da lista
```

⚠️ **Importante**: O sistema pergunta se deseja salvar alterações antes de trocar

---

## 🛠️ Tecnologias

### Core
- **Java 25**
- **JavaFX 25.0.2**
- **Maven**

### Bibliotecas
- **OkHttp 4.12.0** - Cliente HTTP para comunicação com Supabase
- **Gson 2.10.1** - Serialização/deserialização JSON
- **ControlsFX 11.2.2** - Componentes UI avançados
- **FormsFX 11.4.1** - Formulários dinâmicos
- **Ikonli 12.4.0** - Ícones para interface
- **Resend Java 4.12.0** - Envio de emails

### Serviços
- **Supabase** - Serviço de banco de daos

---

## 🎯 Roadmap

### ✅ Versão 1.0 - 1.8
- [x] Autocomplete nos produtos
- [x] Auto-atualização
- [x] Histórico de atualizações
- [x] Impressão
- [x] Conversão para PDF
- [x] Pesquisa
- [x] Importar e exportar em formato .CSV(excel)
- [x] Filtro exclusivo de categorias

### ✅ Versão 2.0 - 2.1
- [x] Reforma visual total da tabela
- [x] Dashboard com gráficos
- [x] Otimização de processamento e memória
- [x] Melhorias de qualidade de vida
- [x] Inúmeras correções de falhas

### ✅ Versão 3.0 (Atual)
- [x] Sincronização com nuvem
- [x] Sistema de login(múltiplas contas)

### 🚧 Versão 3.1+ (Em breve)
- [x] Histórico de movimentações
- [ ] Relatórios
- [x] Backup automático
- [ ] Autenticação de dois fatores

### 🔮 Versão 4.0+ (Futuro)
- [ ] Módulo de fornecedores
- [ ] Código de barras/etiquetas
- [ ] Permissões de usuário (admin/operador)
- [ ] App mobile
- [ ] Configurações robustas

---

Preços

### 🆓 Versão Atual (Beta Público)
Durante o período de desenvolvimento, o sistema está disponível gratuitamente para testes.

**Recursos Disponíveis:**
- Todos os recursos atuais
- Sincronização Supabase (configure suas próprias credenciais)
- Atualizações automáticas

**Limitações:**
- Sem suporte técnico oficial
- Bugs podem ocorrer
- Funcionalidades podem mudar

### 🚀 Futuro (2026-2027)

Após o lançamento oficial, o EstoqueFX será comercializado com planos de assinatura:

- **Community** - Gratuito limitado
- **Básico** - ~R$ 29,90/mês
- **Profissional** - ~R$ 79,90/mês  
- **Enterprise** - Sob consulta

Usuários beta terão condições especiais no lançamento! 🎉

📧 Interessado em licenciamento futuro? Entre em contato via o email: arthurmsouza321@gmail.com

---

## 🐛 Reportar Bugs

Encontrou um problema? [Abra uma issue](https://github.com/Abysswalkerrr/estoque_releases/issues/new) ou [Preencha um formulário](https://docs.google.com/forms/d/e/1FAIpQLSd_phUkuqlleT4CsKnvZPnEruQDdZK7qeCkvGU3HXa8D6ruWw/viewform?usp=dialog) com:

- **Descrição clara** do problema
- **Passos para reproduzir**
- **Comportamento esperado** vs **comportamento atual**
- **Screenshots** (se aplicável)
- **Versão do sistema** (visível em Menu → Sobre)

---

## 💡 Sugestões de Melhorias

Tem ideias para melhorar o sistema? 

1. [Abra uma issue](https://github.com/Abysswalkerrr/estoqueFX/issues/new) com a tag `enhancement` ou [Preencha um formulário](https://forms.gle/tCGuZcpAEiZRfsRo8)
2. Descreva sua sugestão em detalhes
3. Explique por que seria útil

---

## ⭐ Apoie o Projeto

Se este projeto foi útil para você, considere:

- ⭐ Aproveitar o máximo possível
- 🐛 Reportar bugs
- 💡 Sugerir melhorias
- 📢 Compartilhar com outros

---

## 📄 Licença

**Licença de Uso Não Comercial - EstoqueFX**

Copyright © 2026 Valube

Este código está disponível publicamente apenas para fins educacionais.

### Permitido:
✅ Visualizar o código-fonte
✅ Estudar a implementação

### NÃO Permitido:
❌ Uso comercial
❌ Redistribuição
❌ Modificação e distribuição de versões derivadas
❌ Uso em produção sem licença paga

**Violações serão processadas de acordo com a lei brasileira de direitos autorais (Lei 9.610/98).**

---

## 👤 Autor

**Arthur Meneghel de Souza**

- GitHub: [@Abysswalkerrr](https://github.com/Abysswalkerrr)
- Email: arthurmsouza321@gmail.com

