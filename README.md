# 🔐 CyberStegoApp — Android Image Steganography (Java)

> Hide AES‑encrypted text inside images using LSB steganography. Share, save, and extract securely.

---

## 📱 What It Does

StegoApp lets you embed a secret message into an image. The message is first AES‑encrypted, then written bit‑by‑bit into pixel data using Least Significant Bit (LSB). With the same key, the ciphertext is extracted and decrypted back to the original message.

Key features:
- Login/Signup with persistent session (SharedPreferences)
- Tabs: Embedding, Extraction, User info
- Share embedded images to other apps
- Delete account from User info
- Shake‑to‑logout using accelerometer

---

## 🏗️ Project Structure

```
com.example.stegoapp
│
├── LoginActivity.java        // Login + auto-redirect if already logged in
├── SignupActivity.java       // New user registration
├── MainActivity.java         // Tabs: Embedding / Extraction / User info
│
├── ui/
│   ├── EmbeddingFragment.java   // Pick image → encrypt → center-crop square → embed → save/share
│   ├── ExtractionFragment.java  // Pick image → center-crop square → extract → decrypt
│   └── UserInfoFragment.java    // Show username, logout, delete account
│
├── crypto/CryptoUtils.java      // AES-256-GCM, PBKDF2-derived key, random salt + IV
├── crypto/PasswordHasher.java   // Salted PBKDF2 hashing + constant-time verify
├── stego/StegoUtils.java        // LSB embed/extract, 32-bit length header, center-square
├── util/ImageUtils.java         // Software ARGB_8888 decode + PNG save (MediaStore)
├── util/ExecutorProvider.java   // Shared single-thread executor for DB work
│
├── data/
│   ├── AppDatabase.java         // Room database (users table), singleton
│   ├── User.java                // Room entity: username (PK, non-null), passwordHash
│   └── UserDao.java             // DAO: insert/find/deleteByUsername
│
└── res/layout/...               // activity_main, fragment_* and auth screens
```

---

## 🔄 App Flow

```
Launch
  └─► LoginActivity
        ├─► [session exists] ───────────► MainActivity (tabs)
        └─► [no session] login or signup

MainActivity (Tabs)
  ├─► Embedding
  │     1) Pick image (any format; saved as PNG)
  │     2) Enter message + key
  │     3) AES-GCM encrypt → center-square crop → LSB embed
  │     4) Save or Share (document share to avoid recompression)
  │
  ├─► Extraction
  │     1) Pick image (recommend PNG)
  │     2) Center-square crop → read bits → AES-GCM decrypt (auth tag checked)
  │     3) Show message (errors if image was recompressed)
  │
  └─► User info
        - View username
        - Logout
        - Delete account (removes current user + logs out)
```

---

## 🖼️ Steganography

### Center‑Square + LSB
- If the chosen image is rectangular (e.g., 1600×900), the app crops the **center square** (900×900).
- Embedding starts at the first pixel of the square (i.e., the visual center of the original image area).
- Each pixel contributes one bit via the **blue channel’s least significant bit**.

### Data Format
```
[4 bytes: payload bit length] [16 bytes: salt] [12 bytes: IV] [N bytes: ciphertext + 16-byte GCM tag]
```

### Capacity
- Bits available = (squareWidth × squareHeight) − 32
- Bytes available ≈ floor(Bits / 8)

---

## 🔑 Encryption

| Property | Value |
|---|---|
| Algorithm | AES |
| Mode | GCM (authenticated) |
| Padding | None (stream mode) |
| Key Size | 256‑bit |
| Key Derivation | PBKDF2‑HMAC‑SHA1, 120,000 iterations, 16‑byte random salt per message |
| IV | 12 bytes, random per message |
| Auth Tag | 128‑bit |

The salt and IV are not secret and are prefixed to the ciphertext, so extraction still needs only the passphrase. Because both are random per message, encrypting the same text twice produces different output — there is no ECB‑style pattern leakage. A wrong passphrase or an image that has been modified or recompressed fails the authentication tag and is reported as an error rather than returning garbage.

**Format change:** the payload layout is different from the previous ECB build (it now carries the salt, IV, and GCM tag). Images embedded by an older build hold raw ECB ciphertext and cannot be decrypted by this version — re‑embed any message you need to keep.

---

## 🗄️ Database & Session

- Room table `users`:
  - `username` (TEXT, PK, non‑null)
  - `passwordHash` (TEXT) — `iterations:saltHex:hashHex` from PBKDF2‑HMAC‑SHA1 (120,000 iterations, 16‑byte random salt). The password itself is never stored, and the profile screen never displays it. Verification is constant‑time.

The schema is at version 3 with `fallbackToDestructiveMigration()`, so upgrading from an older build clears the `users` table — sign up again after updating.
- Session via `SharedPreferences`:
  - `logged_user` → current username or null

---

## 📤 Sharing Notes

Some apps (e.g., WhatsApp image share) **recompress** images and destroy LSB data. StegoApp’s Share uses a generic MIME (`*/*`) so targets treat it like a **document**, preserving the PNG bytes. When extracting, if the app detects an invalid length header or capacity mismatch, it reports that no hidden data is present (likely recompressed).

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Min/Target SDK | 24 / 34 |
| Build | Gradle 8.5, AGP 8.3 |
| DB | Room (SQLite) |
| Crypto | `javax.crypto` |
| Session | SharedPreferences |
| UI | AppCompat + Material Components |

---

## 🚀 Setup

1) Open the project in Android Studio.  
2) Ensure Gradle JDK is 17 (or 21 with Gradle 8.5+).  
3) Sync and Run on an emulator/device (API 24+).  

No storage permission is required on Android 10+ when picking images via the system picker. MediaStore is used to save PNGs.

---

## 🧪 Quick Test

1. Sign up a user → login.  
2. Embedding tab → pick image → enter message + key → Embed.  
3. Save or Share the embedded image.  
4. Extraction tab → pick the saved/shared image → enter key → Decrypt.  
5. Message appears if the image wasn’t recompressed. A wrong key or an altered image is rejected with an authentication error.  

---

## ⚠️ Notes

- Use PNG or “document” sharing to avoid recompression. JPEG/WebP may destroy hidden bits.  
- Passwords are stored only as salted PBKDF2 hashes; the app never displays or recovers them.  
- The encrypted‑payload format changed with the move to AES‑GCM; images embedded by an older build cannot be decrypted by this version.  
- Center‑square embedding affects output size; the saved embedded image is the cropped square.

---

Built as a concise, fast Android steganography demo using LSB + AES encryption.

