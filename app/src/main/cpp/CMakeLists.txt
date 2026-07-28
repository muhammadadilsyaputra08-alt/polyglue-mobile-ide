cmake_minimum_required(VERSION 3.22.1)
project("polyglue_engine")

set(CMAKE_CXX_STANDARD 20)

find_library(log-lib log)
find_library(android-lib android)
find_library(EGL-lib EGL)
find_library(GLESv3-lib GLESv3)

include_directories(
    ${CMAKE_SOURCE_DIR}
)

file(GLOB_RECURSE ENGINE_SOURCES
    ${CMAKE_SOURCE_DIR}/engine/*.cpp
    ${CMAKE_SOURCE_DIR}/renderer/*.cpp
    ${CMAKE_SOURCE_DIR}/physics/*.cpp
    ${CMAKE_SOURCE_DIR}/audio/*.cpp
    ${CMAKE_SOURCE_DIR}/lua/*.cpp
)

add_library(polyglue_engine SHARED ${ENGINE_SOURCES} polyglue_jni.cpp)

target_link_libraries(polyglue_engine ${log-lib} ${android-lib} ${EGL-lib} ${GLESv3-lib} OpenSLES)

# CATATAN: Box2D & Lua native (C) belum di-vendor ke repo ini.
# Saat ini fisika/scripting native masih stub (lihat engine.cpp), scripting sudah
# ditangani via LuaJ di sisi Kotlin (LuaRuntime.kt). Jika nanti menambahkan
# Box2D asli, taruh source-nya di app/src/main/cpp/third_party/box2d lalu aktifkan lagi:
# add_subdirectory(third_party/box2d)
# target_link_libraries(polyglue_engine box2d)
