# Render Deployment Guide: Web Service + Managed PostgreSQL

This guide provides complete instructions to deploy the **Hostel Room Allocation and Vacancy Registration System (Java 21 Spring Boot Monolith)** on **Render** (https://render.com) using a **Render Web Service** and **Render Managed PostgreSQL Database**.

---

## 🚀 Key Production Features Configured for Render

* **PostgreSQL Support**: Official PostgreSQL JDBC driver integrated with Hibernate PostgreSQL dialect.
* **Smart URL Auto-Parser (`DatabaseConfig`)**: Automatically converts Render's `postgres://` / `postgresql://` connection strings into standard JDBC format (`jdbc:postgresql://...`) with full credentials parsing and SSL support.
* **Auto Schema Generation & Seeding**: Spring Data JPA automatically creates and updates PostgreSQL tables on startup (`spring.jpa.hibernate.ddl-auto=update`), and seeds the sample admin, students, and hostel blocks.
* **Render Free Tier RAM Protection**: JVM tuned with `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` to prevent memory exhaustion on Render's 512MB RAM limit.
* **Reverse Proxy / HTTPS Support**: Configured with `server.forward-headers-strategy=framework` to seamlessly handle Render's SSL/TLS termination.
* **Dynamic Port Binding**: Application automatically reads `${PORT:8080}` assigned by Render.

---

## 📋 Method 1: 1-Click Render Blueprint (Recommended)

Render Blueprints allow you to provision both the PostgreSQL database and the Web Service together in one click using the included [`render.yaml`](./render.yaml).

### Step 1: Push Repository to GitHub
If you haven't already, push this project to a GitHub repository:
```bash
git init
git add .
git commit -m "Configure project for Render Web Service + PostgreSQL"
git remote add origin https://github.com/your-username/hostel-room-allocation-java.git
git branch -M main
git push -u origin main
```

### Step 2: Deploy Blueprint on Render
1. Sign in to your [Render Dashboard](https://dashboard.render.com).
2. Click **New +** in the top navigation bar and select **Blueprint**.
3. Connect your GitHub account and select your repository (`hostel-room-allocation-java`).
4. Render will inspect [`render.yaml`](./render.yaml) and automatically detect:
   * **Web Service**: `hostel-room-allocation` (Docker environment)
   * **PostgreSQL Database**: `hostel-postgres-db` (Database: `hostel_db`, User: `hostel_user`)
5. Click **Apply**.
6. Render will:
   1. Provision the PostgreSQL database.
   2. Build the Docker container with Maven & Java 21.
   3. Automatically wire `DATABASE_URL` from the database into the web service.
   4. Launch the application!

---

## 🛠️ Method 2: Manual Dashboard Setup (Step-by-Step)

If you prefer setting up the services manually via the Render UI:

### Step 1: Create the Managed PostgreSQL Database
1. In the Render Dashboard, click **New +** &rarr; **PostgreSQL**.
2. Fill in the details:
   * **Name**: `hostel-postgres-db`
   * **Database**: `hostel_db`
   * **User**: `hostel_user`
   * **Region**: Choose the closest region (e.g., `Oregon (US West)` or `Frankfurt (EU)`)
   * **Plan**: `Free`
3. Click **Create Database**.
4. Once created, scroll down to the **Connections** section and copy the **Internal Database URL** (e.g. `postgres://hostel_user:password@dpg-xxxxxx-a:5432/hostel_db`).
   *(Note: Internal Database URLs use Render's internal private network for free, ultra-fast bandwidth).*

### Step 2: Create the Web Service
1. In the Render Dashboard, click **New +** &rarr; **Web Service**.
2. Select **Build and deploy from a Git repository** and pick your repo.
3. Configure service settings:
   * **Name**: `hostel-room-allocation`
   * **Region**: **Must match the region of your database** (e.g., `Oregon (US West)`).
   * **Branch**: `main`
   * **Runtime**: **Docker** (Render will automatically detect the root [`Dockerfile`](./Dockerfile)).
   * **Plan**: `Free`

### Step 3: Add Environment Variables
Scroll down to the **Environment Variables** section and add:

| Key | Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `postgres` | Activates PostgreSQL configuration profile |
| `DATABASE_URL` | `postgres://hostel_user:...` | Paste the **Internal Database URL** copied from Step 1 |

*(Optional: You can also use Render's "Add from Database" button to automatically link the database URL without copying & pasting).*

### Step 4: Click Deploy
Click **Create Web Service**. Render will pull the repository, build the Docker container using JDK 21, run database migrations, and boot Apache Tomcat.

Your web service will be live at:
`https://hostel-room-allocation.onrender.com` (or your assigned Render subdomain).

---

## 🔑 Default Login Accounts

On initial startup, `DataInitializer` automatically creates the following default accounts:

### 1. Chief Warden (Administrator)
* **Email**: `admin@hostel.edu`
* **Password**: `Admin@123`
* **Role**: `ROLE_ADMIN`
* **Permissions**: Manage room allocations, approve/reject student applications, create and update hostel rooms and blocks, view all students and occupancy history.

### 2. Sample Students
* **Student 1**:
  * **Email**: `alex.student@hostel.edu`
  * **Password**: `Student@123`
  * **Student ID**: `STU2024001`
* **Student 2**:
  * **Email**: `sarah.student@hostel.edu`
  * **Password**: `Student@123`
  * **Student ID**: `STU2024002`
* **New Registration**: Any student can register directly at `/auth/student-register`.

---

## 💡 Render Free Tier Notes & Optimization Tips

1. **Spin-down / Inactivity Sleep**:
   * Render Free Web Services spin down after 15 minutes of inactivity to conserve resources.
   * When a new request arrives, it may take 30–50 seconds to cold-start. Once awake, the application is fast and responsive.
   * If you need 24/7 continuous instant responses, you can use a free uptime monitor (e.g. UptimeRobot or Cron-job.org) hitting `https://<your-service>.onrender.com/` every 10 minutes.

2. **Database Expiration**:
   * Render Free PostgreSQL databases remain active for 30 days. To keep data permanently for long-term production, upgrade to Render's starter tier ($7/mo) or export backups periodically.

3. **Database Connection Limits**:
   * Render Free PostgreSQL allows up to 20 concurrent connections.
   * This project is preconfigured with HikariCP `maximum-pool-size=5`, leaving plenty of connection headroom.

4. **HTTPS & Domain Names**:
   * Render automatically provides free managed SSL certificates (`https://...`). Custom domains can be added under the Web Service **Settings** tab.
