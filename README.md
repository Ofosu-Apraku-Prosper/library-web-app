# Library Management System — Web Edition

A Spring Boot + Thymeleaf rewrite of the desktop app: login, book inventory,
borrowing/returning with fine calculation, and an admin dashboard — all in
the browser, deployable to a public URL.

## Run it locally first (recommended before deploying)

1. Run `library.sql` in MySQL Workbench against your local MySQL server.
2. Edit `src/main/resources/application.properties` if your local MySQL
   username/password differ from the defaults (root / yourpassword).
3. From the project folder:
   ```
   mvn spring-boot:run
   ```
4. Open http://localhost:8080 in your browser.

## Deploying to Railway (public URL)

### 1. Push this project to GitHub
Create a new GitHub repo and push this folder to it (GitHub Desktop or
`git init && git add . && git commit -m "Initial commit"` and follow
GitHub's instructions to push).

### 2. Create a Railway project
Go to railway.app, sign in with GitHub, click **New Project** →
**Deploy from GitHub repo** → pick your repo. Railway detects the
`pom.xml` and builds it automatically — no Dockerfile needed.

### 3. Add a MySQL database
In the same Railway project, click **New** → **Database** → **Add MySQL**.
Railway spins up a MySQL instance and shows you its connection variables
(host, port, user, password, database) under that service's **Variables** tab.

### 4. Connect your app to the database
Click on your **web service** (not the MySQL one) → **Variables** tab →
add these three, referencing the MySQL service's variables (Railway
autocompletes these with `${{...}}` syntax when you start typing `$`):

```
SPRING_DATASOURCE_URL      = jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME = ${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD = ${{MySQL.MYSQLPASSWORD}}
```

### 5. Run the schema against the Railway database
Click the MySQL service → **Data** tab → open its query console (or connect
with MySQL Workbench using the connection details Railway shows you) and
run `library.sql` there — same script, just pointed at the cloud database.

### 6. Get your public URL
Back on your web service, go to **Settings** → **Networking** → **Generate
Domain**. Railway gives you a URL like `yourapp.up.railway.app` — open it,
you should see the login page.

### 7. Make yourself an admin
Register an account through the live site, then run in the Railway MySQL
query console:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```
Log out and back in — the Admin Dashboard, Inventory, and Users links appear.

## Notes
- Every push to your GitHub repo's main branch automatically redeploys
  on Railway.
- Passwords are stored as SHA-256 hashes, never plain text.
- Fine calculation: $0.50/day overdue, same as the desktop version.
