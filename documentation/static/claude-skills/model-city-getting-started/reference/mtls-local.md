# Reference — Local mTLS with Nginx and an FNMT certificate

Simulates an AWS ALB terminating mTLS and forwarding client-certificate metadata to
the Spring Boot back-end, so the developer can test against real FNMT certificates
without any cloud infrastructure. Two independent certificate profiles: a
self-signed **server** certificate (so the browser accepts `https://localhost:8443`)
and a **trust bundle** of FNMT authorities (so Nginx validates the citizen's personal
certificate). You (the agent) can script everything except trusting the local CA and
selecting the FNMT certificate in the browser.

## 1. Directory layout

```text
mtls-local-demo/
├── certs/
│   ├── local-ca.crt / local-ca.key
│   ├── localhost.crt / localhost.key / localhost.csr / localhost.ext
│   └── fnmt-client-trust-bundle.pem
├── nginx/nginx.conf
└── docker-compose.yml
```

```bash
mkdir -p mtls-local-demo/certs && cd mtls-local-demo
```

## 2. Local CA + server certificate for `localhost`

```bash
cd certs
openssl genrsa -out local-ca.key 4096
openssl req -x509 -new -nodes -key local-ca.key -sha256 -days 3650 \
  -out local-ca.crt -subj "/CN=Local Test CA"

openssl genrsa -out localhost.key 2048
openssl req -new -key localhost.key -out localhost.csr -subj "/CN=localhost"
```

Write `localhost.ext`:

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

```bash
openssl x509 -req -in localhost.csr -CA local-ca.crt -CAkey local-ca.key \
  -CAcreateserial -out localhost.crt -days 365 -sha256 -extfile localhost.ext
cd ..
```

## 3. Trust the local CA (manual — ask the user to do this and confirm)

- macOS: import `certs/local-ca.crt` into *Keychain Access*, mark *Always Trust*.
- Windows: `certmgr.msc` → import into *Trusted Root Certification Authorities*.
- Firefox (own cert store): *Settings → Privacy & Security → Certificates → View
  Certificates → Authorities → Import*.

This only affects the local server certificate, unrelated to the FNMT chain below.

## 4. FNMT trust bundle

Root/intermediate FNMT certs can be found under `infrastructure/resources` in the
platform's GitHub repo (`ac_raiz_fnmt_g2.pem`, `ac_usuarios_g2.pem`,
`fnmt-client-trust-bundle.pem`). The bundle must contain **only** authority public
certificates — never the citizen's personal certificate or private key.

Typical chain: `AC Raíz FNMT-RCM G2` → `AC Usuarios G2` → citizen's personal
certificate (verify the real chain per certificate type; other subordinates like *AC
Representación G2* or *AC Sector Público G2* are possible).

Convert DER → PEM if needed (`openssl x509 -in x.cer -text -noout` errors on DER):

```bash
openssl x509 -inform DER -in ac_usuarios_g2.cer -out ac_usuarios_g2.pem
openssl x509 -inform DER -in ac_raiz_fnmt_g2.cer -out ac_raiz_fnmt_g2.pem
```

Concatenate root then intermediate (Nginx doesn't actually require this order):

```bash
cat ac_raiz_fnmt_g2.pem ac_usuarios_g2.pem > certs/fnmt-client-trust-bundle.pem
```

## 5. Nginx config (`nginx/nginx.conf`)

```nginx
events {}

http {
    server {
        listen 8443 ssl;
        server_name localhost;

        ssl_certificate     /etc/nginx/certs/localhost.crt;
        ssl_certificate_key /etc/nginx/certs/localhost.key;

        ssl_client_certificate /etc/nginx/certs/fnmt-client-trust-bundle.pem;
        ssl_verify_client on;   # use `optional` while first validating the flow
        ssl_verify_depth 4;
        ssl_protocols TLSv1.2 TLSv1.3;

        location / {
            proxy_pass http://host.docker.internal:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-Proto https;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

            proxy_set_header X-Amzn-Mtls-Clientcert-Subject $ssl_client_s_dn;
            proxy_set_header X-Amzn-Mtls-Clientcert-Issuer $ssl_client_i_dn;
            proxy_set_header X-Amzn-Mtls-Clientcert-Serial-Number $ssl_client_serial;
            proxy_set_header X-Amzn-Mtls-Clientcert-Verify $ssl_client_verify;
            proxy_set_header X-Amzn-Mtls-Clientcert-Leaf $ssl_client_escaped_cert;
        }
    }
}
```

These `X-Amzn-Mtls-Clientcert-*` headers reproduce what an AWS ALB forwards, so the
same Spring Boot authorization logic works locally and in the cloud.

## 6. `docker-compose.yml`

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

```bash
docker compose up
```

## 7. Verify

```bash
curl -k https://localhost:8443   # errors without a client cert — expected, confirms mTLS is enforced
```

The definitive test needs a real browser with the FNMT certificate installed, against
the Spring Boot back-end running at `localhost:8080`:

```text
https://localhost:8443/api/core/certificate-verifications
```

That endpoint is a `POST`; a plain address-bar GET correctly returns
`405 Method Not Allowed` — this step only validates the TLS handshake and certificate
selector, which happen before any HTTP method reaches the back-end. Full exercise:

```bash
curl -k --cert-type P12 --cert your-cert.p12:pass -X POST \
  -H "Authorization: Bearer <JWT>" \
  https://localhost:8443/api/core/certificate-verifications
```

If Nginx rejects the certificate, check: missing intermediate authority in the
bundle, certificate not installed in the browser used (Firefox needs an explicit
import), `ssl_verify_depth` too low for the chain, or the certificate is expired /
lacks `clientAuth` extended usage.
