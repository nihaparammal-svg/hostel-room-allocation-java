# Hostel Room Allocation and Vacancy Registration System

A complete, modern, user-friendly College Hostel Management System built strictly with **Java, Spring Boot, Thymeleaf, HTML5, CSS3, and MySQL ONLY**.

> **IMPORTANT STRICT ARCHITECTURAL COMPLIANCE:**
> * **NO JavaScript** used anywhere in the frontend.
> * **NO React, Angular, Vue, Node.js, or Vite**.
> * **100% Server-Side Rendered** with Thymeleaf templates.
> * **Standard HTML forms** for all actions (GET for filtering/searching, POST with CSRF protection for mutations).
> * **Real MySQL persistence** via Spring Data JPA and Hibernate.
> * **Strict Multi-User Isolation**: Student accounts are strictly partitioned; students can never access administrative pages or view another student's applications.

---

## 1. Technology Stack

* **Programming Language**: Java 21 (LTS)
* **Backend Framework**: Spring Boot 3.3.4
* **Template Engine**: Thymeleaf + `thymeleaf-extras-springsecurity6`
* **Styling**: Modern HTML5 + Custom Responsive CSS3 (Blue, White, and Light-Purple theme)
* **Security & Auth**: Spring Security 6 with BCrypt password hashing
* **ORM & Database**: Spring Data JPA, Hibernate, MySQL 8.0 (with H2 zero-setup dev fallback)
* **Build Tool**: Apache Maven (includes Maven Wrapper `mvnw` and `mvnw.cmd`)
* **IDE**: VS Code (preconfigured `.vscode` settings and launch targets)
* **Version Control**: Git / GitHub

---

## 2. Project Directory Structure

```text
hostel-room-allocation-java/
├── .mvn/
│   └── wrapper/
├── .vscode/
│   ├── extensions.json
│   ├── launch.json
│   └── settings.json
├── src/
│   ├── main/
│   │   ├── java/com/college/hostel/
│   │   │   ├── config/
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── CustomAuthenticationSuccessHandler.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   └── StudentController.java
│   │   │   ├── dto/
│   │   │   │   ├── AllocationDto.java
│   │   │   │   ├── ApplicationDto.java
│   │   │   │   ├── ApplicationReviewDto.java
│   │   │   │   ├── RoomDto.java
│   │   │   │   └── StudentRegisterDto.java
│   │   │   ├── entity/
│   │   │   │   ├── Allocation.java
│   │   │   │   ├── Application.java
│   │   │   │   ├── HostelBlock.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── Room.java
│   │   │   │   └── User.java
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── AllocationRepository.java
│   │   │   │   ├── ApplicationRepository.java
│   │   │   │   ├── HostelBlockRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AllocationService.java
│   │   │   │   ├── ApplicationService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── RoomService.java
│   │   │   │   └── UserService.java
│   │   │   └── HostelManagementApplication.java
│   │   └── resources/
│   │       ├── db/migration/
│   │       │   └── schema-mysql.sql
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── style.css
│   │       ├── templates/
│   │       │   ├── admin/
│   │       │   │   ├── allocations.html
│   │       │   │   ├── applications.html
│   │       │   │   ├── dashboard.html
│   │       │   │   ├── room-form.html
│   │       │   │   ├── rooms.html
│   │       │   │   ├── student-history.html
│   │       │   │   └── students.html
│   │       │   ├── auth/
│   │       │   │   ├── admin-login.html
│   │       │   │   ├── login.html
│   │       │   │   ├── student-login.html
│   │       │   │   └── student-register.html
│   │       │   ├── error/
│   │       │   │   ├── 403.html
│   │       │   │   ├── 404.html
│   │       │   │   ├── 500.html
│   │       │   │   └── general.html
│   │       │   ├── fragments/
│   │       │   │   └── layout.html
│   │       │   ├── student/
│   │       │   │   ├── apply.html
│   │       │   │   ├── dashboard.html
│   │       │   │   ├── my-application.html
│   │       │   │   ├── my-room.html
│   │       │   │   ├── notifications.html
│   │       │   │   ├── profile.html
│   │       │   │   └── rooms.html
│   │       │   ├── about.html
│   │       │   ├── contact.html
│   │       │   ├── index.html
│   │       │   └── rooms.html
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-mysql.properties
│   └── test/java/com/college/hostel/
│       ├── HostelApplicationTests.java
│       └── RoomAllocationWorkflowTest.java
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── VERCEL_AND_DEPLOYMENT_GUIDE.md
├── run.bat
└── run.ps1
```

---

## 3. Database Design & Relational Tables

The system is backed by 6 normalized tables in MySQL:

1. **`users`**: Contains student and administrator profiles. Student IDs and emails have unique constraints. Passwords encrypted with BCrypt.
2. **`hostel_blocks`**: Stores residential blocks (e.g., Block A - Boys, Block B - Girls, Block C - Post-Graduate), gender designations, and floor limits.
3. **`rooms`**: Contains room number, floor, capacity, room type (Single, Double, Triple, Dormitory), monthly fee, and facilities. Unique composite constraint on `(block_id, room_number)`.
4. **`applications`**: Tracks student room applications with preferred blocks, room types, special requests, status (`PENDING`, `APPROVED`, `REJECTED`, `WAITLISTED`), and admin remarks.
5. **`allocations`**: Stores active and vacated bed assignments. Links an approved student, room, bed number (e.g., `Bed-1`, `Bed-A`), allocation timestamp, and vacation timestamp.
6. **`notifications`**: Real-time alerts delivered to student dashboards upon application review, bed allocation, and checkout.

### Dynamic Vacancy Calculation Formula
Bed availability is calculated dynamically from relational database state:
$$\text{Available Beds} = \text{Room Capacity} - \text{Active Allocations}$$
Where an active allocation has `status = 'ACTIVE'`. When an allocation is marked `VACATED`, the bed immediately becomes available.

---

## 4. Default Demo Accounts

Upon initial startup, `DataInitializer.java` automatically seeds the database with the following test credentials:

| Role | Name | Identifier / Email | Password | Pre-loaded State |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | Hostel Chief Warden | `admin@hostel.edu` | `Admin@123` | Full administrative console access |
| **Student** | Alex Morgan | `alex.student@hostel.edu` or `STU2024001` | `Student@123` | Has an **Approved** application and **Active Allocation** (Room 101, Bed-1) |
| **Student** | Sarah Connor | `sarah.student@hostel.edu` or `STU2024002` | `Student@123` | Has a **Pending** application under review |

---

## 5. Step-by-Step VS Code Setup

### Prerequisites
1. **JDK 21**: Installed on your machine (e.g. Eclipse Adoptium OpenJDK 21).
2. **VS Code**: With the recommended extension pack `Extension Pack for Java` (`vscjava.vscode-java-pack`).

### Running in VS Code
1. Open VS Code:
   ```bash
   code C:\Users\Niha\.gemini\antigravity\scratch\hostel-room-allocation-java
   ```
2. The preconfigured `.vscode/settings.json` points to Java 21 automatically.
3. Open `src/main/java/com/college/hostel/HostelManagementApplication.java`.
4. Click **Run** or press `F5` (or run `run.bat` / `run.ps1`).
5. Open your browser and navigate to:
   ```text
   http://localhost:8080
   ```

---

## 6. Running with MySQL

By default, the application runs on the `dev` profile using an in-memory MySQL-compatible H2 engine for zero-setup demonstration.

To connect to your local MySQL 8.0 server:
1. Ensure your MySQL server is running on port 3306.
2. Execute the migration script located at `src/main/resources/db/migration/schema-mysql.sql` in MySQL Workbench or the `mysql` CLI:
   ```bash
   mysql -u root -p < src/main/resources/db/migration/schema-mysql.sql
   ```
3. Set your environment variables (or configure `.env`):
   ```bash
   set SPRING_PROFILES_ACTIVE=mysql
   set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hostel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   set SPRING_DATASOURCE_USERNAME=root
   set SPRING_DATASOURCE_PASSWORD=your_mysql_password
   ```
4. Start the application:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

---

## 7. Running with Docker Compose

To launch the complete application with an isolated MySQL 8.0 container in a single command:
```bash
docker compose up -d --build
```
- Web Application: `http://localhost:8080`
- MySQL Database: `localhost:3306`

---

## 8. GitHub Upload Instructions

Run the following commands in the project directory:

```bash
cd C:\Users\Niha\.gemini\antigravity\scratch\hostel-room-allocation-java

# 1. Initialize git repository
git init

# 2. Add all project files
git add .

# 3. Create initial commit
git commit -m "Initial commit: Complete Hostel Management System (Java Spring Boot + Thymeleaf + MySQL)"

# 4. Create repository on GitHub (e.g., https://github.com/your-username/hostel-room-allocation)
# 5. Link and push to main branch
git branch -M main
git remote add origin https://github.com/your-username/hostel-room-allocation.git
git push -u origin main
```

---

## 9. Vercel & Cloud Deployment Summary

* **Vercel Limitation**: Vercel is built for static frontend assets and short-lived serverless functions (Node.js/Python/Go). It cannot host continuous, monolithic Spring Boot applications running an active embedded Tomcat web server.
* **Java Cloud Platforms**: For zero-code 1-click deployment of this Spring Boot + MySQL monolith, use:
  - **Railway.app** (Automated Maven detection + Managed MySQL plugin)
  - **Render.com** (Docker Web Service via included `Dockerfile`)
  - **Fly.io** (`fly launch` using the included `Dockerfile`)
* Full deployment instructions and architecture notes are provided in [VERCEL_AND_DEPLOYMENT_GUIDE.md](VERCEL_AND_DEPLOYMENT_GUIDE.md).

---

## 10. Common Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| `Access Denied (403)` | Student trying to access `/admin/**` or vice-versa | Log in with the appropriate account (`admin@hostel.edu` for admin). |
| `Room is fully booked` | All beds allocated in that room | Select another room or vacate an existing occupant. |
| `Duplicate room in block` | Room number already exists in selected block | Assign a distinct room number per block. |
| `Cannot delete room` | Room has active allocations | Vacate all beds in the room prior to deletion. |
| `Port 8080 already in use` | Another process is using port 8080 | Change `server.port` in `application.properties` (e.g., `server.port=8081`). |
