from typing import Optional

class PolyGlueAI:
    def __init__(self):
        self.context = {}

    def suggest_snippet(self, description: str) -> Optional[str]:
        keywords = {"move": "movement_joystick", "jump": "movement_platformer", "camera": "camera_follow",
                    "enemy": "ai_patrol", "sound": "audio_play", "particle": "particle_emitter",
                    "save": "data_save", "load": "data_load"}
        desc_lower = description.lower()
        for keyword, snippet in keywords.items():
            if keyword in desc_lower:
                return snippet
        return None

    def explain_code(self, lua_code: str) -> str:
        explanations = []
        if "function" in lua_code: explanations.append("Mendefinisikan fungsi")
        if "for" in lua_code: explanations.append("Menggunakan loop")
        if "if" in lua_code: explanations.append("Menggunakan kondisi")
        if "Entity" in lua_code: explanations.append("Memanipulasi entity game")
        if "Input" in lua_code: explanations.append("Menerima input dari pemain")
        return "; ".join(explanations) if explanations else "Kode dasar Lua"

    def fix_code(self, lua_code: str, error_msg: str) -> str:
        fixes = {"attempt to index a nil value": "Periksa apakah variabel sudah diinisialisasi sebelum digunakan",
                 "attempt to call a nil value": "Fungsi tidak ditemukan. Periksa nama fungsi atau import modul",
                 "syntax error": "Periksa penulisan syntax, pastikan semua bracket dan tanda kutip tertutup"}
        for pattern, fix in fixes.items():
            if pattern in error_msg:
                return fix
        return "Periksa kembali logika kode Anda"
