# 📦 EstoqueFX

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

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

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java JDK 25** ou superior
  - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** (para compilar do código-fonte)
  - [Download Maven](https://maven.apache.org/download.cgi)
- **Conta Supabase** (opcional, para sincronização em nuvem)
  - [Criar conta grátis](https://supabase.com/)

---

## 🚀 Instalação

### Opção 1: Baixar Executável (Recomendado)

1. Acesse a [página de releases](https://github.com/Abysswalkerrr/estoqueFX/releases)
2. Baixe o instalador mais recente (`.exe` para Windows)
3. Execute o instalador e siga as instruções
4. Pronto! O sistema está instalado

### Opção 2: Compilar do Código-Fonte

1. **Clone o repositório**
```bash
git clone https://github.com/Abysswalkerrr/estoqueFX.git
cd estoqueFX
```

2. **Configure as variáveis de ambiente** (opcional, para usar Supabase)
```bash
# Windows
setx SUPABASE_URL "https://seu-projeto.supabase.co"
setx SUPABASE_KEY "sua-chave-anon-aqui"

# Linux/Mac
export SUPABASE_URL="https://seu-projeto.supabase.co"
export SUPABASE_KEY="sua-chave-anon-aqui"
```

3. **Compile o projeto**
```bash
mvn clean install
```

4. **Execute**
```bash
java -jar desktop-app/target/desktop-app-1.0.0.jar
```

---

## 💻 Como Usar

### Primeiro Acesso

1. **Tela de Login/Registro**
   - Se tiver conta Supabase: faça login
   - Caso contrário: registre-se ou use modo offline

2. **Criar Estoque**
   - Clique em "Criar Novo Estoque"
   - Digite um nome (ex: "Loja Centro", "Depósito Sul")

3. **Adicionar Produtos**
   - Clique no botão "Criar Produto"
   - Preencha:
     - Nome do produto
     - Categoria
     - Quantidade mínima (para alertas)
     - Valor unitário
     - Quantidade inicial

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

## 🗂️ Estrutura do Projeto

```
estoqueFX/
├── desktop-app/          # Aplicação principal JavaFX
│   ├── src/main/java/
│   │   └── com/estoquefx/
│   │       ├── controller/   # Controllers (MVC)
│   │       ├── model/        # Modelos de dados
│   │       ├── service/      # Serviços (Supabase, etc)
│   │       ├── data/         # Persistência local
│   │       └── util/         # Utilitários
│   └── src/main/resources/
│       └── com/estoquefx/    # Arquivos FXML, CSS
├── updater-core/         # Sistema de auto-atualização
├── estoque-dist/         # Configuração de distribuição
└── pom.xml               # Configuração Maven
```

---

## 🛠️ Tecnologias

### Core
- **Java 25** - Linguagem de programação
- **JavaFX 25.0.2** - Framework para interface gráfica
- **Maven** - Gerenciamento de dependências e build

### Bibliotecas
- **OkHttp 4.12.0** - Cliente HTTP para comunicação com Supabase
- **Gson 2.10.1** - Serialização/deserialização JSON
- **ControlsFX 11.2.2** - Componentes UI avançados
- **FormsFX 11.4.1** - Formulários dinâmicos
- **Ikonli 12.4.0** - Ícones para interface
- **Resend Java 4.12.0** - Envio de emails

### Serviços
- **Supabase** - Backend as a Service (autenticação + banco de dados PostgreSQL)

---

## 📊 Estrutura do Banco de Dados (Supabase)

### Tabela: `estoques`
```sql
CREATE TABLE estoques (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES auth.users(id),
  nome TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

### Tabela: `produtos`
```sql
CREATE TABLE produtos (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  estoque_id UUID REFERENCES estoques(id) ON DELETE CASCADE,
  codigo TEXT NOT NULL,
  nome TEXT NOT NULL,
  categoria TEXT NOT NULL,
  qtd_min INTEGER NOT NULL,
  valor_unitario DECIMAL(10,2) NOT NULL,
  quantidade INTEGER NOT NULL,
  descricao TEXT,
  ultima_alteracao TIMESTAMP DEFAULT NOW(),
  UNIQUE(estoque_id, codigo)
);
```

---

## 🎯 Roadmap

### ✅ Versão 3.0 (Atual)
- [x] Sistema de múltiplos estoques
- [x] Integração com Supabase
- [x] Auto-atualização
- [x] Dashboard com gráficos
- [x] Importação/Exportação CSV

### 🚧 Versão 3.1 (Em breve)
- [ ] Histórico de movimentações
- [ ] Relatórios em PDF
- [ ] Filtros avançados
- [ ] Backup automático

### 🔮 Versão 4.0 (Futuro)
- [ ] Módulo de fornecedores
- [ ] Notas fiscais
- [ ] Código de barras
- [ ] Permissões de usuário (admin/operador)
- [ ] API REST
- [ ] App mobile

---

## 🐛 Reportar Bugs

Encontrou um problema? [Abra uma issue](https://github.com/Abysswalkerrr/estoqueFX/issues/new) com:

- **Descrição clara** do problema
- **Passos para reproduzir**
- **Comportamento esperado** vs **comportamento atual**
- **Screenshots** (se aplicável)
- **Versão do sistema** (visível em Menu → Sobre)

---

## 💡 Sugestões de Melhorias

Tem ideias para melhorar o sistema? 

1. [Abra uma issue](https://github.com/Abysswalkerrr/estoqueFX/issues/new) com a tag `enhancement`
2. Descreva sua sugestão em detalhes
3. Explique por que seria útil

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. **Fork** o projeto
2. Crie uma **branch** para sua feature (`git checkout -b feature/MinhaFeature`)
3. **Commit** suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. **Push** para a branch (`git push origin feature/MinhaFeature`)
5. Abra um **Pull Request**

### Diretrizes
- Código limpo e comentado
- Mantenha o padrão MVC existente
- Teste suas alterações antes de commitar
- Descreva detalhadamente o que foi alterado no PR

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👤 Autor

**Arthur**

- GitHub: [@Abysswalkerrr](https://github.com/Abysswalkerrr)
- Email: arthurmsouza321@gmail.com

---

## ⭐ Apoie o Projeto

Se este projeto foi útil para você, considere:

- ⭐ Dar uma estrela no repositório
- 🐛 Reportar bugs
- 💡 Sugerir melhorias
- 🤝 Contribuir com código
- 📢 Compartilhar com outros

---

<div align="center">

**Desenvolvido com ❤️ por Arthur**

© 2026 EstoqueFX - Todos os direitos reservados

</div>