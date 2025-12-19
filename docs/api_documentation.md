# Student Club Management System - API Documentation

## Authentication & Authorization

All endpoints require JWT authentication via `Authorization: Bearer <token>` header unless specified as public.

---

## Guest Endpoints (Public Access)

### Posts

#### GET `/api/posts`

Get all posts (the returned posts depends on the user role (GUEST, STUDENT, MEMBER)).

**Query Parameters:**

- `page` (optional): Page number (default: 1)
- `limit` (optional): Items per page (default: 20)
- `club_username` (optional): Filter by club username

**Response:** `200 OK`

```json
{
  "posts": [
    {
      "postId": "string",
      "title": "string",
      "content": "string",
      "club": {
            "id": "string"
            "name": "string",
            "profile_picture": {
                "url": "string"
            }
        },
      "createdAt": "date",
      "visibility": "PUBLIC | STUDENTS_ONLY | PRIVATE",
      "attachments": [
        {
          "attachmentId": "string",
          "type": "IMAGE",
          "url": "string"
        }
      ]
    }
  ],
  "pagination": {
    "currentPage": 1,
    "totalPages": 5,
    "totalItems": 100
  }
}
```

#### GET `/api/posts/{postId}`

Get a specific post.

**Response:** `200 OK`

```json
{
  "postId": "string",
  "title": "string",
  "content": "string",
  "club": {
         "id": "string"
         "name": "string",
         "profile_picture": {
             "url": "string"
         }
     },
  "createdAt": "2024-01-15T10:30:00Z",
  "visibility": "PUBLIC | STUDENTS_ONLY | PRIVATE",
  "attachments": [],
  "comments": [
      "commentId": "string",
      "content": "string",
      "user": {
         "id": "string"
         "username": "string",
         "profilePicture": {
            "url": "string"
            },
        }
      "createdAt": "date"
  ]
}
```

#### GET `/api/posts/{postId}/comments`

Get comments of a specific post.

**Response:** `200 OK`

```json
{
    "comments": [
        "commentId": "string",
        "content": "string",
        "user": {
           "id": "string"
           "username": "string",
           "profilePicture": {
              "url": "string"
              },
          }
        "createdAt": "date"
    ]
}
```

### Events

#### GET `/api/events`

Get all events.

**Query Parameters:**

- `page`, `limit`, `club_username` (same as posts)
- `start_date` (optional): Filter events from date
- `end_date` (optional): Filter events until date

**Response:** `200 OK`

```json
{
  "events": [
    {
      "eventId": "string",
      "title": "string",
      "description": "string"
      "club": {
         "id": "string"
         "name": "string",
         "profilePicture": {
             "url": "string"
         }
      },

      "visibility": "PUBLIC | STUDENTS_ONLY | PRIVATE",
      "startDate": "date",
      "endDate": "date",
      "location": "string",
      "attachments": [
        {
          "attachmentId": "string",
          "type": "IMAGE",
          "url": "string"
        }
      ]
    }
  ],
  "pagination": {...}
}
```

#### GET `/api/events/{eventId}`

Get a specific event.

**Response:** `200 OK`

```json
{
      "eventId": "string",
      "title": "string",
      "description": "string"
      "club": {
         "id": "string"
         "name": "string",
         "profile_picture": {
             "url": "string"
         }
      },

      "visibility": "PUBLIC | STUDENTS_ONLY | PRIVATE",
      "startDate": "date",
      "endDate": "date",
      "location": "string",
      "attachments": [
        {
          "attachmentId": "string",
          "type": "IMAGE",
          "url": "string"
        }
      ]
      "comments": [
        "commentId": "string",
        "content": "string",
        "user": {
           "id": "string"
           "username": "string",
           "profilePicture": {
              "url": "string"
              },
          }
        "createdAt": "date"
        ]
}
```

#### GET `/api/event/{eventId}/comments`

Get comments of a specific event.

**Response:** `200 OK`

```json
{
    "comments": [
        "commentId": "string",
        "content": "string",
        "user": {
           "id": "string"
           "username": "string",
           "profilePicture": {
              "url": "string"
              },
          }
        "createdAt": "date"
    ]
}
```

---


## User Endpoints (Authenticated Users)

### Authentication

#### POST `/api/auth/register`

Register a new user.

**Request Body:**

```json
{
  "firstName": "string"
  "lastName": "string"
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Response:** `201 Created`

### POST `/verify`

verify the user

- **Query Params:**
  - `code` (string): the verification code sent to the user via email
- **Response(200)**

### GET `/verify/resend`

resend the verification code to the user email

- **Query Params:**
  - `username` (string): the username to which the code will be sent
- **Response(200)**

#### POST `/api/auth/login`

Login user.

**Request Body:**

```json
{
  "identifier": "string"(the username or the email),
  "password": "string"
}
```

**Response:** `200 OK`

```json
{
  "userId": "string",
  "username": "string",
  "email": "string",
  "roles": ["Student", "ClubPresident"],
  "token": "jwt_token_string"
}
```

### Comments (User)

#### POST `/api/posts/{postId}/comments`

Add comment to a PUBLIC post.

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
  "createdAt": "2024-01-15T12:00:00Z"
}
```

#### POST `/api/events/{eventId}/comments`

Add comment to a PUBLIC event.

---

## Student Endpoints

### Posts & Events

#### GET `/api/posts`

Get PUBLIC and STUDENTS_ONLY posts.

**Query Parameters:** Same as public posts endpoint

**Response:** `200 OK` (Similar structure with additional STUDENTS_ONLY posts)

#### GET `/api/events`

Get PUBLIC and STUDENTS_ONLY events.

### Club Membership

#### GET `/api/clubs`

Get all clubs.

**Response:** `200 OK`

```json
{
  "clubs": [
    {
      "clubId": "string",
      "clubName": "string",
      "description": "string",
      "presidentName": "string",
      "memberCount": 25
    }
  ]
}
```

#### POST `/api/clubs/{clubId}/join-request`

Request to join a club.

**Response:** `201 Created`

```json
{
  "message": "Join request submitted successfully",
  "requestId": "string"
}
```

#### GET `/api/students/me/clubs`

Get student's club memberships.

**Response:** `200 OK`

```json
{
  "memberships": [
    {
      "clubId": "string",
      "clubName": "string",
      "membershipId": "string",
      "clubRole": "MEMBER",
      "joinedDate": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### Event Booking

#### POST `/api/events/{eventId}/book`

Book an event (PUBLIC or STUDENTS_ONLY).

**Response:** `201 Created`

```json
{
  "bookingId": "string",
  "eventId": "string",
  "userId": "string",
  "bookedAt": "2024-01-15T12:00:00Z"
}
```

#### GET `/api/students/me/bookings`

Get student's event bookings.

**Response:** `200 OK`

```json
{
  "bookings": [
    {
      "bookingId": "string",
      "eventId": "string",
      "eventTitle": "string",
      "startDate": "2024-02-01T14:00:00Z",
      "bookedAt": "2024-01-20T10:00:00Z"
    }
  ]
}
```

#### DELETE `/api/events/{eventId}/book`

Cancel event booking.

**Response:** `204 No Content`

### Comments (Student)

#### POST `/api/posts/{postId}/comments`

Comment on PUBLIC and STUDENTS_ONLY posts.

#### POST `/api/events/{eventId}/comments`

Comment on PUBLIC and STUDENTS_ONLY events.

---

## Admin Endpoints

### User Management

#### GET `/api/admin/users`

Get all users.

**Query Parameters:**

- `page`, `limit`
- `role` (optional): Filter by role

**Response:** `200 OK`

```json
{
  "users": [
    {
      "userId": "string",
      "username": "string",
      "email": "string",
      "roles": ["Student"],
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": {}
}
```

#### PUT `/api/admin/users/{userId}/role`

Assign Student role to user.

**Request Body:**

```json
{
  "role": "Student"
}
```

**Response:** `200 OK`

```json
{
  "userId": "string",
  "username": "string",
  "roles": ["Student"]
}
```

#### DELETE `/api/admin/users/{userId}`

Kick out/ban a user.

**Response:** `204 No Content`

### Statistics

#### GET `/api/admin/statistics`

View system statistics.

**Response:** `200 OK`

```json
{
  "totalUsers": 500,
  "totalStudents": 450,
  "totalClubs": 25,
  "totalPosts": 1200,
  "totalEvents": 180,
  "activeStudents": 320,
  "upcomingEvents": 15
}
```

#### GET `/api/admin/statistics/clubs`

Get club-wise statistics.

**Response:** `200 OK`

```json
{
  "clubs": [
    {
      "clubId": "string",
      "clubName": "string",
      "memberCount": 40,
      "postCount": 85,
      "eventCount": 12,
      "activeMembers": 35
    }
  ]
}
```

### ClubsResponsible Management

#### PUT `/api/admin/users/{userId}/clubs-responsible`

Assign ClubsResponsible role.

**Response:** `200 OK`

```json
{
  "userId": "string",
  "responsibleId": "string",
  "assignedAt": "2024-01-15T10:00:00Z"
}
```

---

## ClubsResponsible Endpoints

### Club Management

#### POST `/api/clubs`

Create a new club.

**Request Body:**

```json
{
  "clubName": "string",
  "description": "string",
  "presidentUserId": "string"
}
```

**Response:** `201 Created`

```json
{
  "clubId": "string",
  "clubName": "string",
  "description": "string",
  "presidentId": "string",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

#### PUT `/api/clubs/{clubId}/president`

Change club president.

**Request Body:**

```json
{
  "newPresidentUserId": "string"
}
```

**Response:** `200 OK`

```json
{
  "clubId": "string",
  "previousPresidentId": "string",
  "newPresidentId": "string",
  "changedAt": "2024-01-15T11:00:00Z"
}
```

### Content Moderation

#### GET `/api/responsible/posts`

View all posts (including MEMBERS_ONLY).

**Query Parameters:** `page`, `limit`, `visibility`, `clubId`

**Response:** `200 OK`

#### PUT `/api/responsible/posts/{postId}/hide`

Hide a post.

**Request Body:**

```json
{
  "reason": "string"
}
```

**Response:** `200 OK`

```json
{
  "postId": "string",
  "hidden": true,
  "hiddenAt": "2024-01-15T12:00:00Z",
  "reason": "string"
}
```

#### PUT `/api/responsible/events/{eventId}/hide`

Hide an event.

**Response:** `200 OK`

### Reservation Management

#### GET `/api/responsible/reservations`

View all event reservations/bookings.

**Query Parameters:**

- `eventId` (optional)
- `startDate`, `endDate` (optional)
- `page`, `limit`

**Response:** `200 OK`

```json
{
  "reservations": [
    {
      "bookingId": "string",
      "eventId": "string",
      "eventTitle": "string",
      "userId": "string",
      "username": "string",
      "bookedAt": "2024-01-15T10:00:00Z"
    }
  ],
  "pagination": {}
}
```

#### GET `/api/responsible/calendar`

Get reservation calendar view.

**Query Parameters:**

- `startDate`: Start date
- `endDate`: End date

**Response:** `200 OK`

```json
{
  "events": [
    {
      "eventId": "string",
      "title": "string",
      "startDate": "2024-02-01T14:00:00Z",
      "endDate": "2024-02-01T18:00:00Z",
      "location": "string",
      "bookingsCount": 45,
      "capacity": 100
    }
  ]
}
```

---

## Club President Endpoints

### Club Management

#### GET `/api/clubs/{clubId}/members`

Get club members.

**Query Parameters:** `page`, `limit`

**Response:** `200 OK`

```json
{
  "members": [
    {
      "membershipId": "string",
      "userId": "string",
      "username": "string",
      "email": "string",
      "clubRole": "MEMBER",
      "joinedDate": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": {}
}
```

#### GET `/api/clubs/{clubId}/join-requests`

Get pending join requests.

**Response:** `200 OK`

```json
{
  "requests": [
    {
      "requestId": "string",
      "userId": "string",
      "username": "string",
      "email": "string",
      "requestedAt": "2024-01-15T09:00:00Z"
    }
  ]
}
```

#### POST `/api/clubs/{clubId}/join-requests/{requestId}/accept`

Accept join request.

**Response:** `200 OK`

```json
{
  "membershipId": "string",
  "userId": "string",
  "clubId": "string",
  "clubRole": "MEMBER",
  "joinedDate": "2024-01-15T12:00:00Z"
}
```

#### DELETE `/api/clubs/{clubId}/join-requests/{requestId}`

Reject join request.

**Response:** `204 No Content`

#### DELETE `/api/clubs/{clubId}/members/{membershipId}`

Ban/remove a member.

**Response:** `204 No Content`

### Assistant Member Management

#### PUT `/api/clubs/{clubId}/members/{membershipId}/assistant`

Assign assistant member role.

**Request Body:**

```json
{
  "privileges": ["CREATE_POST", "MANAGE_EVENTS", "ACCEPT_MEMBERS"]
}
```

**Response:** `200 OK`

```json
{
  "privilegeId": "string",
  "membershipId": "string",
  "privileges": ["CREATE_POST", "MANAGE_EVENTS"],
  "grantedDate": "2024-01-15T12:00:00Z"
}
```

#### PUT `/api/clubs/{clubId}/assistant/{privilegeId}`

Update assistant privileges.

**Request Body:**

```json
{
  "privileges": ["CREATE_POST", "MANAGE_EVENTS", "VIEW_ANALYTICS"]
}
```

**Response:** `200 OK`

#### DELETE `/api/clubs/{clubId}/assistant/{privilegeId}`

Remove assistant privileges.

**Response:** `204 No Content`

### Member History

#### GET `/api/clubs/{clubId}/history`

View membership history.

**Query Parameters:** `page`, `limit`, `userId` (optional)

**Response:** `200 OK`

```json
{
  "history": [
    {
      "historyId": "string",
      "userId": "string",
      "username": "string",
      "clubRole": "ASSISTANT_MEMBER",
      "roleDescription": "Event Coordinator",
      "joinedDate": "2024-01-01T00:00:00Z",
      "leftDate": "2024-06-15T00:00:00Z"
    }
  ],
  "pagination": {}
}
```

### Posts Management

#### POST `/api/clubs/{clubId}/posts`

Create a club post.

**Request Body:**

```json
{
  "title": "string",
  "content": "string",
  "visibility": "MEMBERS_ONLY",
  "attachments": [
    {
      "type": "IMAGE",
      "url": "string"
    }
  ]
}
```

**Response:** `201 Created`

```json
{
  "postId": "string",
  "title": "string",
  "content": "string",
  "clubId": "string",
  "visibility": "MEMBERS_ONLY",
  "createdAt": "2024-01-15T12:00:00Z"
}
```

#### PUT `/api/clubs/{clubId}/posts/{postId}`

Update a club post.

**Request Body:** Same as create

**Response:** `200 OK`

#### DELETE `/api/clubs/{clubId}/posts/{postId}`

Delete a club post.

**Response:** `204 No Content`

### Events Management

#### POST `/api/clubs/{clubId}/events`

Create a club event.

**Request Body:**

```json
{
  "title": "string",
  "visibility": "STUDENTS_ONLY",
  "startDate": "2024-02-01T14:00:00Z",
  "endDate": "2024-02-01T18:00:00Z",
  "location": "string",
  "description": "string",
  "capacity": 100
}
```

**Response:** `201 Created`

```json
{
  "eventId": "string",
  "title": "string",
  "clubId": "string",
  "visibility": "STUDENTS_ONLY",
  "startDate": "2024-02-01T14:00:00Z",
  "endDate": "2024-02-01T18:00:00Z",
  "location": "string",
  "capacity": 100,
  "createdAt": "2024-01-15T12:00:00Z"
}
```

#### PUT `/api/clubs/{clubId}/events/{eventId}`

Update a club event.

**Response:** `200 OK`

#### DELETE `/api/clubs/{clubId}/events/{eventId}`

Delete a club event.

**Response:** `204 No Content`

#### GET `/api/clubs/{clubId}/events/{eventId}/bookings`

View event bookings.

**Response:** `200 OK`

```json
{
  "bookings": [
    {
      "bookingId": "string",
      "userId": "string",
      "username": "string",
      "email": "string",
      "bookedAt": "2024-01-20T10:00:00Z"
    }
  ],
  "totalBookings": 45,
  "capacity": 100
}
```

### Comments (Club President)

#### POST `/api/posts/{postId}/comments`

Comment on PUBLIC, STUDENTS_ONLY, and MEMBERS_ONLY posts.

#### POST `/api/events/{eventId}/comments`

Comment on PUBLIC, STUDENTS_ONLY, and MEMBERS_ONLY events.

#### DELETE `/api/posts/{postId}/comments/{commentId}`

Delete a comment on club posts.

**Response:** `204 No Content`

---

## Club Member Endpoints

### Access Content

#### GET `/api/clubs/{clubId}/posts`

View all club posts (PUBLIC, STUDENTS_ONLY, MEMBERS_ONLY).

**Query Parameters:** `page`, `limit`, `visibility`

**Response:** `200 OK`

#### GET `/api/clubs/{clubId}/events`

View all club events.

**Response:** `200 OK`

### Comments

#### POST `/api/posts/{postId}/comments`

Comment on all visibility level posts (if member).

#### POST `/api/events/{eventId}/comments`

Comment on all visibility level events (if member).

### Bookings

#### POST `/api/events/{eventId}/book`

Book any event (member has access to).

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
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": {}
  }
}
```

---

## Notes

1. **Timestamps**: All timestamps are in ISO 8601 format (UTC)
2. **Pagination**: Default page size is 20, max is 100
3. **Role Hierarchy**: ClubsResponsible cannot be ClubPresident or AssistantMember
4. **Visibility Levels**: PUBLIC < STUDENTS_ONLY < MEMBERS_ONLY
5. **Assistant Privileges**: Customizable per club (e.g., CREATE_POST, MANAGE_EVENTS, ACCEPT_MEMBERS, VIEW_ANALYTICS)
