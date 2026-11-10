# Student Club Management System — The Full Documentation

> **Audience:** Engineering Team  
> **Status:** In Review  
> **Document index:** This document defines the system requirements, authorization rules, interfaces, layouts, states, and role-based variations across the system.

---

## Table Of Contents

- [A. System Authorization & Governance](#a-system-authorization--governance)
    - [a. Roles Glossary](#a-roles-glossary)
    - [b. Role Hierarchy](#b-role-hierarchy)
    - [c. Visibility](#c-visibility)
    - [e. Permissions](#e-permissions)
- [B. Platform & Navigation](#b-platform--navigation)
    - [B.1 Mobile Platform](#b1-mobile-platform)
    - [B.2 Web Platform](#b2-web-platform)
- [C. Screens](#c-screens)
- [D. Per-Role User Journeys](#d-per-role-user-journeys)
    - [D.1 Guest to User Journey](#d1-guest-to-user-journey)
    - [D.2 Student Journey](#d2-student-journey)
    - [D.3 Club President Journey](#d3-club-president-journey)
    - [D.4 Assistant Member Journey](#d4-assistant-member-journey)
    - [D.5 Club Member Journey](#d5-club-member-journey)
    - [D.6 Coordination Club President Journey](#d6-coordination-club-president-journey)
    - [D.7 Coordination Assistant Member / Clubs Responsible Journey](#d7-coordination-assistant-member--clubs-responsible-journey)
    - [D.8 Admin Journey](#d8-admin-journey)
- [E. Business Rules](#e-business-rules)
---

## Terminology

- **Coordination Club ("BDE")**: The unique, non-deletable, school-level club bootstrapped at launch to manage other clubs.

- **System Role (Realm Role)**: A high-level platform identity assigned in Keycloak (`GUEST`, `USER`, `STUDENT`, `ADMIN`).

- **Club Role**: A role assigned to a user within a specific club's membership (`MEMBER`, `ASSISTANT_MEMBER`, `CLUB_PRESIDENT`).

- **Privilege (Grantable Endpoint)**: An active permission granted to an `ASSISTANT_MEMBER`, either directly as an individual endpoint or via a `ClubProfile`.

- **Category**: A functional grouping of grantable endpoints (`MANAGE_POSTS`, `MANAGE_EVENTS`, `MANAGE_MEMBERS`, `MANAGE_CLUBS`).

- **Club Profile**: A named custom preset belonging to a club that groups multiple grantable endpoints together (e.g., "Event Manager" profile).

- **Privileged Endpoint**: A special endpoint restricted exclusively to the Coordination Club.

- **Visibility**: Access control scope for posts and events (`PUBLIC`, `STUDENTS_ONLY`, `MEMBERS_ONLY`, `HIDDEN`).


---

# A. System Authorization & Governance

## a. Roles Glossary

| Role | Scope | Description |
| --- | --- | --- |
| `ADMIN` | System | Platform administrator. Handles account bans, platform statistics, and system role changes. |
| `GUEST` | System | Unauthenticated viewer. Can browse public posts, events, and clubs. |
| `USER` | System | Authenticated user (unverified student). Can comment on and book public events. |
| `STUDENT` | System | Verified student account. Can view, comment on, and book student content, as well as request to join clubs. |
| `CLUB_PRESIDENT` | Club | Highest authority in a club. Has all privileges related to this club, including managing the privileges of assistant members. |
| `ASSISTANT_MEMBER` | Club | A club staff member having privileges granted dynamically by the `CLUB_PRESIDENT` (assigned directly or via `ClubProfile`). |
| `MEMBER` | Club | A member in a specific club. Can access members-only posts and events. |

---

## b. Role Hierarchy

**Legend:** `X` → `Y` means `X` inherits all capabilities of `Y`.

### b.1 System Level:
- `STUDENT` → `USER` → `GUEST`


### b.2 Club Level:
- `CLUB_PRESIDENT` → `ASSISTANT_MEMBER` → `MEMBER`


## c. Visibility

| Visibility Scope | Content Visible To | Commenting | Event Booking |
| --- | --- | --- | --- |
| `PUBLIC` | Everyone (Guest, User, Student) | `USER`+ | `USER`+ |
| `STUDENTS_ONLY` | `STUDENT` | `STUDENT`+ | `STUDENT`+ |
| `MEMBERS_ONLY` | `MEMBER` of the specific club | `MEMBER`+ | `MEMBER`+ |
| `HIDDEN` | `CLUB_PRESIDENT`/`ASSISTANT_MEMBER` with priviliges  | Disabled | Disabled |

- **Note:** Coordination Club Content is only `PUBLIC`, `STUDENTS_ONLY`, or `HIDDEN`. Every student is effectively a default recipient of BDE content without explicit membership.


---

## e. Permissions

### Dynamic Permissions Engine Structure

Permissions in the system are fully dynamic and driven by backend-registered API capabilities:

- **Grant Mechanics**:

    1. **Direct Endpoint Selection**: A President can assign specific endpoint names directly to an assistant member (e.g., `endpoints: ["create_post"]`).
    2. **Club Profile Preset**: A President can group endpoints into a named template (`ClubProfile`), then assign that profile to members (e.g., `EVENT_MANAGER`). Members inherit all endpoints configured in the profile.
- **Dynamic Propagation**: If a `ClubProfile`'s endpoint list is updated, all members assigned to that profile automatically acquire the updated capability set.

- **Source Attribution**: The system tracks whether a privilege was granted directly or originated from one or more `ClubProfile`s.

- **Privileged Scope Isolation**: Privileged endpoints are strictly prohibited from being granted in regular clubs and can only be assigned within the Coordination Club.


### Situational Examples

#### Situation 1: Direct Endpoint Grant to a Club Assistant
- **Scenario**: The President of the Robotics Club wants a member to publish posts without giving them event or member management permissions.

- **Action**: The President assigns the individual endpoint `manage_posts` directly to the member.

- **Result**: The member's role becomes `ASSISTANT_MEMBER` with the `manage_posts` capability. No reusable profile template is created.


#### Situation 2: Club Profile Preset Assignment
- **Scenario**: The President of the Music Club wants a standard role for their event team.

- **Action**: The President creates a `ClubProfile` named "Event Lead" containing endpoints `["create_event", `update_event`, `manage_reservations`]`, then assigns "Event Lead" to two assistant members.

- **Result**: Both members receive all three endpoints. Their privilege records link back to the "Event Lead" profile as the source.


#### Situation 3: Dynamic Propagation on Profile Template Update
- **Scenario**: The Music Club President decides Event Leads should also be able to cancel events.

- **Action**: The President updates the "Event Lead" `ClubProfile` to add `cancel_event`.

- **Result**: Both assistant members assigned to "Event Lead" immediately gain the `cancel_event` capability.


#### Situation 4: Coordination Club
- **Scenario**: The Coordination Club President delegates club management tasks to a member of his team.

- **Action**: The Coordination President assigns the privileged endpoint `change_club_president` (`MANAGE_CLUBS` category) to the member.

- **Result**: The member becomes a "Clubs Responsible" capable of reassigning club presidents across any school club.


#### Situation 5: System Admin Scope Boundaries
- **Scenario**: A user violates platform policies.

- **Action**: System `ADMIN` revokes the user's `STUDENT` status or bans their Keycloak account.

- **Boundary**: The System `ADMIN` cannot publish club posts, edit club events, or manage internal club memberships.


---

## B. Platform & Navigation

The system works across two platforms: the **Mobile Platform**, built for daily student use and club activities, and the **Web Platform**, which contains all mobile features along with tools for full school management and administration.

---

### B.1 Mobile Platform
Designed for daily engagement and club-level tasks:

* **`Home Feed`**: Browse posts and events based on access level (`PUBLIC`, `STUDENTS_ONLY`, `MEMBERS_ONLY`), view attachments, and add comments.
* **`Clubs Directory`**: Explore clubs, view club leaders, and send join requests.
* **`My Calendar`**: Check booked events, see club activities, and cancel bookings.
* **`User Profile`**: View personal details, update profile picture, and view joined clubs.
* **`Club Dashboard` (`CLUB_PRESIDENT` & `ASSISTANT_MEMBER`)**:
    * **`Publishing`**: Create, edit, and delete club posts and events with attachments.
    * **`Member Management`**: Accept or reject join requests, view members, and remove members.
    * **`Permissions`**: Create custom role profiles and give or remove permissions for assistant members.
    * **`Statistics`**: View club numbers and see lists of event attendees.

---

### B.2 Web Platform
The complete version of the system that includes every mobile feature plus administrative tools:

* **`Full Mobile Parity`**: Complete access to feeds, event booking, commenting, club browsing, personal calendar, and content publishing.
* **`Club Management`**: Create new clubs, update club details, and assign or change club presidents.
* **`User Management` (`ADMIN`)**: Verify student accounts (change `USER` to `STUDENT`) or ban accounts.
* **`Coordination Club Setup` (`ADMIN`)**: Create the main coordination club and set its first president.
* **`System Statistics` (`ADMIN`)**: View platform-wide numbers, including total users, club counts, and activity metrics.

---

## C. Screens

> **Note:** All screen mockups are available in the shared Google Drive: [click here](https://drive.google.com/drive/folders/1drf6qMhRZ0CjouPv_Ow3bSoXTAtWCmRu?usp=sharing)

---

## D. Per-Role User Journeys

### D.1 Guest to User Journey
1. **Explore Public Content**: A `GUEST` opens the app and browses `PUBLIC` posts, `PUBLIC` events, and the `Clubs Directory` without logging in.
2. **Sign-up Prompt**: When trying to book a `PUBLIC` event, leave a comment, or access the profile tab, the app prompts the `GUEST` to sign in or register.
3. **Account Creation**: The `USER` signs up with their basic details (name, username, email, and password) and logs in.
4. **Active Engagement**: Now logged in as a `USER`, they can:
* Book spots for `PUBLIC` events and view them in their personal `My Calendar`.
* Write comments on `PUBLIC` posts and events.
* Edit `User Profile` details and update their profile picture.

### D.2 Student Journey
1. **Explore Student Content**: A verified `STUDENT` logs in and browses `PUBLIC` and `STUDENTS_ONLY` posts and events on the `Home Feed`.
2. **Engage and Book**: The `STUDENT` comments on posts and events, and books spots for `STUDENTS_ONLY` events, which appear in their personal `My Calendar`.
3. **Browse and Request to Join**: The `STUDENT` browses the `Clubs Directory`, views club details and staff, and submits a join request to a club.
4. **Join the Club**: Once someone with `Member Management` privileges approves the request, the `STUDENT` becomes a `MEMBER` in that specific club, unlocking access to that club's `MEMBERS_ONLY` posts and events.

### D.3 Club President Journey
1. **Take the Lead**: The `Coordination Club` assigns a `STUDENT` as `CLUB_PRESIDENT` when creating a new club or transferring leadership, unlocking full management access for that specific club.
2. **Set Up the Club**: The `CLUB_PRESIDENT` opens the `Club Dashboard`, updates the club description, uploads a club logo, and views the member list.
3. **Build the Team**: The `CLUB_PRESIDENT` creates custom permission sets (`ClubProfile`) and assigns them to selected members, promoting them to `ASSISTANT_MEMBER`s.
4. **Publish and Organize**: The `CLUB_PRESIDENT` posts club news and creates events, choosing whether content is visible to `PUBLIC`, `STUDENTS_ONLY`, or `MEMBERS_ONLY`.
5. **Manage Members**: The `CLUB_PRESIDENT` reviews pending join requests, accepts new members, and removes members when necessary.

### D.4 Assistant Member Journey
1. **Receive Responsibilities**: A `MEMBER` is given specific permissions by the `CLUB_PRESIDENT` (directly or through a role profile) and becomes an `ASSISTANT_MEMBER`.
2. **Access the Dashboard**: The `ASSISTANT_MEMBER` can now open the `Club Dashboard`, seeing only the tools granted to them.
3. **Execute Assigned Duties**: Depending on their assigned permissions, the `ASSISTANT_MEMBER` can:
* Create, update, or remove club posts and events.
* View attendee lists for club events.
* Review pending join requests and manage member approvals or removals.
4. **Stay Involved**: Alongside their staff duties, the assistant continues to browse `MEMBERS_ONLY` updates and participate as an active club member.

### D.5 Club Member Journey
1. **Welcome to the Club**: Once their join request is approved, the student officially becomes a `MEMBER` of that specific club.
2. **Access Exclusive Content**: The `MEMBER` gains immediate access to the club’s `MEMBERS_ONLY` posts and events in their feed.
3. **Engage and Attend**: The `MEMBER` comments on `MEMBERS_ONLY` posts and events, and books spots for `MEMBERS_ONLY` events.
4. **Track Activities**: Booked club events automatically appear in the member's personal `My Calendar`, keeping them up to date with club schedules.

### D.6 Coordination Club President Journey
1. **Appointment**: At the start of the school year, the `ADMIN` appoints a student as the `CLUB_PRESIDENT` of the `Coordination Club`, granting them leadership over the main school club.
2. **Build the Team**: The `CLUB_PRESIDENT` adds team members and prepares custom permission sets for the staff.
3. **Grant Special Permissions**: Along with standard permissions (like posting updates or creating events), the `CLUB_PRESIDENT` can grant unique, school-level privileges—such as creating clubs and managing club presidents—turning a staff member into a `CLUBS RESPONSIBLE`.
4. **Publish Campus Announcements**: The `CLUB_PRESIDENT` creates posts and events that reach the entire student body or the general public.

### D.7 Coordination Assistant Member / Clubs Responsible Journey
1. **Receive Responsibilities**: A member of the `Coordination Club` is assigned special administrative permissions by their President, becoming a `CLUBS RESPONSIBLE`.
2. **Verify Students**: They review and verify student accounts, upgrading them from `USER` to `STUDENT` so they can access school-restricted features.
3. **Manage School Clubs**: With their club-management permissions, they create new clubs, update club details, and assign or change club presidents across the school.
4. **Handle Team Duties**: Depending on other permissions granted to them, they can also publish announcements and organize events for the `Coordination Club`.

### D.8 Admin Journey
1. **Dedicated Access**: The `ADMIN` logs in with dedicated administrator credentials, operating strictly as a platform manager rather than a user.
2. **Set Up Coordination Leadership**: At the start of the school year, the `ADMIN` initializes the `Coordination Club` and appoints or changes its President.
3. **Account Security**: The `ADMIN` assists with account recovery and bans or removes accounts that violate school rules.

## E. Business Rules

The system should enforces a set of clear rules:

- A student can be a `MEMBER` or `ASSISTANT_MEMBER` of multiple clubs, but `CLUB_PRESIDENT` of **at most one club**.