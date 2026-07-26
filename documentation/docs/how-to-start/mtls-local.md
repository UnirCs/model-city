---
title: Local mTLS
sidebar_label: Local mTLS
sidebar_position: 7
---

# Local mTLS adoption with Nginx and an FNMT certificate

This document systematizes the procedure to deploy, in a local development
environment, a mutual TLS (mTLS) authentication architecture using Nginx. The goal
is to simulate the behaviour of an AWS Application Load Balancer (ALB) which, upon
terminating the TLS traffic and validating the client certificate, forwards
certain metadata to a Spring Boot back-end. This way, the developer can test the
integration with real certificates from the Spanish *Fábrica Nacional de Moneda y
Timbre* (FNMT) without deploying any cloud infrastructure.

Throughout the guide two certificate profiles of different natures are generated:
a server certificate self-signed by a local certification authority, needed for
the browser to accept `https://localhost:8443`, and a trust *bundle* with the FNMT
authorities, which Nginx uses to validate the client's personal certificate. The
separation between the two is conceptually relevant: the server certificate
resolves the local domain, while the trust bundle resolves the citizen's identity.

## 1. Folder structure of the demo project

It is recommended to create a separate directory grouping the certificates, the
Nginx configuration and the container orchestrator. The final layout should be:

```text
mtls-local-demo/
├── certs/
│   ├── local-ca.crt
│   ├── local-ca.key
│   ├── localhost.crt
│   ├── localhost.key
│   ├── localhost.csr
│   ├── localhost.ext
│   └── fnmt-client-trust-bundle.pem
├── nginx/
│   └── nginx.conf
└── docker-compose.yml
```

Create the base directory with the usual OS commands:

```bash
mkdir mtls-local-demo
cd mtls-local-demo
```

## 2. Generate the local HTTPS certificate for Nginx

Although the client certificate will be the FNMT one, Nginx requires a valid
server certificate to expose the `https://localhost:8443` endpoint. This
certificate does not belong to the FNMT; it only ensures the browser can establish
an HTTPS connection to the local host.

### 2.1. Create a local certification authority

First generate the certs directory and then the local CA key pair:

```bash
mkdir -p certs
cd certs

openssl genrsa -out local-ca.key 4096

openssl req -x509 \
  -new \
  -nodes \
  -key local-ca.key \
  -sha256 \
  -days 3650 \
  -out local-ca.crt \
  -subj "/CN=Local Test CA"
```

The result is a test root certificate, valid for ten years, to be imported later
into the OS or browser trust store.

### 2.2. Generate the server certificate for `localhost`

Next, create the server key pair and the corresponding signing request:

```bash
openssl genrsa -out localhost.key 2048

openssl req -new \
  -key localhost.key \
  -out localhost.csr \
  -subj "/CN=localhost"
```

For modern browsers to accept the certificate, you must include subject
alternative names (SAN) covering both `localhost` and the IP `127.0.0.1`. Define
the file `localhost.ext` with the following content:

```text
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=@alt_names

[alt_names]
DNS.1=localhost
IP.1=127.0.0.1
```

Sign the certificate with the local CA created above:

```bash
openssl x509 -req \
  -in localhost.csr \
  -CA local-ca.crt \
  -CAkey local-ca.key \
  -CAcreateserial \
  -out localhost.crt \
  -days 365 \
  -sha256 \
  -extfile localhost.ext
```

Then return to the project root:

```bash
cd ..
```

## 3. Trusting the local CA

To avoid the browser showing security warnings when accessing
`https://localhost:8443`, the `certs/local-ca.crt` certificate must be added to
the system trust store.

- **On macOS**: import `local-ca.crt` into *Keychain Access* and mark it as
  *Always Trust*.
- **On Windows**: open `certmgr.msc` and import the certificate into *Trusted Root
  Certification Authorities*.
- **On Firefox**: if the browser uses its own certificate store, import it from
  *Settings → Privacy & Security → Certificates → View Certificates → Authorities →
  Import*.

This step concerns only the local server certificate and is unrelated to the FNMT
trust chain, configured in the next section.

## 4. Preparing the FNMT trust bundle

:::tip 
In order to help you to get AC's certificates to test, you can find under infrastructure/resources path in GitHub the following files:
- `ac_raiz_fnmt_g2.pem`:  The root authority.
- `ac_usuarios_g2.pem`: The intermediate authority .
- `fnmt-client-trust-bundle.pem`: The final bundle you need to create the trust store.
:::

Nginx must know which issuing certification authorities are valid for client
certificates. The output of this section is the file
`certs/fnmt-client-trust-bundle.pem`, which contains exclusively public
certificates of the FNMT chain. It must **not** include the citizen's personal
certificate nor their private key.

### 4.1. Identifying the trust chain

For a typical citizen personal certificate, the chain follows this hierarchy:

```text
AC Raíz FNMT-RCM G2
  └── AC Usuarios G2
        └── Citizen's FNMT personal certificate
```

Nevertheless, verify the real chain of the personal certificate, since other
subordinate entities such as *AC Representación G2* or *AC Sector Público G2* may
be involved depending on the certificate type.

### 4.2. Converting `.cer` formats to PEM

Certificates downloaded from the FNMT may be distributed in DER or PEM format. To
check the format use:

```bash
openssl x509 -in ac_usuarios_g2.cer -text -noout
```

If reading it errors, the file is DER-encoded and must be converted:

```bash
openssl x509 \
  -inform DER \
  -in ac_usuarios_g2.cer \
  -out ac_usuarios_g2.pem
```

Apply the same procedure to the root when needed:

```bash
openssl x509 \
  -inform DER \
  -in ac_raiz_fnmt_g2.cer \
  -out ac_raiz_fnmt_g2.pem
```

### 4.3. Building the final bundle

Once the certificates are in PEM format, concatenate them into a single file. The
recommended order places the root first and the intermediate authority second,
although Nginx is flexible about the order:

```bash
cat ac_raiz_fnmt_g2.pem ac_usuarios_g2.pem > certs/fnmt-client-trust-bundle.pem
```

The final file should look like:

```text
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
```

Including only authority certificates ensures Nginx can validate the client
certificate's signature without exposing the citizen's sensitive information.

## 5. Configuring Nginx with mTLS

Create the config directory and, inside it, the `nginx/nginx.conf` file with the
following content:

```nginx
events {}

http {
    server {
        listen 8443 ssl;
        server_name localhost;

        # Local HTTPS server certificate for Nginx
        ssl_certificate     /etc/nginx/certs/localhost.crt;
        ssl_certificate_key /etc/nginx/certs/localhost.key;

        # Accepted CAs for client certificates.
        # This is the FNMT chain.
        ssl_client_certificate /etc/nginx/certs/fnmt-client-trust-bundle.pem;

        # For initial testing you may use optional.
        # Once the flow is validated, switch it to on.
        ssl_verify_client on;

        ssl_verify_depth 4;

        # Reasonable TLS for local development
        ssl_protocols TLSv1.2 TLSv1.3;

        location / {
            proxy_pass http://host.docker.internal:8080;

            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-Proto https;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

            # Headers simulating those later received from the AWS ALB.
            # Nginx exposes client certificate variables such as:
            # $ssl_client_s_dn, $ssl_client_i_dn, $ssl_client_serial,
            # $ssl_client_verify and $ssl_client_escaped_cert.
            proxy_set_header X-Amzn-Mtls-Clientcert-Subject $ssl_client_s_dn;
            proxy_set_header X-Amzn-Mtls-Clientcert-Issuer $ssl_client_i_dn;
            proxy_set_header X-Amzn-Mtls-Clientcert-Serial-Number $ssl_client_serial;
            proxy_set_header X-Amzn-Mtls-Clientcert-Verify $ssl_client_verify;
            proxy_set_header X-Amzn-Mtls-Clientcert-Leaf $ssl_client_escaped_cert;
        }
    }
}
```

The `ssl_verify_client on` directive forces the client to present a certificate
that chains to one of the authorities in `fnmt-client-trust-bundle.pem`. The
`X-Amzn-Mtls-Clientcert-*` headers reproduce the AWS ALB's behaviour of forwarding
the validated certificate's attributes to the back-end, letting the Spring Boot
back-end implement the same authorization logic both locally and in the cloud.

## 6. Orchestrating the container with Docker Compose

In the project root create the `docker-compose.yml` file:

```yaml
services:
  nginx-mtls:
    image: nginx:latest
    container_name: nginx-mtls-local
    ports:
      - "8443:8443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./certs:/etc/nginx/certs:ro
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

The service mounts the certificates and configuration read-only, and exposes the
container's port `8443` on the same host port. The `extra_hosts` directive ensures
`host.docker.internal` resolves to the host gateway, allowing Nginx to forward
requests to the Spring Boot back-end running at `localhost:8080`.

Bring up the container with:

```bash
docker compose up
```

During the first tests it is convenient to keep the terminal open to observe
Nginx's output and detect possible certificate validation errors.

## 7. Verifying Nginx startup

From another terminal, check that Nginx responds on the HTTPS port:

```bash
curl -k https://localhost:8443
```

Since `ssl_verify_client` is enabled, `curl` without a client certificate will get
an error. This is the expected behaviour and confirms Nginx is requesting mutual
authentication. The definitive test must be run from a browser with the FNMT
certificate installed.

## 8. End-to-end test from the browser

With the Spring Boot back-end up at `localhost:8080` and the FNMT certificate
installed in the system or browser, access:

```text
https://localhost:8443/api/core/certificate-verifications
```

The certificate-verification endpoint is a `POST` (see
[Verify mTLS certificate](../back-end/modules/core/verify-certificate.md)), so a plain browser
address-bar visit will end in a `405 Method Not Allowed` from the back-end — but that is fine here:
what this step validates is the **TLS handshake and certificate selector**, which happen before any
HTTP method reaches the back-end. The expected flow is:

1. The browser establishes the HTTPS connection to `localhost:8443`.
2. Nginx requests the client certificate.
3. The browser shows the certificate selector.
4. The user selects their personal FNMT certificate.
5. Nginx validates the certificate chain against `fnmt-client-trust-bundle.pem`.
6. Nginx forwards the request to the Spring Boot back-end at `localhost:8080`,
   including the configured headers.

To exercise the full endpoint (issuing a verification token) send an authenticated `POST` with the
certificate, e.g. `curl -k --cert-type P12 --cert your-cert.p12:pass -X POST -H "Authorization:
Bearer <JWT>" https://localhost:8443/api/core/certificate-verifications`.

If Nginx rejects the certificate, the most common causes are:

- The correct intermediate authority is missing from
  `fnmt-client-trust-bundle.pem`.
- The FNMT certificate is not available to the browser used.
- Firefox uses its own certificate store and requires an explicit import.
- The `ssl_verify_depth` value is insufficient for the full chain.
- The certificate is expired or lacks the `clientAuth` extended usage.
