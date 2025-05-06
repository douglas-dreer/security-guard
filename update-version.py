#!/usr/bin/env python3
"""
Version Update Script - Automates changelog generation and version management
"""

import os
import re
import sys
import subprocess
from datetime import datetime
import shutil

# Configuration
COMMIT_HISTORY_FILE = "commits.log"
CHANGELOG_FILE = "CHANGELOG.md"
README_FILE = "README.md"
VERSION_FILE = "version.txt"

# Icons for documentation
CHANGELOG_ICON = "📝"  # Document icon for changelog
README_ICON = "📘"     # Book icon for README
VERSION_ICON = "🏷️"    # Tag icon for version
COMMIT_ICON = "✅"     # Checkmark icon for commit

# ANSI color codes for terminal output
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
    """Print a colored message with an optional icon prefix."""
    if icon:
        print(f"{icon} {color}{message}{Colors.RESET}")
    else:
        print(f"{color}{message}{Colors.RESET}")

def check_git_installation():
    """Check if Git is installed and available."""
    try:
        git_version = subprocess.check_output(["git", "--version"], universal_newlines=True).strip()
        print_colored(f"Git found: {git_version}", Colors.GREEN, "✓")
        return True
    except (subprocess.SubprocessError, FileNotFoundError):
        print_colored("Git is not installed or not in PATH", Colors.RED, "✗")
        return False

def get_recent_commits(count=10):
    """Get recent commits from Git history."""
    print_colored("Getting recent commits...", Colors.CYAN, "🔍")
    
    if not os.path.exists(COMMIT_HISTORY_FILE):
        print_colored(f"File {COMMIT_HISTORY_FILE} not found. Getting commits from Git...", Colors.YELLOW, "⚠️")
        try:
            git_log = subprocess.check_output(
                ["git", "log", f"-n{count}", "--pretty=format:%H|%ad|%s", "--date=format:%d-%m-%Y"],
                universal_newlines=True
            )
            with open(COMMIT_HISTORY_FILE, "w", encoding="utf-8") as f:
                f.write(git_log)
            print_colored(f"Commits saved to {COMMIT_HISTORY_FILE} successfully!", Colors.GREEN, "✓")
        except subprocess.SubprocessError as e:
            raise Exception(f"Error executing git log: {e}")
    
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
        raise Exception(f"No commits found in {COMMIT_HISTORY_FILE}")
    
    print_colored(f"Commits loaded successfully: {len(commits)} found.", Colors.GREEN, "✓")
    return commits

def get_section_content(commits, type_pattern):
    """Get content for a specific section of the changelog."""
    section_content = ""
    pattern = re.compile(type_pattern, re.IGNORECASE)
    
    for commit in commits:
        if pattern.search(commit["message"]):
            # Replace the matched pattern with an empty string (only first occurrence)
            message = pattern.sub("", commit["message"], count=1).strip()
            section_content += f"- [{commit['hash'][:7]}] {commit['date']} - {message}\n"
    
    return section_content if section_content else None

def update_changelog(commits):
    """Update the changelog file with commit information."""
    print_colored("Updating changelog...", Colors.CYAN, f"{CHANGELOG_ICON}")
    
    types = {
        "Added": r"(add|feat|new)",
        "Changed": r"(change|update|modify)",
        "Fixed": r"(fix|bug|repair)",
        "Removed": r"(remove|delete|rm)",
        "Security": r"(security|sec|cve)",
        "Tests": r"(test|spec|check)"
    }
    
    type_icons = {
        "Added": "✨",
        "Changed": "🔄",
        "Fixed": "🐛",
        "Removed": "🗑️",
        "Security": "🔒",
        "Tests": "🧪"
    }

    changelog_content = f"# {CHANGELOG_ICON} Changelog\n"
    changelog_content += "All notable changes to this project will be documented in this file.\n"
    changelog_content += "The format is based on [Keep a Changelog](https://keepachangelog.com/)\n"
    changelog_content += "and this project adheres to [Semantic Versioning](https://semver.org/).\n\n"

    current_version = get_version()
    today = datetime.now().strftime("%Y-%m-%d")
    changelog_content += f"## [{current_version}] - {today}\n\n"

    for type_name, pattern in types.items():
        section_content = get_section_content(commits, pattern)
        if section_content:
            icon = type_icons.get(type_name, "•")
            changelog_content += f"### {icon} {type_name}\n{section_content}\n"

    # Try to get repository name from Git
    try:
        remote_url = subprocess.check_output(
            ["git", "config", "--get", "remote.origin.url"],
            universal_newlines=True
        ).strip()
        
        # Extract user/repo from remote URL
        repo_name_match = re.search(r'[:/]([^/]+)/([^/.]+)(\.git)?$', remote_url)
        if repo_name_match:
            user, repo = repo_name_match.groups()[0:2]
            repo_path = f"{user}/{repo}"
            
            changelog_content += f"\n[Unreleased]: https://github.com/{repo_path}/compare/v{current_version}...main\n"
            changelog_content += f"[v{current_version}]: https://github.com/{repo_path}/releases/tag/v{current_version}\n"
    except subprocess.SubprocessError:
        print_colored("Could not determine repository URL. Omitting links.", Colors.YELLOW, "⚠️")

    # Backup existing changelog
    if os.path.exists(CHANGELOG_FILE):
        shutil.copy2(CHANGELOG_FILE, f"{CHANGELOG_FILE}.bak")
        print_colored("Backup of previous changelog created.", Colors.YELLOW, "💾")
    
    with open(CHANGELOG_FILE, "w", encoding="utf-8") as f:
        f.write(changelog_content)
    
    print_colored("Changelog updated successfully!", Colors.GREEN, "✓")

def update_readme(commits):
    """Update the README file with project information."""
    print_colored("Updating README...", Colors.CYAN, f"{README_ICON}")
    
    readme_content = f"# {README_ICON} My Project\n"
    readme_content += "## 📋 About\n"
    readme_content += "This is an amazing project that aims [project description].\n\n"
    readme_content += "## ✨ Features\n"
    
    features = []
    for commit in commits:
        if re.search(r"add|feat|new", commit["message"], re.IGNORECASE):
            feature = re.sub(r"add|feat|new", "", commit["message"], flags=re.IGNORECASE, count=1).strip()
            features.append(f"* {feature}")
    
    if features:
        readme_content += "\n".join(features) + "\n\n"
    
    readme_content += f"## {VERSION_ICON} Version\n"
    readme_content += f"Current version: **{get_version()}**\n\n"
    readme_content += f"## {CHANGELOG_ICON} Version History\n"
    readme_content += "To see the version history, click [here](CHANGELOG.md).\n\n"
    readme_content += "## 👥 Contributing\n"
    readme_content += "Contributions are welcome! For more details, read the [CONTRIBUTING.md](CONTRIBUTING.md) file.\n\n"
    readme_content += "## 📄 License\n"
    readme_content += "This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.\n"

    # Backup existing README
    if os.path.exists(README_FILE):
        shutil.copy2(README_FILE, f"{README_FILE}.bak")
        print_colored("Backup of previous README created.", Colors.YELLOW, "💾")
    
    with open(README_FILE, "w", encoding="utf-8") as f:
        f.write(readme_content)
    
    print_colored("README updated successfully!", Colors.GREEN, "✓")

def get_version():
    """Get the current version from the version file."""
    print_colored("Getting current version...", Colors.CYAN, f"{VERSION_ICON}")
    
    if os.path.exists(VERSION_FILE):
        with open(VERSION_FILE, "r") as f:
            version = f.read().strip()
        print_colored(f"Current version: {version}", Colors.GREEN, f"{VERSION_ICON}")
        return version
    else:
        print_colored("Version not found. Starting with version 1.0.0.", Colors.YELLOW, "⚠️")
        with open(VERSION_FILE, "w") as f:
            f.write("1.0.0")
        return "1.0.0"

def increment_version(version):
    """Increment the version number (patch version)."""
    print_colored("Incrementing version...", Colors.CYAN, "🔢")
    
    version_parts = version.split(".")
    if len(version_parts) < 3:
        version_parts.extend(["0"] * (3 - len(version_parts)))
    
    version_parts[2] = str(int(version_parts[2]) + 1)
    
    return ".".join(version_parts)

def update_version():
    """Update the version file with an incremented version number."""
    print_colored("Updating version...", Colors.CYAN, f"{VERSION_ICON}")
    
    current_version = get_version()
    new_version = increment_version(current_version)
    
    with open(VERSION_FILE, "w") as f:
        f.write(new_version)
    
    print_colored(f"Version updated to {new_version}", Colors.GREEN, f"{VERSION_ICON}")
    return new_version

def commit_and_push(commit_message):
    """Commit changes and push to remote repository."""
    print_colored("Preparing to commit...", Colors.CYAN, "🔄")
    
    # Check if there are changes to commit
    git_status = subprocess.check_output(["git", "status", "--porcelain"], universal_newlines=True)
    if not git_status:
        print_colored("No changes to commit.", Colors.YELLOW, "⚠️")
        return
    
    # Add all changes
    subprocess.check_call(["git", "add", "."])
    
    # Commit changes
    print_colored(f"Committing with message: {commit_message}", Colors.CYAN, f"{COMMIT_ICON}")
    try:
        subprocess.check_call(["git", "commit", "-m", commit_message])
        print_colored("Commit successful!", Colors.GREEN, "✓")
    except subprocess.SubprocessError as e:
        raise Exception(f"Error committing: {e}")
    
    # Push changes
    print_colored("Pushing changes to remote repository...", Colors.CYAN, "☁️")
    try:
        subprocess.check_call(["git", "push"])
        print_colored("Changes pushed successfully!", Colors.GREEN, "✓")
    except subprocess.SubprocessError as e:
        raise Exception(f"Error pushing: {e}")

def cleanup_temp_files():
    """Remove temporary files."""
    print_colored("Cleaning up temporary files...", Colors.CYAN, "🧹")
    
    if os.path.exists(COMMIT_HISTORY_FILE):
        os.remove(COMMIT_HISTORY_FILE)
        print_colored("Temporary file removed.", Colors.YELLOW, "✓")

def main():
    """Main execution flow."""
    try:
        # Check if Git is installed
        if not check_git_installation():
            raise Exception("Git is not installed or not available in PATH. Please install Git and try again.")
        
        # Check Git configuration
        print_colored("Checking Git configuration...", Colors.CYAN, "⚙️")
        try:
            git_user_name = subprocess.check_output(
                ["git", "config", "--global", "user.name"],
                universal_newlines=True
            ).strip()
            git_user_email = subprocess.check_output(
                ["git", "config", "--global", "user.email"],
                universal_newlines=True
            ).strip()
            
            print_colored(f"Git configured: {git_user_name} <{git_user_email}>", Colors.GREEN, "✓")
        except subprocess.SubprocessError:
            print_colored("Git configuration incomplete. Please configure:", Colors.YELLOW, "⚠️")
            print_colored("git config --global user.name 'Your Name'", Colors.YELLOW)
            print_colored("git config --global user.email 'your.email@example.com'", Colors.YELLOW)
            
            if input("Configure Git now? (y/n): ").lower() == 'y':
                user_name = input("Enter your name: ")
                user_email = input("Enter your email: ")
                
                subprocess.check_call(["git", "config", "--global", "user.name", user_name])
                subprocess.check_call(["git", "config", "--global", "user.email", user_email])
                print_colored("Git configured successfully!", Colors.GREEN, "✓")
            else:
                print_colored("Continuing without configuring Git...", Colors.YELLOW, "⚠️")
        
        # Check if we're in a Git repository
        try:
            subprocess.check_output(["git", "rev-parse", "--is-inside-work-tree"], stderr=subprocess.STDOUT)
        except subprocess.SubprocessError:
            print_colored("Not in a Git repository. Initializing...", Colors.YELLOW, "⚠️")
            subprocess.check_call(["git", "init"])
            print_colored("Git repository initialized!", Colors.GREEN, "✓")
        
        # Load commits
        print_colored("Loading commits...", Colors.CYAN, "📂")
        commits = get_recent_commits()
        if not commits:
            raise Exception("No commits found. Make at least one commit before using this script.")
        
        # Update changelog
        update_changelog(commits)
        
        # Update README
        update_readme(commits)
        
        # Update version
        new_version = update_version()
        
        # Prepare commit message
        commit_message = f"chore: version update to {new_version}"
        
        # Ask if user wants to commit
        if input("Do you want to commit the changes? (y/n): ").lower() == 'y':
            commit_and_push(commit_message)
        else:
            print_colored("Changes were not committed.", Colors.YELLOW, "ℹ️")
        
        # Clean up
        cleanup_temp_files()
        
        print_colored("Process completed successfully!", Colors.GREEN, "🎉")
        
    except Exception as e:
        print_colored(f"Error: {str(e)}", Colors.RED, "❌")
        import traceback
        print_colored(f"Location: {traceback.format_exc()}", Colors.RED, "📍")
        cleanup_temp_files()
        sys.exit(1)

if __name__ == "__main__":
    main()