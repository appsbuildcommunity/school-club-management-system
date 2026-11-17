# Security Policy

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Report them privately through GitHub:
[Report a vulnerability](https://github.com/appsbuildcommunity/school-club-management-system/security/advisories/new)

Include what you found, the steps to reproduce it, and the impact you expect.
We aim to reply within 7 days and will keep you updated until it is fixed.

## Supported versions

The project is in active development. Only the `main` branch receives security fixes.

## Automated checks

Every pull request to `main` runs secret scanning (Gitleaks), static analysis
(CodeQL and Semgrep), dependency review, and container and configuration scans
(Trivy and Hadolint). See `.github/workflows/`.
