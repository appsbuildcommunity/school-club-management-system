# Social sign-in setup

The back end uses Spring Security's OAuth2 client support. Google uses OpenID Connect;
Facebook uses OAuth2 plus the Graph API. After a provider login, the back end creates or loads
a local `users` row and issues its own JWT access token plus a rotating refresh token. Provider
credentials are never stored in the repo.

## 1. Configure the environment

Copy `back_end/env.example` to `back_end/.env` and fill in the values you need. At minimum:

```properties
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
JWT_SECRET=<at least 32 random characters>
```

A provider is only registered when both its client id and client secret are present. You can
start with Google only and add Facebook later.

Generate a JWT secret with:

```bash
openssl rand -base64 48
```

## 2. Google Cloud Console

1. Create (or reuse) a project in <https://console.cloud.google.com/>.
2. Go to **APIs & Services → OAuth consent screen** and configure an external consent screen.
   Add test users while the app is in testing mode.
3. Go to **APIs & Services → Credentials → Create credentials → OAuth client ID**.
4. Choose **Web application**.
5. Add an authorized redirect URI:

   ```text
   http://localhost:8080/login/oauth2/code/google
   ```

6. Copy the client ID and client secret into `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`.

Google login requests `openid`, `profile`, and `email`. The email-verified flag from the ID
token is stored on the OAuth account.

## 3. Facebook Developers

1. Create an app at <https://developers.facebook.com/apps/>.
2. Add the **Facebook Login** product and choose **Web**.
3. Set the valid OAuth redirect URI:

   ```text
   http://localhost:8080/login/oauth2/code/facebook
   ```

4. In **App settings → Basic**, copy the App ID and App Secret into
   `FACEBOOK_CLIENT_ID` and `FACEBOOK_CLIENT_SECRET`.
5. Keep the app in development mode and add your Facebook account as a tester.
6. The login requests `email` and `public_profile`. Facebook email addresses are treated as
   unverified.

The Graph API version defaults to `v23.0`; override `FACEBOOK_GRAPH_VERSION` if Meta retires it.

## 4. Run and test

Start the back end:

```bash
cd back_end
./mvnw spring-boot:run
```

Start the frontend, then open its sign-in route. The frontend owns the sign-in UI and the
post-login callback; the back end serves no `/auth/**` pages.

Set `APP_AUTH_REDIRECT_URI` to the frontend callback route. The default matches a Vite dev
server:

```properties
APP_AUTH_REDIRECT_URI=http://localhost:5173/auth/callback
```

### Frontend integration

1. Call `GET http://localhost:8080/api/auth/providers` and render a button for each `true`
   provider. The endpoint is public and works before the user signs in.
2. A button navigates the browser to
   `http://localhost:8080/oauth2/authorization/{google|facebook}`.
3. After consent, the back end redirects to `APP_AUTH_REDIRECT_URI` with the access and refresh
   tokens in the fragment, plus `onboarding_required`. On failure the fragment contains `error`
   and `error_description` instead.
4. The callback route reads the tokens and onboarding flag, keeps them in memory (or another
   client-side store you trust), and removes them from the address bar:

   ```js
   const params = new URLSearchParams(window.location.hash.slice(1));
   const accessToken = params.get("access_token");
   const refreshToken = params.get("refresh_token");
   const onboardingRequired = params.get("onboarding_required") === "true";
   if (accessToken) sessionStorage.setItem("accessToken", accessToken);
   if (refreshToken) sessionStorage.setItem("refreshToken", refreshToken);
   history.replaceState(null, "", window.location.pathname);
   ```

5. If `onboardingRequired` is `true`, call `GET /api/users/me`, render the form with the returned
   `username`, `firstName`, and `lastName`, then save it with `PATCH /api/users/me`.

Use the access token with:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/me
```

The access token's `sub` is the local `user_id` and its `roles` claim is the local role.

### Onboarding

New social users are created with their username and names prefilled from the provider, but
`onboardingCompleted=false`. The frontend should show the editable form once and save it:

```bash
curl -X PATCH http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","firstName":"Alice","lastName":"Doe"}'
```

A successful save marks `onboardingCompleted=true`, so subsequent sign-ins return
`onboarding_required=false`. If the username is already taken the endpoint returns `409`; the
frontend should show the conflict and let the user pick another one.

### Refresh and logout

Access tokens default to one hour (`JWT_ACCESS_TTL=PT1H`). Refresh tokens default to 30 days
(`JWT_REFRESH_TTL=P30D`) and are rotated on every use:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

Replace the stored refresh token with the one in the response. Reusing an old, already-rotated
token revokes every active refresh token for that user and returns `401`.

On sign-out, revoke the refresh token and discard both tokens client-side:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

The endpoint returns `204 No Content`. An access token that was already issued stays valid until
it expires; keep `JWT_ACCESS_TTL` short if immediate revocation matters.

## Redirect URLs behind a proxy

`OAUTH2_REDIRECT_BASE_URL` builds the provider callback URL, and `APP_AUTH_REDIRECT_URI` is where
the browser is sent after sign-in. For a frontend on another origin, point the latter at the
frontend callback and make sure the provider console uses the backend callback:

```properties
OAUTH2_REDIRECT_BASE_URL=https://api.example.com
APP_AUTH_REDIRECT_URI=https://app.example.com/auth/callback
```

## Account linking

An existing local email is not auto-linked to a new provider. If a user signs in with Google and
then tries Facebook with the same email, the sign-in is rejected and the frontend receives
`error=account_error` in the fragment with a message asking them to use the original provider.
This prevents an unverified Facebook email from taking over a Google-created account. Explicit
link/unlink endpoints are a future authenticated feature.

## Bootstrap administrators

Put comma-separated emails in `BOOTSTRAP_ADMIN_EMAILS`. An email receives `ADMIN` only when its
first social sign-in creates the local user:

```properties
BOOTSTRAP_ADMIN_EMAILS=principal@school.edu,it@school.edu
```

Already-provisioned users keep their current role; the bootstrap list only applies when a local
user is first created. There is currently no API endpoint for promoting an existing user to
`ADMIN`.
