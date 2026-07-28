#include "polyglue_engine.h"
#include <android/log.h>
#include <GLES3/gl3.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "PolyGlueEngine", __VA_ARGS__)

namespace polyglue {

// Stub minimal — implementasi penuh Renderer/PhysicsWorld/AudioEngine/Scene
// menyusul. Definisi (walau kosong) wajib ada di sini karena
// std::unique_ptr<T> butuh T lengkap saat ~Engine() di-generate.
class Renderer {};
class PhysicsWorld {};
class AudioEngine {};
class Scene {};

Engine::Engine() = default;
Engine::~Engine() = default;

bool Engine::initialize(const std::string& assetPath) {
    LOGI("Initializing engine with asset path: %s", assetPath.c_str());
    // Initialize OpenGL ES, Box2D, OpenSL ES
    return true;
}

void Engine::shutdown() {
    LOGI("Shutting down engine");
}

void Engine::render(float deltaTime) {
    // OpenGL ES render loop
}

void Engine::resize(int width, int height) {
    m_width = width;
    m_height = height;
    glViewport(0, 0, width, height);
}

void Engine::onTouch(int action, float x, float y, int pointerId) {
    // Handle touch input
}

void Engine::loadScene(const std::string& sceneJson) {
    // Parse JSON and load scene
}

std::string Engine::saveScene() {
    return "{}";
}

uint64_t Engine::createEntity(const std::string& name) {
    static uint64_t nextId = 1;
    return nextId++;
}

void Engine::destroyEntity(uint64_t id) {}
void Engine::setTransform(uint64_t id, float x, float y, float rotation, float scaleX, float scaleY) {}
void Engine::addSprite(uint64_t entityId, const std::string& texturePath, float width, float height) {}
void Engine::setSpriteAnimation(uint64_t entityId, const std::string& animName) {}
void Engine::addRigidbody(uint64_t entityId, int bodyType, float density, float friction, float restitution) {}
void Engine::addBoxCollider(uint64_t entityId, float width, float height, float offsetX, float offsetY) {}
void Engine::applyForce(uint64_t entityId, float fx, float fy) {}
void Engine::setVelocity(uint64_t entityId, float vx, float vy) {}

std::string Engine::raycast(float startX, float startY, float endX, float endY) {
    return "{}";
}

int Engine::loadSound(const std::string& path) { return -1; }
void Engine::playSound(int soundId, float volume, bool loop) {}
void Engine::stopSound(int soundId) {}
void Engine::setMasterVolume(float volume) {}

uint64_t Engine::createParticleSystem(const std::string& configJson) { return 0; }
void Engine::emitParticles(uint64_t systemId, float x, float y, int count) {}
void Engine::destroyParticleSystem(uint64_t systemId) {}

uint64_t Engine::createTilemap(int width, int height, float tileSize) { return 0; }
void Engine::setTile(uint64_t tilemapId, int x, int y, int tileId, int layer) {}
void Engine::setTileCollision(uint64_t tilemapId, int x, int y, bool solid) {}
void Engine::destroyTilemap(uint64_t tilemapId) {}

void Engine::setCameraPosition(float x, float y) {}
void Engine::setCameraZoom(float zoom) {}
void Engine::setCameraBounds(float minX, float minY, float maxX, float maxY) {}
void Engine::shakeCamera(float intensity, float duration) {}

uint64_t Engine::addLight(float x, float y, float radius, float r, float g, float b, float intensity) { return 0; }
void Engine::removeLight(uint64_t lightId) {}
void Engine::setAmbientLight(float r, float g, float b, float intensity) {}

int Engine::loadShader(const std::string& vertexSrc, const std::string& fragmentSrc) { return -1; }
void Engine::useShader(int shaderId) {}
void Engine::setShaderUniformFloat(int shaderId, const std::string& name, float value) {}
void Engine::setShaderUniformVec2(int shaderId, const std::string& name, float x, float y) {}

void Engine::setDebugDraw(bool enabled) { m_debugDraw = enabled; }
float Engine::getFPS() const { return m_fps; }
uint64_t Engine::getMemoryUsage() const { return 0; }
void Engine::drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}
void Engine::drawRect(float x, float y, float w, float h, float r, float g, float b, float a, bool filled) {}
void Engine::drawCircle(float x, float y, float radius, float r, float g, float b, float a, bool filled) {}

} // namespace polyglue
