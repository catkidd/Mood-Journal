# MoodJournal – Tutorial

A step-by-step guide covering local development setup and hosting on [Render](https://render.com).

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Running Locally](#running-locally)
   - [1. Clone the Repository](#1-clone-the-repository)
   - [2. Set Up MySQL](#2-set-up-mysql)
   - [3. Configure the Application](#3-configure-the-application)
   - [4. Run with Maven](#4-run-with-maven)
   - [5. Run with Docker (optional)](#5-run-with-docker-optional)
3. [Hosting on Render](#hosting-on-render)
   - [1. Set Up an External MySQL Database](#1-set-up-an-external-mysql-database)
   - [2. Push Your Code to GitHub](#2-push-your-code-to-github)
   - [3. Create a Web Service on Render](#3-create-a-web-service-on-render)
   - [4. Add Environment Variables](#4-add-environment-variables)
   - [5. Deploy](#5-deploy)
4. [Environment Variables Reference](#environment-variables-reference)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Make sure you have the following installed before starting:

| Tool | Version | Download |
|---|---|---|
| Java (JDK) | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Git | any | https://git-scm.com |
| Docker *(optional)* | any | https://www.docker.com/get-started |

---

## Running Locally

### 1. Clone the Repository

```bash
git clone https://github.com/catkidd/Mood-Journal.git
cd Mood-Journal
```

### 2. Set Up MySQL

Start MySQL and create the database. The app will auto-create the tables on first run thanks to `spring.jpa.hibernate.ddl-auto=update`.

```sql
-- Run this in your MySQL client or workbench
CREATE DATABASE moodjournal;
```

> **Tip:** If your MySQL root password is different from `root`, note it down — you'll need it in the next step.

### 3. Configure the Application

The app reads credentials from **environment variables** with sensible local defaults. You have two options:

**Option A – Set environment variables in your shell (recommended)**

```bash
# Windows (Command Prompt)
set DB_URL=jdbc:mysql://localhost:3306/moodjournal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
set DB_USERNAME=root
set DB_PASSWORD=your_password

# Windows (PowerShell)
$env:DB_URL="jdbc:mysql://localhost:3306/moodjournal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"

# macOS / Linux
export DB_URL="jdbc:mysql://localhost:3306/moodjournal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

**Option B – Edit `application.properties` directly**

Open `src/main/resources/application.properties` and update the defaults:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/moodjournal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

> ⚠️ Do **not** commit real passwords to Git. Use Option A or a `.env` file excluded by `.gitignore`.

### 4. Run with Maven

```bash
# From the project root
mvn spring-boot:run
```

Once started, open your browser and go to:

```
http://localhost:8080
```

To stop the app press `Ctrl + C`.

#### Building a standalone JAR

```bash
mvn clean package -DskipTests

# Run the produced JAR
java -jar target/mood-journal-0.0.1-SNAPSHOT.jar
```

### 5. Run with Docker (optional)

Make sure Docker Desktop is running, then:

```bash
# Build the image
docker build -t mood-journal .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/moodjournal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_password \
  mood-journal
```

> On Windows/macOS, `host.docker.internal` resolves to your local machine so the container can reach your local MySQL. On Linux, use your machine's local IP instead.

Visit `http://localhost:8080` once the container is up.

---

## Hosting on Render

Render is a cloud platform that can deploy your Docker-based Spring Boot app. The free tier works for demos but **spins down after 15 minutes of inactivity**.

### 1. Set Up an External MySQL Database

Render does not provide a managed MySQL service. Use one of these free-tier external providers:

| Provider | Free Tier | Notes |
|---|---|---|
| [**Aiven**](https://aiven.io) | 1 node, 5 GB | Easiest setup, no credit card needed |
| [**PlanetScale**](https://planetscale.com) | 5 GB, 1 billion row reads/mo | Generous limits |
| [**Railway**](https://railway.app) | $5 credit/month | Very simple UI |

After creating your MySQL instance on any of these platforms, note down:
- **Host** (e.g. `mysql-abc123.aivencloud.com`)
- **Port** (usually `3306` or a custom port)
- **Database name**
- **Username**
- **Password**

Your connection URL will look like:

```
jdbc:mysql://<host>:<port>/<database>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

> **Aiven note:** Aiven requires SSL. Make sure `useSSL=true` is in your connection URL.

### 2. Push Your Code to GitHub

If you haven't already, push your code (including the `Dockerfile` and `.dockerignore`) to GitHub:

```bash
git add Dockerfile .dockerignore
git commit -m "chore: add Dockerfile and .dockerignore for Render deployment"
git push origin main
```

### 3. Create a Web Service on Render

1. Go to [render.com](https://render.com) and sign in (or create a free account).
2. Click **New +** → **Web Service**.
3. Click **Connect a repository** and authorize GitHub if prompted.
4. Select your **Mood-Journal** repository.
5. Fill in the service settings:

   | Field | Value |
   |---|---|
   | **Name** | `mood-journal` (or any name you like) |
   | **Region** | Choose the closest to you |
   | **Branch** | `main` |
   | **Runtime** | **Docker** ← important |
   | **Instance Type** | Free (or Starter for always-on) |

6. Leave **Dockerfile Path** as `./Dockerfile` (auto-detected).

### 4. Add Environment Variables

In the Render service settings, scroll down to **Environment Variables** and add:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://<host>:<port>/<dbname>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | your database username |
| `DB_PASSWORD` | your database password |

> Render automatically injects a `PORT` environment variable. Your `application.properties` already handles this with `server.port=${PORT:8080}` ✅

### 5. Deploy

1. Click **Create Web Service**.
2. Render will pull your code from GitHub and build the Docker image. This takes **3–5 minutes** on the first deploy.
3. Watch the **Logs** tab — a successful startup looks like:

   ```
   Started MoodJournalApplication in X.XXX seconds
   ```

4. Once deployed, Render assigns you a public URL like:
   ```
   https://mood-journal-xxxx.onrender.com
   ```

5. Every `git push` to your `main` branch will automatically trigger a new deployment. 🎉

---

## Environment Variables Reference

| Variable | Default (local) | Description |
|---|---|---|
| `PORT` | `8080` | HTTP port the server listens on. Render sets this automatically. |
| `DB_URL` | `jdbc:mysql://localhost:3306/moodjournal?...` | Full JDBC connection string |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | `root` | Database password |

---

## Troubleshooting

**`java.sql.SQLException: Access denied for user`**
→ Your `DB_USERNAME` or `DB_PASSWORD` env var is wrong. Double-check in the Render dashboard.

**`Communications link failure` / `Connection refused`**
→ The app cannot reach the database. Verify:
- Your database host/port is correct in `DB_URL`
- The database provider allows external connections (check firewall/allowlist settings)

**App loads but shows a blank/error page**
→ Check the Render **Logs** tab for stack traces. Usually a missing env var or Hibernate schema error.

**Render free tier: app takes 30+ seconds to respond**
→ This is expected — the free tier spins down the container after 15 minutes of inactivity. Upgrade to **Starter** ($7/mo) for always-on hosting.

**`createDatabaseIfNotExist=true` not working on Aiven/PlanetScale**
→ Cloud MySQL providers usually don't allow auto-creating databases. Create the database manually in their dashboard first, then reference it in `DB_URL`.
