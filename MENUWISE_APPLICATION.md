# Understanding `MenuWiseApplication.java`

## 1. What is this file?

[`MenuWiseApplication.java`](file:///e:/MyCodes/MenuWise/src/main/java/com/menuwise/MenuWiseApplication.java) is the **"Start Button"** (or ignition key) of the entire project.

```java
package com.menuwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MenuWiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuWiseApplication.class, args);
    }
}
```

Even though it is only **13 lines of code**, running this file starts up the whole backend: it creates the database, starts the web server, loads your seed data, and gets the web pages ready.

---

## 2. Why is the code so short?

Spring Boot does almost all the heavy lifting behind the scenes using **`@SpringBootApplication`**.

That single word actually combines **3 big superpowers**:

1. **Auto-Configuration (`@EnableAutoConfiguration`)**:
   Spring looks at your project libraries and automatically configures everything:
   * Sets up an embedded web server (Tomcat on port `8080`).
   * Connects to the H2 database.
   * Enables the Thymeleaf template engine for HTML pages.

2. **Component Scanner (`@ComponentScan`)**:
   Spring searches through all folders inside `com.menuwise` to find:
   * **Controllers**: files handling web pages and API requests (`@Controller`, `@RestController`).
   * **Services**: files handling business logic like orders and weather calculations (`@Service`).
   * **Repositories**: files that read and write data to the database (`@Repository`).

3. **Configuration Hub (`@SpringBootConfiguration`)**:
   Marks this class as the master configuration file for the app.

---

## 3. What happens when you click "Run"? (Step-by-Step)

```
       [ You click RUN ]
               │
               ▼
1. Loads Configuration
   Reads application.yml and settings.
               │
               ▼
2. Starts the Web Server
   Starts an embedded Tomcat server at http://localhost:8080.
               │
               ▼
3. Creates the Database in Memory
   Starts the H2 SQL database in RAM (no setup required).
               │
               ▼
4. Builds the Tables
   Hibernate checks entities (Item, Ingredient, Order, Category)
   and automatically creates the SQL tables.
               │
               ▼
5. Seeds Demo Data
   DatabaseSeeder inserts sample categories, ingredients, and dishes.
               │
               ▼
6. Ready!
   The app is live and waiting for requests in your browser.
```

---

## 4. What can you visit once it is running?

Open your browser and explore:

| Page / Feature | Browser URL | What it is |
| :--- | :--- | :--- |
| **Dashboard** | [http://localhost:8080/](http://localhost:8080/) | Overview metrics, revenue, and alerts |
| **Point of Sale (POS)** | [http://localhost:8080/pos](http://localhost:8080/pos) | Dish menu and order taking |
| **Live Inventory** | [http://localhost:8080/inventory](http://localhost:8080/inventory) | Stock levels and low-stock alerts |
| **Weather Prep** | [http://localhost:8080/weather](http://localhost:8080/weather) | Smart prep suggestions based on weather |
| **Food Rescue** | [http://localhost:8080/rescue](http://localhost:8080/rescue) | Expiring ingredients and waste mitigation |
| **H2 Database Console** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | Visual SQL database browser (`jdbc:h2:mem:menuwisedb`, user `sa`) |

---

## 5. How to Run It

### Method 1: In the IDE (Easiest)
1. Open [`MenuWiseApplication.java`](file:///e:/MyCodes/MenuWise/src/main/java/com/menuwise/MenuWiseApplication.java).
2. Click **Run** above the `main` method (or press <kbd>F5</kbd>).

### Method 2: In PowerShell Terminal
```powershell
$env:JAVA_HOME = "C:\Users\mtxma\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
.\mvnw.cmd spring-boot:run
```

### Method 3: Run All Tests
```powershell
$env:JAVA_HOME = "C:\Users\mtxma\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
.\mvnw.cmd test
```
