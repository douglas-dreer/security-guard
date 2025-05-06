# 📝 Changelog
Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/)
e este projeto adere ao [Versionamento Semântico](https://semver.org/).

## [1.0.3] - 2025-05-05

### ✨ Adicionado
- Documentação JavaDoc completa para todas as classes do projeto
- Testes unitários para o serviço de autenticação com blacklist
- Tratamento global de exceções
- Validação para o campo de username em LoginRequest
- TokenMapper para conversão entre Token e TokenResponse

### 🔄 Alterado
- Atualização de versão para 1.0.3
- Melhorias na documentação e configurações

### 🔒 Segurança
- Integração da blacklist com serviço de autenticação
- Atualização de permissões de acesso para endpoints de autenticação

## [1.0.2] - 2025-05-05

### ✨ Adicionado
- [ead0b3e] 05-05-2025 - : remove unused import in BlacklistService interface
- [55c841c] 05-05-2025 - : atualiza configurações do Flyway e JWT, ajusta porta do servidor e configurações do Springdoc
- [d88aaa6] 05-05-2025 - : adiciona método para extrair data de expiração do token e validação da chave secreta
- [306cac8] 05-05-2025 - : aprimora autenticação e gerenciamento de tokens com validação e blacklist
- [f8da6b5] 05-05-2025 - : implementa modelo e repositório para gerenciamento de tokens
- [a96261d] 05-05-2025 - : adiciona relacionamento com usuário e descrição na blacklist
- [2afd7c5] 05-05-2025 - : adiciona exceções para tratamento de tokens inválidos e não encontrados

### 🔄 Alterado
- [c881a02] 05-05-2025 - chore: version  to 1.0.1

### 🗑️ Removido
- [ead0b3e] 05-05-2025 - feat:  unused import in BlacklistService interface

### 🔒 Segurança
- [d88aaa6] 05-05-2025 - feat: adiciona método para extrair data de expiração do token e validação da chave secreta


[Unreleased]: https://github.com/douglas-dreer/security-guard/compare/v1.0.3...main
[v1.0.3]: https://github.com/douglas-dreer/security-guard/compare/v1.0.2...v1.0.3
[v1.0.2]: https://github.com/douglas-dreer/security-guard/releases/tag/v1.0.2
