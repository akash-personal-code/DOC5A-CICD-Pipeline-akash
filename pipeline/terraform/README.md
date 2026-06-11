# Terraform / Azure Validation Notes

This repository is primarily an assessment architecture and CI/CD evidence package. The Terraform folder is intentionally minimal so that it does not create paid cloud infrastructure by accident.

For Azure validation on a free/trial account, use one of these options:

1. **Recommended for screenshots:** run the GitHub Actions evidence pipeline and the local Docker Compose stack on your machine or Azure VM. This proves build, scan, test, runtime, and evidence generation without needing AKS.
2. **Optional AKS demo:** create a small AKS cluster manually or through your own Terraform module, install ArgoCD/Helm, create Kubernetes Secrets, then deploy `pipeline/helm/novapay-lite`.

Before AKS/ArgoCD deployment, replace `repoURL` in `pipeline/argocd/novapay-lite-application.yaml` with your actual GitHub repository URL.

Do not commit Azure service principal secrets, kubeconfig files, registry passwords, or `.env` files.
