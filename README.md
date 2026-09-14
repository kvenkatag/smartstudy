# SmartStudyApp

This Spring Boot app serves a lightweight static front-end for school-board practice tests, technical modules, and MPC-EAMCET mock assessments.

## Deploy to Render for public use

This project already includes deployment-ready files for Render:
- Dockerfile
- render.yaml
- application.properties configured to read PORT from the environment

### Recommended Render setup
1. Push this project to a GitHub repository.
2. Sign in to Render at https://render.com.
3. Click New > Web Service.
4. Select your repository and branch.
5. Use these settings:
   - Name: smartstudypractice
   - Runtime: Docker
   - Plan: Free
6. Render will automatically detect the Dockerfile and build the app.
7. After deployment, Render gives you a public URL that you can share with students.

### Native Maven option (if you prefer not to use Docker)
- Build command: mvn -DskipTests package
- Start command: java -Dserver.port=$PORT -jar target/*.jar

## Notes
- The app listens on the PORT environment variable automatically.
- The front-end pages live in src/main/resources/static.
- REST API endpoints include /api/subjects and /api/questions/{subject}.
- The app stores recent history and leaderboard data in the browser using localStorage.

## Local run
```bash
mvn spring-boot:run
```
Then open:
- http://localhost:8080

## Public URL after deployment
Render provides a shareable public URL after the service is live. You can send that link to students for free on the Render free tier.

