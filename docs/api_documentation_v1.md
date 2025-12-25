# Student Club Management System - API Documentation

## Public End Points

the same urls as <Public end points> with and extra /public/.. (ex: /api/public/posts) and the api/auth/ urls.

## Authentication & Authorization

All endpoints require JWT authentication via `Authorization: Bearer <token>` header unless specified as public.

---

## Shared End Points

### Clubs

#### GET `/api/clubs`

Get all clubs.

**Response:** `200 OK`

```json
{
  "clubs": [
    {
      "clubName": "string",
      "description": "string",
      "profilePicture": {
         "url": "string"
       }
      "memberCount": 25
    }
  ]
}
```

#### GET `/api/clubs/{club_name}`

Get all clubs.

**Response:** `200 OK`

```json
{
      "clubName": "string",
      "description": "string",
      "profilePicture": {
         "url": "string"
       }
      "stuff": [
          {
            "username": "string"
            "firstName": "string"
            "lastName": "string"
            "profilePicture": {
               "url": "string"
             }
            "role": CLUB_PRESIDENT | ASSISTENT_MEMBER
            "roleDescription": "(optional)string"
          }
        ]
      "memberCount": 25
}
```

### Posts

#### GET `/api/posts`

Get all posts (the returned posts depends on the user role (GUEST, STUDENT, MEMBER)).

**Query Parameters:**

- `page` (optional): Page number (default: 1)
- `limit` (optional): Items per page (default: 20)
- `club_name` (optional): Filter by club username

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
            "profilePicture": {
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

- `page`, `limit`, `club_name` (same as posts)
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
      "location": {
            "id": "number",
            "name": "string",
            "internalLocation": "boolean"
        },
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
      "location": {
            "id": "number",
            "name": "string",
            "internalLocation": "boolean"
        },
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
  "username": "string",
  "email": "string",
  "roles": "STUDENT"|"CLUBPRESIDENT"|"ADMIN"|"USER",
  "token": "jwt_token_string"
}
```

### Comments (User)

#### POST `/api/posts/{postId}/comments`

Add comment to a post.

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
  "createdAt": "date"
}
```

#### POST `/api/events/{eventId}/comments`

Add comment to a event (same as to post).

---

## Student Endpoints

### Club Membership

#### POST `/api/clubs/{clubId}/join-request`

Request to join a club (STUDENT or CLUBS_PRESIDENT)

**Response:** `201 Created`

#### GET `/api/students/me/clubs`

Get student's club memberships.

**Response:** `200 OK`

```json
{
  "memberships": [
    {
      "clubName": "string",
      "clubFullName": "string"
      "description": "string"
      "profilePicture": {
         "url": "string"
       }
      "clubRole": Role,
      "joinedDate": "date"
    }
  ]
}
```

### Event Booking

#### POST `/api/events/{eventId}/book`

Book an event.

**Response:** `201 Created`

#### GET `/api/me/bookings`

Get user event bookings.

**Response:** `200 OK`

```json
{
  "bookings": [
    {
      "bookingId": "string",
      "eventId": "string",
      "eventTitle": "string",
      "startDate": "date",
      "bookedAt": "date"
    }
  ]
}
```

#### DELETE `/api/events/{eventId}/book`

Cancel event booking.

**Response:** `204 No Content`

---

## ClubsResponsible Endpoints

### Club Management

#### POST `/api/clubs`

Create a new club.

**Request Body:**

```json
{
  "clubName": "string",
  "clubFullName": "string"
  "description": "string",
  "presidentUsername": "string"(optional)
}
```

**Response:** `201 Created`

#### PUT `/api/clubs/{clubId}/president`

Change club president.

**Request Body:**

```json
{
  "presidentUsername": "string"
}
```

**Response:** `200 OK`

### Content Moderation

#### PUT `/api/posts/{postId}/hide`

Hide a post.

**Request Body:**

```json
{
  "reason": "string"
}
```

**Response:** `200 OK`

#### PUT `/api/events/{eventId}/hide`

Hide an event.

**Request Body:**

```json
{
  "reason": "string"
}
```

**Response:** `200 OK`

### Reservation Management

#### GET `/api/reservations`

View all event reservations.

**Query Parameters:**

- `location` (optional)
- `startDate`, `endDate` (optional)
- `status` (optional)
- `club_name` (optional)
- `event_id` (optional)
- `page`, `limit`

**Response:** `200 OK`

```json
{
  "reservations": [
    {
      "id": "number",
      "startDate": "date",
      "endDate": "date",
      "clubName": "string",
      "location": {
        "id": "number",
        "name": "string",
        "internalLocation": "boolean"
      }
    }
  ],
  "pagination": {}
}
```

#### PUT `/api/reservations/{reservationId}`

Update a reservation status.

**Query Parameters:**

- `status` (optional)

**Request body:**

```json
{
    "alternatives": [
        {
            "id": "number"
        }
    ] (optional)
}
```

**Response:** `200 OK`

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
      "username": "string",
      "firstName": "string",
      "lastName": "string",
      "profilePicture": {
         "url": "string"
       }
      "email": "string",
      "membership": {
        "id": "number",
        "clubRole": "number",
        "description": "string",
        "joinedDate": "date"
        },
    }
  ],
  "pagination": {}
}
```

#### GET `/api/clubs/{clubId}/join-requests`

Get pending join requests.

**Query Parameters:** `page`, `limit`

- `status` (optional)

**Response:** `200 OK`

```json
{
  "joinRequest": [
    {
      "id": "string",
       "user": {
          "username": "string",
          "firstName": "string",
          "lastName": "string",
          "profilePicture": {
             "url": "string"
           }
          "email": "string",
         }
      "joinRequestStatus": DemandStatus,
      "joinRequestDate": "date"
    }
  ]
}
```

#### Put `/api/clubs/{clubId}/join-requests/{requestId}`

Change join request status.

**Query Parameters:**

- `status` (optional)

**Response:** `200 OK`

#### DELETE `/api/clubs/{clubId}/members/{membershipId}`

Ban/remove a member.

**Response:** `204 No Content`

#### PUT `/api/clubs/{clubId}/members/{membershipId}`

Change member role.

**Query Parameters:**

- `role` (optional)

**Response:** `200 OK`

### Assistant Member Management

#### PUT `/api/clubs/{clubId}/members/{membershipId}/assistant`

Assign assistant member privileges.

**Request Body:**

```json
{
  "privileges": [Privilege]
}
```

**Response:** `200 OK`

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

Authorized: CLUB_PRESIDENT, and all ASSISTANT_MEMBER with the right privileges

#### POST `/api/clubs/{clubId}/posts`

Create a club post.

**Request Body:**

```json
{
  "title": "string",
  "description": "string",
  "visibility": Visibility,
  "attachments": [
    {
      "file": "binary"??
    }
  ]
}
```

**Response:** `201 Created`

```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "visibility": Visibility,
  "attachments": [
    {
      "type": AttachmentType,
      "url": "string"
    }
  ]
  "createdAt": "date"
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
  "description": "string",
  "visibility": Visibility,
  "startDate": "date",
  "endDate": "date",
  "attachments": [
    {
      "file": "binary"??
    }
  ]
  "reservations": [
     {
        "id": "number",
        "startDate": "date",
        "endDate": "date"
     }
    ],
}
```

**Response:** `201 Created`

```json
{
  "id": "number"
  "title": "string",
  "description": "string",
  "visibility": Visibility,
  "startDate": "date",
  "endDate": "date",
  "attachments": [
    {
      "type": AttachementType;
      "url": "string"
    }
  ]
  "reservations": [
     {
        "id": "number",
        "startDate": "date",
        "endDate": "date"
        "status": DemandStatus
     }
    ],
}
```

#### PUT `/api/clubs/{clubId}/events/{eventId}`

Update a club event.

**Request Body:** Same as create

**Response:** `200 OK`

#### DELETE `/api/clubs/{clubId}/events/{eventId}`

Delete a club event.

**Response:** `204 No Content`

<!-- #### GET `/api/clubs/{clubId}/events/{eventId}/bookings` -->

<!-- View event bookings. -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "bookings": [ -->
<!--     { -->
<!--       "bookingId": "string", -->
<!--       "userId": "string", -->
<!--       "username": "string", -->
<!--       "email": "string", -->
<!--       "bookedAt": "2024-01-20T10:00:00Z" -->
<!--     } -->
<!--   ], -->
<!--   "totalBookings": 45, -->
<!--   "capacity": 100 -->
<!-- } -->
<!-- ``` -->

<!-- ### Comments (Club President) -->

<!-- #### POST `/api/posts/{postId}/comments` -->

<!-- Comment on PUBLIC, STUDENTS_ONLY, and MEMBERS_ONLY posts. -->

<!-- #### POST `/api/events/{eventId}/comments` -->

<!-- Comment on PUBLIC, STUDENTS_ONLY, and MEMBERS_ONLY events. -->

<!-- #### DELETE `/api/posts/{postId}/comments/{commentId}` -->

<!-- Delete a comment on club posts. -->

<!-- **Response:** `204 No Content` -->

---

<!-- ## Club Member Endpoints -->

<!-- ### Access Content -->

<!-- ### Bookings -->

<!-- #### POST `/api/events/{eventId}/book` -->

<!-- Book any event (member has access to). -->

<!-- ## Admin Endpoints -->

<!-- ### User Management -->

<!-- #### GET `/api/admin/users` -->

<!-- Get all users. -->

<!-- **Query Parameters:** -->

<!-- - `page`, `limit` -->
<!-- - `role` (optional): Filter by role -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "users": [ -->
<!--     { -->
<!--       "userId": "string", -->
<!--       "username": "string", -->
<!--       "email": "string", -->
<!--       "roles": ADMIN | USER | STUDENT, -->
<!--       "createdAt": "2024-01-01T00:00:00Z" -->
<!--     } -->
<!--   ], -->
<!--   "pagination": {} -->
<!-- } -->
<!-- ``` -->

<!-- #### PUT `/api/admin/users/{userId}/role` -->

<!-- Assign Student role to user. -->

<!-- **Request Body:** -->

<!-- ```json -->
<!-- { -->
<!--   "role": "Student" -->
<!-- } -->
<!-- ``` -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "userId": "string", -->
<!--   "username": "string", -->
<!--   "roles": ["Student"] -->
<!-- } -->
<!-- ``` -->

<!-- #### DELETE `/api/admin/users/{userId}` -->

<!-- Kick out/ban a user. -->

<!-- **Response:** `204 No Content` -->

<!-- ### Statistics -->

<!-- #### GET `/api/admin/statistics` -->

<!-- View system statistics. -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "totalUsers": 500, -->
<!--   "totalStudents": 450, -->
<!--   "totalClubs": 25, -->
<!--   "totalPosts": 1200, -->
<!--   "totalEvents": 180, -->
<!--   "activeStudents": 320, -->
<!--   "upcomingEvents": 15 -->
<!-- } -->
<!-- ``` -->

<!-- #### GET `/api/admin/statistics/clubs` -->

<!-- Get club-wise statistics. -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "clubs": [ -->
<!--     { -->
<!--       "clubId": "string", -->
<!--       "clubName": "string", -->
<!--       "memberCount": 40, -->
<!--       "postCount": 85, -->
<!--       "eventCount": 12, -->
<!--       "activeMembers": 35 -->
<!--     } -->
<!--   ] -->
<!-- } -->
<!-- ``` -->

<!-- ### ClubsResponsible Management -->

<!-- #### PUT `/api/admin/users/{userId}/clubs-responsible` -->

<!-- Assign ClubsResponsible role. -->

<!-- **Response:** `200 OK` -->

<!-- ```json -->
<!-- { -->
<!--   "userId": "string", -->
<!--   "responsibleId": "string", -->
<!--   "assignedAt": "2024-01-15T10:00:00Z" -->
<!-- } -->
<!-- ``` -->

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
5. **Assistant Privileges**: Customizable per club
