# 🍽️ Smart Campus Canteen Automation System

![Android](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?style=flat-square&logo=firebase)
![Java](https://img.shields.io/badge/Language-Java-red?style=flat-square&logo=java)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=flat-square)

> A real-time canteen management Android app that eliminates the communication gap between Kitchen Staff and Billing Counter using Firebase Realtime Database.

---

## 👨‍💻 Project Info

| Field | Details |
|---|---|
| **Student** | Yash Dhananjay Dalavi |
| **PRN** | 202301100017 |
| **Batch** | A4 |
| **Subject** | Software Project Management Lab |
| **College** | MIT |

---

## 📱 App Modules

### 🎓 Student Module
- Browse 30+ live menu items across 5 categories
- Real-time availability status (Available / Sold Out)
- Place order and get unique Digital Token number
- Live 3-step order tracking (Received → Preparing → Ready)
- Google Sign-In authentication

### 🍳 Kitchen Module
- Toggle food item availability with ON/OFF switch
- Incoming orders queue — view new orders in real-time
- Mark orders as "Preparing" and "Ready"
- Password protected access (Staff only)

### 🏪 Counter Panel
- Real-time orders dashboard with pending count
- Filter orders by All / Pending / Ready status
- Mark order as Ready — student gets instant notification
- Password protected access (Manager only)

---

## ✨ Key Features

| Feature | Implementation |
|---|---|
| Real-time sync | Firebase Realtime Database |
| Google Login | Firebase Authentication + OAuth 2.0 |
| Unique Token | Firebase Transactions (concurrent-safe) |
| Offline Support | Firebase Persistence enabled |
| Role-based Auth | Google (Student) + Password (Staff) |
| Live Status | ValueEventListener on order node |

---

## 🛠️ Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Backend:** Firebase Realtime Database
- **Authentication:** Firebase Auth (Google Sign-In)
- **UI:** RecyclerView, CardView, GridLayout
- **Architecture:** Activity-based MVC

---

## 🗂️ Firebase Database Structure
```json
{
  "menu_items": {
    "item_001": {
      "name": "Samosa",
      "price": 15,
      "category": "Snacks",
      "isAvailable": true
    }
  },
  "orders": {
    "order_id": {
      "studentName": "Yash",
      "itemName": "Samosa",
      "tokenNumber": 101,
      "status": "Preparing"
    }
  },
  "counters": {
    "token_counter": {
      "lastToken": 101
    }
  }
}
```

---

## 🔄 App Flow
LoginActivity
│
├── 🎓 Student (Google Login)
│       └── StudentActivity (Menu)
│               └── OrderTrackingActivity (Token + Live Status)
│
├── 🍳 Kitchen Staff (Password: kitchen123)
│       └── KitchenActivity
│               ├── Tab 1: Menu Toggle
│               └── Tab 2: Incoming Orders Queue
│
└── 🏪 Counter Manager (Password: counter123)
└── CounterActivity (Orders Dashboard)

---

## 🚀 How to Run

1. Clone this repository
```bash
git clone https://github.com/yash-D-Dalavi/SmartCampusCanteen.git
```

2. Open in Android Studio

3. Add your own `google-services.json` in `app/` folder
   - Create Firebase project at console.firebase.google.com
   - Enable Realtime Database and Google Authentication
   - Download and add google-services.json

4. Import Firebase JSON data into Realtime Database

5. Run on Android device (API 24+)

---

## 📊 Project Timeline (CPM - 12 Weeks)

| Phase | Activities | Duration |
|---|---|---|
| Planning | Topic Selection, Requirements | Week 1-2 |
| Design | System Design, Database Design | Week 3-4 |
| Development | Frontend + Backend (Parallel) | Week 5-8 |
| Integration | Firebase Sync, All 3 Modules | Week 9-10 |
| Testing | End-to-end Testing, Bug Fixes | Week 11 |
| Deployment | APK Generation, GitHub | Week 12 |

---

## 💡 Problem Solved

**Before this app:**
- Kitchen staff had no way to update item availability digitally
- Counter manager had to physically ask kitchen about stock
- Students crowded the counter just to check menu
- Paper token system caused errors and delays

**After this app:**
- Kitchen toggles item ON/OFF — counter and students see it instantly
- Digital token eliminates paper system
- Students track order status on their phone
- Real-time sync eliminates all communication gaps

---

## 📂 Project Structure
SmartCampusCanteen/
└── app/src/main/
├── java/com/mit/smartcampuscanteen/
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── StudentActivity.java
│   ├── KitchenActivity.java
│   ├── CounterActivity.java
│   ├── OrderTrackingActivity.java
│   ├── OrderAdapter.java
│   ├── MenuItem.java
│   └── Order.java
└── res/
├── layout/          (8 XML files)
├── drawable/        (10 shape files)
└── values/          (colors, strings)

---

## 🔐 Staff Credentials

| Role | Password |
|---|---|
| Kitchen Staff | kitchen123 |
| Counter Manager | counter123 |

---

*Smart Campus Canteen Automation System — SPM Lab Project 2026*
