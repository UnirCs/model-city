# Model City — Infraestructura

Orquestación centralizada del despliegue de una instancia de ciudad en AWS.
Cada topología es autocontenida (scripts + Terraform) y son **mutuamente
excluyentes**: comparten nombres de recursos (ALB, cluster, RDS, dominio), así
que solo puede haber una desplegada a la vez.

```
infrastructure/
├── microservices/          # Spring Cloud Gateway + Eureka + 5 servicios
│   ├── deploy.sh           # menú de despliegue (init / redeploy / config / cleanup)
│   ├── scripts/            # init DB, generación/importación de certificados
│   └── terraform/aws/
└── monolith/               # un único artefacto Spring Boot
    ├── deploy-monolith.sh
    ├── scripts/
    └── terraform/aws-monolith/
```

## Ubicación del back-end y del front-end

El despliegue construye y dockeriza tanto el back-end como el front-end, que
viven en repositorios separados de la infraestructura. Sus rutas se declaran
como **variables de Terraform** en `terraform.tfvars` (copiar desde
`terraform.tfvars.example`):

| Variable               | Descripción                                                     |
|------------------------|-----------------------------------------------------------------|
| `backend_project_dir`  | Ruta absoluta a la raíz Maven del back-end (contiene `pom.xml`). |
| `frontend_project_dir` | Ruta absoluta al proyecto Next.js (contiene `Dockerfile`).      |

El script de despliegue lee estas rutas de los outputs de Terraform. Como
alternativa se pueden pasar por variable de entorno (`BACKEND_PROJECT_DIR`,
`FRONTEND_PROJECT_DIR`); si faltan ambas, el script las pide de forma
interactiva.

## Uso

```bash
cd microservices        # o: cd monolith
cp terraform/aws/terraform.tfvars terraform/aws/terraform.tfvars
# editar terraform.tfvars: backend_project_dir, frontend_project_dir, secretos...
./deploy.sh             # o: ./deploy-monolith.sh
```
