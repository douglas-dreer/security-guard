#!/usr/bin/env python3
"""
Script de Atualização de Versão - Automatiza a geração de changelog e o gerenciamento de versões

Este script fornece funcionalidades para:
- Gerenciar automaticamente o número de versão de um projeto
- Gerar e atualizar changelogs com base em mensagens de commit
- Atualizar a documentação do projeto
- Automatizar o fluxo de trabalho Git (commit + push)
"""

import os
import re
import sys
import shutil
import logging
import argparse
import subprocess
from datetime import datetime
from typing import Dict, List, Optional, Tuple, Union, Any

# Configuração
DEFAULT_COMMIT_HISTORY_FILE = "commits.log"
DEFAULT_CHANGELOG_FILE = "CHANGELOG.md"
DEFAULT_README_FILE = "README.md"
DEFAULT_VERSION_FILE = "version.txt"
DEFAULT_LOG_FILE = "version_update.log"

# Ícones para documentação
ICONS = {
    "changelog": "📝",  # Ícone de documento para changelog
    "readme": "📘",     # Ícone de livro para README
    "version": "🏷️",    # Ícone de etiqueta para versão
    "commit": "✅",     # Ícone de marca de verificação para commit
    "feature": "✨",    # Ícone para novas funcionalidades
    "change": "🔄",     # Ícone para alterações
    "bug": "🐛",        # Ícone para correções de bugs
    "remove": "🗑️",     # Ícone para remoções
    "security": "🔒",   # Ícone para atualizações de segurança
    "test": "🧪",       # Ícone para testes
    "success": "✓",     # Ícone para sucesso
    "warning": "⚠️",    # Ícone para aviso
    "error": "❌",      # Ícone para erro
    "info": "ℹ️",       # Ícone para informação
    "backup": "💾",     # Ícone para backup
    "cleanup": "🧹",    # Ícone para limpeza
    "celebration": "🎉", # Ícone para celebração
    "sync": "🔄",       # Ícone para sincronização
    "fetch": "⬇️",      # Ícone para fetch
    "push": "☁️",       # Ícone para push
    "search": "🔍",     # Ícone para busca
    "config": "⚙️",     # Ícone para configuração
    "number": "🔢",     # Ícone para números
}

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

# Configuração de tipos de commits
COMMIT_TYPES = {
    "Adicionado": {
        "pattern": r"(add|feat|new|feature)",
        "icon": ICONS["feature"],
        "description": "Novas funcionalidades adicionadas ao projeto"
    },
    "Alterado": {
        "pattern": r"(change|update|modify|refactor|perf|improvement)",
        "icon": ICONS["change"],
        "description": "Alterações em funcionalidades existentes"
    },
    "Corrigido": {
        "pattern": r"(fix|bug|repair|solve|resolve)",
        "icon": ICONS["bug"],
        "description": "Correções de bugs"
    },
    "Removido": {
        "pattern": r"(remove|delete|rm|drop)",
        "icon": ICONS["remove"],
        "description": "Recursos removidos"
    },
    "Segurança": {
        "pattern": r"(security|sec|cve|vuln|protect)",
        "icon": ICONS["security"],
        "description": "Correções de segurança"
    },
    "Testes": {
        "pattern": r"(test|spec|check|assert|validate)",
        "icon": ICONS["test"],
        "description": "Testes adicionados ou atualizados"
    },
    "Documentação": {
        "pattern": r"(doc|docs|readme|comment)",
        "icon": "📚",
        "description": "Alterações na documentação"
    },
    "Build": {
        "pattern": r"(build|ci|cd|workflow|pipeline)",
        "icon": "🔨",
        "description": "Alterações no sistema de build ou CI/CD"
    },
    "Configuração": {
        "pattern": r"(config|conf|cfg|settings|env)",
        "icon": "⚙️",
        "description": "Alterações em arquivos de configuração"
    }
}

class VersionManager:
    """
    Gerenciador de versões para projetos Git.

    Esta classe fornece métodos para automatizar o fluxo de trabalho de
    atualização de versão, incluindo gerenciamento de changelogs, README
    e operações Git.
    """

    def __init__(self, 
                 commit_history_file: str = DEFAULT_COMMIT_HISTORY_FILE,
                 changelog_file: str = DEFAULT_CHANGELOG_FILE,
                 readme_file: str = DEFAULT_README_FILE,
                 version_file: str = DEFAULT_VERSION_FILE,
                 log_file: str = DEFAULT_LOG_FILE,
                 log_level: int = logging.INFO,
                 create_backups: bool = True,
                 verbose: bool = True):
        """
        Inicializa o gerenciador de versões.

        Args:
            commit_history_file: Nome do arquivo para armazenar histórico de commits
            changelog_file: Nome do arquivo de changelog
            readme_file: Nome do arquivo README
            version_file: Nome do arquivo de versão
            log_file: Nome do arquivo de log
            log_level: Nível de logging
            create_backups: Se deve criar backups dos arquivos
            verbose: Se deve exibir mensagens detalhadas no terminal
        """
        self.commit_history_file = commit_history_file
        self.changelog_file = changelog_file
        self.readme_file = readme_file
        self.version_file = version_file
        self.create_backups = create_backups
        self.verbose = verbose

        # Configuração de logging
        self._setup_logging(log_file, log_level)

        # Verifica ambiente Git
        self._check_environment()

    def _setup_logging(self, log_file: str, log_level: int):
        """Configura o sistema de logging."""
        self.logger = logging.getLogger("version_manager")
        self.logger.setLevel(log_level)

        # Handler para arquivo
        file_handler = logging.FileHandler(log_file)
        file_handler.setLevel(log_level)

        # Handler para console
        console_handler = logging.StreamHandler()
        console_handler.setLevel(log_level)

        # Formatador
        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        file_handler.setFormatter(formatter)
        console_handler.setFormatter(formatter)

        # Adiciona handlers
        self.logger.addHandler(file_handler)
        # Só adiciona console handler para logging se não estiver em modo verbose
        # (quando verbose=True, mensagens são impressas diretamente via print_colored)
        if not self.verbose:
            self.logger.addHandler(console_handler)

    def print_colored(self, message: str, color: str = Colors.WHITE, icon: str = ""):
        """
        Imprime uma mensagem colorida com um prefixo de ícone opcional.

        Args:
            message: Mensagem a ser exibida
            color: Cor ANSI para a mensagem
            icon: Ícone opcional para prefixar a mensagem
        """
        if self.verbose:
            if icon:
                print(f"{icon} {color}{message}{Colors.RESET}")
            else:
                print(f"{color}{message}{Colors.RESET}")

        # Registra no log independentemente do modo verbose
        log_message = f"{icon} {message}" if icon else message
        self.logger.info(log_message)

    def _check_environment(self):
        """Verifica se o ambiente está configurado corretamente."""
        # Verifica se o Git está instalado
        if not self._check_git_installation():
            raise EnvironmentError("Git não está instalado ou não está disponível no PATH. Por favor, instale o Git e tente novamente.")

    def _check_git_installation(self) -> bool:
        """
        Verifica se o Git está instalado e disponível.

        Returns:
            bool: True se o Git estiver instalado, False caso contrário
        """
        try:
            git_version = subprocess.check_output(
                ["git", "--version"], 
                universal_newlines=True,
                stderr=subprocess.STDOUT
            ).strip()
            self.print_colored(f"Git encontrado: {git_version}", Colors.GREEN, ICONS["success"])
            return True
        except (subprocess.SubprocessError, FileNotFoundError):
            self.print_colored("Git não está instalado ou não está no PATH", Colors.RED, ICONS["error"])
            return False

    def _check_git_repository(self) -> bool:
        """
        Verifica se o diretório atual é um repositório Git.

        Returns:
            bool: True se for um repositório Git, False caso contrário
        """
        try:
            subprocess.check_output(
                ["git", "rev-parse", "--is-inside-work-tree"], 
                stderr=subprocess.STDOUT,
                universal_newlines=True
            )
            self.print_colored("Diretório atual é um repositório Git", Colors.GREEN, ICONS["success"])
            return True
        except subprocess.SubprocessError:
            self.print_colored("Diretório atual não é um repositório Git", Colors.YELLOW, ICONS["warning"])
            return False

    def _init_git_repository(self) -> bool:
        """
        Inicializa um novo repositório Git, se necessário.

        Returns:
            bool: True se a inicialização for bem-sucedida, False caso contrário
        """
        try:
            self.print_colored("Inicializando repositório Git...", Colors.CYAN, ICONS["config"])
            subprocess.check_call(["git", "init"])
            self.print_colored("Repositório Git inicializado com sucesso!", Colors.GREEN, ICONS["success"])
            return True
        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao inicializar repositório Git: {e}", Colors.RED, ICONS["error"])
            return False

    def check_git_configuration(self) -> bool:
        """
        Verifica e configura o Git se necessário.

        Returns:
            bool: True se o Git estiver configurado corretamente, False caso contrário
        """
        # Verifica se o diretório atual é um repositório Git
        if not self._check_git_repository():
            if not self._init_git_repository():
                return False

        # Verifica a configuração de usuário do Git
        try:
            git_user_name = subprocess.check_output(
                ["git", "config", "--get", "user.name"],
                universal_newlines=True,
                stderr=subprocess.DEVNULL
            ).strip()

            git_user_email = subprocess.check_output(
                ["git", "config", "--get", "user.email"],
                universal_newlines=True,
                stderr=subprocess.DEVNULL
            ).strip()

            if git_user_name and git_user_email:
                self.print_colored(
                    f"Git configurado: {git_user_name} <{git_user_email}>", 
                    Colors.GREEN, 
                    ICONS["success"]
                )
                return True
            else:
                raise subprocess.SubprocessError("Configuração de Git incompleta")

        except subprocess.SubprocessError:
            self.print_colored("Configuração do Git incompleta.", Colors.YELLOW, ICONS["warning"])
            self.print_colored("Execute: git config --global user.name 'Seu Nome'", Colors.YELLOW)
            self.print_colored("Execute: git config --global user.email 'seu.email@exemplo.com'", Colors.YELLOW)

            # Solicita credenciais
            user_name = input("Digite seu nome: ")
            user_email = input("Digite seu email: ")

            if not user_name or not user_email:
                self.print_colored("Credenciais inválidas. Continuando sem configurar o Git...", Colors.YELLOW, ICONS["warning"])
                return False

            try:
                subprocess.check_call(["git", "config", "--global", "user.name", user_name])
                subprocess.check_call(["git", "config", "--global", "user.email", user_email])
                self.print_colored("Git configurado com sucesso!", Colors.GREEN, ICONS["success"])
                return True
            except subprocess.SubprocessError as e:
                self.print_colored(f"Erro ao configurar Git: {e}", Colors.RED, ICONS["error"])
                return False

    def check_git_credentials(self) -> bool:
        """
        Verifica se as credenciais Git estão configuradas e podem acessar o repositório remoto.

        Returns:
            bool: True se as credenciais estiverem configuradas, False caso contrário
        """
        # Verifica se há um repositório remoto configurado
        try:
            remote_url = subprocess.check_output(
                ["git", "config", "--get", "remote.origin.url"],
                universal_newlines=True,
                stderr=subprocess.DEVNULL
            ).strip()

            if not remote_url:
                self.print_colored("Nenhum repositório remoto configurado", Colors.YELLOW, ICONS["warning"])
                remote_url = input("Digite a URL do repositório remoto (deixe em branco para pular): ")

                if remote_url:
                    try:
                        subprocess.check_call(["git", "remote", "add", "origin", remote_url])
                        self.print_colored(f"Repositório remoto adicionado: {remote_url}", Colors.GREEN, ICONS["success"])
                    except subprocess.SubprocessError as e:
                        self.print_colored(f"Erro ao adicionar repositório remoto: {e}", Colors.RED, ICONS["error"])
                        return False
                else:
                    self.print_colored("Continuando sem repositório remoto", Colors.YELLOW, ICONS["warning"])
                    return True

            # Tenta fazer fetch para testar as credenciais
            self.print_colored("Testando conexão com repositório remoto...", Colors.CYAN, ICONS["sync"])
            try:
                subprocess.check_call(
                    ["git", "fetch", "--dry-run"],
                    stderr=subprocess.PIPE,
                    stdout=subprocess.PIPE
                )
                self.print_colored("Conexão com repositório remoto estabelecida", Colors.GREEN, ICONS["success"])
                return True
            except subprocess.SubprocessError:
                self.print_colored("Falha na conexão com repositório remoto", Colors.RED, ICONS["error"])
                self.print_colored("Verifique suas credenciais Git", Colors.YELLOW, ICONS["warning"])

                # Solicita credenciais se necessário
                auth_method = input("Método de autenticação (1=SSH, 2=HTTPS, 3=Token, 4=Pular): ")

                if auth_method == "1":
                    self.print_colored("Usando SSH. Certifique-se que suas chaves SSH estão configuradas.", Colors.CYAN)
                    # Verifica se as chaves SSH existem
                    ssh_path = os.path.expanduser("~/.ssh/id_rsa")
                    if not os.path.exists(ssh_path):
                        self.print_colored("Chave SSH não encontrada. Gerando nova chave...", Colors.YELLOW)
                        email = input("Digite seu email para a chave SSH: ")
                        subprocess.call(["ssh-keygen", "-t", "rsa", "-b", "4096", "-C", email])

                    # Exibe a chave pública para o usuário
                    with open(os.path.expanduser("~/.ssh/id_rsa.pub"), "r") as f:
                        public_key = f.read().strip()
                    self.print_colored("Adicione esta chave SSH à sua conta do Git:", Colors.CYAN)
                    print(public_key)
                    input("Pressione Enter depois de adicionar a chave...")

                elif auth_method == "2":
                    username = input("Digite seu nome de usuário Git: ")
                    password = input("Digite sua senha Git: ")

                    # Configura credenciais para cache
                    remote_parts = remote_url.split("://")
                    if len(remote_parts) > 1:
                        new_remote = f"https://{username}:{password}@{remote_parts[1]}"
                        subprocess.call(["git", "remote", "set-url", "origin", new_remote])
                        self.print_colored("Credenciais configuradas", Colors.GREEN, ICONS["success"])

                elif auth_method == "3":
                    token = input("Digite seu token de acesso pessoal: ")
                    username = input("Digite seu nome de usuário Git: ")

                    # Configura token para autenticação
                    remote_parts = remote_url.split("://")
                    if len(remote_parts) > 1:
                        new_remote = f"https://{username}:{token}@{remote_parts[1]}"
                        subprocess.call(["git", "remote", "set-url", "origin", new_remote])
                        self.print_colored("Token configurado", Colors.GREEN, ICONS["success"])

                # Testa novamente
                try:
                    subprocess.check_call(
                        ["git", "fetch", "--dry-run"],
                        stderr=subprocess.PIPE,
                        stdout=subprocess.PIPE
                    )
                    self.print_colored("Conexão com repositório remoto estabelecida", Colors.GREEN, ICONS["success"])
                    return True
                except subprocess.SubprocessError:
                    self.print_colored("Falha na conexão mesmo após configuração", Colors.RED, ICONS["error"])
                    if input("Deseja continuar mesmo sem acesso ao repositório remoto? (s/n): ").lower() == 's':
                        return True
                    return False

        except subprocess.SubprocessError:
            self.print_colored("Nenhum repositório remoto configurado", Colors.YELLOW, ICONS["warning"])
            return True

    def check_remote_changes(self) -> bool:
        """
        Verifica se há alterações remotas que precisam ser puxadas.

        Returns:
            bool: True se o repositório local estiver atualizado, False caso contrário
        """
        self.print_colored("Verificando alterações remotas...", Colors.CYAN, ICONS["sync"])

        try:
            # Verifica se origem remota existe
            remote_exists = subprocess.call(
                ["git", "config", "--get", "remote.origin.url"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            ) == 0

            if not remote_exists:
                self.print_colored("Nenhum repositório remoto configurado", Colors.YELLOW, ICONS["warning"])
                return True

            # Executa git fetch para atualizar referências remotas
            fetch_result = subprocess.call(
                ["git", "fetch"], 
                stdout=subprocess.DEVNULL, 
                stderr=subprocess.DEVNULL
            )

            if fetch_result != 0:
                self.print_colored("Falha ao executar git fetch", Colors.YELLOW, ICONS["warning"])
                return False

            # Verifica branch atual
            current_branch = subprocess.check_output(
                ["git", "rev-parse", "--abbrev-ref", "HEAD"],
                universal_newlines=True
            ).strip()

            # Verifica se origem remota existe para o branch atual
            remote_branch_exists = subprocess.call(
                ["git", "ls-remote", "--exit-code", "--heads", "origin", current_branch],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            ) == 0

            if not remote_branch_exists:
                self.print_colored(f"Branch {current_branch} não existe no remoto", Colors.YELLOW, ICONS["warning"])
                return True

            # Compara commits local e remoto
            try:
                local_commit = subprocess.check_output(
                    ["git", "rev-parse", "HEAD"], 
                    universal_newlines=True
                ).strip()

                remote_commit = subprocess.check_output(
                    ["git", "rev-parse", f"origin/{current_branch}"], 
                    universal_newlines=True
                ).strip()

                if local_commit != remote_commit:
                    # Verifica se local está atrás do remoto
                    merge_base = subprocess.check_output(
                        ["git", "merge-base", local_commit, remote_commit],
                        universal_newlines=True
                    ).strip()

                    if merge_base == local_commit:
                        self.print_colored("Repositório local está atrás do remoto", Colors.YELLOW, ICONS["warning"])
                        self.print_colored("Execute git pull antes de continuar", Colors.YELLOW, ICONS["warning"])
                        return False
                    elif merge_base == remote_commit:
                        self.print_colored("Repositório local está à frente do remoto", Colors.GREEN, ICONS["success"])
                        return True
                    else:
                        self.print_colored("Branches divergiram, merge necessário", Colors.YELLOW, ICONS["warning"])
                        return False

                self.print_colored("Repositório local está atualizado com o remoto", Colors.GREEN, ICONS["success"])
                return True

            except subprocess.SubprocessError:
                self.print_colored("Não foi possível comparar commits local e remoto", Colors.YELLOW, ICONS["warning"])
                return False

        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao verificar alterações remotas: {e}", Colors.RED, ICONS["error"])
            return False

    def perform_pull(self) -> bool:
        """
        Executa git pull para atualizar o repositório local.

        Returns:
            bool: True se o pull for bem-sucedido, False caso contrário
        """
        self.print_colored("Executando git pull para atualizar o repositório local...", Colors.CYAN, ICONS["fetch"])

        try:
            # Verifica se origem remota existe
            remote_exists = subprocess.call(
                ["git", "config", "--get", "remote.origin.url"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            ) == 0

            if not remote_exists:
                self.print_colored("Nenhum repositório remoto configurado. Pulando pull.", Colors.YELLOW, ICONS["warning"])
                return True

            # Executa git pull
            pull_result = subprocess.check_output(
                ["git", "pull"], 
                universal_newlines=True,
                stderr=subprocess.STDOUT
            ).strip()

            if "Already up to date" in pull_result:
                self.print_colored("Repositório já está atualizado", Colors.GREEN, ICONS["success"])
            else:
                self.print_colored(f"Pull realizado com sucesso: {pull_result}", Colors.GREEN, ICONS["success"])

            return True
        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao executar git pull: {e}", Colors.RED, ICONS["error"])

            # Verifica se há conflitos
            if "CONFLICT" in str(e):
                self.print_colored("Conflitos de merge detectados", Colors.RED, ICONS["error"])
                self.print_colored("Resolva os conflitos manualmente antes de continuar", Colors.YELLOW, ICONS["warning"])

            return False

    def get_recent_commits(self, count: int = 10) -> List[Dict[str, str]]:
        """
        Obtém commits recentes do histórico do Git.

        Args:
            count: Número de commits a serem obtidos

        Returns:
            List[Dict[str, str]]: Lista de commits com hash, data e mensagem
        """
        self.print_colored("Obtendo commits recentes...", Colors.CYAN, ICONS["search"])

        # Executa fetch e pull antes de obter commits
        self.check_remote_changes()
        self.perform_pull()

        # Se o arquivo de histórico de commits existe e é recente, use-o
        use_cached = False
        if os.path.exists(self.commit_history_file):
            file_mtime = os.path.getmtime(self.commit_history_file)
            current_time = datetime.now().timestamp()

            # Usa cache se o arquivo for mais recente que 1 hora
            if (current_time - file_mtime) < 3600:
                use_cached = True
                self.print_colored(f"Usando histórico de commits em cache ({self.commit_history_file})", Colors.CYAN, ICONS["info"])

        if not use_cached:
            self.print_colored(f"Obtendo commits do Git...", Colors.CYAN, ICONS["search"])
            try:
                git_log = subprocess.check_output(
                    [
                        "git", "log", 
                        f"-n{count}", 
                        "--pretty=format:%H|%ad|%an|%ae|%s", 
                        "--date=format:%Y-%m-%d"
                    ],
                    universal_newlines=True
                )

                with open(self.commit_history_file, "w", encoding="utf-8") as f:
                    f.write(git_log)

                self.print_colored(f"Commits salvos em {self.commit_history_file}", Colors.GREEN, ICONS["success"])
            except subprocess.SubprocessError as e:
                if os.path.exists(self.commit_history_file):
                    self.print_colored(f"Erro ao executar git log: {e}. Usando arquivo de cache.", Colors.YELLOW, ICONS["warning"])
                else:
                    raise RuntimeError(f"Erro ao executar git log: {e}")

        # Carrega commits do arquivo
        commits = []
        try:
            with open(self.commit_history_file, "r", encoding="utf-8") as f:
                for line in f:
                    parts = line.split("|", 4)
                    if len(parts) >= 5:
                        commits.append({
                            "hash": parts[0],
                            "date": parts[1],
                            "author": parts[2],
                            "email": parts[3],
                            "message": parts[4].strip()
                        })

            self.print_colored(f"Commits carregados: {len(commits)} encontrados", Colors.GREEN, ICONS["success"])
        except Exception as e:
            self.print_colored(f"Erro ao carregar commits: {e}", Colors.RED, ICONS["error"])
            raise

        if not commits:
            self.print_colored("Nenhum commit encontrado", Colors.YELLOW, ICONS["warning"])
            if input("Deseja criar um commit inicial? (s/n): ").lower() == 's':
                # Cria arquivo inicial se não existir
                if not os.path.exists("README.md"):
                    with open("README.md", "w", encoding="utf-8") as f:
                        f.write("# Meu Projeto\n\nInício do projeto.")

                # Adiciona e faz commit
                try:
                    subprocess.check_call(["git", "add", "."])
                    subprocess.check_call(["git", "commit", "-m", "feat: commit inicial"])

                    # Recarrega commits
                    return self.get_recent_commits(count)
                except subprocess.SubprocessError as e:
                    self.print_colored(f"Erro ao criar commit inicial: {e}", Colors.RED, ICONS["error"])

        return commits

    def get_section_content(self, commits: List[Dict[str, str]], commit_type: str) -> str:
        """
        Obtém conteúdo para uma seção específica do changelog baseado no tipo de commit.

        Args:
            commits: Lista de commits
            commit_type: Tipo de commit (chave de COMMIT_TYPES)

        Returns:
            str: Conteúdo formatado para a seção
        """
        section_content = ""

        if commit_type not in COMMIT_TYPES:
            self.print_colored(f"Tipo de commit desconhecido: {commit_type}", Colors.YELLOW, ICONS["warning"])
            return ""

        pattern = re.compile(COMMIT_TYPES[commit_type]["pattern"], re.IGNORECASE)

        for commit in commits:
            if pattern.search(commit["message"]):
                # Substitui o padrão correspondente por uma string vazia (apenas a primeira ocorrência)
                message = pattern.sub("", commit["message"], count=1).strip()
                section_content += f"- [{commit['hash'][:7]}] {commit['date']} - {message}\n"

        return section_content if section_content else None

    def update_changelog(self, commits: List[Dict[str, str]]) -> bool:
        """
        Atualiza o arquivo de changelog com informações de commit.

        Args:
            commits: Lista de commits para incluir no changelog

        Returns:
            bool: True se o changelog for atualizado com sucesso, False caso contrário
        """
        self.print_colored("Atualizando changelog...", Colors.CYAN, ICONS["changelog"])

        # Cria backup do changelog existente
        if os.path.exists(self.changelog_file) and self.create_backups:
            backup_file = f"{self.changelog_file}.bak"
            shutil.copy2(self.changelog_file, backup_file)
            self.print_colored(f"Backup do changelog criado: {backup_file}", Colors.YELLOW, ICONS["backup"])

        # Obtém a versão atual
        current_version = self.get_version()
        today = datetime.now().strftime("%Y-%m-%d")

        # Cabeçalho do changelog
        changelog_content = f"# {ICONS['changelog']} Changelog\n"
        changelog_content += "Todas as alterações notáveis neste projeto serão documentadas neste arquivo.\n"
        changelog_content += "O formato é baseado em [Keep a Changelog](https://keepachangelog.com/)\n"
        changelog_content += "e este projeto adere ao [Versionamento Semântico](https://semver.org/).\n\n"

        # Seção da versão atual
        changelog_content += f"## [v{current_version}] - {today}\n\n"

        # Adiciona seções para cada tipo de commit
        sections_added = False
        for type_name, type_info in COMMIT_TYPES.items():
            section_content = self.get_section_content(commits, type_name)
            if section_content:
                changelog_content += f"### {type_info['icon']} {type_name}\n{section_content}\n"
                sections_added = True

        # Se nenhuma seção foi adicionada, adiciona uma mensagem
        if not sections_added:
            changelog_content += "Nenhuma alteração significativa nesta versão.\n\n"

        # Adiciona links para comparação de versões (se possível)
        try:
            remote_url = subprocess.check_output(
                ["git", "config", "--get", "remote.origin.url"],
                universal_newlines=True,
                stderr=subprocess.DEVNULL
            ).strip()

            # Extrai usuário/repo da URL remota
            repo_name_match = re.search(r'[:/]([^/]+)/([^/.]+)(\.git)?', remote_url)
            if repo_name_match:
                user, repo = repo_name_match.groups()[0:2]
                repo_path = f"{user}/{repo}"

                # Adiciona links de comparação
                changelog_content += f"\n[Unreleased]: https://github.com/{repo_path}/compare/v{current_version}...main\n"
                changelog_content += f"[v{current_version}]: https://github.com/{repo_path}/releases/tag/v{current_version}\n"
        except subprocess.SubprocessError:
            self.print_colored("Não foi possível determinar a URL do repositório. Omitindo links.", Colors.YELLOW, ICONS["warning"])

        # Salva o arquivo de changelog
        try:
            with open(self.changelog_file, "w", encoding="utf-8") as f:
                f.write(changelog_content)

            self.print_colored("Changelog atualizado com sucesso!", Colors.GREEN, ICONS["success"])
            return True
        except Exception as e:
            self.print_colored(f"Erro ao salvar changelog: {e}", Colors.RED, ICONS["error"])
            return False

    def update_readme(self, commits: List[Dict[str, str]], project_name: str = None, description: str = None) -> bool:
        """
        Atualiza o arquivo README com informações do projeto.

        Args:
            commits: Lista de commits
            project_name: Nome do projeto (opcional)
            description: Descrição do projeto (opcional)

        Returns:
            bool: True se o README for atualizado com sucesso, False caso contrário
        """
        self.print_colored("Atualizando README...", Colors.CYAN, ICONS["readme"])

        # Tenta obter o nome do projeto do diretório atual se não fornecido
        if not project_name:
            project_name = os.path.basename(os.path.abspath(os.getcwd()))
            self.print_colored(f"Nome do projeto não especificado. Usando nome do diretório: {project_name}", Colors.YELLOW, ICONS["info"])

        # Tenta obter a descrição do projeto do arquivo package.json ou pyproject.toml se não fornecido
        if not description:
            for config_file in ["package.json", "pyproject.toml", "setup.py"]:
                if os.path.exists(config_file):
                    with open(config_file, "r", encoding="utf-8") as f:
                        content = f.read()
                        desc_match = re.search(r'"description"\s*:\s*"([^"]+)"', content) or \
                                    re.search(r'description\s*=\s*["\']([^"\']+)["\']', content)
                        if desc_match:
                            description = desc_match.group(1)
                            self.print_colored(f"Descrição encontrada em {config_file}: {description}", Colors.CYAN, ICONS["info"])
                            break

        # Se ainda não tiver descrição, usa uma genérica
        if not description:
            description = "Um projeto incrível em desenvolvimento."

        # Cria backup do README existente
        if os.path.exists(self.readme_file) and self.create_backups:
            backup_file = f"{self.readme_file}.bak"
            shutil.copy2(self.readme_file, backup_file)
            self.print_colored(f"Backup do README criado: {backup_file}", Colors.YELLOW, ICONS["backup"])

        # Cabeçalho do README
        readme_content = f"# {ICONS['readme']} {project_name}\n\n"

        # Badges (opcional)
        try:
            version = self.get_version()
            readme_content += f"![Versão](https://img.shields.io/badge/versão-{version}-blue)\n"

            # Adiciona mais badges se puder detectar informações do projeto
            if os.path.exists("LICENSE"):
                with open("LICENSE", "r", encoding="utf-8") as f:
                    license_content = f.read().lower()
                    if "mit" in license_content:
                        readme_content += "![Licença](https://img.shields.io/badge/licença-MIT-green)\n"

            readme_content += "\n"
        except Exception:
            # Se não conseguir adicionar badges, simplesmente continua
            pass

        # Seção Sobre
        readme_content += "## 📋 Sobre\n"
        readme_content += f"{description}\n\n"

        # Seção Funcionalidades
        readme_content += "## ✨ Funcionalidades\n\n"

        # Extrai funcionalidades de commits do tipo "Adicionado"
        features = []
        feature_pattern = re.compile(COMMIT_TYPES["Adicionado"]["pattern"], re.IGNORECASE)
        for commit in commits:
            if feature_pattern.search(commit["message"]):
                feature = feature_pattern.sub("", commit["message"], flags=re.IGNORECASE, count=1).strip()
                features.append(f"- {feature}")

        if features:
            readme_content += "\n".join(features[:10])  # Limita a 10 funcionalidades
            if len(features) > 10:
                readme_content += "\n- E mais..."
        else:
            readme_content += "- Em desenvolvimento...\n"

        readme_content += "\n\n"

        # Seção Instalação
        readme_content += "## 🚀 Instalação\n\n"

        # Detecta tipo de projeto
        if os.path.exists("package.json"):
            readme_content += "```bash\n# Instalar dependências\nnpm install\n\n# Executar em modo de desenvolvimento\nnpm run dev\n```\n\n"
        elif os.path.exists("requirements.txt") or os.path.exists("pyproject.toml"):
            readme_content += "```bash\n# Criar ambiente virtual\npython -m venv venv\n\n# Ativar ambiente\n# No Windows:\nvenv\\Scripts\\activate\n# No Linux/Mac:\nsource venv/bin/activate\n\n# Instalar dependências\npip install -r requirements.txt\n```\n\n"
        else:
            readme_content += "Clone o repositório e siga os passos abaixo:\n\n```bash\ngit clone [URL_DO_REPOSITORIO]\ncd [NOME_DO_PROJETO]\n```\n\n"

        # Seção Versão
        readme_content += f"## {ICONS['version']} Versão\n\n"
        readme_content += f"Versão atual: **{self.get_version()}**\n\n"

        # Seção Histórico de Versões
        readme_content += f"## {ICONS['changelog']} Histórico de Versões\n\n"
        readme_content += f"Para ver o histórico de versões, consulte o [CHANGELOG](CHANGELOG.md).\n\n"

        # Seção Contribuindo
        readme_content += "## 👥 Contribuindo\n\n"
        readme_content += "Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.\n\n"

        # Seção Licença
        readme_content += "## 📄 Licença\n\n"
        if os.path.exists("LICENSE"):
            with open("LICENSE", "r", encoding="utf-8") as f:
                license_content = f.read().lower()
                if "mit" in license_content:
                    readme_content += "Este projeto está licenciado sob a [Licença MIT](LICENSE).\n"
                elif "apache" in license_content:
                    readme_content += "Este projeto está licenciado sob a [Licença Apache 2.0](LICENSE).\n"
                elif "gpl" in license_content:
                    readme_content += "Este projeto está licenciado sob a [Licença GPL](LICENSE).\n"
                else:
                    readme_content += "Este projeto está licenciado sob a licença especificada no arquivo [LICENSE](LICENSE).\n"
        else:
            readme_content += "Este projeto ainda não possui uma licença definida.\n"

        # Salva o arquivo README
        try:
            with open(self.readme_file, "w", encoding="utf-8") as f:
                f.write(readme_content)

            self.print_colored("README atualizado com sucesso!", Colors.GREEN, ICONS["success"])
            return True
        except Exception as e:
            self.print_colored(f"Erro ao salvar README: {e}", Colors.RED, ICONS["error"])
            return False

    def get_version(self) -> str:
        """
        Obtém a versão atual do arquivo de versão.

        Returns:
            str: Versão atual no formato X.Y.Z
        """
        self.print_colored("Obtendo versão atual...", Colors.CYAN, ICONS["version"])

        if os.path.exists(self.version_file):
            with open(self.version_file, "r") as f:
                version = f.read().strip()

            # Valida o formato da versão
            if not re.match(r'^\d+\.\d+\.\d+$', version):
                self.print_colored(f"Formato de versão inválido: {version}. Usando 1.0.0.", Colors.YELLOW, ICONS["warning"])
                version = "1.0.0"
                with open(self.version_file, "w") as f:
                    f.write(version)

            self.print_colored(f"Versão atual: {version}", Colors.GREEN, ICONS["version"])
            return version
        else:
            self.print_colored("Arquivo de versão não encontrado. Iniciando com a versão 1.0.0.", Colors.YELLOW, ICONS["warning"])
            with open(self.version_file, "w") as f:
                f.write("1.0.0")
            return "1.0.0"

    def increment_version(self, version_type: str = "patch") -> str:
        """
        Incrementa o número da versão seguindo o versionamento semântico.

        Args:
            version_type: Tipo de incremento ("major", "minor" ou "patch")

        Returns:
            str: Nova versão incrementada
        """
        self.print_colored(f"Incrementando versão ({version_type})...", Colors.CYAN, ICONS["number"])

        current_version = self.get_version()
        version_parts = current_version.split(".")

        # Garante que temos três partes
        if len(version_parts) < 3:
            version_parts.extend(["0"] * (3 - len(version_parts)))

        # Incrementa a parte apropriada
        if version_type == "major":
            version_parts[0] = str(int(version_parts[0]) + 1)
            version_parts[1] = "0"
            version_parts[2] = "0"
        elif version_type == "minor":
            version_parts[1] = str(int(version_parts[1]) + 1)
            version_parts[2] = "0"
        else:  # patch (padrão)
            version_parts[2] = str(int(version_parts[2]) + 1)

        new_version = ".".join(version_parts)
        self.print_colored(f"Nova versão: {new_version}", Colors.GREEN, ICONS["version"])

        return new_version

    def update_version(self, version_type: str = "patch") -> str:
        """
        Atualiza o arquivo de versão com um número de versão incrementado.

        Args:
            version_type: Tipo de incremento ("major", "minor" ou "patch")

        Returns:
            str: Nova versão
        """
        self.print_colored("Atualizando versão...", Colors.CYAN, ICONS["version"])

        new_version = self.increment_version(version_type)

        with open(self.version_file, "w") as f:
            f.write(new_version)

        self.print_colored(f"Versão atualizada para {new_version}", Colors.GREEN, ICONS["version"])
        return new_version

    def check_pending_push(self) -> bool:
        """
        Verifica se existem commits locais que precisam ser enviados ao remoto.

        Returns:
            bool: True se houver commits pendentes, False caso contrário
        """
        self.print_colored("Verificando commits pendentes para push...", Colors.CYAN, ICONS["search"])

        try:
            # Verifica se origem remota existe
            remote_exists = subprocess.call(
                ["git", "config", "--get", "remote.origin.url"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            ) == 0

            if not remote_exists:
                self.print_colored("Nenhum repositório remoto configurado", Colors.YELLOW, ICONS["warning"])
                return False

            # Executa git branch -v para ver se está à frente do remoto
            branch_info = subprocess.check_output(
                ["git", "branch", "-v"], 
                universal_newlines=True
            )

            # Verifica se existe "ahead" no branch atual
            if re.search(r"\s+ahead\s+", branch_info):
                self.print_colored("Existem commits pendentes para push", Colors.YELLOW, ICONS["warning"])
                return True

            self.print_colored("Não há commits pendentes para push", Colors.GREEN, ICONS["success"])
            return False

        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao verificar commits pendentes: {e}", Colors.RED, ICONS["error"])
            return False

    def commit_and_push(self, commit_message: str) -> bool:
        """
        Realiza commit das alterações e push para o repositório remoto.

        Args:
            commit_message: Mensagem de commit

        Returns:
            bool: True se commit e push forem bem-sucedidos, False caso contrário
        """
        self.print_colored("Preparando para fazer commit...", Colors.CYAN, ICONS["commit"])

        # Verifica se há alterações para commit
        git_status = subprocess.check_output(
            ["git", "status", "--porcelain"], 
            universal_newlines=True
        )

        if not git_status:
            self.print_colored("Não há alterações para fazer commit.", Colors.YELLOW, ICONS["warning"])
            return False

        # Realiza fetch e pull antes do commit
        if not self.check_remote_changes():
            if not self.perform_pull():
                self.print_colored("Não foi possível atualizar o repositório local", Colors.RED, ICONS["error"])
                if input("Continuar mesmo assim? (s/n): ").lower() != 's':
                    return False

        # Adiciona todas as alterações
        self.print_colored("Adicionando alterações ao stage...", Colors.CYAN, ICONS["sync"])
        try:
            subprocess.check_call(["git", "add", "."])
            self.print_colored("Alterações adicionadas com sucesso", Colors.GREEN, ICONS["success"])
        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao adicionar alterações: {e}", Colors.RED, ICONS["error"])
            return False

        # Faz commit das alterações
        self.print_colored(f"Fazendo commit com a mensagem: {commit_message}", Colors.CYAN, ICONS["commit"])
        try:
            subprocess.check_call(["git", "commit", "-m", commit_message])
            self.print_colored("Commit realizado com sucesso!", Colors.GREEN, ICONS["success"])
        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao fazer commit: {e}", Colors.RED, ICONS["error"])
            return False

        # Verifica se há repositório remoto
        remote_exists = subprocess.call(
            ["git", "config", "--get", "remote.origin.url"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        ) == 0

        if not remote_exists:
            self.print_colored("Nenhum repositório remoto configurado. Commit local realizado.", Colors.YELLOW, ICONS["warning"])
            return True

        # Push das alterações
        self.print_colored("Enviando alterações para o repositório remoto...", Colors.CYAN, ICONS["push"])
        try:
            subprocess.check_call(["git", "push"])
            self.print_colored("Alterações enviadas com sucesso!", Colors.GREEN, ICONS["success"])
            return True
        except subprocess.SubprocessError as e:
            self.print_colored(f"Erro ao fazer push: {e}", Colors.RED, ICONS["error"])

            # Sugere configurar o branch upstream
            if "no upstream branch" in str(e):
                current_branch = subprocess.check_output(
                    ["git", "rev-parse", "--abbrev-ref", "HEAD"],
                    universal_newlines=True
                ).strip()

                self.print_colored(f"O branch {current_branch} não tem upstream configurado", Colors.YELLOW, ICONS["warning"])

                if input(f"Configurar upstream para {current_branch}? (s/n): ").lower() == 's':
                    try:
                        subprocess.check_call(["git", "push", "--set-upstream", "origin", current_branch])
                        self.print_colored(f"Upstream configurado e push realizado com sucesso!", Colors.GREEN, ICONS["success"])
                        return True
                    except subprocess.SubprocessError as e2:
                        self.print_colored(f"Erro ao configurar upstream: {e2}", Colors.RED, ICONS["error"])

            return False

    def cleanup_temp_files(self):
        """Remove arquivos temporários."""
        self.print_colored("Limpando arquivos temporários...", Colors.CYAN, ICONS["cleanup"])

        temp_files = [self.commit_history_file]

        for file in temp_files:
            if os.path.exists(file):
                try:
                    os.remove(file)
                    self.print_colored(f"Arquivo temporário removido: {file}", Colors.YELLOW, ICONS["success"])
                except OSError as e:
                    self.print_colored(f"Erro ao remover arquivo temporário {file}: {e}", Colors.RED, ICONS["error"])

    def run(self, version_type: str = "patch", skip_cleanup: bool = False):
        """
        Executa o fluxo principal de atualização de versão.

        Args:
            version_type: Tipo de incremento de versão ("major", "minor" ou "patch")
            skip_cleanup: Se True, não remove arquivos temporários
        """
        try:
            # Verifica configuração do Git
            if not self.check_git_configuration():
                raise EnvironmentError("Configuração do Git incompleta")

            # Verifica credenciais Git
            if not self.check_git_credentials():
                raise EnvironmentError("Credenciais Git não configuradas corretamente")

            # Carrega commits
            self.print_colored("Carregando commits...", Colors.CYAN, ICONS["search"])
            commits = self.get_recent_commits()
            if not commits:
                raise RuntimeError("Nenhum commit encontrado")

            # Atualiza changelog
            if not self.update_changelog(commits):
                if input("Continuar mesmo com erro no changelog? (s/n): ").lower() != 's':
                    return False

            # Atualiza README
            if not self.update_readme(commits):
                if input("Continuar mesmo com erro no README? (s/n): ").lower() != 's':
                    return False

            # Atualiza versão
            new_version = self.update_version(version_type)

            # Prepara mensagem de commit
            commit_message = f"chore: atualização de versão para {new_version}"

            # Pergunta se o usuário deseja fazer commit
            if input("Deseja fazer commit das alterações? (s/n): ").lower() == 's':
                if not self.commit_and_push(commit_message):
                    self.print_colored("Não foi possível fazer commit e push das alterações", Colors.RED, ICONS["error"])
            else:
                self.print_colored("As alterações não foram commitadas.", Colors.YELLOW, ICONS["info"])

            # Limpa arquivos temporários
            if not skip_cleanup:
                self.cleanup_temp_files()

            self.print_colored("Processo concluído com sucesso!", Colors.GREEN, ICONS["celebration"])
            return True

        except Exception as e:
            self.print_colored(f"Erro: {str(e)}", Colors.RED, ICONS["error"])
            import traceback
            self.print_colored(f"Detalhes: {traceback.format_exc()}", Colors.RED, ICONS["error"])

            if not skip_cleanup:
                self.cleanup_temp_files()

            return False


def parse_arguments():
    """
    Analisa os argumentos da linha de comando.

    Returns:
        argparse.Namespace: Objeto com os argumentos analisados
    """
    parser = argparse.ArgumentParser(
        description="Script de Atualização de Versão - Automatiza a geração de changelog e o gerenciamento de versões"
    )

    parser.add_argument(
        "--version-type", "-t",
        choices=["major", "minor", "patch"],
        default="patch",
        help="Tipo de incremento de versão (major, minor, patch)"
    )

    parser.add_argument(
        "--skip-cleanup", "-s",
        action="store_true",
        help="Não remove arquivos temporários ao finalizar"
    )

    parser.add_argument(
        "--quiet", "-q",
        action="store_true",
        help="Não exibe mensagens detalhadas durante a execução"
    )

    parser.add_argument(
        "--no-backup", "-n",
        action="store_true",
        help="Não cria backups dos arquivos modificados"
    )

    parser.add_argument(
        "--custom-version", "-c",
        help="Define uma versão específica em vez de incrementar"
    )

    parser.add_argument(
        "--commit-history-file",
        default=DEFAULT_COMMIT_HISTORY_FILE,
        help=f"Nome do arquivo para armazenar histórico de commits (padrão: {DEFAULT_COMMIT_HISTORY_FILE})"
    )

    parser.add_argument(
        "--changelog-file",
        default=DEFAULT_CHANGELOG_FILE,
        help=f"Nome do arquivo de changelog (padrão: {DEFAULT_CHANGELOG_FILE})"
    )

    parser.add_argument(
        "--readme-file",
        default=DEFAULT_README_FILE,
        help=f"Nome do arquivo README (padrão: {DEFAULT_README_FILE})"
    )

    parser.add_argument(
        "--version-file",
        default=DEFAULT_VERSION_FILE,
        help=f"Nome do arquivo de versão (padrão: {DEFAULT_VERSION_FILE})"
    )

    parser.add_argument(
        "--log-file",
        default=DEFAULT_LOG_FILE,
        help=f"Nome do arquivo de log (padrão: {DEFAULT_LOG_FILE})"
    )

    return parser.parse_args()


def main():
    """Função principal do script."""
    # Exibe banner
    print("\n" + "="*80)
    print(" "*20 + "GERENCIADOR DE VERSÕES E CHANGELOG")
    print(" "*20 + "Desenvolvido por: Security Guard Team")
    print("="*80 + "\n")

    # Analisa argumentos
    args = parse_arguments()

    # Cria gerenciador de versões
    version_manager = VersionManager(
        commit_history_file=args.commit_history_file,
        changelog_file=args.changelog_file,
        readme_file=args.readme_file,
        version_file=args.version_file,
        log_file=args.log_file,
        create_backups=not args.no_backup,
        verbose=not args.quiet  # Por padrão, mostra mensagens (a menos que --quiet seja especificado)
    )

    # Define versão personalizada se especificada
    if args.custom_version:
        version_manager.print_colored(f"Definindo versão personalizada: {args.custom_version}", Colors.CYAN, ICONS["version"])
        with open(version_manager.version_file, "w") as f:
            f.write(args.custom_version)

    # Executa o gerenciador
    success = version_manager.run(
        version_type=args.version_type,
        skip_cleanup=args.skip_cleanup
    )

    # Finaliza com código de saída apropriado
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
