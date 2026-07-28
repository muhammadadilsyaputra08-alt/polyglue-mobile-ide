# PolyGlue Mobile IDE — Dokumen Handoff

> Dokumen ini ditulis untuk agent (AI atau manusia) yang melanjutkan development
> project ini. Tujuannya: agar tidak perlu re-audit dari nol, tidak mengulang bug
> yang sudah pernah diperbaiki, dan paham keputusan desain yang sudah diambil.
>
> Terakhir diperbarui: 28 Juli 2026.

---

## 1. Apa Project Ini

**PolyGlue Mobile IDE** — aplikasi Android untuk membuat game 2D langsung dari
HP/tablet. Arsitektur: UI editor (Kotlin/Compose) mengontrol game engine native
(C++/OpenGL ES/Box2D) lewat JNI, scripting game pakai Lua (via LuaJ), dan
rencana ke depan sebagian tooling (codegen, optimasi asset) pakai Python
(Chaquopy).

**Target akhir (visi user):** pengguna bikin game di HP → export jadi project
Android siap pakai → push ke GitHub (auto-build via GitHub Actions yang sudah
disiapkan) **atau** buka langsung di Android Studio → hasil akhirnya APK
sungguhan yang bisa diinstall, default landscape untuk game.

---

## 2. Tech Stack

| Layer | Teknologi |
|---|---|
| UI | Jetpack Compose, Material3, Navigation Compose |
| Bahasa | Kotlin 2.0 (JVM 17), C++ (NDK r26b) |
| Build | AGP 8.5.0, Gradle 8.7, KSP 2.0.0-1.0.22 |
| Database lokal | Room (KSP) |
| Scripting game | LuaJ 3.0.1 (Lua interpreter murni Java, BUKAN Lua C native) |
| Native engine | OpenGL ES 3.0 (rencana), Box2D (rencana, belum di-vendor), OpenSL ES (rencana) |
| CI/CD | GitHub Actions (`.github/workflows/build.yml`, `nightly.yml`) |
| Python tools (belum aktif) | Chaquopy (plugin belum di-apply) |

---

## 3. Peta Struktur Folder

```
app/src/main/
├── java/com/polyglue/ide/
│   ├── MainActivity.kt              # shell IDE, NavHost, orientasi bebas
│   ├── PolyGlueApplication.kt       # DI manual, init NativeEngine
│   ├── di/AppModule.kt              # provider Repository/DAO manual (bukan Hilt)
│   ├── core/engine/NativeEngine.kt  # 53 external fun -> JNI
│   ├── core/lua/LuaRuntime.kt       # binding LuaJ <-> NativeEngine
│   ├── editor/GamePreviewActivity.kt# activity fullscreen landscape utk preview
│   ├── ui/screens/                  # 7 layar: Home, Projects, Editor, Assets,
│   │                                 #   Snippets, Build, Settings
│   ├── ui/viewmodel/                # 4 ViewModel (Editor, Project, Build, Snippet)
│   ├── ui/navigation/               # NavHost polos, TIDAK ADA drawer/rail/bottomnav
│   └── data/
│       ├── local/                   # Room: 7 entity, 6 DAO
│       └── repository/              # 5 repository (termasuk BuildRepository — lihat §5)
├── cpp/
│   ├── CMakeLists.txt               # third_party box2d/lua DIHAPUS (belum ada source-nya)
│   ├── polyglue_jni.cpp             # 38 dari 53 fungsi JNI terimplementasi
│   └── engine/
│       ├── polyglue_engine.h        # forward-declare Renderer/PhysicsWorld/AudioEngine/Scene
│       └── engine.cpp               # SEMUA method stub/no-op, lihat §4
├── python/                          # ADA isinya tapi MATI TOTAL, lihat §4
│   ├── main.py
│   └── polyglue_tools/{codegen,optimize,procedural,ai_assistant}.py
└── res/                             # themes, mipmap ikon (placeholder), xml rules

.github/workflows/
├── build.yml     # lint -> test -> build-debug -> (tag v*) build-release + GH Release
└── nightly.yml   # build debug harian jam 2 UTC
```

---

## 4. Status Fitur — Apa yang Nyata vs Stub

### ✅ Nyata & berfungsi
- Build pipeline lengkap (Gradle/lint/unit test/compile APK debug) — CI hijau per 28 Juli 2026
- Navigasi antar 7 layar Compose (routing jalan, tapi TANPA chrome navigasi permanen)
- Room database — CRUD project/scene/script/asset/snippet/build-config beneran ke SQLite lokal
- Lua scripting (LuaJ) — API `Entity/Camera/Audio/dll` ter-bind dengan tipe benar
- Native `.so` ter-compile & ter-load, lifecycle (init/shutdown/resize) terpanggil

### ❌ Stub / kosong / mati (PEKERJAAN UTAMA ada di sini)
- **Rendering** — `Engine::render()` di `engine.cpp` isinya kosong, tidak menggambar apa pun
- **Physics** — Box2D tidak pernah di-vendor. `CMakeLists.txt` sempat mencoba
  `add_subdirectory(third_party/box2d)` tapi foldernya tidak ada → sudah DIHAPUS
  supaya CMake configure tidak gagal. Semua `addRigidbody/applyForce/setVelocity`
  di `engine.cpp` adalah no-op.
- **Audio** — `loadSound()` selalu return `-1`, `playSound/stopSound` no-op. Sama
  seperti Box2D, `third_party/lua` (Lua C native) juga sempat direferensikan di
  CMake tapi dihapus — Lua sekarang JALAN LEWAT LuaJ (Java) di sisi Kotlin, BUKAN
  lewat native. Jangan bingung: ada 2 jalur Lua yang direncanakan di
  `BUILD_GUIDE.md` awal (native C Lua + LuaJ), yang benar-benar dipakai sekarang
  cuma LuaJ.
- **Particle system, Tilemap, Lighting, Shader** — semua fungsi native return `0`/kosong
- **15 dari 53 `external fun`** di `NativeEngine.kt` belum ada implementasi C++
  sama sekali (lihat daftar di §7) — akan `UnsatisfiedLinkError` kalau dipanggil
- **Fitur "Build" (`BuildScreen`/`BuildRepository.kt`)** — SAAT INI cuma membuat
  file `.zip` (berisi teks Lua + JSON scene) yang diganti ekstensi jadi `.apk`.
  **BUKAN APK asli** (tidak ada `.dex`/manifest/signing). Ini yang akan diganti
  total di Fase 6 (lihat §6) jadi "Export Project" yang menghasilkan source
  project Android valid.
- **Python tools** — plugin `com.chaquo.python` TIDAK PERNAH di-apply di
  `app/build.gradle.kts`. Folder `python/` ada isinya tapi Gradle sama sekali
  tidak memprosesnya. `proguard-rules.pro` sudah punya rules untuk `com.chaquo.python`
  (sisa rencana awal) tapi plugin-nya sendiri belum ada.
- **`GamePreviewActivity`** — cuma teks placeholder "Game Preview", belum
  terhubung ke `GLSurfaceView`/render loop apa pun. Sudah landscape-locked
  (`android:screenOrientation="sensorLandscape"`) di manifest — ini contoh yang
  bisa dicontoh untuk Fase 7.
- **⚠️ SEMUA 7 SCREEN TIDAK TERHUBUNG KE VIEWMODEL SAMA SEKALI** (ditemukan
  28 Juli 2026, sore). `grep -c "ViewModel" *.kt` di seluruh `ui/screens/`
  hasilnya `0` di ketujuh file. Semua screen cuma pakai
  `remember { mutableStateOf(...) }` lokal — artinya walau Room DB, DAO,
  Repository, dan 4 ViewModel semuanya compile dengan benar, **tidak ada satu
  pun yang benar-benar terpanggil dari UI**. Contoh: `ProjectsScreen` punya
  dialog "buat project baru" tapi tidak pernah memanggil
  `ProjectRepository.insertProject()`; `BuildScreen` API key/package
  name/version cuma `OutlinedTextField` dengan `onValueChange = {}` (hardcoded,
  tidak bisa diketik). **Ini gap arsitektur besar yang harus diperbaiki
  sebelum Fase 6 (Export Project) masuk akal dikerjakan** — export butuh baca
  data project SUNGGUHAN dari Room, bukan dari state UI yang tidak tersimpan.
  Juga belum ada pola DI untuk ViewModel (AppModule cuma provide Repository,
  tidak ada `ViewModelProvider.Factory` di mana pun).

---

## 5. Permintaan Baru dari User (28 Juli 2026) — Prioritas Berikutnya

User mengklarifikasi rancangan awal untuk fitur "Build":

1. **Export Project, bukan compile-di-device.** Generate project Android
   (Gradle) LENGKAP & VALID dari game yang dibuat di editor — siap:
   - di-push manual ke repo GitHub baru → otomatis build via
     `.github/workflows/build.yml` (workflow-nya SUDAH ADA & SUDAH TERUJI
     lolos CI untuk PolyGlue sendiri, tinggal di-template-kan untuk project
     hasil export)
   - dibuka langsung di Android Studio (`gradlew`/struktur project harus valid
     agar Android Studio bisa Open & Sync tanpa error)
2. **Orientasi — SUDAH DIPUTUSKAN & SEBAGIAN DIIMPLEMENTASI (28 Juli 2026):**
   - **IDE PolyGlue sendiri** (`MainActivity`) → dikunci
     `android:screenOrientation="sensorLandscape"` (SUDAH diterapkan di
     manifest) supaya area kerja lebih luas saat mengerjakan project.
   - **Game hasil export** → orientasi BISA DIPILIH per-project (landscape
     ATAU portrait), bukan dikunci landscape secara paksa. Field
     `BuildConfigEntity.targetOrientation` (String, default `"landscape"`,
     nilai valid: `landscape`/`portrait`/`sensorLandscape`/`sensorPortrait`)
     SUDAH DITAMBAHKAN sebagai fondasi data model. UI selector
     (`SingleChoiceSegmentedButtonRow` Landscape/Portrait) SUDAH ADA di
     `BuildScreen.kt`, TAPI baru state lokal (`remember`) — **BELUM
     tersambung ke `BuildConfigEntity`/Room** karena gap arsitektur di §4
     (screen tidak wired ke ViewModel). Begitu Fase "wiring
     screen↔ViewModel" selesai, tinggal sambungkan
     `targetOrientation` state ini ke `BuildViewModel.setOrientation()` (perlu
     dibuat) → `BuildRepository.saveBuildConfig()` (sudah ada).
   - Saat Fase 6 (Export Project) dikerjakan, nilai `targetOrientation` dari
     `BuildConfigEntity` project yang bersangkutan dipakai untuk mengisi
     `android:screenOrientation` di `AndroidManifest.xml` TEMPLATE hasil
     export (bukan manifest PolyGlue sendiri).
   - **✅ TAMBAHAN (28 Juli 2026): "Run/Play" preview di dalam IDE juga
     SEKARANG mengikuti orientasi project**, bukan cuma hasil export.
     `GamePreviewActivity` tidak lagi dikunci `sensorLandscape` statis di
     manifest (`AndroidManifest.xml` sekarang `android:screenOrientation=
     "unspecified"`) — orientasinya di-set DINAMIS lewat kode
     (`requestedOrientation = ...` di `onCreate()`), dibaca dari Intent extra
     `GamePreviewActivity.EXTRA_ORIENTATION`. `EditorScreen.kt` tombol
     Play sekarang benar-benar memanggil
     `context.startActivity(GamePreviewActivity.createIntent(context,
     projectId, targetOrientation))`. **CATATAN PENTING:** `targetOrientation`
     di `EditorScreen.kt` SAAT INI masih state lokal terpisah dari yang di
     `BuildScreen.kt` (dua `remember` yang tidak saling tahu, sama-sama default
     `"landscape"`) — begitu wiring DB (§4) selesai, KEDUANYA harus baca dari
     SUMBER YANG SAMA (`BuildConfigEntity` project itu via `BuildViewModel`),
     jangan biarkan tetap dua state terpisah karena akan gampang tidak
     sinkron (user ganti di Build tapi Editor masih pakai nilai lama).
3. **UI IDE dioptimasi ke pola navigasi populer / side menu.** Saat ini
   `PolyGlueNavHost.kt` HANYA `NavHost` polos — tidak ada `Scaffold` dengan
   drawer/rail/bottom-nav sama sekali (dicek langsung dari source, lihat §3).
   Rekomendasi: pakai Material3 **adaptive navigation** (`NavigationSuiteScaffold`
   atau kombinasi manual `ModalNavigationDrawer` untuk portrait +
   `NavigationRail` untuk landscape/tablet) — ini pola yang direkomendasikan
   Google saat ini. Catatan: karena §5.2 di atas MainActivity SEKARANG SUDAH
   landscape-only, breakpoint "portrait" untuk drawer mungkin tidak akan
   pernah kejadian di IDE-nya sendiri — **perlu dikonfirmasi ke user apakah
   NavigationRail permanen saja sudah cukup** (tidak perlu logic adaptive
   portrait/landscape sama sekali kalau IDE memang selalu landscape).

---

## 6. Roadmap Kerja (urutan disarankan)

| # | Fase | Kenapa urutan ini |
|---|---|---|
| 0 | **Wiring screen↔ViewModel↔Room** (semua 7 screen, lihat §4, §13) | **BARU DITAMBAHKAN, PRIORITAS TERTINGGI.** Prasyarat Fase 6 (Export butuh data project sungguhan dari DB) dan hampir semua fitur lain. Disarankan mulai dari Smart Snippets (§12) sebagai proof-of-concept scope kecil sebelum ke screen kompleks (Editor/Projects/Build). |
| 1 | **Rendering OpenGL ES 3.0 dasar** — `GLSurfaceView` + render loop nyata di `engine.cpp`, minimal bisa gambar quad/sprite bertekstur | Fondasi — physics/particle/lighting semua butuh ada yang digambar dulu untuk divalidasi |
| 2 | **Physics Box2D** — vendor source ke `third_party/box2d` (submodule atau `FetchContent`), aktifkan lagi link di `CMakeLists.txt`, implementasi rigidbody/collider/raycast | Game 2D tanpa fisika sangat terbatas gunanya |
| 3 | **Audio OpenSL ES / Oboe** | Independen, bisa paralel dengan fase 2 |
| 4 | **Particle, Tilemap, Lighting, Shader** | Bergantung pada renderer (fase 1) sudah ada |
| 5 | **Lengkapi 15 fungsi native yang hilang** | Setelah subsistem terkait (fase 1-4) ada, baru fungsi wrapper-nya bisa diisi logic sungguhan |
| 6 | **Redesain "Build" → "Export Project"** (§5.1, §10) — generator TEMPLATE PROJECT RINGAN (source only, NDK compile terjadi di GitHub Actions, bukan di device). Butuh `export-template/` skeleton baru (belum ada) | Bergantung Fase 0 (baca data project dari DB) |
| 7 | **Orientasi project untuk hasil export** (§5.2) — SEBAGIAN SUDAH JALAN untuk preview in-app (`GamePreviewActivity` dinamis via Intent extra), tinggal terapkan pola sama ke `AndroidManifest.xml` TEMPLATE saat generate export | Satu paket dengan fase 6, bagian kecil |
| 8 | **Redesain navigasi UI (adaptive nav)** (§5.3) | Independen, bisa dikerjakan kapan saja, prioritas UX |
| 9 | **Aktifkan Chaquopy + wire Python tools** | Independen, paling rendah prioritas kecuali user butuh fitur AI-assist/codegen segera |

---

## 7. Fungsi Native yang Belum Ada Implementasi C++ (dari 53 `external fun`)

Deklarasi ada di `NativeEngine.kt`, TIDAK ADA implementasi `Java_com_polyglue_...`
di `polyglue_jni.cpp`:

```
nativeAddCircleCollider   nativeAddSpriteFrame     nativeCallFunction
nativeDestroyEntity       nativeDestroyParticleSystem  nativeDestroyTilemap
nativeGetGlobal           nativeGetTransform        nativeLoadScript
nativeSaveScene           nativeSetGlobal            nativeSetGroupVolume
nativeSetShaderUniformFloat  nativeSetShaderUniformVec2  nativeSetTileCollision
```

Cara cek ulang (kalau file berubah): jalankan
`grep -oP '(?<=external fun )\w+' NativeEngine.kt` vs
`grep -oP '(?<=Java_com_polyglue_ide_core_engine_NativeEngine_)\w+' polyglue_jni.cpp`
lalu `comm -23` pada dua list yang sudah di-`sort`.

---

## 8. Riwayat Bug yang SUDAH Diperbaiki (Jangan Diulang!)

Ini daftar kronologis bug CI/build yang sudah ditelusuri & diperbaiki lewat
banyak siklus log-analisis. Kalau bug serupa muncul lagi di file lain, polanya
sudah dikenal:

1. **Compose Compiler plugin hilang** — Kotlin 2.0+ WAJIB plugin
   `org.jetbrains.kotlin.plugin.compose` terpisah, tidak cukup
   `composeOptions.kotlinCompilerExtensionVersion`.
2. **CMake gagal configure** — `add_subdirectory(third_party/box2d|lua)`
   dipanggil padahal foldernya tidak ada di repo → dihapus dari
   `CMakeLists.txt`. **Kalau mau tambah Box2D sungguhan (Fase 2), source-nya
   harus benar-benar di-vendor dulu ke `third_party/box2d`, baru aktifkan lagi
   baris ini.**
3. **`glViewport` undefined** — `engine.cpp` pakai OpenGL ES tanpa
   `#include <GLES3/gl3.h>`.
4. **`org.luaj.vm2.*` unresolved** — dependency LuaJ belum dideklarasikan di
   `build.gradle.kts` → ditambahkan `org.luaj:luaj-jse:3.0.1`.
5. **Resource linking gagal** — `@mipmap/ic_launcher` dipakai tapi mipmap
   kosong → dibuatkan ikon placeholder (perlu diganti ikon asli nanti).
6. **`GamePreviewActivity` di manifest tapi class-nya tidak ada** → dibuatkan
   stub-nya (masih placeholder, lihat §4).
7. **`gradlew` tidak pernah di-commit** → workflow CI sekarang punya step
   auto-generate + retry 3x (`services.gradle.org` kadang down), tapi
   **sangat disarankan commit `gradlew`/`gradlew.bat`/`gradle-wrapper.jar`
   beneran** supaya tidak bergantung jaringan tiap run.
8. **`FAIL_ON_PROJECT_REPOS` bentrok** — `settings.gradle.kts` pakai mode
   repo terpusat, tapi `build.gradle.kts` (root) masih ada
   `allprojects { repositories {...} }` → dihapus, semua repo project
   didefinisikan di `settings.gradle.kts` saja.
9. **`LuaValue.valueOf()` tidak ada overload `Long`** — dua tempat di
   `LuaRuntime.kt` pakai fungsi native yang return `Long` langsung dioper ke
   `valueOf()` → wajib `.toDouble()`.
10. **`object : LuaValue()` salah base class** — LuaJ butuh
    `OneArgFunction`/`ZeroArgFunction` (subclass yang sudah implement
    `type()`), bukan `LuaValue` abstract langsung.
11. **`Alignment` unresolved** di 2 screen Compose — lupa
    `import androidx.compose.ui.Alignment`.
12. **`emptyArray()` gagal infer generic** saat dioper ke parameter
    `vararg args: Any` — solusinya hapus saja argumennya kalau memang kosong.
13. **5 lint error**: `NewApi` (windowLightNavigationBar butuh API27, minSdk24
    → suppress `tools:targetApi="27"`), `PermissionImpliesUnsupportedChromeOsHardware`
    (CAMERA permission tanpa `<uses-feature required="false">`),
    `FullBackupContent` (`<exclude domain="external">` invalid karena domain
    itu tidak pernah di-`include`).
14. **`sdkmanager: command not found`** di CI — runner GitHub Actions tidak
    otomatis punya sdkmanager di PATH → tambahkan
    `uses: android-actions/setup-android@v3` sebelum panggil `sdkmanager`.
15. **`unique_ptr<T>` incomplete type** — `Renderer/PhysicsWorld/AudioEngine/Scene`
    cuma forward-declared di header, tidak pernah didefinisikan → `~Engine()`
    (implicit via `= default`) gagal compile. Fix sementara: definisi stub
    kosong di `engine.cpp`. **Begitu subsistem asli diimplementasikan (Fase
    1-4), definisi stub kosong ini WAJIB diganti jadi class sungguhan** (jangan
    lupa, gampang kelewat).
16. **Room KSP race condition** — `exportSchema = true` +
    `ksp { arg("room.schemaLocation", ...) }` menyebabkan `kspDebugKotlin` &
    `kspReleaseKotlin` menulis paralel ke folder sama → `Empty schema file`.
    Fix: `exportSchema = false` (project ini tidak pakai migration testing).
    **Kalau nanti butuh migration testing sungguhan, pisahkan schemaLocation
    per-variant, jangan aktifkan balik dengan config yang sama.**
17. *(Bukan bug, catatan skema)* **`PolyGlueDatabase` version 1 → 2**
    (28 Juli 2026) — karena `BuildConfigEntity` dapat field baru
    `targetOrientation`. Aman karena `fallbackToDestructiveMigration()` sudah
    aktif dan project belum production/belum ada user data nyata. Kalau nanti
    project sudah production, JANGAN pakai `fallbackToDestructiveMigration`
    lagi untuk perubahan skema — tulis `Migration` object yang benar.

---

## 9. Cara Kerja yang Terbukti Efektif Sepanjang Sesi Ini

Alur debugging yang dipakai & berhasil: user push ke GitHub → CI gagal → user
download & upload ZIP log dari tab Actions → dianalisis baris per baris → fix
→ ulangi. Untuk lint, laporan HTML lengkap (`lint-report.zip` dari artifact,
BUKAN cuma log step) jauh lebih efisien karena log step CI cuma menampilkan
"first failure", sedangkan report HTML berisi SEMUA error/warning sekaligus.

**Saran untuk agent berikutnya:** kalau debugging CI lagi, minta report HTML
lengkap (lint, test, dsb) di awal, jangan cuma log step — lebih hemat siklus.

---

## 10. Asumsi & Keputusan Desain yang Perlu Dikonfirmasi ke User

- Belum jelas: apakah "Export Project" (Fase 6) generate SELURUH struktur
  Android project mirip PolyGlue sendiri (dengan native engine dsb ikut
  ter-bundle sebagai library/AAR), atau template project game yang jauh lebih
  ringan (cuma runtime interpreter Lua + assets, tanpa perlu NDK sama sekali di
  sisi project hasil export)? Ini keputusan arsitektur besar — pengaruh besar
  ke seberapa kompleks generator-nya.
  **✅ SUDAH DIPUTUSKAN (28 Juli 2026):** opsi ringan. Hasil export = TEMPLATE
  PROJECT SIAP PUSH (source code saja: `.kt`/`.cpp`/`CMakeLists.txt`/assets/
  scene JSON/Lua scripts + `.github/workflows/build.yml` yang sudah
  dikonfigurasi), BUKAN `.so`/APK pre-compiled. NDK/CMake/compile native
  BENAR-BENAR DIJALANKAN DI GITHUB ACTIONS setelah user push, persis seperti
  alur PolyGlue sendiri di-build lewat CI selama sesi debugging kita. Artinya
  generator "Export Project" pada dasarnya: (1) copy template project Android
  minimal (skeleton mirip repo ini tapi lebih ringan — TIDAK perlu semua fitur
  IDE, cuma runtime game), (2) isi dengan data project user (scene JSON, Lua
  scripts, assets) dari Room, (3) generate `AndroidManifest.xml` dengan
  `targetOrientation` project yang bersangkutan, (4) copy
  `.github/workflows/build.yml` (bisa dipakai APA ADANYA atau disederhanakan
  karena project hasil export tidak perlu job lint/sonar serumit PolyGlue
  sendiri), (5) zip semuanya / tulis ke folder yang bisa di-share user (SAF)
  untuk di-`git init` & push manual, ATAU (kalau nanti mau lebih advanced)
  integrasi GitHub API langsung dari app untuk auto-create repo + push. Bagian
  paling penting yang belum ada sama sekali: **template project game minimal**
  itu sendiri (folder terpisah, misal `export-template/`, belum dibuat).
- ~~Landscape lock: sensor atau fixed?~~ **SUDAH DIPUTUSKAN** (lihat §5.2):
  IDE = `sensorLandscape` fixed, game hasil export = pilihan user per-project
  lewat `BuildConfigEntity.targetOrientation`.
- Pola nav: karena IDE sekarang landscape-only (§5.2), apakah
  `NavigationRail` permanen saja sudah cukup (tanpa perlu adaptive
  portrait/landscape switching), atau tetap disiapkan adaptive untuk jaga-jaga
  kalau lock orientation IDE ini diubah lagi nanti? Perlu konfirmasi user.
- `BuildScreen` orientation selector saat ini masih state lokal
  (`remember`), belum tersimpan ke Room. Perlu diputuskan: apakah
  wiring screen↔ViewModel dikerjakan sebagai task tersendiri terlebih dahulu
  (mempengaruhi SEMUA 7 screen, bukan cuma BuildScreen), atau cukup wiring
  minimal untuk `BuildScreen` saja dulu sebagai quick-fix?

---

## 11. Referensi Cepat

- File asli acuan struktur/urutan build: `BUILD_GUIDE.md` (di root repo)
- Workflow CI: `.github/workflows/build.yml`, `.github/workflows/nightly.yml`
- Semua secrets & variables CI yang perlu di-set ada dijelaskan di histori PR/
  commit message, ringkasannya: `KEYSTORE_BASE64/PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD` (wajib untuk release), `FIREBASE_APP_ID/CREDENTIALS`,
  `SONAR_TOKEN` (opsional, gated lewat repo variable `ENABLE_FIREBASE`/
  `ENABLE_SONAR`)

---

## 12. Dokumentasi Fitur "Smart Snippets" — Contoh yang Sudah Berjalan Baik

Berbeda dari screen lain, **`SnippetsScreen.kt` genuinely berfungsi baik** untuk
apa yang dirancang — browse, filter kategori, dan search 28 template Lua
snippet siap pakai (Tilemap, Camera, Animation, Audio, Particle, Collision,
Controller, AI, Pathfinding, RPG, Multiplayer, UI, Data, Mobile, Physics,
Shader, AI Tools, Build, Debug, Productivity). Layak dijadikan REFERENSI POLA
UI untuk screen lain (search+filter+list dengan `LazyColumn`+`ScrollableTabRow`
sudah rapi & idiomatic Compose).

**Cara kerjanya sekarang (penting dipahami sebelum sentuh file ini):**
- Data 28 snippet **hardcoded langsung di Composable** sebagai
  `List<Triple<String, String, String>>` (nama, deskripsi, kategori) — BUKAN
  dari Room.
- Search & filter kategori 100% client-side (`.filter{}` biasa di Kotlin),
  makanya terasa instan & "berjalan baik" — tidak butuh network/DB roundtrip.
- Tombol "Add" (ikon `+` di tiap kartu) **belum melakukan apa-apa**
  (`onClick = {}`) — seharusnya menyisipkan template Lua snippet itu ke
  script project yang lagi dibuka di Editor.

**Backend-nya SUDAH DIRANCANG LENGKAP tapi 100% tidak terpakai (dead code):**
- `SnippetEntity` — skema lengkap: `id, category, name, description, icon,
  visualJson, luaTemplate, cppBackend, paramsJson, previewData, isBuiltIn,
  isFavorite, usageCount`. Perhatikan field `luaTemplate` — ini tempat
  SEHARUSNYA isi kode Lua snippet asli disimpan (di UI sekarang cuma ada nama+
  deskripsi, tidak ada kode template beneran).
- `SnippetDao` — query lengkap: getAll, getByCategory (ORDER BY usageCount
  DESC — ada tracking popularitas!), getFavorites, search (LIKE query nama+
  deskripsi), insert/insertBatch, update, incrementUsage, setFavorite, delete.
- `SnippetRepository` — wrapper bersih di atas DAO, semua expose sebagai
  `Flow`.
- `SnippetViewModel` — SUDAH ADA `search()`, `toggleFavorite()`,
  `useSnippet()` dengan `StateFlow` + `stateIn(WhileSubscribed(5000))` (best
  practice Android), TAPI tidak pernah di-instantiate dari `SnippetsScreen.kt`.

**Yang perlu dikerjakan untuk fitur ini "utuh":**
1. Seed 28 built-in snippet (dengan `luaTemplate` kode Lua ASLI, sekarang cuma
   nama+deskripsi) ke tabel `snippets` saat pertama kali app jalan (mis. lewat
   `RoomDatabase.Callback.onCreate()` atau one-time seeding di
   `PolyGlueApplication`).
2. Ganti `SnippetsScreen.kt` dari hardcoded list → collect dari
   `SnippetViewModel.allSnippets`/`searchResults`.
3. Wire tombol "Add"/"Favorite" ke `useSnippet()`/`toggleFavorite()` yang
   sudah ada di ViewModel.
4. Sisipkan `luaTemplate` snippet terpilih ke editor script yang aktif
   (butuh koordinasi dengan `EditorViewModel`/`ScriptDao`).

Task ini scope-nya KECIL & MANDIRI (tidak bergantung fase render/physics/audio
sama sekali) — kandidat bagus untuk "quick win" atau dikerjakan sebagai
PROOF-OF-CONCEPT wiring screen↔ViewModel↔Room pertama di project ini sebelum
diterapkan ke screen lain yang lebih kompleks (Editor, Projects, Build).

---

## 13. Daftar Fitur yang Direkomendasikan Tambahan (belum ada di rencana manapun)

Di luar 9 fase yang sudah tercatat di §6, berikut fitur/perbaikan yang
teridentifikasi selama audit tapi belum masuk roadmap manapun:

| Fitur | Kenapa direkomendasikan |
|---|---|
| **Wiring screen↔ViewModel↔Room** (semua 7 screen) | Prasyarat HAMPIR SEMUA fitur lain — tanpa ini, data project/scene/script/build-config tidak pernah benar-benar tersimpan. Prioritas tertinggi sebelum Fase 6. |
| **Seed & wire Smart Snippets** (§12) | Quick win, scope kecil, bagus jadi pola percontohan wiring pertama |
| **Undo/Redo di Editor** | Tombol sudah ada di UI (`Icons.Default.Undo/Redo`) tapi `onClick = { /* Undo */ }` kosong total |
| **`GLSurfaceView` nyata di `GamePreviewActivity`** | Saat ini cuma `Text("Game Preview")` — begitu Fase 1 (renderer) ada, ini titik integrasinya |
| **Asset import nyata** (`AssetsScreen`) | Perlu dicek — kemungkinan besar sama seperti screen lain (state lokal saja, belum benar baca/tulis file assets project) |
| **Auto-save Editor state** | `EditorStateEntity`/`EditorStateDao` sudah ada di skema DB tapi belum diaudit apakah dipakai — kemungkinan besar juga dead code, perlu dicek |
| **Export ke GitHub langsung dari app** (opsional, lanjutan Fase 6) | Alih-alih user manual `git push`, integrasi GitHub REST API (create repo + push via Personal Access Token yang diinput user) — nice-to-have, bukan prasyarat |
| **Preview orientasi live-switch tanpa restart activity** | Sekarang `GamePreviewActivity` set orientasi sekali di `onCreate()`; kalau user ganti setting `targetOrientation` project SAAT preview sedang terbuka, tidak ada mekanisme refresh — perlu `ActivityResultLauncher`/relaunch |
| **Validasi `targetOrientation`** | Saat ini string bebas tanpa validasi di layer manapun (Kotlin maupun saat nanti generate manifest export) — rawan typo silent-fail (fallback ke landscape tanpa warning). Pertimbangkan enum/sealed class alih-alih `String` mentah. |
| **Template `export-template/`** (§10) | Folder/skeleton project game minimal untuk Fase 6 — belum ada sama sekali, perlu dibuat dari nol |

---

## 14. Progress Sesi Ini (Update Otomatis)

Mengerjakan Fase 0 (§6, prioritas tertinggi) dimulai dari Smart Snippets (§12)
sebagai proof-of-concept wiring screen↔ViewModel↔Room, ditambah wiring
`ProjectsScreen` karena itu prasyarat hampir semua screen lain.

**Selesai:**
- `AppViewModelFactory` (baru, `di/AppViewModelFactory.kt`) — implementasi
  `ViewModelProvider.Factory` manual yang sebelumnya tidak ada sama sekali
  (lihat §4/§13). Menyediakan `ProjectViewModel`, `SnippetViewModel`,
  `EditorViewModel`, `BuildViewModel` dari `AppModule` + `NativeEngine`.
  Diekspos lewat `PolyGlueApplication.viewModelFactory`.
- **Smart Snippets (§12) item 1–3 selesai:**
  - `SnippetSeed.kt` (baru) — 28 snippet built-in dengan `luaTemplate` kode
    Lua ASLI (sebelumnya cuma nama+deskripsi di UI, field `luaTemplate` di
    DB kosong/dead).
  - `PolyGlueDatabase` — seed otomatis lewat `RoomDatabase.Callback.onCreate()`
    (hanya jalan sekali saat file DB baru dibuat, tidak duplikat).
  - `SnippetsScreen.kt` — diganti total dari hardcoded `List<Triple<...>>` ke
    `SnippetViewModel.allSnippets`/`searchResults` (Room via Flow/StateFlow).
    Tombol Favorite & Add sekarang benar-benar memanggil
    `toggleFavorite()`/`useSnippet()` (tersimpan ke Room, bukan `onClick={}`
    kosong lagi).
  - Item 4 (sisip `luaTemplate` ke script Editor aktif) **BELUM selesai** —
    `SnippetsScreen` sekarang menerima parameter opsional
    `onInsertSnippet: ((SnippetEntity) -> Unit)?` sebagai titik integrasi,
    tapi `EditorScreen` sendiri masih 100% state lokal (belum ada konsep
    scene/script aktif sama sekali di layer UI-nya — lihat catatan di bawah),
    jadi belum ada yang bisa disambungkan ke callback ini. Perlu dikerjakan
    bersamaan dengan wiring `EditorScreen` (lihat poin "Belum Dikerjakan").
- **`ProjectsScreen.kt` wired ke `ProjectViewModel`/Room:**
  - List proyek sekarang dari `ProjectViewModel.projects` (Room, real-time via
    Flow), bukan `sampleProjects` hardcoded.
  - Dialog "Proyek Baru" sekarang benar-benar memanggil
    `viewModel.createProject()` (sebelumnya `onClick` di confirm button cuma
    `showDialog = false`, tidak pernah insert ke DB).
  - Klik kartu proyek navigasi pakai `project.id` asli (sebelumnya hardcoded
    `"sample-id"` untuk semua proyek).
  - Menu "⋮" sekarang fungsional: Duplikat (`duplicateProject`) & Hapus
    (`deleteProject`, dengan dialog konfirmasi) — sebelumnya `onClick = {}`.
  - Empty-state ditambahkan untuk kondisi belum ada proyek.

**Belum dikerjakan (di luar scope sesi ini, prioritas lanjutan):**
- `EditorScreen`, `AssetsScreen`, `BuildScreen`, `HomeScreen`, `SettingsScreen`
  MASIH belum wired ke ViewModel (§4 poin "SEMUA 7 SCREEN..." sekarang tinggal
  sebagian benar — Projects & Snippets sudah, 5 sisanya belum). `EditorScreen`
  khususnya masih belum punya konsep "scene aktif" di UI sama sekali (tidak
  memanggil `EditorViewModel.loadScene()`, hierarchy panel & inspector masih
  teks statis) — ini butuh pekerjaan tersendiri yang cukup besar (text editor
  untuk tab "Code" yang bind ke `SceneEntity.luaScript`, hierarchy dari
  `entitiesJson`, dsb) sebelum item 4 Smart Snippets & fase Export (§6 fase 6)
  bisa lanjut.
- Fase 1–9 di roadmap (§6) — rendering, physics, audio, particle/tilemap/
  lighting/shader, 15 fungsi native yang hilang, Export Project, adaptive nav,
  Chaquopy — SEMUA masih di status yang sama seperti sebelum sesi ini, TIDAK
  disentuh sesi ini.

**Catatan verifikasi:** Sandbox pengerjaan tidak punya akses jaringan/Android
SDK/Gradle wrapper (`gradlew` belum di-commit — lihat §8 bug #7), jadi
perubahan di atas TIDAK bisa dibuild/dites lewat `./gradlew build` di sini.
Sudah diverifikasi manual: kurung/brace seimbang di semua file yang diubah,
seluruh import dipakai & sesuai package yang benar, tanda tangan fungsi yang
dipanggil dari `PolyGlueNavHost.kt` tetap kompatibel (parameter baru
opsional), dan pola yang dipakai (Room `Callback`, `viewModel(factory=...)`,
`collectAsState()`) konsisten dengan dependency yang sudah ada di
`app/build.gradle.kts` (`lifecycle-viewmodel-compose`, `room-ktx`). **Agent
berikutnya WAJIB jalankan CI/`gradlew build` sungguhan sebagai verifikasi
final** begitu ada akses jaringan, sebelum menganggap perubahan ini 100% aman
(ikuti alur debugging CI di §9 kalau ada error).

---

## 15. Progress Sesi Ini #2 — Lanjutan "Lengkapi Semua Fitur" (Update Otomatis)

Diminta melengkapi SEMUA fitur. Realitanya scope penuh (rendering native
OpenGL, physics Box2D, audio engine, particle/tilemap/lighting/shader native,
15 fungsi native yang hilang, Export Project APK sungguhan, Chaquopy) adalah
pekerjaan native C++ berminggu-minggu yang butuh NDK + device fisik untuk
diverifikasi — TIDAK realistis diselesaikan atau diverifikasi dalam satu sesi
sandbox tanpa Android SDK/NDK/network. Fokus sesi ini: **menuntaskan Fase 0**
(wiring UI↔ViewModel↔Room untuk seluruh 7 screen) sampai benar-benar selesai,
karena itu prasyarat semua fase lain dan sepenuhnya bisa dikerjakan dari sini.

**Selesai (melengkapi sisa Fase 0):**
- **`EditorScreen` + `EditorViewModel`** — sebelumnya 100% state lokal, sekarang:
  - `loadProject(projectId)` memuat scene pertama project dari Room otomatis.
  - Tab **Code**: `OutlinedTextField` beneran terikat ke `SceneEntity.luaScript`,
    tersimpan ke Room tiap perubahan lewat `updateScript()`.
  - Tab **Nodes** & panel Hierarchy/Inspector: entity sekarang model nyata
    (`SceneNode`, JSON array di `entitiesJson`), bisa tambah/hapus/pilih node
    dan edit transform X/Y — semua tersimpan ke Room (`addNode`/`deleteNode`/
    `updateNodeTransform`).
  - Tab **Console**: log Play/Stop & aktivitas lain dari `EditorViewModel`.
  - Tombol Play sekarang membuka `GamePreviewActivity` dengan orientasi dari
    `BuildConfigEntity` project (bukan hardcoded), dan menjalankan
    `luaScript` scene lewat `LuaRuntime.executeScript()`.
  - Tombol Snippets sekarang membawa `sceneId` scene aktif — jadi **item 4
    Smart Snippets (§12) SEKARANG SELESAI**: tombol "Add" di `SnippetsScreen`
    betul-betul menyisipkan `luaTemplate` ke `luaScript` scene aktif lewat
    `SceneRepository.appendScript()` (query SQL `luaScript || :snippet`).
  - Bug kecil ikut diperbaiki: `EditorViewModel.onTouch()` sebelumnya memanggil
    `callFunction(..., arrayOf(...))` (satu array dibungkus jadi 1 argumen,
    tidak match `vararg args: Any`) — sekarang di-spread jadi argumen individual.
- **`BuildScreen` + `BuildViewModel`** — sebelumnya semua field statis/
  `onClick = {}` termasuk tombol "Build APK" itu sendiri:
  - UI sekarang state-machine penuh mengikuti `BuildRepository.BuildState`
    (Idle → Preparing → Compiling → Packaging → Signing → Success/Error),
    progress bar & pesan REAL dari repository, bukan `buildProgress` lokal
    yang tidak pernah diisi.
  - Field Package Name & Version Name sekarang mengedit `ProjectEntity` asli
    (tersimpan ke Room).
  - Checkbox "Enable ProGuard" & pilihan orientasi sekarang baca/tulis
    `BuildConfigEntity` asli lewat `BuildViewModel.loadConfig/setOrientation/
    setEnableProguard` — **ini menuntaskan catatan §5.2 soal orientasi harus
    baca dari sumber yang sama**: Editor (Play) dan Build sekarang sama-sama
    baca `BuildConfigEntity.targetOrientation`.
  - Tombol "Build APK" memanggil `BuildViewModel.build(projectId)` yang
    benar-benar mengambil `ProjectEntity` + scene pertama dari Room lalu
    memanggil `BuildRepository.buildAPK()`.
  - **Catatan jujur**: keluaran "APK" tetap ZIP (source Lua + scene JSON),
    BUKAN APK terinstall sungguhan — ini keterbatasan `BuildRepository`
    sendiri (lihat §4), bukan sesuatu yang bisa diperbaiki lewat wiring UI;
    perlu Fase 6 (Export Project/Gradle template generator) yang memang
    belum digarap. Pesan ini sekarang ditampilkan eksplisit ke user di UI
    Build agar tidak menyesatkan.
- **`AssetsScreen` + `AssetRepository`** — sebelumnya grid 12 kartu placeholder
  statis & tombol Import kosong:
  - List asset sekarang dari Room (`AssetRepository.getAssetsByProject`,
    real-time via Flow), difilter per tab (image/audio/font/script/shader).
  - Tombol Import sekarang benar-benar membuka System Access Framework
    (`ActivityResultContracts.OpenDocument`), meng-copy file terpilih ke
    `getExternalFilesDir()/projects/{id}/assets/`, deteksi tipe dari MIME/
    ekstensi, lalu insert `AssetEntity` ke Room.

**Sekarang semua 7 screen (Home, Projects, Snippets, Editor, Assets, Build,
Settings) terhubung ke ViewModel/Room** kecuali `SettingsScreen`, yang
sengaja dibiarkan sebagai daftar menu statis (GitHub Integration, Editor
Preferences, Performance, About) karena masing-masing itemnya adalah fitur
besar sendiri di luar cakupan "wiring" (OAuth GitHub, preference storage,
profiler) — bukan bug, tapi placeholder yang jujur belum diimplementasi.

**BENAR-BENAR belum dikerjakan (di luar kemampuan sandbox ini):**
- Fase 1–9 penuh: renderer OpenGL ES native, physics Box2D, audio engine,
  particle/tilemap/lighting/shader native, 15 fungsi native `external fun`
  yang belum ada implementasi C++-nya (lihat daftar `nativeXxx` di
  `NativeEngine.kt` — semuanya `external`, belum tentu semua diimplementasikan
  di sisi JNI/C++, dan sandbox ini tidak mengecek/menyentuh source C++ sama
  sekali di kedua sesi), Export Project (template Gradle project baru yang
  bisa di-`gradlew assembleRelease` jadi APK asli), integrasi Chaquopy,
  adaptive navigation per §11. Ini genuinely pekerjaan native/C++
  multi-minggu yang butuh NDK toolchain + device fisik untuk verifikasi —
  TIDAK bisa dan tidak coba dikerjakan di sandbox tanpa SDK/NDK/network ini.
- Sub-fitur di `SettingsScreen` (GitHub OAuth, editor preferences storage,
  performance profiler, About) — placeholder statis, belum ada.

**Verifikasi:** sama seperti sesi #1 — tidak ada akses network/SDK/NDK/
`gradlew` di sandbox ini, jadi tidak ada satu pun perubahan (sesi ini maupun
sebelumnya) yang sudah dibuild sungguhan. Sudah dicek manual: kurung/brace/
bracket seimbang di SEMUA file yang diubah sesi ini, seluruh pemanggilan
fungsi (`SceneRepository`, `ProjectRepository`, `AssetRepository`,
`BuildRepository`, DAO) dicocokkan satu-satu terhadap signature aslinya,
semua route Navigation Compose (`PolyGlueNavHost.kt`) dan pemanggilnya
dicocokkan string literal-nya. **Prioritas #1 untuk agent/developer
berikutnya: jalankan `./gradlew assembleDebug` (atau CI) begitu ada akses
Android SDK/NDK** — ini satu-satunya cara memverifikasi sungguhan bahwa kode
di atas benar-benar compile, sebelum lanjut ke Fase 1 (native rendering).
