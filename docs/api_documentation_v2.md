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

enum Privilege {
  MANAGE_POSTS = 'MANAGE_POSTS',
  MANAGE_EVENTS = 'MANAGE_EVENTS',
  MANAGE_MEMBERS = 'MANAGE_MEMBERS',
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
```

---

## Authentication

All endpoints require JWT authentication via `Authorization: Bearer <token>` header unless marked as **Public**.

---

## Authentication Endpoints

### Register User

**POST** `/api/auth/register`

**Authorization:** Public

**Request Body:**

```json
{
  "firstName": "string",
  "lastName": "string",
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Response:** `201 Created`

---

### Verify User

**POST** `/api/auth/verify`

**Authorization:** Public

**Query Parameters:**

- `code` (string, required): Verification code sent via email

**Response:** `200 OK`

---

### Resend Verification Code

**GET** `/api/auth/verify/resend`

**Authorization:** Public

**Query Parameters:**

- `username` (string, required): Username to receive verification code

**Response:** `200 OK`

---

### Login

**POST** `/api/auth/login`

**Authorization:** Public

**Request Body:**

```json
{
  "identifier": "string", // username or email
  "password": "string"
}
```

**Response:** `200 OK`

```json
{
  "username": "string",
  "email": "string",
  "roles": UserRole,
  "token": "string"
}
```

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
  "key": "string"
}
```

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
  "key": "clubs/{clubId}/profile_picture.{ext}"
}
```

**S3 key pattern:** `clubs/{clubId}/profile_picture.{ext}`

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
