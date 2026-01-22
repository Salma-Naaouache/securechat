SecureMessenger Cloud (Zero-Trust Architecture)
This project is an advanced-level Android messaging application built on a Zero-Trust Model. While it uses Firebase for real-time data synchronization, the application assumes the server is potentially compromised and implements Client-Side Encryption to ensure that plain text messages never leave the device.


🛠 Technology Stack
Language: Kotlin 
Architecture: MVVM (Model-View-ViewModel) with a clean separation of concerns 
Backend: Firebase (Auth, Firestore) 
Security: Android Keystore System, Biometric API, AES Encryption 

🏗 Project Architecture
The project is structured into three distinct layers to isolate security logic from the UI:


1. Core (The "Brain")
Contains the primary security and utility logic:
CryptoManager.kt: The AES engine responsible for encrypting and decrypting strings.
KeyStoreHelper.kt: Manages hardware-backed encryption keys that never leave the device's Trusted Execution Environment (TEE).
BiometricAuth.kt: Handles fingerprint and face recognition prompts.
RootDetector.kt: Checks for system integrity and "su" binaries to alert users of rooted environments.

2. Data (The "Worker")
Handles local and remote data management:
Remote: AuthHelper.kt for Firebase login and ChatService.kt for sending/receiving encrypted messages.
Local: SecureStorage.kt using EncryptedSharedPreferences for sensitive user settings.

3. UI (The "Face")
Fragmented into features for Authentication, Home/Dashboard, and the Chat Interface:
Auth: Login and Signup screens.
Home: A RecyclerView listing active chat rooms and a Floating Action Button for new chats.
Chat: Real-time messaging screen where messages are decrypted only for display.

🔒 Security Requirements
To maintain the Zero-Trust integrity, the following defenses are implemented:
Data-at-Rest Encryption: Plain text is passed through CryptoManager.encrypt() before reaching Firebase. The Firebase Console only displays random ciphertext (e.g., U2FsdGVkX1...).
Hardware-Backed Keys: Encryption keys are generated within the Android Keystore and are never hardcoded or exposed in the source code.
Biometric Lock: The app enforces a biometric prompt on every launch. The chat interface remains hidden until authentication succeeds, even if the user is already logged into Firebase.



Obfuscation: R8/ProGuard is enabled in release builds to rename classes and protect against reverse engineering.
