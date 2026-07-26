---
title: Verify mTLS certificate
sidebar_label: Verify certificate
sidebar_position: 8
---

# Verify mTLS certificate (DNIe / electronic certificate)

`POST /api/core/certificate-verifications` → `HandleCertificateVerificationUseCase`
(`DecodeCertificateUseCase` + `VerifyCertificateUseCase`)

Part of the Spanish electronic-identity flow over mTLS: the **AWS ALB** performs the mutual-TLS handshake and
forwards the client certificate in `X-Amzn-Mtls-Clientcert-*` headers. The use case parses and
validates the certificate (issuer, subject, validity), extracts the document number (DNI) from the
subject, binds the **hashed** DNI to the authenticated account and issues a signed
**verification token**. Because it creates a *certificate-verification* resource (persists the DNI
hash and mints a token) it is a **`POST`**, not a `GET`.

The token is the credential later forwarded to
[create challenge](./create-challenge.md) (operation authorization) for DNI-bound operations (e.g.
voting). The response is always plain JSON — the endpoint never redirects.

Certificate errors: missing issuer or subject → `400`; expired certificate → `410 Gone`.
Authentication is required (the gateway must inject `X-Auth-Sub`); otherwise → `401`.

## Inputs

**`POST /api/core/certificate-verifications`** — no request body; the certificate travels in the
`X-Amzn-Mtls-Clientcert-*` headers added by the ALB, and the caller's `Authorization: Bearer <JWT>`
is turned into `X-Auth-Sub` by the gateway.

## Outputs

- **`200 OK`** — `CertificateIdentityDto` (JSON). No redirect.
- **`400 Bad Request`** — incomplete certificate (missing issuer or subject).
- **`401 Unauthorized`** — no authenticated principal (`X-Auth-Sub` missing).
- **`410 Gone`** — certificate expired.

```json
{
  "verificationToken": "eyJ.....",
  "dni": "edcdb39bcd7d186385ad3c4b9532d299"
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor B as Browser
    box rgb(255,243,224) Perimeter
        participant ALB as AWS ALB (mTLS)
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant UC as CertificateVerificationsController
        participant HC as HandleCertificateVerificationUseCase
        participant DC as DecodeCertificateUseCase
        participant VC as VerifyCertificateUseCase
    end

    B->>ALB: mTLS handshake (client certificate)
    ALB->>GW: POST /api/core/certificate-verifications<br>+ X-Amzn-Mtls-Clientcert-* headers + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: POST /certificate-verifications (cert headers, X-Auth-Sub=sub)
    UC->>HC: execute(sub, subject, issuer, validity, leaf)
    alt no X-Auth-Sub
        HC-->>B: 401 Unauthorized
    else authenticated
        HC->>DC: parse(subject, issuer, validity, leaf)
        alt missing issuer or subject
            DC-->>B: 400 (IncompleteCertificate)
        else expired certificate
            DC-->>B: 410 Gone (CertificateExpired)
        else valid
            DC-->>HC: raw DNI
            HC->>VC: bindAndIssueToken(sub, rawDni)
            VC-->>HC: signed verification token
            HC-->>B: 200 CertificateIdentityDto (dni hash + verificationToken)
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor B as Browser
    box rgb(255,243,224) Perimeter
        participant ALB as AWS ALB (mTLS)
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant UC as CertificateVerificationsController
        participant HC as HandleCertificateVerificationUseCase
        participant DC as DecodeCertificateUseCase
        participant VC as VerifyCertificateUseCase
    end

    B->>ALB: mTLS handshake (client certificate)
    ALB->>SEC: POST /api/core/certificate-verifications<br>+ X-Amzn-Mtls-Clientcert-* headers + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: POST /certificate-verifications (cert headers, X-Auth-Sub=sub)
    UC->>HC: execute(sub, subject, issuer, validity, leaf)
    alt no X-Auth-Sub
        HC-->>B: 401 Unauthorized
    else authenticated
        HC->>DC: parse(subject, issuer, validity, leaf)
        alt missing issuer or subject
            DC-->>B: 400 (IncompleteCertificate)
        else expired certificate
            DC-->>B: 410 Gone (CertificateExpired)
        else valid
            DC-->>HC: raw DNI
            HC->>VC: bindAndIssueToken(sub, rawDni)
            VC-->>HC: signed verification token
            HC-->>B: 200 CertificateIdentityDto (dni hash + verificationToken)
        end
    end
```
