# Changelog

Este arquivo documenta todas as alterações relevantes do módulo **Security Guard** do framework **Soejin**, seguindo o padrão [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o versionamento [SemVer](https://semver.org/lang/pt-BR/spec/v2.0.0.html). Utilizamos **ícones** para facilitar a identificação de cada categoria de mudança.

---

## [0.1.1]  2025-04-29
- `0d3742a` 📝 **docs:** Adicionado `CHANGELOG.md`
- 📝 **docs:** Adicionado `README.md`
- 🎯 **chore:** Preparação da versão **0.1.1-SNAPSHOT**
    - Atualizar dependências de segurança (Spring Security, JWT)
    - Refinar fluxo de autenticação multi-sistema
    - Ampliar cobertura de testes e tratar edge cases

---

## [0.1.0] – 2025-04-29

### 🎉 Funcionalidades
- `646c05e` **feat:** Esqueleto do CRUD de Usuário e Autenticação
    - 🧩 Criação das entidades **User** e **Role**
    - 🔗 Endpoints REST para operações de usuário (criar, ler, atualizar, excluir)
    - 🔄 Mapeamento de DTOs com MapStruct para request/response

### 🗄️ Migrações de Banco de Dados
- `70b2daf` **migration:** Configuração inicial do Flyway
    - 📦 Script SQL para criação de tabelas `users`, `roles` e relacionamentos
    - 🧪 Inserção de dados de exemplo para ambiente de desenvolvimento

### ⚙️ Configurações e Infraestrutura
- `7757913` **config:** Ajustes em `application.yml`
    - 🌱 Perfis **dev** e **prod** configurados
    - 🔌 Integração de DataSource, JPA e Flyway
- `d8c0dac` **config:** Rotas e segurança com Spring Security
    - 🚧 Definição de endpoints públicos e protegidos
    - 🔑 Filtro JWT para autenticação stateless

### ✅ Testes
- `91548d1` **test:** Teste unitário inicial para login
    - ✔️ Cenários de sucesso e falha na autenticação
    - 🔍 Validação do token JWT gerado

### 🧹 Chores e Setup
- `8bf4fa0` **chore:** Commit inicial do projeto
    - 📁 Estrutura de diretórios organizada (`src/main`, `src/test`)
    - 📦 Dependências essenciais: Spring Boot, Spring Security, Flyway

---

> **Data de geração:** 2025-04-29  
> _Mantenha este changelog sempre atualizado conforme novas versões forem liberadas._

---

## 📖 Legenda de Ícones

- 🎉: Novas funcionalidades
- 🗄️: Migrações de Banco de Dados
- ⚙️: Configurações e Infraestrutura
- ✅: Testes
- 🧹: Chores e Setup
- 🎯: Preparação e manutenção da versão
- 📝: Documentação
- 🧩: Modelagem de entidades
- 🔗: Endpoints REST
- 🔄: Mapeamento de DTOs
- 📦: Scripts de migração SQL
- 🧪: Dados de exemplo
- 🌱: Perfis de ambiente
- 🔌: Integração de componentes
- 🚧: Configuração de rotas
- 🔑: Autenticação JWT

