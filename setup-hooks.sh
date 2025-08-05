#!/usr/bin/env bash
set -e

echo "[*] Setting up Git hooks for code quality..."

mkdir -p .git/hooks

cat > .git/hooks/pre-commit << 'EOF'
#!/usr/bin/env bash
set -e

echo "[HOOK] Auto-formatting code before commit..."

# Check if there are any staged files that need formatting
if git diff --cached --name-only | grep -q '\.java$\|\.xml$\|\.md$\|\.ya?ml$'; then
    echo "[HOOK] Applying code formatting..."
    
    # Format all files (not just staged ones to avoid partial formatting)
    mvn spotless:apply -q
    
    # Add any formatting changes back to staging area
    git add -A
    
    echo "[HOOK] ✓ Code formatting applied and staged"
else
    echo "[HOOK] No files to format"
fi
EOF

chmod +x .git/hooks/pre-commit

cat > .git/hooks/pre-push << 'EOF'
#!/usr/bin/env bash
set -e

echo "[HOOK] Running pre-push quality checks..."

# Ensure formatting is applied (safety check)
echo "[HOOK] Ensuring code is formatted..."
mvn spotless:apply -q

# Run tests
echo "[HOOK] Running tests..."
mvn test -q

echo "[HOOK] ✓ All pre-push checks passed"
EOF

chmod +x .git/hooks/pre-push

echo "hooks configured"
echo "- pre-commit: auto-formats code and stages changes"
echo "- pre-push: ensures formatting and runs tests"