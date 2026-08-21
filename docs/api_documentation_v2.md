# Student Club Management System - API Documentation

## Type Definitions

### Enums

```typescript
enum UserRole {
  GUEST = 'GUEST',
  USER = 'USER',
  STUDENT = 'STUDENT',
  ADMIN = 'ADMIN',
}

enum ClubRole {
  MEMBER = 'MEMBER',
  ASSISTANT_MEMBER = 'ASSISTANT_MEMBER',
  CLUB_PRESIDENT = 'CLUB_PRESIDENT',
}

enum Visibility {
  PUBLIC = 'PUBLIC',
  STUDENTS_ONLY = 'STUDENTS_ONLY',
  MEMBERS_ONLY = 'MEMBERS_ONLY',
  HIDDEN = 'HIDDEN',
}

enum AttachmentType {
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  DOCUMENT = 'DOCUMENT',
}

enum DemandStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
}

enum Category {
  MANAGE_POSTS = 'MANAGE_POSTS',
  MANAGE_EVENTS = 'MANAGE_EVENTS',
  MANAGE_MEMBERS = 'MANAGE_MEMBERS',
  MANAGE_CLUBS = 'MANAGE_CLUBS',
}
```

### Common Structures

```typescript
// User Reference
interface UserReference {
  id: string
  username: string
  firstName: string
  lastName: string
  email: string
  profilePicture: {
    url: string
  }
}

// Club Reference
interface ClubReference {
  id: string
  name: string
  description: string
  profilePicture: {
    url: string
  }
}

// Location
interface Location {
  id: number
  name: string
  internalLocation: boolean
}

// Attachment
interface Attachment {
  attachmentId: string
  type: AttachmentType
  url: string
}

// Comment
interface Comment {
  commentId: string
  content: string
  user: UserReference
  createdAt: string // ISO 8601 date
}

// Pagination
interface Pagination {
  currentPage: number
  totalPages: number
  totalItems: number
}

// Member
interface Member {
  username: string
  firstName: string
  lastName: string
  profilePicture?: {
    url: string
  }
  role: ClubRole
  roleDescription?: string
}

// Endpoint (grantable API capability)
interface EndpointResponse {
  name: string          // unique identifier, e.g. "manage_profiles"
  description: string   // human-readable description
  category: Category    // MANAGE_POSTS | MANAGE_EVENTS | MANAGE_MEMBERS | MANAGE_CLUBS
  privileged: boolean   // true only for MANAGE_CLUBS (admin-only endpoints)
}

// Club Profile (role template grouping multiple endpoints)
interface ClubProfileRequest {
  name: string            // profile name
  endpoints: string[]     // list of endpoint names included in this profile
}

interface ClubProfileResponse {
  profileId: number
  name: string
  endpoints: string[]     // list of endpoint names included in this profile
}

// Privilege Assignment
interface AssignPrivilegesRequest {
  profileId?: number | null   // if set, grant all endpoints from this profile
  endpoints?: string[] | null // if profileId is null, grant these individual endpoints
}

// Member Privileges View
interface MemberPrivilegesResponse {
  membershipId: number
  privileges: MemberPrivilege[]
}

interface MemberPrivilege {
  endpointName: string      // e.g. "manage_profiles"
  grantedDate: string       // ISO 8601 date
  sources: SourceProfile[]  // which profiles this privilege came from (empty = individual grant)
}

interface SourceProfile {
  profileId: number
  name: string
}
```

---

## Authentication

Authentication is delegated to Keycloak (OpenID Connect, Authorization Code + PKCE for the Flutter app). The back end is a pure **resource server**: every protected endpoint requires a valid JWT access token from Keycloak via the `Authorization: Bearer <token>` header.

- The token issuer is the realm `school_clubs_management_system` at `http://localhost:8082/realms/school_clubs_management_system` (`KEYCLOAK_ISSUER_URI`).
- Authorities are derived from the token's `realm_access.roles` claim (e.g. `USER`, `STUDENT`, `ADMIN`), mapped to Spring Security `ROLE_*` authorities. The DB `users` table is **not** the identity source.
- A newly registered user gets the `USER` realm role (default). `STUDENT` and `ADMIN` are additional realm roles.
- When a user registers through Keycloak, Keycloak fires a `REGISTER` event to the back end webhook (see **Keycloak Webhook** section below). This endpoint is idempotent.
- `POST /verify` remains public for the S3 upload-verification test flow.
- Since the resource server is only active for the **non-`Test`** Spring profile, the endpoints described in the earlier `v1` document (`/api/auth/register`, `/api/auth/login`, ...) no longer exist in the current build.

### Current User

**GET** `/api/users/me`

**Authorization:** Bearer token

Returns the local profile of the authenticated user (looked up by the token's `sub` claim), plus the roles present in the token's `realm_access.roles` claim.

**Response:** `200 OK`

```json
{
  "userId": 1,
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "profilePictureUrl": "string | null",
  "roles": ["USER"]
}
```

---

### Update User Role

**PATCH** `/api/users/{userId}/role`

**Authorization:** `ADMIN` only (`hasRole('ADMIN')`)

Switches a user's realm role between `USER` and `STUDENT` in Keycloak (idempotent: no-op if the user already has the target role).

**Request Body:**

```json
{
  "role": "STUDENT"
}
```

`role` must be `USER` or `STUDENT` (case-insensitive). `ADMIN` assignment stays in the Keycloak admin console.

**Behavior:**

- The change is made through the Keycloak Admin REST API using the `school_clubs_management_backend` service-account client.
- No tokens are revoked and the user is **not** logged out. The realm uses a 180-second access-token lifetime, so the new role takes effect at the user's next token refresh (automatic, via the still-valid refresh token) — within ~3 minutes of the change.

**Responses:**

- `200 OK` — role updated
- `400 Bad Request` — `role` is not `USER`/`STUDENT`
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — caller is not an `ADMIN`
- `404 Not Found` — no user with that `userId`

---

### Profile Picture Upload URL

**GET** `/api/users/me/profile-picture/upload-url?name=<filename>`

**Authorization:** Bearer token

Returns a presigned S3 `PUT` URL (and the object `key`) so the app can upload the authenticated user's profile picture directly.

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "key": "string"
}
```

---

## Keycloak Webhook

### User Registered Webhook

**POST** `/api/webhooks/keycloak/user-registered`

**Authorization:** HTTP Basic Auth (`KEYCLOAK_WEBHOOK_USERNAME` / `KEYCLOAK_WEBHOOK_PASSWORD`)

Called by Keycloak when a new user completes registration. Creates or syncs the local `users` row (Keycloak `sub` stored as `users.keycloak_sub`, unique). Idempotent — no-op if the user already exists.

**Request Body:**

```json
{
  "type": "REGISTER",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "details": {
    "username": "johndoe",
    "email": "john@example.com",
    "first_name": "John",
    "last_name": "Doe"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Event type (`REGISTER`) |
| `userId` | string | Keycloak user `sub` claim |
| `details.username` | string | Keycloak username |
| `details.email` | string | User email |
| `details.first_name` | string | First name (note: underscore-separated in webhook payload) |
| `details.last_name` | string | Last name (note: underscore-separated in webhook payload) |

**Response:** `204 No Content`

**Response Codes:**

- `204 No Content` — user created or already exists (idempotent)
- `401 Unauthorized` — invalid Basic Auth credentials
- `400 Bad Request` — malformed event payload

---

## Club Endpoints

### List All Clubs

**GET** `/api/clubs`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

**Response:** `200 OK`

```json
{
  "clubs": [
    {
      "clubName": "string",
      "description": "string",
      "profilePicture": {
        "url": "string"
      },
      "memberCount": 25
    }
  ]
}
```

---

### Get Club Details

**GET** `/api/clubs/{clubName}`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

**Response:** `200 OK`

```json
{
  "clubName": "string",
  "description": "string",
  "profilePicture": {
    "url": "string"
  },
  "staff": [Member], // See Type Definitions
  "memberCount": 25
}
```

---

### Create Club

**POST** `/api/clubs`

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

**Request Body:**

```json
{
  "clubName": "string",
  "clubFullName": "string",
  "description": "string",
  "presidentUsername": "string" // optional
}
```

**Response:** `201 Created`

---

### Change Club President

**PUT** `/api/clubs/{clubId}/president`

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

**Request Body:**

```json
{
  "presidentUsername": "string"
}
```

**Response:** `200 OK`

---

### Get Club Members

**GET** `/api/clubs/{clubId}/members`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER, MEMBER
**Query Parameters:**

- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100

**Response:** `200 OK`

```json
{
  "members": [
    {
      "username": "string",
      "firstName": "string",
      "lastName": "string",
      "profilePicture": {
        "url": "string"
      },
      "email": "string",
      "membership": {
        "id": "number",
        "clubRole": ClubRole,
        "description": "string",
        "joinedDate": "string" // ISO 8601 date
      }
    }
  ],
  "pagination": Pagination
}
```

---

### Get Join Requests

**GET** `/api/clubs/{clubId}/join-requests`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

**Query Parameters:**

- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100
- `status` (DemandStatus, optional): Filter by status

**Response:** `200 OK`

```json
{
  "joinRequests": [
    {
      "id": "string",
      "user": UserReference,
      "joinRequestStatus": DemandStatus,
      "joinRequestDate": "string" // ISO 8601 date
    }
  ],
  "pagination": Pagination
}
```

---

### Update Join Request Status

**PUT** `/api/clubs/{clubId}/join-requests/{requestId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

**Query Parameters:**

- `status` (DemandStatus, optional): New status

**Response:** `200 OK`

---

### Remove Club Member

**DELETE** `/api/clubs/{clubId}/members/{membershipId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

**Response:** `204 No Content`

---

### Update Member Role

**PUT** `/api/clubs/{clubId}/members/{membershipId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

**Query Parameters:**

- `role` (ClubRole, optional): New role

**Response:** `200 OK`

---

### Assign Assistant Member Privileges

**PUT** `/api/clubs/{clubId}/members/{membershipId}/assistant`

**Authorization:** CLUB_PRESIDENT

**Request Body:**

```json
{
  "privileges": [Privilege]
}
```

**Response:** `200 OK`

---

### Get Membership History

**GET** `/api/clubs/{clubId}/history`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

**Query Parameters:**

- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100
- `user_id` (string, optional): Filter by user

**Response:** `200 OK`

```json
{
  "history": [
    {
      "historyId": "string",
      "userId": "string",
      "username": "string",
      "clubRole": ClubRole,
      "roleDescription": "string",
      "joinedDate": "string", // ISO 8601 date
      "leftDate": "string" // ISO 8601 date
    }
  ],
  "pagination": Pagination
}
```

---

### Request to Join Club

**POST** `/api/clubs/{clubId}/join-request`

**Authorization:** STUDENT

**Response:** `201 Created`

---

### Get My Club Memberships

**GET** `/api/students/me/clubs`

**Authorization:** STUDENT, CLUB_PRESIDENT

**Response:** `200 OK`

```json
{
  "memberships": [
    {
      "clubName": "string",
      "clubFullName": "string",
      "description": "string",
      "profilePicture": {
        "url": "string"
      },
      "clubRole": ClubRole,
      "joinedDate": "string" // ISO 8601 date
    }
  ]
}
```

---

## Post Endpoints

### List All Posts

**GET** `/api/posts`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Query Parameters:**

- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100
- `club_name` (string, optional): Filter by club

**Response:** `200 OK`

```json
{
  "posts": [
    {
      "postId": "string",
      "title": "string",
      "content": "string",
      "club": ClubReference,
      "createdAt": "string", // ISO 8601 date
      "visibility": Visibility,
      "attachments": [Attachment]
    }
  ],
  "pagination": Pagination
}
```

---

### Get Post Details

**GET** `/api/posts/{postId}`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Response:** `200 OK`

```json
{
  "postId": "string",
  "title": "string",
  "content": "string",
  "club": ClubReference,
  "createdAt": "string", // ISO 8601 date
  "visibility": Visibility,
  "attachments": [Attachment],
  "comments": [Comment]
}
```

---

### Get Post Comments

**GET** `/api/posts/{postId}/comments`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Response:** `200 OK`

```json
{
  "comments": [Comment]
}
```

---

### Create Post

**POST** `/api/clubs/{clubId}/posts`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS privilege)

**Request Body:**

```json
{
  "title": "string",
  "content": "string",
  "visibility": Visibility,
  "attachments": [
    {
      "file": "binary" // File upload format depends on implementation
    }
  ]
}
```

**Response:** `201 Created`

```json
{
  "id": "string",
  "title": "string",
  "content": "string",
  "visibility": Visibility,
  "attachments": [Attachment],
  "createdAt": "string" // ISO 8601 date
}
```

---

### Update Post

**PUT** `/api/clubs/{clubId}/posts/{postId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS privilege)

**Request Body:** Same as Create Post

**Response:** `200 OK`

---

### Delete Post

**DELETE** `/api/clubs/{clubId}/posts/{postId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS privilege)

**Response:** `204 No Content`

---

### Hide Post

**PUT** `/api/posts/{postId}/hide`

**Authorization:** CLUBS_RESPONSIBLE

**Request Body:**

```json
{
  "reason": "string"
}
```

**Response:** `200 OK`

---

### Add Comment to Post

**POST** `/api/posts/{postId}/comments`

**Authorization:** USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Request Body:**

```json
{
  "content": "string"
}
```

**Response:** `201 Created`

```json
{
  "commentId": "string",
  "content": "string",
  "userId": "string",
  "createdAt": "string" // ISO 8601 date
}
```

---

## Event Endpoints

### List All Events

**GET** `/api/events`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Query Parameters:**

- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100
- `club_name` (string, optional): Filter by club
- `start_date` (string, optional): Filter events from date (ISO 8601)
- `end_date` (string, optional): Filter events until date (ISO 8601)

**Response:** `200 OK`

```json
{
  "events": [
    {
      "eventId": "string",
      "title": "string",
      "description": "string",
      "club": ClubReference,
      "visibility": Visibility,
      "startDate": "string", // ISO 8601 date
      "endDate": "string", // ISO 8601 date
      "location": Location,
      "attachments": [Attachment]
    }
  ],
  "pagination": Pagination
}
```

---

### Get Event Details

**GET** `/api/events/{eventId}`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Response:** `200 OK`

```json
{
  "eventId": "string",
  "title": "string",
  "description": "string",
  "club": ClubReference,
  "visibility": Visibility,
  "startDate": "string", // ISO 8601 date
  "endDate": "string", // ISO 8601 date
  "location": Location,
  "attachments": [Attachment],
  "comments": [Comment]
}
```

---

### Get Event Comments

**GET** `/api/events/{eventId}/comments`

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Response:** `200 OK`

```json
{
  "comments": [Comment]
}
```

---

### Create Event

**POST** `/api/clubs/{clubId}/events`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Request Body:**

```json
{
  "title": "string",
  "description": "string",
  "visibility": Visibility,
  "startDate": "string", // ISO 8601 date
  "endDate": "string", // ISO 8601 date
  "attachments": [
    {
      "file": "binary" // File upload format depends on implementation
    }
  ],
  "reservations": [
    {
      "id": "number",
      "startDate": "string", // ISO 8601 date
      "endDate": "string" // ISO 8601 date
    }
  ]
}
```

**Response:** `201 Created`

```json
{
  "id": "number",
  "title": "string",
  "description": "string",
  "visibility": Visibility,
  "startDate": "string", // ISO 8601 date
  "endDate": "string", // ISO 8601 date
  "attachments": [Attachment],
  "reservations": [
    {
      "id": "number",
      "startDate": "string", // ISO 8601 date
      "endDate": "string", // ISO 8601 date
      "status": DemandStatus
    }
  ]
}
```

---

### Update Event

**PUT** `/api/clubs/{clubId}/events/{eventId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Request Body:** Same as Create Event

**Response:** `200 OK`

---

### Delete Event

**DELETE** `/api/clubs/{clubId}/events/{eventId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Response:** `204 No Content`

---

### Accept Reservation Alternative

**PUT** `/api/clubs/{clubId}/events/{eventId}/reservations/{reservationId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Request Body:**

```json
{
  "alternativeId": "number"
}
```

**Response:** `204 No Content`

---

### Hide Event

**PUT** `/api/events/{eventId}/hide`

**Authorization:** CLUBS_RESPONSIBLE

**Request Body:**

```json
{
  "reason": "string"
}
```

**Response:** `200 OK`

---

### Add Comment to Event

**POST** `/api/events/{eventId}/comments`

**Authorization:** USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Request Body:**

```json
{
  "content": "string"
}
```

**Response:** `201 Created`

```json
{
  "commentId": "string",
  "content": "string",
  "userId": "string",
  "createdAt": "string" // ISO 8601 date
}
```

---

### Book Event

**POST** `/api/events/{eventId}/book`

**Authorization:** USER

**Response:** `201 Created`

---

### Cancel Event Booking

**DELETE** `/api/events/{eventId}/book/me`

**Authorization:** USER

**Response:** `204 No Content`

---

### Get My Event Bookings

**GET** `/api/me/bookings`

**Authorization:** USER

**Response:** `200 OK`

```json
{
  "bookings": [
    {
      "bookingId": "string",
      "eventId": "string",
      "eventTitle": "string",
      "startDate": "string", // ISO 8601 date
      "bookedAt": "string" // ISO 8601 date
    }
  ]
}
```

### Get Event Bookings

**GET** `/api/event/{eventId}/bookings`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Response:** `200 OK`

```json
{
  "bookings": [
    {
      "user": UserReference,
      "bookingId": "string",
      "bookedAt": "string" // ISO 8601 date
    }
  ]
}
```

---

## Reservation Endpoints

### List All Reservations

**GET** `/api/reservations`

**Authorization:** CLUBS_RESPONSIBLE

**Query Parameters:**

- `location` (string, optional): Filter by location
- `start_date` (string, optional): Filter from date (ISO 8601)
- `end_date` (string, optional): Filter until date (ISO 8601)
- `status` (DemandStatus, optional): Filter by status
- `club_name` (string, optional): Filter by club
- `event_id` (string, optional): Filter by event
- `page` (number, optional): Default 1
- `limit` (number, optional): Default 20, max 100

**Response:** `200 OK`

```json
{
  "reservations": [
    {
      "id": "number",
      "startDate": "string", // ISO 8601 date
      "endDate": "string", // ISO 8601 date
      "clubName": "string",
      "location": Location,
      "status": DemandStatus
    }
  ],
  "pagination": Pagination
}
```

---

### Update Reservation Status

**PUT** `/api/reservations/{reservationId}`

**Authorization:** CLUBS_RESPONSIBLE

**Query Parameters:**

- `status` (DemandStatus, optional): New status

**Request Body:**

```json
{
  "alternatives": [
    {
      "id": "number"
    }
  ] // optional
}
```

**Response:** `200 OK`

---

## Storage Endpoints

File uploads follow a **two-step presigned URL flow**:

1. **Request a presigned PUT URL** — call the appropriate endpoint below to get a short-lived upload URL and an S3 object key.
2. **Upload directly to S3** — `PUT` the file to the returned `uploadUrl` with the matching `Content-Type` header. No auth header is needed for this request; the presigned URL is self-authenticating.
3. **Confirm the upload** — call the verify endpoint with the returned `key` so the backend can validate the upload and persist the reference.

---

### Get Presigned URL — User Profile Picture

**GET** `/api/storage/upload/users/{userId}/profile-picture`

**Authorization:** CLUBS_RESPONSIBLE, or the authenticated user matching `userId`

**Query Parameters:**

- `filename` (string, required): Original filename including extension (e.g. `avatar.png`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "key": "users/{userId}/{uuid}.{ext}"
}
```

**S3 key pattern:** `users/{userId}/{uuid}.{ext}`

---

### Get Presigned URL — Club Profile Picture

**GET** `/api/storage/upload/clubs/{clubId}/profile-picture`

**Authorization:** CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Query Parameters:**

- `filename` (string, required): Original filename including extension (e.g. `logo.jpg`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string", // Presigned S3 PUT URL (expires in 5 minutes)
  "key": "clubs/{clubId}/{uuid}.{ext}"
}
```

**S3 key pattern:** `clubs/{clubId}/{uuid}.{ext}`

---

### Get Presigned URL — Event Attachment

**GET** `/api/storage/upload/clubs/{clubId}/events/{eventId}/attachments`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Query Parameters:**

- `filename` (string, required): Original filename including extension (e.g. `schedule.pdf`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string", // Presigned S3 PUT URL (expires in 5 minutes)
  "key": "string"
}
```

---

### Get Presigned URL — Post Attachment

**GET** `/api/storage/upload/clubs/{clubId}/posts/{postId}/attachments`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS privilege)

**Query Parameters:**

- `filename` (string, required): Original filename including extension (e.g. `flyer.png`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string", // Presigned S3 PUT URL (expires in 5 minutes)
  "key": "string"
}
```

---

### Confirm Upload

**POST** `/api/storage/verify`

Must be called after the file has been successfully PUT to S3. The backend checks that the object exists, then associates it with the correct entity (user, club, event, or post) based on the key pattern. If the associated entity is not found, the orphaned S3 object is automatically deleted.

**Authorization:** Authenticated (same role requirements as the corresponding upload endpoint)

**Query Parameters:**

- `key` (string, required): The S3 object key returned by the presigned URL endpoint

**Response:** `200 OK` — upload confirmed and record saved

**Response:** `404 Not Found` — object not found in S3 (upload may not have completed)

---

### Get Presigned Download URL

**GET** `/api/storage/download`

Returns a short-lived presigned GET URL for any stored S3 object. Use the `key` values returned from entity responses (e.g. `profilePicture.url` contains the key, or use the `url` field on `Attachment`).

**Authorization:** Authenticated; access is subject to the visibility rules of the resource the object belongs to

**Query Parameters:**

- `key` (string, required): The full S3 object key

**Response:** `200 OK`

```json
{
  "url": "string"
}
```

### Get Presigned URL — Update User Profile Picture

**GET** `/api/storage/update/users/{userId}/profile-picture/{profilePictureId}`

**Authorization:** CLUBS_RESPONSIBLE, or the authenticated user matching `userId`

**Query Parameters:**

- `filename` (string, required): New filename including extension (e.g. `avatar.png`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "oldKey": "string",
  "newKey": "string"
}
```

`newKey` follows the upload key pattern: `users/{userId}/{uuid}.{ext}`

---

### Get Presigned URL — Update Club Profile Picture

**GET** `/api/storage/update/clubs/{clubId}/profile-picture/{profilePictureId}`

**Authorization:** CLUB_PRESIDENT, CLUBS_RESPONSIBLE

**Query Parameters:**

- `filename` (string, required): New filename including extension (e.g. `logo.jpg`)

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "oldKey": "string",
  "newKey": "string"
}
```

`newKey` follows the upload key pattern: `clubs/{clubId}/{uuid}.{ext}`

---

### Get Presigned URL — Update Event Attachment

**GET** `/api/storage/update/clubs/{clubId}/events/{eventId}/attachments/{attachmentId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_EVENTS privilege)

**Query Parameters:**

- `filename` (string, required): New filename including extension

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "oldKey": "string",
  "newKey": "string"
}
```

---

### Get Presigned URL — Update Post Attachment

**GET** `/api/storage/update/clubs/{clubId}/posts/{postId}/attachments/{attachmentId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS privilege)

**Query Parameters:**

- `filename` (string, required): New filename including extension

**Response:** `200 OK`

```json
{
  "uploadUrl": "string",
  "oldKey": "string",
  "newKey": "string"
}
```

---

### Confirm Update

**POST** `/api/storage/verify/update`

Must be called after the new file has been successfully PUT to S3. The backend verifies the new object exists, updates the S3 key in the DB, and deletes the old object from S3. If the new object is not found, nothing is changed.

**Authorization:** Authenticated (same role requirements as the corresponding update endpoint)

**Query Parameters:**

- `oldKey` (string, required): The old S3 object key returned by the update presigned URL endpoint
- `newKey` (string, required): The new S3 object key returned by the update presigned URL endpoint

**Response:** `200 OK` — update confirmed, record updated, old object deleted

**Response:** `404 Not Found` — new object not found in S3 (upload may not have completed)

---

### Delete Attachment

**DELETE** `/api/storage/attachments/{attachmentId}`

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_POSTS or MANAGE_EVENTS privilege)

**Response:** `204 No Content`

---

### Delete User Profile Picture

**DELETE** `/api/storage/users/profile-picture/{profilePictureId}`

**Authorization:** CLUBS_RESPONSIBLE, or the authenticated user owning the profile picture

**Response:** `204 No Content`

---

### Delete Club Profile Picture

**DELETE** `/api/storage/clubs/profile-picture/{profilePictureId}`

**Authorization:** CLUB_PRESIDENT, CLUBS_RESPONSIBLE

## **Response:** `204 No Content`

---

## Endpoint Registry

The privilege system is driven by a dynamic registry of grantable API endpoints. Each endpoint that can be assigned to club members is registered at startup via the `@GrantableEndpoint` annotation. This section exposes that registry.

### List All Grantable Endpoints

**GET** `/api/endpoints`

**Authorization:** Bearer token

Returns all grantable endpoints. Admins and coordination-club presidents see the full list including privileged endpoints (`MANAGE_CLUBS`). Regular members see only non-privileged endpoints.

**Response:** `200 OK`

```json
[
  {
    "name": "manage_profiles",
    "description": "Manage role profiles for a club",
    "category": "MANAGE_MEMBERS",
    "privileged": false
  },
  {
    "name": "assign_profile",
    "description": "Assign a profile or individual privileges to a member",
    "category": "MANAGE_MEMBERS",
    "privileged": false
  },
  {
    "name": "manage_club_settings",
    "description": "Manage club settings and coordination club status",
    "category": "MANAGE_CLUBS",
    "privileged": true
  }
]
```

**Response Codes:**

- `200 OK` — list returned
- `401 Unauthorized` — missing/invalid token

---

## Privilege Management

These endpoints manage **club profiles** (role templates that group multiple grantable endpoints) and the **assignment/revocation** of those privileges to individual club members.

**Base path:** `/api/clubs/{clubId}`

### Permission Model

- **Profile CRUD** (`manage_profiles`): Requires `ADMIN` role or the `manage_profiles` grantable endpoint on the club.
- **Privilege Assignment** (`assign_profile`): Requires `ADMIN` role or the `assign_profile` grantable endpoint.
- **Privilege Unassignment** (`unassign_profile`): Requires `ADMIN` role or the `unassign_profile` grantable endpoint.
- **Privilege Revocation** (`revoke_privilege`): Requires `ADMIN` role or the `revoke_privilege` grantable endpoint.
- **List Profiles** and **View Member Privileges**: Requires `ADMIN` role, any `MANAGE_MEMBERS` category privilege, or membership ownership (for viewing your own privileges).

---

### Create Profile

**POST** `/api/clubs/{clubId}/profiles`

**Authorization:** `ADMIN` or `manage_profiles` endpoint

Creates a new role profile (template) for the club.

**Request Body:**

```json
{
  "name": "Content Manager",
  "endpoints": ["manage_profiles", "assign_profile"]
}
```

All endpoint names in the `endpoints` list must exist in the endpoint registry. Duplicates are silently skipped.

**Response:** `201 Created`

```json
{
  "profileId": 1,
  "name": "Content Manager",
  "endpoints": ["manage_profiles", "assign_profile"]
}
```

**Response Codes:**

- `201 Created` — profile created
- `400 Bad Request` — blank name or invalid endpoint names
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — club not found

---

### Update Profile

**PUT** `/api/clubs/{clubId}/profiles/{profileId}`

**Authorization:** `ADMIN` or `manage_profiles` endpoint

Updates a profile's name and/or endpoint list. Changes take effect immediately — members holding this profile see the updated set of endpoints on next access check.

**Request Body:**

```json
{
  "name": "Senior Content Manager",
  "endpoints": ["manage_profiles", "assign_profile", "revoke_privilege"]
}
```

**Response:** `200 OK`

```json
{
  "profileId": 1,
  "name": "Senior Content Manager",
  "endpoints": ["manage_profiles", "assign_profile", "revoke_privilege"]
}
```

**Response Codes:**

- `200 OK` — profile updated
- `400 Bad Request` — blank name or invalid endpoint names
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — profile or club not found

---

### Delete Profile

**DELETE** `/api/clubs/{clubId}/profiles/{profileId}`

**Authorization:** `ADMIN` or `manage_profiles` endpoint

Deletes a profile. Members who held this profile keep their grants — the profile join record (`club_membership_profile`) is removed, but individually granted endpoints are unaffected.

**Response:** `204 No Content`

**Response Codes:**

- `204 No Content` — profile deleted
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — profile or club not found

---

### List Club Profiles

**GET** `/api/clubs/{clubId}/profiles`

**Authorization:** `ADMIN` or any club member

Returns all role profiles for the club.

**Response:** `200 OK`

```json
[
  {
    "profileId": 1,
    "name": "Content Manager",
    "endpoints": ["manage_profiles", "assign_profile"]
  },
  {
    "profileId": 2,
    "name": "Event Organizer",
    "endpoints": ["manage_events"]
  }
]
```

**Response Codes:**

- `200 OK` — list returned
- `401 Unauthorized` — missing/invalid token
- `404 Not Found` — club not found

---

### Assign Privileges to Member

**POST** `/api/clubs/{clubId}/members/{membershipId}/privileges`

**Authorization:** `ADMIN` or `assign_profile` endpoint

Grants privileges to a club member. This endpoint has two mutually exclusive modes:

**Mode 1 — Profile-based assignment** (`profileId` is set):

```json
{
  "profileId": 1,
  "endpoints": null
}
```

Grants all endpoints from the specified profile to the member. A `ClubMembershipProfile` join record is created. If `sync=true` is later used on the profile, changes propagate automatically. All endpoint names in the profile's template must be valid (400 if any unknown endpoint is found).

**Mode 2 — Individual assignment** (`profileId` is null):

```json
{
  "profileId": null,
  "endpoints": ["manage_profiles", "assign_profile"]
}
```

Grants the specified individual endpoints with no profile link. These grants persist even if profiles are updated or deleted.

**Role check:** Only `ASSISTANT_MEMBER` role can receive privileges. Returns `400` if the target member is `CLUB_PRESIDENT` or `MEMBER`.

**Duplicate handling:** If the member already holds a privilege from the same source (same profile for profile-based, or already individually granted), the duplicate is silently skipped.

**Response:** `201 Created` (empty body)

**Response Codes:**

- `201 Created` — privileges assigned
- `400 Bad Request` — invalid role, blank/invalid endpoint names, or profile endpoints not in registry
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — club or membership not found

---

### Unassign Profile from Member

**DELETE** `/api/clubs/{clubId}/members/{membershipId}/profiles/{profileId}`

**Authorization:** `ADMIN` or `unassign_profile` endpoint

Removes a profile assignment from a member. All endpoint grants that came from this profile are revoked. Grants that have another source (e.g., an individual grant or a different profile) are preserved.

**Response:** `200 OK` (empty body)

**Response Codes:**

- `200 OK` — profile unassigned
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — membership or profile assignment not found

---

### Revoke Individual Privilege from Member

**DELETE** `/api/clubs/{clubId}/members/{membershipId}/privileges/{endpointName}`

**Authorization:** `ADMIN` or `revoke_privilege` endpoint

Revokes a specific individual (null-profile) privilege from a member. If the privilege was granted via a profile, use the unassign-profile endpoint instead.

**Response:** `200 OK` (empty body)

**Response Codes:**

- `200 OK` — privilege revoked
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — membership or privilege not found

---

### View Member Privileges

**GET** `/api/clubs/{clubId}/members/{membershipId}/privileges`

**Authorization:** `ADMIN`, any `MANAGE_MEMBERS` category privilege, or the member themselves (membership owner)

Returns all privileges currently held by a member, grouped by endpoint, including which profiles contributed each grant.

**Response:** `200 OK`

```json
{
  "membershipId": 5,
  "privileges": [
    {
      "endpointName": "manage_profiles",
      "grantedDate": "2026-03-15T10:30:00Z",
      "sources": [
        {
          "profileId": 1,
          "name": "Content Manager"
        }
      ]
    },
    {
      "endpointName": "assign_profile",
      "grantedDate": "2026-03-15T10:30:00Z",
      "sources": [
        {
          "profileId": 1,
          "name": "Content Manager"
        }
      ]
    },
    {
      "endpointName": "revoke_privilege",
      "grantedDate": "2026-04-01T14:00:00Z",
      "sources": []
    }
  ]
}
```

- Privileges with a non-empty `sources` list were granted via profile assignment.
- Privileges with an empty `sources` list were granted individually (no profile link).

**Response Codes:**

- `200 OK` — privileges returned
- `401 Unauthorized` — missing/invalid token
- `403 Forbidden` — insufficient permissions (not admin, no MANAGE_MEMBERS privilege, and not the membership owner)
- `404 Not Found` — membership not found

---

## Common Response Codes

- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource or constraint violation
- `500 Internal Server Error` - Server error

## Error Response Format

```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": {} // optional
  }
}
```

---

## Notes

1. **Timestamps**: All timestamps are in ISO 8601 format (UTC)
2. **Pagination**: Default page size is 20, max is 100
3. **Role Hierarchy**:
   - CLUBS_RESPONSIBLE cannot be CLUB_PRESIDENT or ASSISTANT_MEMBER
   - CLUB_PRESIDENT can have multiple clubs
4. **Visibility Levels**: PUBLIC < STUDENTS_ONLY < MEMBERS_ONLY
5. **Assistant Privileges**: Customizable per club
6. **Public Endpoints**: Also accessible at `/api/public/*` routes (e.g., `/api/public/posts`)
7. **File Uploads**: All file uploads use a presigned S3 URL flow — request a PUT URL via `/api/storage/upload/*`, upload directly to S3, then confirm via `POST /api/storage/verify`. See the **Storage Endpoints** section for full details.
8. **Presigned URL Expiry**: PUT upload URLs expire after **5 minutes**; GET download URLs expire after **15 minutes**.
