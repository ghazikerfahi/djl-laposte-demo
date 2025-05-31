# DJL Image Classification - Spring Boot + Vue.js

This project demonstrates how to **train and use an image classification model** using:
- **Spring Boot + DJL (Deep Java Library)** for backend training and prediction
- **Vue.js + Vite** for a simple user interface
- **PyTorch Engine**

---

## Project Structure

```
djl-image-laposte-demo/
├── backend/              # Spring Boot API (training + prediction)
└── frontend/             # Vue.js client

---

## 1. Requirements

- Java 17
- Maven
- Node.js (v18+ recommended)
- npm

---

## 2. Dataset Setup

backend/src/main/resources/dataset/
├── cats/
│   ├── cats_0.jpg
│   └── ...
└── dogs/
    ├── dogs_0.jpg
    └── ...
```

---

## 3. Running the Backend (Spring Boot)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### API Endpoints

| Method | Endpoint                    | Description               |
|--------|-----------------------------|---------------------------|
| POST   | `/api/image/classify`       | Predict image class       |
| POST   | `/api/image/customClassify` | Predict custom image class|

---

## 4. Running the Frontend (Vue.js + Vite)

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 5. Example Prediction (via `curl`)

```bash
curl -X POST http://localhost:8080/api/image/classify -F image=@path/to/image.jpg
```

---

## Notes

- The trained model is saved under `backend/trained_model/`
- You must **train** the model at least once before prediction works
```bash
mvn compile exec:java -Dexec.mainClass="com.laposte.djl.service.TrainLaPosteModel"
```
---

## Optional Enhancements

- Upload images from Vue.js to backend for training
- Display prediction results visually
- Dockerize the backend + frontend

