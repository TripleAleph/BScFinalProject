# 🐾 PAWsitive Life

An intelligent Android app for dog owners, offering a personalized, location-aware, and user-friendly experience to help manage and improve dog care through reminders, maps, and AI-generated content.

---

## 🎓 Final Project – B.Sc. in Computer Science  
**Institution:** Afeka College of Engineering  
**Team Members:** Or Avichzer Elmalih, Noa Danon, Mark Tsirlin  
**Project Supervisor:** Dr. Sharon Yalov-Handzel  
**Semester:** Fall 2024 – Spring 2025  

---

> ⚠️ **Note:** This is a final-year academic project. Features are stable but may not yet be production-grade.

---

## 📱 Overview

**PAWsitive Life** was developed to simplify dog ownership by centralizing essential care tasks and offering:

- 🐶 Rich dog profile management  
- ⏰ Smart reminders for vet visits and care tasks  
- 🗺️ Real-time map with nearby dog parks, vets, and pet stores  
- 🧠 AI-personalized articles based on breed, age, and health  
- 👥 Community features: “I’m Coming” park check-ins, vet/store assignments

---

## 🧠 Motivation

Dog care is complex and fragmented – owners often use several disconnected apps for tracking health, locating parks, or finding relevant tips.  
**PAWsitive Life** unifies all of these into one intuitive, AI-powered app to improve both the pet’s wellbeing and the owner’s experience.

---

## 🧱 Core Features

| Module                  | Description                                                                 |
|-------------------------|-----------------------------------------------------------------------------|
| Dog Profile             | Add/edit/delete detailed profiles (breed, age, health, behavior, notes)    |
| Reminder System         | Push notifications + calendar view for vaccines, grooming, vet visits      |
| Map ("MAPuppy")         | Shows dog parks, vets, pet stores near user using **Mapbox API**           |
| Vet & Pet Store Assign  | Link dogs to a vet/store, view and call them directly from the app          |
| Article Recommendation  | AI filters articles based on dog's age/stage via **Cohere NLP**            |
| Social Feature          | "I'm Coming!" check-in + view which dogs are at the park in real time      |

---

## 🔧 Tech Stack

| Layer          | Technologies                                               |
|----------------|------------------------------------------------------------|
| Frontend       | Kotlin, Android SDK, XML layouts, Jetpack Navigation       |
| UI Libraries   | Material Components, Glide, CalendarView                   |
| Backend        | Firebase Firestore, Firebase Auth                          |
| Location       | Mapbox SDK, GPS, Haversine formula                         |
| AI Integration | Cohere NLP API, Dog CEO API                                |
| Build & Tools  | Gradle, GitHub, Android Studio, Logcat                     |
| Testing        | JUnit, AndroidX Test, Espresso, Manual Functional Testing  |

---

## 🏗️ Architecture

**MVVM (Model–View–ViewModel)** design pattern for better separation of concerns and testability.

### Key Components

- `Dog`, `Reminder`, `Vet`, `DogPark`, `Article`, etc. – Kotlin data classes
- `Repositories` – Abstracted data access from Firebase or static APIs
- `Adapters` – RecyclerView adapters for dynamic lists (dogs, vets, parks, etc.)
- `ViewModels` – Manage data lifecycle, used across fragments
- `Fragments` – UI screens for each functional unit (DogProfile, Mapuppy, etc.)

---

## 📌 Use Cases

- ✅ **Create Dog Profiles:** Add demographic + medical details  
- 📆 **Reminders:** Get alerts for upcoming treatments  
- 🗺️ **Map Integration:** View favorite park, nearby services, live check-ins  
- 🧠 **AI Articles:** Custom reading by dog’s age (puppy, adult, senior)  
- 📞 **Contact Vets/Stores:** In-app call functionality from profile page  
- 🐕 **Social Features:** Know who’s at the park, send “I’m coming!” status  

---

## 🧪 Testing Strategy

Testing guided by formal **STP** and executed via structured **STD**, covering:

- ✅ Functional tests: Dog management, reminders, map, articles
- 🧪 Unit tests: Data validation, alarm logic
- 🔁 Integration tests: Firebase, Mapbox, Cohere AI
- 📱 Manual testing on physical devices and emulators

> Full test case list: See `PAWsitive Life - STD.docx`  

---

## 🧪 Example Test Cases

- Add/Edit/Delete a dog profile and see live updates
- Set and trigger a vaccine reminder
- Assign vet/store and initiate in-app call
- Display dogs at a park after pressing “I’m Coming!”
- Filter article feed by age tags (puppy/adult/senior)

---

## 📂 Folder Structure (Simplified)

📦app/
┣ 📂ui/
┃ ┣ 📄DogProfileFragment.kt
┃ ┣ 📄MapuppyFragment.kt
┃ ┗ ...
┣ 📂model/
┃ ┣ 📄Dog.kt
┃ ┣ 📄Reminder.kt
┣ 📂data/
┃ ┣ 📄DogRepository.kt
┃ ┣ 📄DogApi.kt (Dog CEO API)
┣ 📂viewmodel/
┃ ┣ 📄DogViewModel.kt
┣ 📂adapter/
┃ ┣ 📄DogAdapter.kt
┗ 📄MainActivity.kt

---

## 📖 Research Foundations

PAWsitive Life draws on literature across:

- 🐕 Canine health, psychology & behavior
- 🤝 Human–dog communication
- 🧠 Mobile UX for pet-tech platforms
- 📚 Sources: Miklósi et al. (2004), Horowitz (2010), Humane Society (2024), Cohere Docs (2025), etc.

> See: `/docs/literature_review.pdf`

---

## 🚀 Development Timeline

| Week | Milestone                                      |
|------|------------------------------------------------|
| 1–2  | Project setup, Firebase integration            |
| 3–4  | Backend logic + OpenAI & Dog CEO integration   |
| 5–6  | Fragments, UI layouts, Mapbox, Feed/Walk logs  |
| 7    | Testing, debugging, documentation              |
| 8    | Final packaging, APK, user guide, presentation |

---

## 📘 User Guide

A full visual guide (`UserGuide.pdf`) includes instructions for:

- Dog profile creation and editing  
- Reminder setup and calendar use  
- Navigating maps and using “I’m Coming!”  
- Filtering articles and reading AI-based content  
- Calling a vet or pet store directly  

> 📄 See: `PAWsitive Life - UserGuide.pdf`

---

## 📜 License

This application was developed solely for academic purposes as a capstone project.  
Not licensed for commercial distribution.

---

## 🙋 Contact Us

Have questions or want to collaborate?

- 📧 Or Avichzer Elmalih – oravi528@gmail.com  
- 📧 Noa Danon – noadanon220@gmail.com  
- 📧 Mark Tsirlin – mark20013009@gmail.com

---

