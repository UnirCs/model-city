---
title: Invite agent (staff)
sidebar_label: Invite agent
sidebar_position: 9
---

# Invite agent (staff)

`POST /api/core/agents` → `InviteAgentUseCase`

Invites a new city-hall staff member (`BACKOFFICE`, `OPERATOR` or `MOBILITY_AGENT`). **Admin
only** (`PLATFORM_ADMIN`). The use case maps the received `role` to its Auth0 `roleId` (if the
role is not one of the three valid ones → `400`), creates the user in **Auth0**, assigns the
role, generates a **password-change ticket** and emails the invitation with that link. Returns
`202 Accepted` (accepted; the account materializes when the invitee sets their password).

The record in `core`'s database is created later, on the agent's own sign-in (JIT provisioning),
which detects the role already assigned in Auth0. Records the `AGENT_INVITED` audit event.

## Inputs

**`POST /api/core/agents`**

```json
{
  "name": "Marta Ruiz",
  "email": "marta.ruiz@ayto.example.com",
  "role": "MODEL-CITY-BACKOFFICE"
}
```

> `role` must be one of `MODEL-CITY-BACKOFFICE`, `MODEL-CITY-OPERATOR` or
> `MODEL-CITY-MOBILITY-AGENT`.

## Outputs

- **`202 Accepted`** — invitation accepted and email sent (no body).
- **`400 Bad Request`** — invalid `role`.
- **`403 Forbidden`** — the requester is not an admin.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant AC as AgentController
        participant IA as InviteAgentUseCase
        participant A0F as Auth0ManagementFacade
        participant MS as InvitationMailService
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant A0 as Auth0 Mgmt API
        participant SMTP as SMTP
    end

    C->>GW: POST /api/core/agents {email,name,role} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>AC: POST /agents (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>C: 403 Forbidden
    else admin
        AC->>IA: execute(sub, request)
        Note over IA: resolves UserRole → Auth0 roleId (else 400)
        IA->>A0F: createUser(email, name)
        A0F->>A0: POST /users
        A0-->>A0F: auth0UserId
        IA->>A0F: assignRoles(auth0UserId, [roleId])
        IA->>A0F: createPasswordChangeTicket(auth0UserId)
        A0F->>A0: POST /tickets/password-change
        A0-->>A0F: ticketUrl
        IA->>MS: sendInvitation(email, roleLabel, ticketUrl)
        MS->>SMTP: sends HTML invitation
        IA->>TG: agentInvited (audit → core_trails)
        IA-->>C: 202 Accepted
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant AC as AgentController
        participant IA as InviteAgentUseCase
        participant A0F as Auth0ManagementFacade
        participant MS as InvitationMailService
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant A0 as Auth0 Mgmt API
        participant SMTP as SMTP
    end

    C->>SEC: POST /api/core/agents {email,name,role} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>AC: POST /agents (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>C: 403 Forbidden
    else admin
        AC->>IA: execute(sub, request)
        Note over IA: resolves UserRole → Auth0 roleId (else 400)
        IA->>A0F: createUser(email, name)
        A0F->>A0: POST /users
        A0-->>A0F: auth0UserId
        IA->>A0F: assignRoles(auth0UserId, [roleId])
        IA->>A0F: createPasswordChangeTicket(auth0UserId)
        A0F->>A0: POST /tickets/password-change
        A0-->>A0F: ticketUrl
        IA->>MS: sendInvitation(email, roleLabel, ticketUrl)
        MS->>SMTP: sends HTML invitation
        IA->>TG: agentInvited (audit → core_trails)
        IA-->>C: 202 Accepted
    end
```
