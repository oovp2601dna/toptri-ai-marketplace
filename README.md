# Toptri – AI Marketplace Recommendation

Toptri is a simple AI-based food recommendation app using:
- Java Spring Boot (backend)
- Firebase Firestore (database)
- HTML, CSS, JavaScript (frontend)

Users can search food using natural language and get recommendations based on price, speed, sweetness, and simplicity.

---

## Features

- Natural language search (e.g. "nasi padang max 17000")
- Priority-based ranking:
  - cheapest
  - fastest
  - sweet
  - simple
- Best Offer Now
- Firebase Firestore integration
- BUY button with alert confirmation

---

## Requirements

- Java JDK 17+
- Maven
- Firebase project with Firestore enabled
- Git
- Vanilla JavaScript
- Spring Boot 3

---

## Firebase Setup

1. Create a Firebase Service Account (JSON)
2. Place the file here:
backend/src/main/resources/firebase-service-account.json
3. This file is ignored by Git (do not upload to GitHub)

---

## How to Run

## Project Structure
```text
toptri/
│
├── backend/ # Java Spring Boot backend
│ ├── src/main/java/com/toptri
│ ├── src/main/resources
│ └── pom.xml
│
├── frontend/ # Simple HTML/CSS/JS frontend
│ ├── index.html
│ ├── app.js
│ └── style.css
│
├── .gitignore
└── README.md
```
### 1. Run Backend
```
bash
cd backend
mvn spring-boot:run
backend runs on
http://localhost:8080
```
--- 
### 2. Run Frontend
```
cd frontend
python -m http.server 5500
Open browser:
http://localhost:5500
```



### Example Commands
```
- nasi padang
- something sweet
- nasi padang max 17000
- best offer now
- best offer in nasi padang
```
### Notes
```
- Backend must be running before frontend
- If frontend shows Failed to fetch, backend is not running
- Change API URL in frontend/app.js if backend port changes
- Firebase service account key is ignored using .gitignore
```

