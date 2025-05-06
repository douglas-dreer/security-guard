# *******************************************************
# Script de Atualização de Versão e Documentação
# *******************************************************

# Configurações Iniciais
$COMMIT_HISTORY_FILE = "commits.log"
$CHANGELOG_FILE = "CHANGELOG.md"
$README_FILE = "README.md"
$VERSION_FILE = "version.txt"

# Ícones para documentação
$CHANGELOG_ICON = "📝" # Ícone de documento para changelog
$README_ICON = "📘"    # Ícone de livro para README
$VERSION_ICON = "🏷️"   # Ícone de etiqueta para versão
$COMMIT_ICON = "✅"    # Ícone de concluído para commit

# Função para escrever mensagens de log com cores
function Write-ColorLog {
    param(
        [string]$Message,
        [string]$Color = "White",
        [string]$Icon = ""
    )
    
    if ($Icon) {
        Write-Host "$Icon " -NoNewline
    }
    Write-Host $Message -ForegroundColor $Color
}

# Função para verificar se o Git está instalado
function Test-GitInstallation {
    try {
        $gitVersion = git --version
        Write-ColorLog "Git encontrado: $gitVersion" "Green" "✓"
        return $true
    }
    catch {
        Write-ColorLog "Git não está instalado ou não está no PATH" "Red" "✗"
        return $false
    }
}

# Função para obter commits recentes
function Get-RecentCommits {
    [CmdletBinding()]
    param(
        [int]$Count = 10
    )
    Write-ColorLog "Obtendo commits recentes..." "Cyan" "🔍"
    
    if (!(Test-Path $COMMIT_HISTORY_FILE)) {
        Write-ColorLog "Arquivo $COMMIT_HISTORY_FILE não encontrado. Obtendo commits do Git..." "Yellow" "⚠️"
        git log -n $Count --pretty=format:"%H|%ad|%s" --date=format:"%d-%m-%Y" > $COMMIT_HISTORY_FILE
        if ($LASTEXITCODE -ne 0) {
            throw "Erro ao executar git log. Código de erro: $LASTEXITCODE"
        }
        Write-ColorLog "Commits salvos em $COMMIT_HISTORY_FILE com sucesso!" "Green" "✓"
    }
    
    $commits = Get-Content $COMMIT_HISTORY_FILE | ForEach-Object {
        $parts = $_ -split '\|', 3
        if ($parts.Count -ge 3) {
            [PSCustomObject]@{
                Hash    = $parts[0]
                Date    = $parts[1]
                Message = $parts[2].Trim()
            }
        }
    }
    
    if (!$commits) {
        throw "Nenhum commit encontrado no arquivo $COMMIT_HISTORY_FILE"
    }
    
    Write-ColorLog "Commits carregados com sucesso: $($commits.Count) encontrados." "Green" "✓"
    return $commits
}

# Função para atualizar o changelog
function Update-Changelog {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        $Commits
    )
    Write-ColorLog "Atualizando changelog..." "Cyan" "$CHANGELOG_ICON"
    
    $types = @{
        Added    = [regex] '(add|feat|new)'
        Changed  = [regex] '(change|update|modify)'
        Fixed    = [regex] '(fix|bug|repair)'
        Removed  = [regex] '(remove|delete|rm)'
        Security = [regex] '(security|sec|cve)'
        Tests    = [regex] '(test|spec|check)'
    }

    $changelogContent = "# $CHANGELOG_ICON Changelog`n"
    $changelogContent += "All notable changes to this project will be documented in this file.`n"
    $changelogContent += "The format is based on [Keep a Changelog](https://keepachangelog.com/)`n"
    $changelogContent += "and this project adheres to [Semantic Versioning](https://semver.org/).`n`n"

    $currentVersion = Get-Version
    $changelogContent += "## [$currentVersion] - $(Get-Date -Format "yyyy-MM-dd")`n`n"

    foreach ($type in $types.GetEnumerator()) {
        $sectionContent = Get-SectionContent -Commits $Commits -Type $type
        if ($sectionContent) {
            $typeIcon = switch ($type.Name) {
                "Added"    { "✨" }
                "Changed"  { "🔄" }
                "Fixed"    { "🐛" }
                "Removed"  { "🗑️" }
                "Security" { "🔒" }
                "Tests"    { "🧪" }
                default    { "•" }
            }
            $changelogContent += "### $typeIcon $($type.Name)`n"
            $changelogContent += $sectionContent
            $changelogContent += "`n"
        }
    }

    # Tente obter o nome do repositório de forma mais segura
    try {
        $remoteUrl = git config --get remote.origin.url
        $repoName = $remoteUrl -replace '.*[:/]([^/]+)/([^/.]+)(\.git)?$', '$1/$2'
        
        $changelogContent += "`n[Unreleased]: https://github.com/$repoName/compare/v$currentVersion...main`n"
        $changelogContent += "[v$currentVersion]: https://github.com/$repoName/releases/tag/v$currentVersion`n"
    }
    catch {
        Write-ColorLog "Não foi possível determinar o URL do repositório. Omitindo links." "Yellow" "⚠️"
    }

    if (Test-Path $CHANGELOG_FILE) {
        Copy-Item $CHANGELOG_FILE "$CHANGELOG_FILE.bak" -Force
        Write-ColorLog "Backup do changelog anterior criado." "Yellow" "💾"
        Remove-Item $CHANGELOG_FILE
    }
    
    $changelogContent | Out-File -FilePath $CHANGELOG_FILE -Encoding utf8
    Write-ColorLog "Changelog atualizado com sucesso!" "Green" "✓"
}

# Função para obter conteúdo de uma seção do changelog
function Get-SectionContent {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        $Commits,
        [Parameter(Mandatory = $true)]
        $Type
    )

    $sectionContent = ""
    foreach ($commit in $Commits) {
        if ($commit.Message -imatch $Type.Value) {
            # Encontra a primeira ocorrência do padrão e o substitui por uma string vazia
            # Usamos -replace em vez de -ireplace com a mesma funcionalidade case-insensitive
            $message = ($commit.Message -replace $Type.Value, "")
            $sectionContent += "- [$($commit.Hash.Substring(0,7))] $($commit.Date) - $($message.Trim())`n"
        }
    }

    if ($sectionContent) {
        return $sectionContent
    }
    return $null
}

# Função para atualizar o README
function Update-Readme {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        $Commits
    )
    Write-ColorLog "Atualizando README..." "Cyan" "$README_ICON"
    
    $readmeContent = "# $README_ICON Meu Projeto`n"
    $readmeContent += "## 📋 Sobre`n"
    $readmeContent += "Este é um projeto incrível que visa [descrição do projeto].`n`n"
    $readmeContent += "## ✨ Funcionalidades`n"
    
    $features = @()
    foreach ($commit in $Commits) {
        if ($commit.Message -imatch "add|feat|new") {
            $feature = $commit.Message -ireplace "add|feat|new", "", 1
            $features += "* $($feature.Trim())"
        }
    }

    if ($features) {
        $readmeContent += ($features -join "`n") + "`n`n"
    }

    $readmeContent += "## $VERSION_ICON Versão`n"
    $readmeContent += "Versão atual: **$(Get-Version)**`n`n"
    $readmeContent += "## $CHANGELOG_ICON Histórico de Versões`n"
    $readmeContent += "Para ver o histórico de versões, clique [aqui](CHANGELOG.md).`n`n"
    $readmeContent += "## 👥 Contribuição`n"
    $readmeContent += "Contribuições são bem-vindas! Para mais detalhes, leia o arquivo [CONTRIBUTING.md](CONTRIBUTING.md).`n`n"
    $readmeContent += "## 📄 Licença`n"
    $readmeContent += "Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para mais detalhes.`n"

    if (Test-Path $README_FILE) {
        Copy-Item $README_FILE "$README_FILE.bak" -Force
        Write-ColorLog "Backup do README anterior criado." "Yellow" "💾"
        Remove-Item $README_FILE
    }
    
    $readmeContent | Out-File -FilePath $README_FILE -Encoding utf8
    Write-ColorLog "README atualizado com sucesso!" "Green" "✓"
}

# Função para obter a versão atual
function Get-Version {
    [CmdletBinding()]
    param(
        [string]$VersionFile = $VERSION_FILE
    )
    Write-ColorLog "Obtendo versão atual..." "Cyan" "$VERSION_ICON"
    
    if (Test-Path $VersionFile) {
        $version = Get-Content $VersionFile
        Write-ColorLog "Versão atual: $version" "Green" "$VERSION_ICON"
        return $version
    }
    else {
        Write-ColorLog "Versão não encontrada. Iniciando com versão 1.0.0." "Yellow" "⚠️"
        "1.0.0" | Set-Content $VersionFile
        return "1.0.0"
    }
}

# Função para incrementar a versão
function Increment-Version {
    [CmdletBinding()]
    param(
        [string]$version
    )
    Write-ColorLog "Incrementando versão..." "Cyan" "🔢"
    
    $versionParts = $version -split "\."
    if ($versionParts.Count -lt 3) {
        $versionParts += 0
    }
    
    $versionParts[2] = [int]$versionParts[2] + 1
    
    return $versionParts -join "."
}

# Função para atualizar a versão
function Update-Version {
    [CmdletBinding()]
    param(
        [string]$VersionFile = $VERSION_FILE
    )
    Write-ColorLog "Atualizando versão..." "Cyan" "$VERSION_ICON"
    
    $currentVersion = Get-Version -VersionFile $VersionFile
    $newVersion = Increment-Version $currentVersion
    
    $newVersion | Set-Content $VersionFile
    Write-ColorLog "Versão atualizada para $newVersion" "Green" "$VERSION_ICON"
}

# Função para commitar e dar push
function Commit-And-Push {
    [CmdletBinding()]
    param(
        [string]$CommitMessage
    )
    Write-ColorLog "Preparando para commitar..." "Cyan" "🔄"
    
    $gitStatus = git status --porcelain
    if (!$gitStatus) {
        Write-ColorLog "Nenhuma alteração para commitar." "Yellow" "⚠️"
        return
    }
    
    git add .
    
    Write-ColorLog "Commitando com mensagem: $CommitMessage" "Cyan" "$COMMIT_ICON"
    git commit -m $CommitMessage
    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao commitar: $CommitMessage"
    }
    Write-ColorLog "Commit realizado com sucesso!" "Green" "✓"
    
    Write-ColorLog "Enviando alterações para o repositório remoto..." "Cyan" "☁️"
    git push
    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao dar push"
    }
    Write-ColorLog "Alterações enviadas com sucesso!" "Green" "✓"
}

# Função para limpar arquivos temporários
function Cleanup-TempFiles {
    [CmdletBinding()]
    param(
        [string]$CommitHistoryFile = $COMMIT_HISTORY_FILE
    )
    Write-ColorLog "Limpando arquivos temporários..." "Cyan" "🧹"
    
    if (Test-Path $CommitHistoryFile) {
        Remove-Item $CommitHistoryFile
        Write-ColorLog "Arquivo temporário removido." "Yellow" "✓"
    }
}

# Fluxo principal
try {
    # Verificar se o PowerShell está em modo de execução adequado
    $executionPolicy = Get-ExecutionPolicy
    Write-ColorLog "Política de execução atual: $executionPolicy" "Cyan" "ℹ️"
    
    if ($executionPolicy -eq "Restricted") {
        Write-ColorLog "A política de execução está restrita. Isso pode impedir a execução do script." "Yellow" "⚠️"
        Write-ColorLog "Considere executar: Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process" "Yellow" "💡"
    }
    
    # Verificar se o Git está disponível
    if (!(Test-GitInstallation)) {
        throw "Git não está instalado ou não está disponível no PATH. Por favor, instale o Git e tente novamente."
    }

    # Verificar configurações do Git
    Write-ColorLog "Verificando configurações do Git..." "Cyan" "⚙️"
    $gitUserName = git config --global user.name
    $gitUserEmail = git config --global user.email
    
    if (!$gitUserName -or !$gitUserEmail) {
        Write-ColorLog "Configurações do Git incompletas. Por favor, configure:" "Yellow" "⚠️"
        Write-ColorLog "git config --global user.name 'Seu Nome'" "Yellow" 
        Write-ColorLog "git config --global user.email 'seu.email@example.com'" "Yellow"
        
        $configureNow = Read-Host "Deseja configurar agora? (S/N)"
        if ($configureNow -eq "S" -or $configureNow -eq "s") {
            $userName = Read-Host "Digite seu nome"
            $userEmail = Read-Host "Digite seu email"
            
            git config --global user.name $userName
            git config --global user.email $userEmail
            Write-ColorLog "Git configurado com sucesso!" "Green" "✓"
        }
        else {
            Write-ColorLog "Continuando sem configurar o Git..." "Yellow" "⚠️"
        }
    }
    else {
        Write-ColorLog "Git configurado: $gitUserName <$gitUserEmail>" "Green" "✓"
    }

    # Verificar se estamos em um repositório Git
    $isGitRepo = git rev-parse --is-inside-work-tree 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-ColorLog "Não estamos em um repositório Git. Inicializando..." "Yellow" "⚠️"
        git init
        Write-ColorLog "Repositório Git inicializado!" "Green" "✓"
    }

    # Carregar commits
    Write-ColorLog "Carregando commits..." "Cyan" "📂"
    $commits = Get-RecentCommits
    if (!$commits) {
        throw "Nenhum commit encontrado. Execute 'git commit' pelo menos uma vez antes de usar este script."
    }

    # Atualizar changelog
    Update-Changelog -Commits $commits

    # Atualizar README
    Update-Readme -Commits $commits

    # Atualizar versão
    Update-Version

    # Preparar mensagem de commit
    $commitMessage = "chore: atualização de versão para $(Get-Version)"

    # Perguntar se deseja fazer commit
    $doCommit = Read-Host "Deseja commitar as alterações? (S/N)"
    if ($doCommit -eq "S" -or $doCommit -eq "s") {
        # Commitar e dar push
        Commit-And-Push -CommitMessage $commitMessage
    }
    else {
        Write-ColorLog "Alterações não foram commitadas." "Yellow" "ℹ️"
    }

    # Limpar arquivos temporários
    Cleanup-TempFiles

    Write-ColorLog "Processo concluído com sucesso!" "Green" "🎉"
}
catch {
    Write-ColorLog "Erro: $($Error[0].Exception.Message)" "Red" "❌"
    Write-ColorLog "Local do erro: $($Error[0].ScriptStackTrace)" "Red" "📍"
    Cleanup-TempFiles
    exit 1
}
# *******************************************************