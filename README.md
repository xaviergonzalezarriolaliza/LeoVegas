# LeoVegas
Leo Vegas API java tests with RestAssured + JUnit

---

## How to link your local workspace to this repository

### Option A — Your local folder is NOT yet a git repository

```bash
cd /path/to/your/local/LeoVegas

# 1. Initialise git
git init

# 2. Add all your files
git add .
git commit -m "Initial commit"

# 3. Link to this GitHub repo
git remote add origin https://github.com/xaviergonzalezarriolaliza/LeoVegas.git

# 4. Push (replace 'main' with your branch name if different)
git push -u origin main
```

---

### Option B — Your local folder is already a git repository

```bash
cd /path/to/your/local/LeoVegas

# 1. Link to this GitHub repo (skip if remote already exists)
git remote add origin https://github.com/xaviergonzalezarriolaliza/LeoVegas.git

# 2. Pull remote content first to avoid conflicts
git pull origin main --allow-unrelated-histories

# 3. Push your local files
git push -u origin main
```

---

### Option C — You already have a remote set and just want to push

```bash
cd /path/to/your/local/LeoVegas

# Verify remote is correct
git remote -v

# Push
git push -u origin main
```

---

> **Tip:** Replace `main` with `master` (or any other branch name) if that is the default branch in your local repository.
