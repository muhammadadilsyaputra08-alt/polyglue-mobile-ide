# PolyGlue Mobile IDE

> **"Unity Ringan di Saku Anda"**

All-in-One Android Game Engine dengan arsitektur hybrid multi-bahasa:
- **C++** — Core Engine (Rendering, Fisika, Audio)
- **Lua** — Gameplay Logic
- **TypeScript (QuickJS)** — Editor UI
- **Python** — Heavy Tooling
- **Kotlin** — Host App & Build System

## Fitur

- Visual Scene Editor dengan multi-touch gesture
- Smart Snippet Library (20 kategori, 100+ blok)
- Two-Way Sync: Visual Node ↔ Lua Code
- Live Preview tanpa build panjang
- Export APK langsung dari perangkat
- GitHub Integration

## Build

### Local Build
```bash
./gradlew assembleDebug
```

### CI/CD (GitHub Actions)
Push ke branch `main` akan otomatis trigger build dan release APK.

## Arsitektur

```
┌─────────────────────────────────────────────┐
│  Kotlin Host App (UI, File, Gradle, JNI)   │
├─────────────────────────────────────────────┤
│  TypeScript/QuickJS  →  Editor UI Layer      │
├─────────────────────────────────────────────┤
│  Lua Runtime  →  Gameplay Logic             │
├─────────────────────────────────────────────┤
│  C++ Engine  →  OpenGL ES, Box2D, OpenSL    │
├─────────────────────────────────────────────┤
│  Python (Embedded) →  Tooling on-demand     │
└─────────────────────────────────────────────┘
```

## License
MIT
