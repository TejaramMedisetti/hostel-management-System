# Hostel Management System

A Spring Boot REST API for running a hostel: students, rooms, bed allocation, fee payments,
complaints, visitor log and an admin dashboard. Secured with JWT and role-based access.

**Stack:** Java 17, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA/Hibernate,
H2 (default) or MySQL, Bean Validation, Swagger UI (springdoc).

## Features
- JWT login with roles: `ADMIN`, `WARDEN`, `STUDENT`
- Student CRUD (a login account is created automatically; username = email)
- Room CRUD with live occupancy / availability
- Room allocation and vacating (blocks double allocation and over-capacity)
- Fee payments (PENDING / PAID) with revenue totals
- Complaints with status tracking (OPEN / IN_PROGRESS / RESOLVED)
- Visitor check-in / check-out
- Dashboard statistics
- Student self-service endpoints under `/api/me/**`
- Global error handling with clean JSON errors

## Run it
```bash
mvn spring-boot:run
```
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console  (JDBC URL `jdbc:h2:mem:hostel`, user `sa`, empty password)

Default admin (seeded on first start): **admin / admin123** - change it before deploying.

### Use MySQL instead
```bash
export DB_USER=root DB_PASSWORD=yourpassword
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
Set `JWT_SECRET` (a Base64 string of 32+ bytes) in production.

## Quick API tour
```bash
# 1. Log in and copy the token
curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
TOKEN=<paste token>

# 2. Add a student
curl -s -X POST localhost:8080/api/students -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"fullName":"Ravi Kumar","email":"ravi@example.com","phone":"9999999999","course":"B.Tech IT","academicYear":2,"password":"ravi123"}'

# 3. See available rooms, then allocate
curl -s "localhost:8080/api/rooms?availableOnly=true" -H "Authorization: Bearer $TOKEN"
curl -s -X POST localhost:8080/api/allocations -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"studentId":1,"roomId":1}'
```

## Endpoints
| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/login` |
| Students | `GET/POST /api/students`, `GET/PUT/DELETE /api/students/{id}` |
| Rooms | `GET/POST /api/rooms`, `GET/PUT/DELETE /api/rooms/{id}` |
| Allocations | `GET/POST /api/allocations`, `PUT /api/allocations/{id}/vacate` |
| Payments | `GET/POST /api/payments`, `PUT /api/payments/{id}/pay`, `GET /api/payments/student/{id}` |
| Complaints | `GET/POST /api/complaints`, `PUT /api/complaints/{id}` |
| Visitors | `GET/POST /api/visitors`, `PUT /api/visitors/{id}/checkout` |
| Dashboard | `GET /api/dashboard` |
| Student self-service | `GET /api/me/profile`, `/allocations`, `/payments`, `/complaints`; `POST /api/me/complaints` |

Admin/Warden can use everything under `/api/**`; students can use `/api/me/**` and read rooms.
