# MediBridge+ 🏥
### Patient-Held, Consent-Driven Healthcare & Medical Records System

MediBridge+ is an all-in-one modern Android application built with **Kotlin** and **Jetpack Compose** (Material Design 3). It solves **Problem Statement 3: Patient-Held Consent-Driven Health Record** — empowering patients with full ownership and granular control over their medical records, clinical consultations, medicine delivery, and healthcare provider data access.

---

## 🌟 Core Philosophy: Problem Statement 3 Alignment
Traditional healthcare systems silo patient data across hospital EMRs, clinics, and diagnostic labs without patient oversight. MediBridge+ shifts data custody directly into the hands of the patient:
- **Patient Ownership:** Medical records are stored securely in an on-device encrypted vault.
- **Granular Consent:** Patients explicitly choose *which* records to share (e.g. only a specific prescription or lab report), *who* can access them (Doctor, Hospital, Pharmacist, Caregiver), and for *how long* (1 Hour, 24 Hours, 7 Days, 30 Days, Until Revoked).
- **Zero Full-History Leak:** The system never shares the patient's entire medical record history automatically.
- **Immediate Revocation:** The patient can revoke any provider's access grant with a single tap at any time.
- **Audit Logging:** Every grant, record access, download, or revocation is immutably logged in a verifiable access history.
- **Time-Limited QR / Passcodes:** Ephemeral, auto-expiring tokens and QR codes generated strictly for selected documents.

---

## 📱 Navigation & Features

The app features a 6-tab navigation structure:

### 1. 🏠 Home (Patient Dashboard)
A clean, non-duplicate dashboard providing high-level summaries:
1. **My Health Summary:** Key metrics showing active prescriptions, recent lab reports, upcoming follow-ups, and active consent grants.
2. **Health Timeline:** Chronological timeline of clinical visits, prescriptions, diagnostics, and discharge summaries.
3. **Consent Center Overview:** Real-time visibility into who currently has access to patient records with quick **Review Access** and **Revoke** options.
4. **Follow-Up Plan:** Important upcoming follow-up appointments and pending health tasks.
5. **Emergency Health Information:** Critical emergency health info (Blood group, allergies, current medicines, emergency contact) with an instant patient-controlled emergency sharing toggle.
6. **Recent Activity:** Audit log tracking actions such as records added, shared, accessed, and revoked.

### 2. 📅 Appointments & Live Queue
- Search verified hospitals and specialist doctors.
- Book outpatient consultation tokens.
- **Live Queue Tracker:** Real-time token waiting tracker with sound/vibration alerts.
- **Voluntary Consultation Sharing:** Explicit consent toggle to temporarily share selected records with the consulting doctor for 24 hours.

### 3. 💊 Medicines & Verified Pharmacy
- Nearby pharmacy discovery and catalog search.
- Prescription upload and medicine ordering.
- **Explicit Pharmacist Consent:** Voluntary consent required to share prescriptions with pharmacists for order fulfillment.

### 4. 🤖 AI Health Assistant
- Educational multilingual guidance (English, Telugu, Hindi).
- Explains prescription instructions, summarizes diagnostic lab reports, and clarifies dosage precautions.
- **Safety Boundary:** AI is strictly an educational tool and does not make medical decisions or automatically share records.

### 5. 📂 Health Records (Vault & Consent Center)
- **Document Categories:** Prescriptions, Lab Reports, Discharge Summaries, Medical Bills, and Other Medical Records.
- **Record Lifecycle:** Add camera photo, gallery image, or PDF with doctor/hospital name, record date, and clinical notes. View, edit, download, share, or delete records.
- **Sharing Workflow:**
  $$\text{Select Record} \rightarrow \text{Share} \rightarrow \text{Choose Recipient} \rightarrow \text{Select Records} \rightarrow \text{Set Access Duration} \rightarrow \text{Review} \rightarrow \text{Give Access} \rightarrow \text{Confirmation}$$
- **In-Vault Consent Center:** Active grants table, duration management, and instant revocation.
- **Access History & Temporary QR Code:** Time-limited passcodes and dynamic visual QR codes with auto-expiring countdown timers.

### 6. ❤️ Health & Family Care
- Medicine reminders with on-device alarm scheduling.
- Family member health profiles and care tracking.
- Senior citizen high-contrast mode with live digital clock.

### ⚙️ Settings (Privacy, Consent & Security)
*Accessible via the gear icon in the top header:*
- **Active Access & Consent History:** List of active provider grants and archive of revoked permissions.
- **Sharing Preferences:** Enforce duration limits, paramedical emergency responder access, and access notifications.
- **Security Options:** App PIN lock, Biometric authentication (Fingerprint / Face ID), auto-lock timeouts, and on-device AES-256 vault status.
- **API Configuration:** Configurable REST microservice endpoints for live backend integration.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Local Persistence:** Room Database with Kotlin Coroutines & Flow
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchronous Flow:** Coroutines, StateFlow, collectAsStateWithLifecycle
- **Storage:** Local app-private encrypted storage (zero orphaned files)
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`)

---

## 🚀 How to Run Locally in Android Studio

1. **Clone the repository:**
   ```bash
   git clone https://github.com/<your-username>/medibridge.git
   cd medibridge
   ```
2. **Open in Android Studio:**
   - Launch Android Studio (Ladybug / Iguana or later).
   - Select **Open an existing project** and choose the `medibridge` folder.
3. **Sync & Build:**
   - Allow Gradle to sync dependencies.
   - Run on an Android device or emulator running **API 26+ (Android 8.0+)**.

---

## 📄 License
This project is licensed under the Apache License 2.0.
