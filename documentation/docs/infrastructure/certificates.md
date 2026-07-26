---
title: TLS and certificates
sidebar_label: Certificates
sidebar_position: 3
---

# TLS and certificates

The ALB needs **two separate pieces** of certificate material:

1. **A server certificate** stored in **ACM** so the ALB can terminate HTTPS on
   ports `443` and `8443`. For tests this is usually a Let's Encrypt certificate.
2. **An FNMT trust bundle** stored in **S3** and referenced by an ALB trust store.
   This bundle contains only FNMT certification authority certificates and is
   used by the `8443` listener in `verify` mode to validate citizen client
   certificates.

These are independent: the server certificate proves the identity of the
service, the trust bundle validates the identity of the citizen.

## 1. Create a test Let's Encrypt certificate

The script `infrastructure/resources/scripts/generate-letsencrypt-cert.sh`
outlines the fastest way to obtain a certificate for the sample domain
`model-city.example.org` using a DNS-01 challenge. The process is:

1. Install `certbot` (or `acme.sh`).
2. Request a certificate for your public domain with DNS validation:
   ```bash
   sudo certbot certonly --manual --preferred-challenges dns -d model-city.example.org
   ```
3. Add the `_acme-challenge.model-city.example.org` TXT record your DNS
   provider asks for, wait for propagation, then press Enter.
4. The certificate and private key are written to
   `/etc/letsencrypt/live/model-city.example.org/`.
5. Copy them to the working directory used by the import script:
   ```bash
   mkdir -p letsencrypt-certs
   sudo cp /etc/letsencrypt/live/model-city.example.org/fullchain.pem letsencrypt-certs/
   sudo cp /etc/letsencrypt/live/model-city.example.org/privkey.pem letsencrypt-certs/
   sudo chmod 644 letsencrypt-certs/*.pem
   ```

Alternatively, automate the DNS challenge with `acme.sh` if your DNS provider
has a plugin:

```bash
curl https://get.acme.sh | sh
~/.acme.sh/acme.sh --issue --dns dns_<provider> -d model-city.example.org --server letsencrypt
```

:::tip[Domain name]

Replace `model-city.example.org` with your real domain before running the
scripts. The same domain must be set in `terraform.tfvars` as `domain`.

:::

## 2. Import the certificate into ACM

Run `infrastructure/resources/scripts/import-cert-to-acm.sh` from the same
`resources/scripts/` directory. It expects the files in `letsencrypt-certs/`:

- `privkey.pem` — the private key.
- Either `cert.pem` + `chain.pem`, or a `fullchain.pem` that the script will
  split into the two parts.

The script:

1. Verifies the private key exists.
2. Extracts the server certificate and the intermediate chain from
   `fullchain.pem` if `cert.pem`/`chain.pem` are not present.
3. Imports the certificate into **ACM in `us-east-1`**:
   ```bash
   aws acm import-certificate \
     --certificate fileb://cert.pem \
     --certificate-chain fileb://chain.pem \
     --private-key fileb://privkey.pem \
     --region us-east-1
   ```
4. Prints the resulting `CertificateArn`.
5. Reminds you to copy the ARN into `terraform.tfvars` as
   `acm_certificate_arn` and to point your DNS to the ALB.

```bash
cd infrastructure/resources/scripts
./generate-letsencrypt-cert.sh
# follow the manual DNS steps, then copy the cert files
./import-cert-to-acm.sh
# copy the printed ARN into terraform.tfvars
```

:::note[ACM region]

The import script uses `us-east-1` because that is the default region for the
Model City stacks. The ALB and the ACM certificate must be in the same region.

:::

## 3. Build the FNMT trust bundle

The ALB listener on port `8443` validates citizen client certificates in
`verify` mode. The list of accepted CAs is supplied as an ALB **trust store**
that points to a PEM bundle in S3.

The repository already contains the FNMT chain files:

- `infrastructure/resources/ac_raiz_fnmt_g2.pem` — root CA, **AC Raíz FNMT-RCM
  G2**.
- `infrastructure/resources/ac_usuarios_g2.pem` — intermediate CA, **AC Usuarios
  G2**, that issues citizen personal certificates.
- `infrastructure/resources/fnmt-client-trust-bundle.pem` — concatenation of the
  two previous files, ready to upload.

A typical citizen certificate chain looks like:

```text
AC Raíz FNMT-RCM G2
  └── AC Usuarios G2
        └── Citizen's FNMT personal certificate
```

If you need to rebuild the bundle (for example because the citizen certificate
you are testing with was issued by a different FNMT intermediate such as *AC
Representación G2* or *AC Sector Público G2*), concatenate the PEM files in the
correct order:

```bash
cat ac_raiz_fnmt_g2.pem ac_usuarios_g2.pem > fnmt-client-trust-bundle.pem
```

:::warning[Only CA certificates]

The trust bundle must contain **only** public CA certificates. Never include a
citizen's personal certificate or its private key.

:::

## 4. Wire the trust bundle into Terraform

Set the local path in `terraform.tfvars`:

```hcl
mtls_trust_bundle_path = "../../../resources/fnmt-client-trust-bundle.pem"
```

`alb.tf` then:

1. Creates an S3 bucket (`${local.name}-mtls-truststore-...`).
2. Uploads the bundle as `fnmt-client-trust-bundle.pem`.
3. Creates an `aws_lb_trust_store` resource named `${local.name}-fnmt` that
   points at the S3 object.
4. Attaches the trust store to the `8443` listener with `mutual_authentication`
   set to `mode = "verify"`.

```hcl
resource "aws_lb_listener" "https_mtls" {
  load_balancer_arn = aws_lb.public.arn
  port              = 8443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.acm_certificate_arn

  mutual_authentication {
    mode            = "verify"
    trust_store_arn = aws_lb_trust_store.fnmt.arn
  }

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway.arn  # or monolith
  }
}
```

## 5. Certificate renewal

Let's Encrypt certificates are valid for **90 days**. Before they expire,
renew and re-import:

```bash
sudo certbot renew
./import-cert-to-acm.sh
```

Then update `acm_certificate_arn` in `terraform.tfvars` with the new ARN and run
`terraform apply` so the ALB picks it up. For production workloads, consider a
managed ACM certificate or an automated renewal pipeline instead of the manual
script.
