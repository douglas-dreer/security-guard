#!/usr/bin/env python3
"""
Script de Atualização de Versão - Automatiza a geração de changelog e o gerenciamento de versões
"""

import os
import re
import sys
import subprocess
from datetime import datetime
import shutil

# Configuração
COMMIT_HISTORY_FILE = "commits.log"
CHANGELOG_FILE = "CHANGELOG.md"
README_FILE = "README.md"
VERSION_FILE = "version.txt"

# Ícones para documentação
CHANGELOG_ICON = "📝"  # Ícone de documento para changelog
README_ICON = "📘"     # Ícone de livro para README
VERSION_ICON = "🏷️"    # Ícone de etiqueta para versão
COMMIT_ICON = "✅"     # Ícone de marca de verificação para commit

# Códigos de cores ANSI para saída de terminal
class Colors:
    RESET = "\033[0m"
    RED = "\033[91m"
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    MAGENTA = "\033[95m"
    CYAN = "\033[96m"
    WHITE = "\033[97m"

def print_colored(message, color=Colors.WHITE, icon=""):
    """Imprime uma mensagem colorida com um prefixo de ícone opcional."""
    if icon:
        print(f"{icon} {color}{message}{Colors.RESET}")
    else:
        print(f"{color}{message}{Colors.RESET}")

def check_git_installation():
    """Verifica se o Git está instalado e disponível."""
    try:
        git_version = subprocess.check_output(["git", "--version"], universal_newlines=True).strip()
        print_colored(f"Git encontrado: {git_version}", Colors.GREEN, "✓")
        return True
    except (subprocess.SubprocessError, FileNotFoundError):
        print_colored("Git não está instalado ou não está no PATH", Colors.RED, "✗")
        return False

def check_remote_changes():
    """Verifica se há alterações remotas que precisam ser puxadas antes de realizar um push."""
    print_colored("Verificando alterações remotas...", Colors.CYAN, "🔄")
    
    try:
        # Executa git fetch para atualizar referências remotas
        subprocess.check_call(["git", "fetch"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print_colored("Fetch realizado com sucesso", Colors.GREEN, "✓")
        
        # Verifica se existem commits remotos que não estão no repositório local
        local_commit = subprocess.check_output(["git", "rev-parse", "HEAD"], universal_newlines=True).strip()
        remote_commit = subprocess.check_output(["git", "rev-parse", "@{u}"], universal_newlines=True).strip()
        
        if local_commit != remote_commit:
            print_colored("Existem alterações remotas que não estão no repositório local.", Colors.YELLOW, "⚠️")
            print_colored("Execute git pull antes de fazer push.", Colors.YELLOW, "⚠️")
            return False
        
        print_colored("Repositório local está atualizado com o remoto.", Colors.GREEN, "✓")
        return True
    except subprocess.SubprocessError as e:
        print_colored(f"Erro ao verificar alterações remotas: {e}", Colors.RED, "❌")
        return False

def perform_pull():
    """Executa git pull para atualizar o repositório local."""
    print_colored("Executando git pull para atualizar o repositório local...", Colors.CYAN, "⬇️")
    
    try:
        pull_result = subprocess.check_output(["git", "pull"], universal_newlines=True).strip()
        print_colored(f"Pull realizado com sucesso: {pull_result}", Colors.GREEN, "✓")
        return True
    except subprocess.SubprocessError as e:
        print_colored(f"Erro ao executar git pull: {e}", Colors.RED, "❌")
        return False

def get_recent_commits(count=10):
    """Obtém commits recentes do histórico do Git."""
    print_colored("Obtendo commits recentes...", Colors.CYAN, "🔍")
    
    # Executa fetch e pull antes de obter commits
    check_remote_changes()
    perform_pull()
    
    if not os.path.exists(COMMIT_HISTORY_FILE):
        print_colored(f"Arquivo {COMMIT_HISTORY_FILE} não encontrado. Obtendo commits do Git...", Colors.YELLOW, "⚠️")
        try:
            git_log = subprocess.check_output(
                ["git", "log", f"-n{count}", "--pretty=format:%H|%ad|%s", "--date=format:%d-%m-%Y"],
                universal_newlines=True
            )
            with open(COMMIT_HISTORY_FILE, "w", encoding="utf-8") as f:
                f.write(git_log)
            print_colored(f"Commits salvos em {COMMIT_HISTORY_FILE} com sucesso!", Colors.GREEN, "✓")
        except subprocess.SubprocessError as e:
            raise Exception(f"Erro ao executar git log: {e}")
    
    commits = []
    with open(COMMIT_HISTORY_FILE, "r", encoding="utf-8") as f:
        for line in f:
            parts = line.split("|", 2)
            if len(parts) >= 3:
                commits.append({
                    "hash": parts[0],
                    "date": parts[1],
                    "message": parts[2].strip()
                })
    
    if not commits:
        raise Exception(f"Nenhum commit encontrado em {COMMIT_HISTORY_FILE}")
    
    print_colored(f"Commits carregados com sucesso: {len(commits)} encontrados.", Colors.GREEN, "✓")
    return commits

def get_section_content(commits, type_pattern):
    """Obtém conteúdo para uma seção específica do changelog."""
    section_content = ""
    pattern = re.compile(type_pattern, re.IGNORECASE)
    
    for commit in commits:
        if pattern.search(commit["message"]):
            # Substitui o padrão correspondente por uma string vazia (apenas a primeira ocorrência)
            message = pattern.sub("", commit["message"], count=1).strip()
            section_content += f"- [{commit['hash'][:7]}] {commit['date']} - {message}\n"
    
    return section_content if section_content else None

def update_changelog(commits):
    """Atualiza o arquivo de changelog com informações de commit."""
    print_colored("Atualizando changelog...", Colors.CYAN, f"{CHANGELOG_ICON}")
    
    types = {
        "Adicionado": r"(add|feat|new)",
        "Alterado": r"(change|update|modify)",
        "Corrigido": r"(fix|bug|repair)",
        "Removido": r"(remove|delete|rm)",
        "Segurança": r"(security|sec|cve)",
        "Testes": r"(test|spec|check)"
    }
    
    type_icons = {
        "Adicionado": "✨",
        "Alterado": "🔄",
        "Corrigido": "🐛",
        "Removido": "🗑️",
        "Segurança": "🔒",
        "Testes": "🧪"
    }

    changelog_content = f"# {CHANGELOG_ICON} Changelog\n"
    changelog_content += "Todas as alterações notáveis neste projeto serão documentadas neste arquivo.\n"
    changelog_content += "O formato é baseado em [Keep a Changelog](https://keepachangelog.com/)\n"
    changelog_content += "e este projeto adere ao [Versionamento Semântico](https://semver.org/).\n\n"

    current_version = get_version()
    today = datetime.now().strftime("%Y-%m-%d")
    changelog_content += f"## [{current_version}] - {today}\n\n"

    for type_name, pattern in types.items():
        section_content = get_section_content(commits, pattern)
        if section_content:
            icon = type_icons.get(type_name, "•")
            changelog_content += f"### {icon} {type_name}\n{section_content}\n"

    # Tenta obter o nome do repositório do Git
    try:
        remote_url = subprocess.check_output(
            ["git", "config", "--get", "remote.origin.url"],
            universal_newlines=True
        ).strip()
        
        # Extrai usuário/repo da URL remota
        repo_name_match = re.search(r'[:/]([^/]+)/([^/.]+)(\.git)?$', remote_url)
        if repo_name_match:
            user, repo = repo_name_match.groups()[0:2]
            repo_path = f"{user}/{repo}"
            
            changelog_content += f"\n[Unreleased]: https://github.com/{repo_path}/compare/v{current_version}...main\n"
            changelog_content += f"[v{current_version}]: https://github.com/{repo_path}/releases/tag/v{current_version}\n"
    except subprocess.SubprocessError:
        print_colored("Não foi possível determinar a URL do repositório. Omitindo links.", Colors.YELLOW, "⚠️")

    # Backup do changelog existente
    if os.path.exists(CHANGELOG_FILE):
        shutil.copy2(CHANGELOG_FILE, f"{CHANGELOG_FILE}.bak")
        print_colored("Backup do changelog anterior criado.", Colors.YELLOW, "💾")
    
    with open(CHANGELOG_FILE, "w", encoding="utf-8") as f:
        f.write(changelog_content)
    
    print_colored("Changelog atualizado com sucesso!", Colors.GREEN, "✓")

def update_readme(commits):
    """Atualiza o arquivo README com informações do projeto."""
    print_colored("Atualizando README...", Colors.CYAN, f"{README_ICON}")
    
    readme_content = f"# {README_ICON} Meu Projeto\n"
    readme_content += "## 📋 Sobre\n"
    readme_content += "Este é um projeto incrível que visa [descrição do projeto].\n\n"
    readme_content += "## ✨ Funcionalidades\n"
    
    features = []
    for commit in commits:
        if re.search(r"add|feat|new", commit["message"], re.IGNORECASE):
            feature = re.sub(r"add|feat|new", "", commit["message"], flags=re.IGNORECASE, count=1).strip()
            features.append(f"* {feature}")
    
    if features:
        readme_content += "\n".join(features) + "\n\n"
    
    readme_content += f"## {VERSION_ICON} Versão\n"
    readme_content += f"Versão atual: **{get_version()}**\n\n"
    readme_content += f"## {CHANGELOG_ICON} Histórico de Versões\n"
    readme_content += "Para ver o histórico de versões, clique [aqui](CHANGELOG.md).\n\n"
    readme_content += "## 👥 Contribuindo\n"
    readme_content += "Contribuições são bem-vindas! Para mais detalhes, leia o arquivo [CONTRIBUTING.md](CONTRIBUTING.md).\n\n"
    readme_content += "## 📄 Licença\n"
    readme_content += "Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.\n"

    # Backup do README existente
    if os.path.exists(README_FILE):
        shutil.copy2(README_FILE, f"{README_FILE}.bak")
        print_colored("Backup do README anterior criado.", Colors.YELLOW, "💾")
    
    with open(README_FILE, "w", encoding="utf-8") as f:
        f.write(readme_content)
    
    print_colored("README atualizado com sucesso!", Colors.GREEN, "✓")

def get_version():
    """Obtém a versão atual do arquivo de versão."""
    print_colored("Obtendo versão atual...", Colors.CYAN, f"{VERSION_ICON}")
    
    if os.path.exists(VERSION_FILE):
        with open(VERSION_FILE, "r") as f:
            version = f.read().strip()
        print_colored(f"Versão atual: {version}", Colors.GREEN, f"{VERSION_ICON}")
        return version
    else:
        print_colored("Versão não encontrada. Iniciando com a versão 1.0.0.", Colors.YELLOW, "⚠️")
        with open(VERSION_FILE, "w") as f:
            f.write("1.0.0")
        return "1.0.0"

def increment_version(version):
    """Incrementa o número da versão (versão de patch)."""
    print_colored("Incrementando versão...", Colors.CYAN, "🔢")
    
    version_parts = version.split(".")
    if len(version_parts) < 3:
        version_parts.extend(["0"] * (3 - len(version_parts)))
    
    version_parts[2] = str(int(version_parts[2]) + 1)
    
    return ".".join(version_parts)

def update_version():
    """Atualiza o arquivo de versão com um número de versão incrementado."""
    print_colored("Atualizando versão...", Colors.CYAN, f"{VERSION_ICON}")
    
    current_version = get_version()
    new_version = increment_version(current_version)
    
    with open(VERSION_FILE, "w") as f:
        f.write(new_version)
    
    print_colored(f"Versão atualizada para {new_version}", Colors.GREEN, f"{VERSION_ICON}")
    return new_version

def check_pending_push():
    """Verifica se existem commits locais que precisam ser enviados ao remoto."""
    print_colored("Verificando commits pendentes para push...", Colors.CYAN, "🔍")
    
    try:
        # Executa git branch -v para ver se está à frente do remoto
        branch_info = subprocess.check_output(["git", "branch", "-v"], universal_newlines=True)
        
        # Verifica se existe "ahead" no branch atual
        if re.search(r"\s+ahead\s+", branch_info):
            return True
        
        return False
    except subprocess.SubprocessError as e:
        print_colored(f"Erro ao verificar commits pendentes: {e}", Colors.RED, "❌")
        return False

def commit_and_push(commit_message):
    """Realiza commit das alterações e push para o repositório remoto."""
    print_colored("Preparando para fazer commit...", Colors.CYAN, "🔄")
    
    # Verifica se há alterações para commit
    git_status = subprocess.check_output(["git", "status", "--porcelain"], universal_newlines=True)
    if not git_status:
        print_colored("Não há alterações para fazer commit.", Colors.YELLOW, "⚠️")
        return
    
    # Realiza fetch e pull antes do commit
    check_remote_changes()
    perform_pull()
    
    # Verifica se há commits pendentes para push
    if check_pending_push():
        print_colored("ERRO: Existem commits pendentes para push.", Colors.RED, "❌")
        print_colored("Por favor, envie esses commits antes de continuar.", Colors.RED, "⚠️")
        return False
    
    # Adiciona todas as alterações
    subprocess.check_call(["git", "add", "."])
    
    # Faz commit das alterações
    print_colored(f"Fazendo commit com a mensagem: {commit_message}", Colors.CYAN, f"{COMMIT_ICON}")
    try:
        subprocess.check_call(["git", "commit", "-m", commit_message])
        print_colored("Commit realizado com sucesso!", Colors.GREEN, "✓")
    except subprocess.SubprocessError as e:
        raise Exception(f"Erro ao fazer commit: {e}")
    
    # Push das alterações
    print_colored("Enviando alterações para o repositório remoto...", Colors.CYAN, "☁️")
    try:
        subprocess.check_call(["git", "push"])
        print_colored("Alterações enviadas com sucesso!", Colors.GREEN, "✓")
    except subprocess.SubprocessError as e:
        raise Exception(f"Erro ao fazer push: {e}")
    
    return True

def cleanup_temp_files():
    """Remove arquivos temporários."""
    print_colored("Limpando arquivos temporários...", Colors.CYAN, "🧹")
    
    if os.path.exists(COMMIT_HISTORY_FILE):
        os.remove(COMMIT_HISTORY_FILE)
        print_colored("Arquivo temporário removido.", Colors.YELLOW, "✓")

def main():
    """Fluxo principal de execução."""
    try:
        # Verifica se o Git está instalado
        if not check_git_installation():
            raise Exception("Git não está instalado ou não está disponível no PATH. Por favor, instale o Git e tente novamente.")
        
        # Verifica a configuração do Git
        print_colored("Verificando configuração do Git...", Colors.CYAN, "⚙️")
        try:
            git_user_name = subprocess.check_output(
                ["git", "config", "--global", "user.name"],
                universal_newlines=True
            ).strip()
            git_user_email = subprocess.check_output(
                ["git", "config", "--global", "user.email"],
                universal_newlines=True
            ).strip()
            
            print_colored(f"Git configurado: {git_user_name} <{git_user_email}>", Colors.GREEN, "✓")
        except subprocess.SubprocessError:
            print_colored("Configuração do Git incompleta. Por favor, configure:", Colors.YELLOW, "⚠️")
            print_colored("git config --global user.name 'Seu Nome'", Colors.YELLOW)
            print_colored("git config --global user.email 'seu.email@exemplo.com'", Colors.YELLOW)
            
            if input("Configurar Git agora? (s/n): ").lower() == 's':
                user_name = input("Digite seu nome: ")
                user_email = input("Digite seu email: ")
                
                subprocess.check_call(["git", "config", "--global", "user.name", user_name])
                subprocess.check_call(["git", "config", "--global", "user.email", user_email])
                print_colored("Git configurado com sucesso!", Colors.GREEN, "✓")
            else:
                print_colored("Continuando sem configurar o Git...", Colors.YELLOW, "⚠️")
        
        # Verifica se estamos em um repositório Git
        try:
            subprocess.check_output(["git", "rev-parse", "--is-inside-work-tree"], stderr=subprocess.STDOUT)
        except subprocess.SubprocessError:
            print_colored("Não está em um repositório Git. Inicializando...", Colors.YELLOW, "⚠️")
            subprocess.check_call(["git", "init"])
            print_colored("Repositório Git inicializado!", Colors.GREEN, "✓")
        
        # Carrega commits
        print_colored("Carregando commits...", Colors.CYAN, "📂")
        commits = get_recent_commits()
        if not commits:
            raise Exception("Nenhum commit encontrado. Faça pelo menos um commit antes de usar este script.")
        
        # Atualiza changelog
        update_changelog(commits)
        
        # Atualiza README
        update_readme(commits)
        
        # Atualiza versão
        new_version = update_version()
        
        # Prepara mensagem de commit
        commit_message = f"chore: atualização de versão para {new_version}"
        
        # Pergunta se o usuário deseja fazer commit
        if input("Deseja fazer commit das alterações? (s/n): ").lower() == 's':
            commit_and_push(commit_message)
        else:
            print_colored("As alterações não foram commitadas.", Colors.YELLOW, "ℹ️")
        
        # Limpa arquivos temporários
        cleanup_temp_files()
        
        print_colored("Processo concluído com sucesso!", Colors.GREEN, "🎉")
        
    except Exception as e:
        print_colored(f"Erro: {str(e)}", Colors.RED, "❌")
        import traceback
        print_colored(f"Local: {traceback.format_exc()}", Colors.RED, "📍")
        cleanup_temp_files()
        sys.exit(1)

if __name__ == "__main__":
    main()