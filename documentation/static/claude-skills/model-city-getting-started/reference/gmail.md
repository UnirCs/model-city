# Reference — Gmail (SMTP)

The *core* vertical (and the monolith) send transactional emails (OTP, invitations)
over SMTP via Spring Mail. Gmail requires an **App Password** (16 characters), not
the account's regular password.

| Variable | Value | Notes |
| --- | --- | --- |
| `MAIL_HOST` | `smtp.gmail.com` | default, no need to set |
| `MAIL_PORT` | `587` | STARTTLS |
| `MAIL_USERNAME` | sender's `@gmail.com` address | |
| `MAIL_PASSWORD` | app password (16 chars) | **secret** |

Optional branding: `MAIL_CITY_NAME`, `MAIL_ADDRESS` (email footer).

## Walk-through

1. **Enable 2-Step Verification** on the sending Google account:
   `https://myaccount.google.com/security` → *How you sign in to Google* → *2-Step
   Verification*. Without it Google hides the app-passwords option.
2. **Create the app password**: `https://myaccount.google.com/apppasswords` → re-auth
   if asked → App name e.g. `Model City Core` → *Create*. Google shows a 16-character
   password once, in 4 blocks — capture it immediately (`MAIL_PASSWORD`); if lost,
   delete the entry and create another. Spaces can be kept or removed; if put in a
   `.env`, quote the value.
3. `MAIL_USERNAME` = the full Gmail address used above.

## Summary — read this back to the user at the end

`MAIL_USERNAME`, `MAIL_PASSWORD`, and optionally `MAIL_CITY_NAME` / `MAIL_ADDRESS`.
