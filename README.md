# CyberStegoApp

An Android application for secure image steganography — hide and extract secret messages within images using LSB encoding.

## Features

- Encode text messages into images using Least Significant Bit (LSB) steganography
- Decode hidden messages from stego images
- Supports PNG and BMP formats for lossless encoding
- Clean Material Design UI

## Getting Started

Clone the repo and open it in Android Studio:

```bash
git clone https://github.com/charan12309/CyberStegoApp.git
```

Open the project in Android Studio, sync Gradle, and run on an emulator or physical device (API 21+).

## Usage

1. **Encode:** Select a cover image → enter your secret message → tap Encode → save the output image
2. **Decode:** Select a stego image → tap Decode → view the hidden message

## Tech Stack

- Kotlin / Java (Android)
- Android SDK (API 21+)
- LSB steganography algorithm
