import json
import sys
from polyglue_tools.procedural import generate_dungeon, generate_terrain
from polyglue_tools.optimize import optimize_texture, create_texture_atlas
from polyglue_tools.codegen import generate_lua_from_nodes, generate_npc_name, generate_dialogue
from polyglue_tools.ai_assistant import PolyGlueAI

def main():
    ai = PolyGlueAI()
    while True:
        try:
            line = input()
            command = json.loads(line)
            action = command.get("action")

            if action == "generate_dungeon":
                result = generate_dungeon(command["width"], command["height"], command.get("room_count", 10))
                print(json.dumps({"status": "ok", "result": result}))
            elif action == "generate_terrain":
                result = generate_terrain(command["width"], command["height"], command.get("seed"))
                print(json.dumps({"status": "ok", "result": result}))
            elif action == "optimize_texture":
                success = optimize_texture(command["input"], command["output"], command.get("max_size", 1024))
                print(json.dumps({"status": "ok" if success else "error"}))
            elif action == "create_atlas":
                result = create_texture_atlas(command["images"], command["output"], command.get("max_size", 2048))
                print(json.dumps({"status": "ok", "result": result}))
            elif action == "generate_lua":
                result = generate_lua_from_nodes(command["node_graph"])
                print(json.dumps({"status": "ok", "code": result}))
            elif action == "generate_npc":
                result = generate_npc_name(command.get("style", "fantasy"))
                print(json.dumps({"status": "ok", "name": result}))
            elif action == "generate_dialogue":
                result = generate_dialogue(command.get("topic", "greeting"))
                print(json.dumps({"status": "ok", "dialogue": result}))
            elif action == "suggest_snippet":
                result = ai.suggest_snippet(command["description"])
                print(json.dumps({"status": "ok", "snippet": result}))
            elif action == "explain_code":
                result = ai.explain_code(command["code"])
                print(json.dumps({"status": "ok", "explanation": result}))
            elif action == "fix_code":
                result = ai.fix_code(command["code"], command["error"])
                print(json.dumps({"status": "ok", "fix": result}))
            else:
                print(json.dumps({"status": "error", "message": "Unknown action"}))
        except EOFError:
            break
        except Exception as e:
            print(json.dumps({"status": "error", "message": str(e)}))
            sys.stdout.flush()

if __name__ == "__main__":
    main()
