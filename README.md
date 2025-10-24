# Guida Repository - Le Basi

## 🌳 Struttura Branch

### Branch Principali

**`main`** → Produzione (codice stabile)
**`develop`** → Sviluppo (base per nuove feature)

### Branch Temporanei

**`feature/*`** → Nuove funzionalità
**`bugfix/*`** → Correzione bug
**`hotfix/*`** → Fix urgenti in produzione

---

## 🔄 Workflow Base

### 1. Creare una Feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-feature
```

### 2. Lavorare sulla Feature
```bash
# Modifica i file...
git add .
git commit -m "feat: descrizione modifica"
git push -u origin feature/nome-feature
```

### 3. Aggiornare il Branch
```bash
git checkout develop
git pull origin develop
git checkout feature/nome-feature
git rebase develop
git push --force-with-lease
```

### 4. Creare Pull Request

1. Vai su GitHub/GitLab
2. Crea PR da `feature/nome-feature` → `develop`
3. Chiedi review
4. Dopo approvazione: **Merge**
5. Elimina il branch

---

## 📝 Convenzioni Commit
```bash
feat: nuova funzionalità
fix: correzione bug
docs: documentazione
refactor: refactoring codice
test: aggiunta test
```

**Esempi:**
```bash
git commit -m "feat: aggiungi login utente"
git commit -m "fix: correggi validazione email"
git commit -m "docs: aggiorna README"
```

---

## 🔍 Pull Request

### Template Minimo
```markdown
## Cosa fa
Breve descrizione

## Modifiche
- Punto 1
- Punto 2

## Test
- [ ] Testato localmente
- [ ] Test passano
```

### Processo
1. Crea PR
2. Assegna reviewer
3. Attendi feedback
4. Implementa modifiche
5. Merge dopo approvazione

---

## ⚠️ Regole Importanti

### ✅ DA FARE
- Commit frequenti e piccoli
- Branch piccoli (< 400 righe)
- Testa prima di pushare
- Aggiorna spesso da develop

### ❌ NON FARE
- Push diretto su `main` o `develop`
- Force push su branch condivisi
- Commit di file sensibili (.env, password)
- Branch aperti troppo a lungo

---

## 🛠️ Comandi Essenziali
```bash
# Vedere branch
git branch -a

# Cambiare branch
git checkout nome-branch

# Status modifiche
git status

# Vedere differenze
git diff

# Annullare modifiche
git checkout -- file.js

# Eliminare branch locale
git branch -d feature/nome

# Aggiornare da remoto
git pull origin develop

# Salvare modifiche temporanee
git stash
git stash pop
```

---

## 🆘 Risoluzione Problemi

### Ho committato sul branch sbagliato
```bash
git reset --soft HEAD~1  # Annulla ultimo commit
git checkout branch-giusto
git commit -m "messaggio"
```

### Conflitti durante rebase/merge
```bash
# Risolvi conflitti nei file
git add .
git rebase --continue
# oppure
git merge --continue
```

### Devo modificare l'ultimo commit
```bash
git commit --amend -m "nuovo messaggio"
```

---

## 📊 Flusso Completo
```
develop
  ↓
  └─→ feature/mia-feature
        ↓ (lavoro + commit)
        ↓ (push)
        ↓ (Pull Request)
        ↓ (Review + Approvazione)
        ↓ (Merge)
  ←───┘
develop (aggiornato)
```

---

**Ricorda**: In caso di dubbi, chiedi sempre al team prima di fare operazioni potenzialmente pericolose!
