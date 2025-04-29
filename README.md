# Security Guard — Doce de Leite (v0.1.0-SNAPSHOT)

**Security Guard** é um módulo de autenticação e autorização do framework **Soejin**, desenvolvido com **Java 21**, **Spring Boot** e **Spring Security**. Ele fornece uma base robusta e extensível para o gerenciamento de usuários, controle de acesso, definição de perfis de usuários e suporte a múltiplos sistemas.

> 🍬 **Codinome da versão**: Doce de Leite — uma homenagem à tradição mineira.

---

## ✨ Funcionalidades

- 🔒 Autenticação baseada em JWT
- 👤 Gerenciamento de usuários
- 🢑 Controle de roles e permissões
- 🌐 Suporte a múltiplos sistemas (multi-tenant logic)
- 🔄 Rotinas de redefinição de senha e expiração
- 🥒 Registro de login e controle de status de conta
- 📦 Pronto para integração com outros módulos do **Soejin Framework**

---

## ⚙️ Tecnologias

- **Java 21**
- **Spring Boot 3**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Token)**

---

## 📁 Estrutura Padrão

```
src/
 ├── main/
 │   ├── java/
 │   │   └── br/com/soejin/framework/security_guard/
 │   │       ├── configs/         📑 Configurações de segurança
 │   │       ├── controller/      📡 Controladores de API
 │   │       │   ├── api/          🔗 Endpoints de autenticação
 │   │       │   ├── mapper/       🔄 Mapeadores de DTOs
 │   │       │   ├── request/      📥 Requisições da API
 │   │       │   └── response/     📤 Respostas da API
 │   │       ├── exception/        🚨 Tratamento de exceções
 │   │       ├── model/            🧩 Modelos de domínio
 │   │       ├── repository/       💾 Repositórios JPA
 │   │       ├── service/          🔧 Serviços
 │   │       │   └── impl/         🛠️ Implementações de serviços
 │   │       └── util/             🧰 Utilitários
 │   └── resources/
 │       ├── db/
 │       │   └── migration/        🗄️ Scripts de migração Flyway
 │       ├── static/               🖼️ Arquivos estáticos (se aplicável)
 │       └── templates/            📝 Templates (se aplicável)
 └── test/
     └── java/
         └── br/com/soejin/framework/security_guard/  🧪 Testes unitários e de integração
```

---

## 🚀 Execução

Para rodar o projeto localmente:

```bash
  ./gradlew clean bootRun
```

---

## 🧪 Testes

Execute os testes com cobertura:

```bash
  ./gradlew clean test
```

---

## 📌 Requisitos

- Java 21
- PostgreSQL 15+
- Gradle 8+

---

## 📃 Documentação

A documentação completa estará disponível em breve como parte da suite **Soejin**.

---

## 🤝 Contribuindo

Este módulo é mantido pela equipe core do Soejin Framework. Contribuições são bem-vindas via pull requests seguindo as [convenções de commits](https://www.conventionalcommits.org/pt-br/v1.0.0/).

---

## 📟 Licença

Distribuído sob a licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.

