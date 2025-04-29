# Script para atualização de versão e documentação
# Autor: AI Assistant
# Data: $(Get-Date -Format "yyyy-MM-dd")

# Configurações
$COMMIT_HISTORY_FILE = "commit-history.log"
$CHANGELOG_FILE = "CHANGELOG.md"
$README_FILE = "README.md"
$POM_FILE = "pom.xml"
$COMMITS_TO_ANALYZE = 10

# Função para obter commits recentes
function Get-RecentCommits {
    Write-Host "📥 Obtendo últimos $COMMITS_TO_ANALYZE commits..."
    git log -n $COMMITS_TO_ANALYZE --pretty=format:"%h|%ad|%s" --date=short > $COMMIT_HISTORY_FILE
    Write-Host "✅ Histórico salvo em $COMMIT_HISTORY_FILE"
}

# Função para analisar commits e gerar changelog
function Update-Changelog {
    Write-Host "🔍 Analisando commits e atualizando changelog..."
    
    $commits = Get-Content $COMMIT_HISTORY_FILE | ForEach-Object {
        $parts = $_ -split '\|'
        @{
            Hash = $parts[0]
            Date = $parts[1]
            Message = $parts[2]
        }
    }

    # Filtrar commits relevantes
    $relevantCommits = $commits | Where-Object {
        $_.Message -notmatch "changelog|readme|version"
    }

    # Agrupar por data
    $groupedCommits = $relevantCommits | Group-Object Date | Sort-Object Name -Descending

    # Gerar conteúdo do changelog
    $newVersion = Get-CurrentVersion
    $newVersion = Increment-Version $newVersion
    $today = Get-Date -Format "yyyy-MM-dd"

    $changelogContent = @"
## [$newVersion] - $today

### ✨ Adicionado
$(Get-AddedFeatures $groupedCommits)

### 🐛 Corrigido
$(Get-FixedBugs $groupedCommits)

### 🔧 Alterado
$(Get-ChangedFeatures $groupedCommits)

### 🗑️ Removido
$(Get-RemovedFeatures $groupedCommits)

### 📦 Dependências
$(Get-Dependencies $groupedCommits)

### 🧪 Testes
$(Get-Tests $groupedCommits)

### 🚀 Performance
$(Get-Performance $groupedCommits)

### 🔒 Segurança
$(Get-Security $groupedCommits)

"@

    # Adicionar ao início do changelog
    $currentChangelog = Get-Content $CHANGELOG_FILE -Raw
    $newChangelog = $changelogContent + "`n" + $currentChangelog
    $newChangelog | Set-Content $CHANGELOG_FILE
    Write-Host "✅ Changelog atualizado"
}

# Função para obter a versão atual do pom.xml
function Get-CurrentVersion {
    $pomContent = Get-Content $POM_FILE -Raw
    $versionMatch = [regex]::Match($pomContent, '<version>(.*?)</version>')
    return $versionMatch.Groups[1].Value
}

# Função para incrementar versão
function Increment-Version {
    param($version)
    $version = $version -replace '-SNAPSHOT$', ''
    $parts = $version -split '\.'
    $parts[2] = [int]$parts[2] + 1
    return ($parts -join '.') + '-SNAPSHOT'
}

# Função para atualizar versão no pom.xml
function Update-PomVersion {
    param($newVersion)
    Write-Host "🔖 Atualizando versão no pom.xml..."
    $pomContent = Get-Content $POM_FILE -Raw
    $pomContent = $pomContent -replace '<version>.*?</version>', "<version>$newVersion</version>"
    $pomContent | Set-Content $POM_FILE
    Write-Host "✅ Versão atualizada para $newVersion"
}

# Funções auxiliares para categorizar commits
function Get-AddedFeatures {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^add|^feat|^new' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-FixedBugs {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^fix|^bug' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-ChangedFeatures {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^change|^update|^modify' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-RemovedFeatures {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^remove|^delete' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-Dependencies {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^dep|^depend|^upgrade' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-Tests {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^test|^spec' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-Performance {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^perf|^optimize' } | 
        ForEach-Object { "- $($_.Message)" }
}

function Get-Security {
    param($commits)
    return $commits | Where-Object { $_.Message -match '^security|^sec|^vuln' } | 
        ForEach-Object { "- $($_.Message)" }
}

# Função para atualizar README
function Update-Readme {
    Write-Host "📖 Atualizando README..."
    $newVersion = Get-CurrentVersion
    $readmeContent = Get-Content $README_FILE -Raw
    
    # Atualizar badges de versão
    $readmeContent = $readmeContent -replace 'version-\d+\.\d+\.\d+', "version-$newVersion"
    
    $readmeContent | Set-Content $README_FILE
    Write-Host "✅ README atualizado"
}

# Função para fazer commit e push
function Commit-And-Push {
    param($message)
    Write-Host "✔️ Fazendo commit: $message"
    git add .
    git commit -m $message
    git push
    Write-Host "✅ Alterações enviadas para o repositório"
}

# Função para limpar arquivos temporários
function Cleanup-TempFiles {
    Write-Host "🧹 Limpando arquivos temporários..."
    if (Test-Path $COMMIT_HISTORY_FILE) {
        Remove-Item $COMMIT_HISTORY_FILE
        Write-Host "✅ Arquivo $COMMIT_HISTORY_FILE removido"
    }
}

# Execução principal
try {
    # Obter commits recentes
    Get-RecentCommits
    
    # Atualizar changelog
    Update-Changelog
    
    # Atualizar versão no pom.xml
    $newVersion = Get-CurrentVersion
    $newVersion = Increment-Version $newVersion
    Update-PomVersion $newVersion
    
    # Atualizar README
    Update-Readme
    
    # Commit e push
    Commit-And-Push "chore: atualização de versão para $newVersion"
    Commit-And-Push "docs: atualização de changelog e readme"
    
    # Limpar arquivos temporários
    Cleanup-TempFiles
    
    Write-Host "🎉 Processo de atualização concluído com sucesso!"
} catch {
    Write-Host "❌ Erro durante o processo: $_"
    # Garantir que o arquivo de log seja removido mesmo em caso de erro
    Cleanup-TempFiles
    exit 1
} 