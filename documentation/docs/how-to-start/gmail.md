---
title: Email (Gmail SMTP)
sidebar_label: Gmail (SMTP)
sidebar_position: 6
---

# Email configuration for the *core*'s email delivery

:::info
This guide has been created using Gmail as the SMTP provider. Other providers
are possible but not covered here.
:::

The *core* vertical (and the monolith) send transactional emails — OTP and
invitations — over **SMTP** via Spring Mail. The configuration is
(`application.yml`):

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

Gmail does **not** accept the account's regular password for SMTP: it requires an
**App Password**, a 16-character token used as `MAIL_PASSWORD`.

## Environment variables obtained here

| Variable | Value | Notes |
| --- | --- | --- |
| `MAIL_HOST` | `smtp.gmail.com` | Default value, no need to set it |
| `MAIL_PORT` | `587` | STARTTLS |
| `MAIL_USERNAME` | your `@gmail.com` address | The sender account |
| `MAIL_PASSWORD` | app password (16 characters) | **Secret** |

Optionally, the project lets you customise the email footer with `MAIL_CITY_NAME`
and `MAIL_ADDRESS`.

---

## 1. Prerequisite: two-step verification

1. Go to your Google account → `https://myaccount.google.com/security`.
2. Under **How you sign in to Google**, enable **2-Step Verification** (2FA).
   Without it, Google does **not** show the app passwords option.
3. Complete the process (phone or authenticator app).

## 2. Create the app password

1. Go directly to `https://myaccount.google.com/apppasswords` (or search "App
   passwords" in the security settings).
2. If asked to re-authenticate, enter your password.
3. Under **App name** type a recognizable identifier, e.g. `Model City Core`.
4. Click **Create**.
5. Google shows a **16-character** password grouped in 4 blocks, for example:
   ```
   xxxx xxxx xxxx xxxx
   ```
6. Copy it. This is the value of `MAIL_PASSWORD`.
   - You can keep or remove the spaces; Gmail accepts both formats. If you use it
     in a `.env`, wrap it in quotes to avoid problems with the spaces.
7. The password is **shown only once**. If you lose it, delete that entry and
   create another.

## 3. Assign the variables

1. `MAIL_USERNAME` = the full Gmail account address (e.g.
   `my.account@gmail.com`).
2. `MAIL_PASSWORD` = the app password from step 2.
