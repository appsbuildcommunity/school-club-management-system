# Student Club Management System - API Documentation

## Type Definitions

### Enums

```typescript
enum UserRole {
  GUEST = "GUEST",
  USER = "USER",
  STUDENT = "STUDENT",
  ADMIN = "ADMIN"
}

enum ClubRole {
  MEMBER = "MEMBER",
  ASSISTANT_MEMBER = "ASSISTANT_MEMBER",
  CLUB_PRESIDENT = "CLUB_PRESIDENT"
}

enum Visibility {
  PUBLIC = "PUBLIC",
  STUDENTS_ONLY = "STUDENTS_ONLY",
  MEMBERS_ONLY = "MEMBERS_ONLY"
}

enum AttachmentType {
  IMAGE = "IMAGE",
  VIDEO = "VIDEO",
  DOCUMENT = "DOCUMENT"
}

enum DemandStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
  CANCELLED = "CANCELLED"
}

enum Privilege {
  MANAGE_POSTS = "MANAGE_POSTS",
  MANAGE_EVENTS = "MANAGE_EVENTS",
  MANAGE_MEMBERS = "MANAGE_MEMBERS"
}
```

### Common Structures

```typescript
// User Reference
interface UserReference {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  profilePicture: {
    url: string;
  };
}

// Club Reference
interface ClubReference {
  id: string;
  name: string;
  description: string;
  profilePicture: {
    url: string;
  };
}

// Location
interface Location {
  id: number;
  name: string;
  internalLocation: boolean;
}

// Attachment
interface Attachment {
  attachmentId: string;
  type: AttachmentType;
  url: string;
}

// Comment
interface Comment {
  commentId: string;
  content: string;
  user: UserReference;
  createdAt: string; // ISO 8601 date
}

// Pagination
interface Pagination {
  currentPage: number;
  totalPages: number;
  totalItems: number;
}

// Member
interface Member {
  username: string;
  firstName: string;
  lastName: string;
  profilePicture?: {
    url: string;
  };
  role: ClubRole;
  roleDescription?: string;
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

**GET** `/api/clubs/{club_name}`

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

**Authorization:** CLUB_PRESIDENT, ASSISTANT_MEMBER (with MANAGE_MEMBERS privilege)

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

**Authorization:** CLUB_PRESIDENT

**Response:** `204 No Content`

---

### Update Member Role

**PUT** `/api/clubs/{clubId}/members/{membershipId}`

**Authorization:** CLUB_PRESIDENT

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
- `userId` (string, optional): Filter by user

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

**Authorization:** STUDENT, CLUB_PRESIDENT

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** Public, GUEST, USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

### Hide Event

**PUT** `/api/events/{eventId}/hide`

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** USER, STUDENT, CLUB_PRESIDENT, CLUBS_RESPONSIBLE, ADMIN

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

**Authorization:** STUDENT, CLUB_PRESIDENT

**Response:** `201 Created`

---

### Cancel Event Booking

**DELETE** `/api/events/{eventId}/book`

**Authorization:** STUDENT, CLUB_PRESIDENT

**Response:** `204 No Content`

---

### Get My Event Bookings

**GET** `/api/me/bookings`

**Authorization:** STUDENT, CLUB_PRESIDENT

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

---

## Reservation Endpoints

### List All Reservations

**GET** `/api/reservations`

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

**Query Parameters:**
- `location` (string, optional): Filter by location
- `startDate` (string, optional): Filter from date (ISO 8601)
- `endDate` (string, optional): Filter until date (ISO 8601)
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

**Authorization:** CLUBS_RESPONSIBLE, ADMIN

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
7. **File Uploads**: Actual implementation format (multipart/form-data, base64, etc.) depends on backend implementation
