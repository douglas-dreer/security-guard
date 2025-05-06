#!/usr/bin/env python3
import os
import re
import subprocess
from datetime import datetime
import shutil
import sys

CHANGELOG_FILE = "CHANGELOG.md"
README_FILE = "README.md"
GRADLE_FILE = "gradle.properties"

def get_version():
    if os.path.exists(GRADLE_FILE):
        with open(GRADLE_FILE, "r", encoding="utf-8") as f:
            for line in f:
                m = re.match(r"version\s*=\s*([\d\.]+)", line)
                if m:
                    return m.group(1)
    return "1.0.0"

def set_version(new_version):
    if os.path.exists(GRADLE_FILE):
        with open(GRADLE_FILE, "r", encoding="utf-8") as f:
            content = f.read()
        content = re.sub(r"version\s*=\s*[\d\.]+", f"version={new_version}", content)
        with open(GRADLE_FILE, "w", encoding="utf-8") as f:
            f.write(content)

def increment_version(version, commit_msg):
    # SemVer: MAJOR.MINOR.PATCH
    major, minor, patch = map(int, version.split("."))
    if re.search(r"(?i)break|major", commit_msg):
        major += 1
        minor = 0
        patch = 0
    elif re.search(r"(?i)feat|minor", commit_msg):
        minor += 1
        patch = 0
    else:
        patch += 1
    return f"{major}.{minor}.{patch}"

def get_commits():
    log = subprocess.check_output(
        ["git", "log", "-n", "10", "--pretty=format:%H|%ad|%s", "--date=format:%d-%m-%Y"],
        universal_newlines=True
    )
    commits = []
    for line in log.splitlines():
        parts = line.split("|", 2)
        if len(parts) == 3:
            commits.append({"hash": parts[0], "date": parts[1], "message": parts[2]})
    return commits

def update_changelog(commits, version):
    today = datetime.now().strftime("%Y-%m-%d")
    changelog = f"# 📝 Changelog\nTodas as alterações notáveis neste projeto serão documentadas neste arquivo.\nO formato é baseado em [Keep a Changelog](https://keepachangelog.com/)\ne este projeto adere ao [Versionamento Semântico](https://semver.org/).\n\n"
    changelog += f"## [{version}] - {today}\n\n"
    for c in commits:
        changelog += f"- [{c['hash'][:7]}] {c['date']} - {c['message']}\n"
    with open(CHANGELOG_FILE, "w", encoding="utf-8") as f:
        f.write(changelog)

def update_readme(version):
    with open(README_FILE, "r", encoding="utf-8") as f:
        content = f.read()
    content = re.sub(r"Versão atual: \*\*[\d\.]+\*\*", f"Versão atual: **{version}**", content)
    with open(README_FILE, "w", encoding="utf-8") as f:
        f.write(content)

def main():
    commits = get_commits()
    if not commits:
        print("Nenhum commit encontrado.")
        sys.exit(1)
    current_version = get_version()
    # Usa o commit mais recente para decidir o tipo de incremento
    new_version = increment_version(current_version, commits[0]["message"])
    set_version(new_version)
    update_changelog(commits, new_version)
    update_readme(new_version)
    print(f"Versão atualizada para {new_version}")

if __name__ == "__main__":
    main()import os
    actor = os.environ.get("GITHUB_ACTOR")
