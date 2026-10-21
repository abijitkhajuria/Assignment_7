# CricPulse — Comprehensive Viva Questions & Answers
## Web Technology & Full-Stack Java (Spring Boot, JPA, MySQL, REST, SSE)

This guide contains frequently asked viva questions, oral exam topics, and architectural design inquiries with in-depth technical answers tailored for **CricPulse — Live Cricket Score Management System**.

---

## Category 1: Spring Boot & Framework Architecture

### Q1. What is Spring Boot, and why was it chosen for CricPulse over traditional Spring MVC?
**Answer:**  
Spring Boot is an opinionated, production-ready extension of the Spring framework. In CricPulse, it was chosen because:
1. **Auto-Configuration**: Automatically configures DataSource, Hibernate JPA, and Jackson serialization without verbose XML or manual Java `@Bean` declarations.
2. **Embedded Servlet Container**: Ships with an embedded Tomcat 10 server on port 8080, allowing the application to run as a self-contained JAR (`mvn spring-boot:run`) without needing external application servers.
3. **Starter Dependencies**: Standard starters (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`) resolve transitively compatible dependency versions, eliminating dependency conflicts.

### Q2. Explain the layered architecture used in CricPulse.
**Answer:**  
CricPulse follows the industry-standard **Controller-Service-Repository** pattern:
- **Presentation / Controller Layer (`com.cricpulse.controller`)**: Handles HTTP requests, performs input validation via `@Valid`, and delegates business tasks to the service layer. Exposes REST endpoints and Swagger documentation.
- **Service Layer (`com.cricpulse.service`)**: Encapsulates business logic, including cricket scoring calculations, legal ball validation, strike rotation, undo logic, and transactional boundaries (`@Transactional`).
- **Data Access / Repository Layer (`com.cricpulse.repository`)**: Interfaces extending `JpaRepository` providing CRUD operations and derived query methods (e.g. `findByStatusInOrderByMatchDateDesc`).
- **Entity Layer (`com.cricpulse.entity`)**: JPA entities mapping directly to MySQL relational tables.
- **DTO Layer (`com.cricpulse.dto`)**: Data Transfer Objects preventing entity over-exposure and decoupling database models from client representations.

### Q3. What is Dependency Injection (DI) and how is it implemented in CricPulse?
**Answer:**  
Dependency Injection is an implementation of Inversion of Control (IoC) where object dependencies are provided by the Spring IoC container rather than created directly using the `new` keyword.  
In CricPulse, **Constructor-based Dependency Injection** is utilized across all controllers and services (e.g., `ScoringController(ScoringEngineService scoringEngineService)`). Constructor injection ensures immutability (`final` fields), prevents null pointer exceptions during unit testing, and makes dependencies explicit.

### Q4. What is Spring Boot Actuator, and what purpose does it serve in this project?
**Answer:**  
Spring Boot Actuator exposes production-ready endpoints for operational monitoring. In CricPulse, accessing `/actuator/health` displays the health status of the application, verifying whether the embedded web server, disk space, and MySQL database connection pool (HikariCP) are active (`UP`).

---

## Category 2: REST APIs & HTTP Protocol

### Q5. What are the key constraints of a RESTful API?
**Answer:**  
Representational State Transfer (REST) adheres to six architectural constraints:
1. **Client-Server Architecture**: Separation of UI concerns from data storage concerns.
2. **Statelessness**: Every request contains all necessary context; the server stores no client session context across REST calls.
3. **Cacheability**: Responses explicitly declare whether they can be cached.
4. **Uniform Interface**: Resource identification via URIs (e.g. `/api/matches/1`), manipulation via representations (JSON), and self-descriptive messages.
5. **Layered System**: Clients cannot detect whether they are connected directly to the end server or an intermediary proxy.
6. **Code on Demand (Optional)**: Server can extend client functionality by transferring executable code.

### Q6. Which HTTP methods are used in CricPulse, and what are their semantic meanings?
**Answer:**  
- **`GET`**: Safe, idempotent read operations. Used to retrieve teams (`/api/teams`), live matches (`/api/matches/live`), and player stats (`/api/players/1/statistics`).
- **`POST`**: Non-idempotent resource creation or command execution. Used to record deliveries (`/api/innings/1/deliveries`), undo deliveries (`/api/innings/1/undo-last-delivery`), and schedule matches (`/api/admin/matches`).
- **`PUT`**: Idempotent complete replacement/update of an existing resource (`/api/teams/{id}`).
- **`DELETE`**: Idempotent resource removal (`/api/admin/matches/{id}`).

### Q7. Explain the HTTP status codes returned by CricPulse.
**Answer:**  
- **`200 OK`**: Successful request returning data (e.g. scorecards, summaries).
- **`201 Created`**: Successful creation of a new resource (e.g., newly recorded `Delivery` or created `CricketMatch`).
- **`204 No Content`**: Successful execution returning no content (e.g. team deletion).
- **`400 Bad Request`**: Validation errors (e.g., striker and non-striker are identical, or negative runs).
- **`404 Not Found`**: Resource requested by ID does not exist in the database (e.g., unknown player or match ID).
- **`422 Unprocessable Entity`**: Semantic cricket rule violations (e.g., attempting to score a delivery in an already completed innings).
- **`500 Internal Server Error`**: Unexpected system or server exceptions.

### Q8. How does CricPulse implement centralized exception handling?
**Answer:**  
Via `@RestControllerAdvice` in `GlobalExceptionHandler.java`. Custom exceptions like `ResourceNotFoundException`, `InvalidScoringException`, and `BadRequestException` are annotated with `@ExceptionHandler`. When thrown, Spring intercepts them and formats an RFC-compliant JSON response with a timestamp, HTTP status code, error type, and descriptive message.

---

## Category 3: Spring Data JPA, Hibernate & MySQL Database

### Q9. What is the difference between JPA and Hibernate?
**Answer:**  
- **JPA (Jakarta Persistence API)** is a Java specification that defines standard interfaces and annotations (`@Entity`, `@Table`, `@Id`, `@ManyToOne`, `EntityManager`) for Object-Relational Mapping.
- **Hibernate** is the actual ORM library implementation of the JPA specification used under the hood by Spring Boot to generate SQL queries, manage the persistence context, and map Java objects to relational tables.

### Q10. What cascade types and fetch types are used in CricPulse?
**Answer:**  
- **`FetchType.EAGER`** is used on entities like `Delivery` and `Innings` where related entities (Team, Striker, Bowler) are constantly required in UI scorecards, preventing `LazyInitializationException`.
- **Foreign Key Constraints (`ON DELETE CASCADE`)**: Configured in database DDL (`schema.sql`) so that deleting a match cleanly cascades to associated innings, deliveries, and score records.

### Q11. Why is the database normalized to 3NF in CricPulse?
**Answer:**  
If batting scores or over counts were stored as arbitrary unstructured text in a single table:
1. Updating a delivery would require error-prone string manipulation.
2. Calculating career averages across multiple matches would require parsing text.
3. Modifying a player's name would cause data anomalies across multiple match logs.  
By normalizing into separate `teams`, `players`, `matches`, `innings`, `deliveries`, `batting_scores`, and `bowling_scores` tables:
- Every entity has a primary key.
- Transitive dependencies are removed.
- Historical deliveries can be replayed or undone while keeping individual score figures synchronized.

### Q12. What is the purpose of `@Transactional` in `ScoringEngineService`?
**Answer:**  
Recording a cricket ball is a multi-step operation: saving the `Delivery` record, updating innings runs, updating legal ball count, updating striker runs, updating bowler runs conceded, checking for wickets, and logging fall of wickets.  
The `@Transactional` annotation guarantees **ACID properties (Atomicity, Consistency, Isolation, Durability)**. If any failure occurs midway (e.g., database constraint violation), all changes in that transaction are automatically rolled back, preventing partial or corrupted cricket scores.

---

## Category 4: Real-Time Updates: Server-Sent Events (SSE) vs Polling vs WebSockets

### Q13. What are Server-Sent Events (SSE), and how do they work in CricPulse?
**Answer:**  
Server-Sent Events (SSE) is an HTTP standard (`text/event-stream`) allowing a server to push real-time data down an open HTTP connection to browser clients.  
In CricPulse:
1. The browser initiates a connection to `/api/matches/{id}/live-stream` using the native JavaScript `EventSource` API.
2. The Spring Boot backend registers an `SseEmitter` instance inside `SseBroadcastService`.
3. When the admin records a delivery in `ScoringEngineService`, the service calls `sseBroadcastService.broadcast(matchId, snapshot)`.
4. The event is pushed to all subscribed clients, triggering UI updates without requiring page refreshes.

### Q14. How does SSE compare to WebSockets and Short Polling?
**Answer:**  
| Feature | Short Polling | Server-Sent Events (SSE) | WebSockets |
|---|---|---|---|
| **Direction** | Bidirectional (repetitive client requests) | Unidirectional (Server to Client) | Full Duplex Bidirectional |
| **Protocol** | Standard HTTP | Standard HTTP (`text/event-stream`) | WS / WSS (Protocol Upgrade) |
| **Overhead** | High (constant HTTP headers) | Low (single persistent HTTP connection) | Very Low |
| **Proxy / Firewall Compatibility** | Excellent | Excellent (standard HTTP/1.1 or HTTP/2) | Can require special proxy configuration |
| **Ideal Use Case** | Infrequent updates | Live sports scores, stock tickers, news feeds | Real-time chat, multiplayer gaming |

*Note: For a cricket score viewer, score data flows exclusively from server to client. SSE is more lightweight and simpler than WebSockets while offering instant updates compared to polling.*

### Q15. Why does CricPulse include a fallback polling mechanism alongside SSE?
**Answer:**  
In real-world networks, corporate firewalls or older mobile browsers may drop persistent HTTP streams or buffer streaming responses. CricPulse's client (`api.js`) monitors the `EventSource` connection. If the SSE stream fails or triggers an error, the client gracefully transitions to **3-second polling** using `fetch('/api/matches/{id}/live-snapshot')`, ensuring continuous service without manual user intervention.

---

## Category 5: Frontend Architecture & Vanilla JavaScript

### Q16. Why was Vanilla JavaScript and Bootstrap 5 chosen instead of React or Angular?
**Answer:**  
1. **Academic Alignment**: Meets Web Technology curriculum goals by demonstrating mastery of fundamental HTML5, CSS3, DOM manipulation, and ES6+ JavaScript.
2. **Zero Build Overhead**: Eliminates complex Node.js build pipelines, webpack/Vite configuration, or heavy `node_modules` bundles.
3. **Instant Serving**: The frontend files reside directly in `src/main/resources/static/` and are served by the embedded Tomcat server on port 8080.
4. **Fast Performance**: Sub-second page load times with minimal JavaScript footprint.

### Q17. How does the JavaScript Fetch API work, and how are asynchronous operations handled?
**Answer:**  
The Fetch API provides a promise-based interface for making HTTP requests. In CricPulse, asynchronous network calls are managed using modern **`async/await`** syntax wrapped inside `try...catch` blocks in `api.js`:
```javascript
async function request(endpoint, options = {}) {
    const response = await fetch(endpoint, options);
    if (!response.ok) throw new Error('API Error');
    return await response.json();
}
```
This avoids "callback hell" and enables clean linear asynchronous code.

### Q18. How does Chart.js render player statistics?
**Answer:**  
Chart.js utilizes the HTML5 `<canvas>` element to render hardware-accelerated 2D charts. CricPulse renders a **Radar Chart** on the player detail modal, displaying six normalized skill vectors (Batting Power, Boundary Hitting, Strike Rotation, Bowling Control, Economy, Impact Rating) derived from player statistics.

---

## Category 6: Cricket Scoring Engine Rules & Edge Cases

### Q19. How does the scoring engine differentiate between a Wide and a No-Ball?
**Answer:**  
- **Wide**: The ball is out of reach of the batsman. 1 extra run is awarded to the team, the delivery is marked `isLegalDelivery = false`, and the batsman **does not face a ball** (`ballsFaced` is not incremented).
- **No-Ball**: The bowler oversteps or commits a height infringement. 1 extra run is awarded to the team, the delivery is `isLegalDelivery = false`, but the batsman **does face the ball** (`ballsFaced += 1`) and receives credit for any runs scored off the bat.

### Q20. Are Byes and Leg-Byes charged to the bowler?
**Answer:**  
**No.** Under the Laws of Cricket, Byes and Leg-Byes represent fielding or delivery evasion occurrences, not bowling inaccuracies. Therefore, while Byes and Leg-Byes are counted as legal deliveries (`isLegalDelivery = true`) and added to the team's total runs and extras, they are **not charged to the bowler's runs conceded**, protecting the bowler's economy rate.

### Q21. Which dismissals are credited to the bowler, and which are not?
**Answer:**  
- **Credited to Bowler (`bowlerScore.wickets += 1`)**: `BOWLED`, `CAUGHT`, `LBW`, `STUMPED`, `HIT_WICKET`.
- **Not Credited to Bowler**: `RUN_OUT` and `RETIRED_HURT`. (Run-outs are fielding events and do not enhance the bowler's bowling figures).

### Q22. How is strike rotation handled during an innings?
**Answer:**  
1. **During an Over**: Strike rotates whenever an **odd number of runs** (1, 3, 5) are completed by the batsmen running between wickets (or odd extra runs on byes/leg-byes).
2. **At the End of an Over**: When the legal ball count reaches 6 in an over (`legalBalls % 6 == 0`), the over ends. The batsmen switch ends, meaning the non-striker becomes the striker for the first ball of the subsequent over.

### Q23. How does the Undo feature work without corrupting statistics?
**Answer:**  
The `undoLastDelivery` method operates as an atomic reversal of the latest `Delivery` record:
- Innings total runs and legal balls are decremented.
- Batter runs, balls faced, and boundaries (4s, 6s) are deducted.
- Bowler balls bowled, runs conceded, and wickets are deducted.
- If a wicket occurred, the dismissed batsman's `isOut` flag is cleared, and the `FallOfWicket` record is purged.
- The previous striker, non-striker, and bowler pointers are reinstated on the `Innings`.
- The delivery row is deleted from MySQL, and an updated live snapshot is broadcasted via SSE.

### Q24. How is the Required Run Rate (RRR) computed during a chase?
**Answer:**  
$$\text{Remaining Legal Balls} = (\text{Total Overs} \times 6) - \text{Legal Balls Bowled}$$
$$\text{Runs Needed} = \text{Target Runs} - \text{Current Total Runs}$$
$$\text{Required Run Rate (RRR)} = \frac{\text{Runs Needed}}{\text{Remaining Legal Balls} / 6.0}$$
If the chasing team surpasses or equals the target runs, the match terminates immediately, declaring victory by the remaining wickets in hand ($10 - \text{wickets}$).

---

*CricPulse — Comprehensive Viva Preparation Manual | Web Technology 2026*
