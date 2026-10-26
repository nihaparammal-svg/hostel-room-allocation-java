# Deployment Guide & Platform Architecture Analysis

## 1. Vercel Architecture Limitations for Java Spring Boot

### Why Vercel Cannot Host a Continuous Spring Boot Server Out of the Box:
- **Serverless & Edge Runtime Architecture**: Vercel is purpose-built for static websites and event-driven, short-lived Serverless Functions (Node.js, Edge JavaScript/TypeScript, Python, and Go). It shuts down functions immediately after processing an HTTP request.
- **No Continuous JVM Lifecycle**: A standard Java Spring Boot monolith maintains an active JVM process, persistent thread pool, HikariCP database connection pool, in-memory sessions, and embedded Apache Tomcat HTTP server. Vercel does not offer continuous Linux container hosting for long-running JVM servers.
- **Cold Starts**: If Java were packaged as AWS Lambda / Vercel Serverless community runtimes (e.g., `vercel-java`), cold starts for a traditional Spring Boot application with JPA and Hibernate take 5 to 15 seconds per request, making server-side Thymeleaf rendering impractical.

### Practical Alternatives Without Changing Java, Thymeleaf, or MySQL:
To run this **exact monolithic Java Spring Boot + Thymeleaf + MySQL application** without changing a single line of Java or adding JavaScript:

| Recommended Platform | Free/Low-Cost Tier | Deploy Method | MySQL Support |
| :--- | :--- | :--- | :--- |
| **Railway (railway.app)** | Developer trial available | 1-Click GitHub Deploy or Dockerfile | Built-in managed MySQL database plugin |
| **Render (render.com)** | Free tier available | Web Service from GitHub repo | External MySQL (PlanetScale, Aiven, or Render PostgreSQL) |
| **Fly.io** | Generous free allowances | `fly launch` (via included Dockerfile) | Managed MySQL or Docker container |
| **Docker Compose** | Free (self-hosted / VPS) | `docker compose up -d` | Built-in MySQL 8.0 container (included) |

---

## 2. Option A: Deploying on Railway (Recommended)

Railway runs standard Spring Boot projects directly from your GitHub repository with zero configuration:
1. Push your repository to GitHub.
2. Sign in to [Railway.app](https://railway.app).
3. Click **New Project** &rarr; **Provision MySQL**.
4. Click **New Service** &rarr; **GitHub Repo** &rarr; select `hostel-room-allocation-java`.
5. Under service **Variables**, link Railway's MySQL environment variables:
   - `SPRING_PROFILES_ACTIVE`: `mysql`
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `SPRING_DATASOURCE_USERNAME`: `${{MySQL.MYSQLUSER}}`
   - `SPRING_DATASOURCE_PASSWORD`: `${{MySQL.MYSQLPASSWORD}}`
6. Click **Deploy**. Railway will compile with Maven and launch the application on an assigned `.up.railway.app` URL.

---

## 3. Option B: Deploying on Render (render.com)

1. Create an account on [Render.com](https://render.com).
2. Connect your GitHub repository.
3. Choose **Web Service** with the following settings:
   - **Environment**: `Docker` (Render automatically detects the included `Dockerfile`)
   - **Region**: Closest to your users
   - **Branch**: `main`
4. Under **Environment Variables**, provide your managed MySQL connection:
   - `SPRING_PROFILES_ACTIVE`: `mysql`
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://<your-db-host>:3306/<your-db-name>?useSSL=true`
   - `SPRING_DATASOURCE_USERNAME`: `<your-db-user>`
   - `SPRING_DATASOURCE_PASSWORD`: `<your-db-password>`
5. Click **Create Web Service**.

---

## 4. Option C: Local / VPS Deployment with Docker Compose

Run the entire system (Java Spring Boot + MySQL 8.0) locally or on any Linux VPS in one command:
```bash
docker compose up -d --build
```
- Web Application: http://localhost:8080
- MySQL Database: `localhost:3306` (Database: `hostel_db`, User: `hostel_user`)
