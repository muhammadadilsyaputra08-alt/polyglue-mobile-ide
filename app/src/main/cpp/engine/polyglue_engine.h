#pragma once

#include <memory>
#include <string>
#include <vector>
#include <unordered_map>

namespace polyglue {

class Renderer;
class PhysicsWorld;
class AudioEngine;
class Scene;

class Engine {
public:
    Engine();
    ~Engine();

    bool initialize(const std::string& assetPath);
    void shutdown();

    void render(float deltaTime);
    void resize(int width, int height);
    void onTouch(int action, float x, float y, int pointerId);

    void loadScene(const std::string& sceneJson);
    std::string saveScene();

    uint64_t createEntity(const std::string& name);
    void destroyEntity(uint64_t id);
    void setTransform(uint64_t id, float x, float y, float rotation, float scaleX, float scaleY);

    void addSprite(uint64_t entityId, const std::string& texturePath, float width, float height);
    void setSpriteAnimation(uint64_t entityId, const std::string& animName);
    void addRigidbody(uint64_t entityId, int bodyType, float density, float friction, float restitution);
    void addBoxCollider(uint64_t entityId, float width, float height, float offsetX, float offsetY);
    void applyForce(uint64_t entityId, float fx, float fy);
    void setVelocity(uint64_t entityId, float vx, float vy);
    std::string raycast(float startX, float startY, float endX, float endY);

    int loadSound(const std::string& path);
    void playSound(int soundId, float volume, bool loop);
    void stopSound(int soundId);
    void setMasterVolume(float volume);

    uint64_t createParticleSystem(const std::string& configJson);
    void emitParticles(uint64_t systemId, float x, float y, int count);
    void destroyParticleSystem(uint64_t systemId);

    uint64_t createTilemap(int width, int height, float tileSize);
    void setTile(uint64_t tilemapId, int x, int y, int tileId, int layer);
    void setTileCollision(uint64_t tilemapId, int x, int y, bool solid);
    void destroyTilemap(uint64_t tilemapId);

    void setCameraPosition(float x, float y);
    void setCameraZoom(float zoom);
    void setCameraBounds(float minX, float minY, float maxX, float maxY);
    void shakeCamera(float intensity, float duration);

    uint64_t addLight(float x, float y, float radius, float r, float g, float b, float intensity);
    void removeLight(uint64_t lightId);
    void setAmbientLight(float r, float g, float b, float intensity);

    int loadShader(const std::string& vertexSrc, const std::string& fragmentSrc);
    void useShader(int shaderId);
    void setShaderUniformFloat(int shaderId, const std::string& name, float value);
    void setShaderUniformVec2(int shaderId, const std::string& name, float x, float y);

    void setDebugDraw(bool enabled);
    float getFPS() const;
    uint64_t getMemoryUsage() const;
    void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a);
    void drawRect(float x, float y, float w, float h, float r, float g, float b, float a, bool filled);
    void drawCircle(float x, float y, float radius, float r, float g, float b, float a, bool filled);

private:
    std::unique_ptr<Renderer> m_renderer;
    std::unique_ptr<PhysicsWorld> m_physics;
    std::unique_ptr<AudioEngine> m_audio;
    std::unique_ptr<Scene> m_scene;

    int m_width = 0;
    int m_height = 0;
    float m_fps = 0.0f;
    bool m_debugDraw = false;
};

} // namespace polyglue
