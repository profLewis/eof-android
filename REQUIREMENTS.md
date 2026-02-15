# Requirements and Setup (`eof-android`)

## Required tools
- macOS on Apple Silicon or Intel.
- Java 17 (recommended for Android Gradle Plugin 8.x).
- Android Studio (latest stable), including:
  - Android SDK Platform 35
  - Android SDK Build-Tools
  - Android SDK Command-line Tools
- Git

## Optional but recommended
- GitHub CLI (`gh`) for repo creation/auth and scripted push workflows.

## Current machine status (checked)
- `gh` is installed.
- Java installed: JDK 24 found.
- `gh` auth token is invalid and must be refreshed.

## Install / fix commands

### 0) One-command Android SDK install (recommended)
```bash
./scripts/install_android_sdk.sh
```
This installs/updates `~/Library/Android/sdk` with:
- `platform-tools`
- `platforms;android-35`
- `build-tools;35.0.0`
- `cmdline-tools;latest`

### 1) Install Java 17 (if missing)
```bash
brew install --cask temurin@17
```

Then set Java 17 for this shell:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version
```

### 2) Install Android Studio (if missing)
```bash
brew install --cask android-studio
```

Open Android Studio once and install SDK components in SDK Manager.

### 3) Re-authenticate GitHub CLI (required for auto upload)
```bash
gh auth login -h github.com
# verify
gh auth status
```

## Gradle usage
Once Java/SDK are set:
```bash
./gradlew test
./gradlew installDebug
```

Notes:
- First run downloads a Gradle distribution from `services.gradle.org`.
- If network is unavailable, Gradle cannot bootstrap.

## Auto-upload (auto push after each commit)
This repo includes:
- `.githooks/post-commit` (opt-in push hook)
- `scripts/enable_auto_push.sh`

Enable it:
```bash
./scripts/enable_auto_push.sh
```

What it does:
- Sets `core.hooksPath=.githooks`
- Sets `hooks.autopush=true`
- After each commit, runs `git push -u origin <current-branch>`

Disable it:
```bash
git config hooks.autopush false
```

## One-time GitHub remote setup
If not already set:
```bash
# from eof-android
gh repo create eof-android --public --source . --remote origin --push
```

Or with an existing repo:
```bash
git remote add origin <repo-url>
git push -u origin main
```
