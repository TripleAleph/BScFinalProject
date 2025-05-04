# 🐾 PAWsitive Life

A smart Android app for dog owners, combining personalized health content, location-based services, and social features to enhance the everyday experience of raising a dog.

---

## 🎓 Final Project – B.Sc. in Computer Science  
**Institution:** Afeka College of Engineering  
**Team Members:** Or Avichzer Elmalih, Noa Danon, Mark Tsirlin.  
**Semester:** Fall 2024

---

> ⚠️ **Note:** This project is currently under active development.  
> The code and features in this repository are not final and may change frequently.

---

## 📱 Overview

**PAWsitive Life** empowers dog owners with intelligent, real-time tools to manage their pet's health and social life.  
It combines modern mobile technologies with AI and geolocation services to offer:

- Tailored articles based on breed, age, and health data
- Smart reminders for vaccines and treatments
- Nearby dog parks and vets via GPS
- Social features like dog friend circles and group purchases *(planned)*

---

## 🔧 Tech Stack

| Layer     | Technologies                            |
|-----------|------------------------------------------|
| Frontend  | Kotlin, Android Studio, XML UI Layout    |
| Backend   | Firebase Realtime DB + Firebase Auth     |
| APIs      | OpenAI API, Google Maps API              |
| Tools     | Git, GitHub, Gradle, Lint, Logcat        |

---

## 🧱 Architecture

The app follows the **MVC (Model-View-Controller)** pattern:

- **Model:** Dog profiles, reminders, articles
- **View:** Android interface (XML layouts)
- **Controller:** Data flow, API interactions, Firebase communication

---

## 📦 Key Components

- `User` – Registers and manages dogs and settings  
- `Dog` – Stores detailed dog profiles  
- `Reminder` – Handles health/vaccine alerts  
- `Article` – Represents AI-generated personalized content  
- `DogPark` – Interfaces with Google Maps for nearby parks  
- `AI Service` – Analyzes dog data and fetches tailored content  
- `API Gateway` – Mediates between mobile app and external services

---

## 🧪 Testing

- Unit tests with **JUnit**
- Manual functional testing during development
- Optional crash monitoring via **Firebase Crashlytics**

---

## 📅 Development Timeline

| Week | Milestone                                      |
|------|------------------------------------------------|
| 1–2  | Setup environment, link Firebase/Auth          |
| 3–4  | Develop backend + integrate OpenAI API         |
| 5–6  | Build UI, implement Google Maps integration    |
| 7    | Debug, optimize, test                          |
| 8    | Package APK, write docs, deliver presentation  |

---

## 💡 Use Cases

- **Dog profile creation:** Add dog name, age, chip ID, vet info, etc.  
- **Health tracking:** Set vaccine dates, receive reminders  
- **Nearby locations:** Find parks/vets using GPS and Maps API  
- **Smart content:** Get custom tips and articles based on your dog  
- **Social features (planned):** Friend circles, group buys, dog chat  

---

## 📖 Sources & Research

The app's content logic is supported by academic literature and behavioral studies on:

- Canine health and development  
- Human-dog interaction  
- Causes and prevention of dog abandonment  
- The impact of walks and socialization  

> See `/docs/literature_review.pdf` for detailed references (if included in repo)

---

## 📜 License

This project is developed for academic purposes only and is not licensed for commercial distribution.

---

## 🙋 Contact

For questions or collaboration:  
📧 oravi528@gmail.com
📧 noadanon220@gmail.com
📧 mark20013009@gmail.com
