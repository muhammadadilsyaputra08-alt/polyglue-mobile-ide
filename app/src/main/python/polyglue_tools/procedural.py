import random
from typing import List, Tuple, Dict

def generate_dungeon(width: int, height: int, room_count: int = 10) -> Dict:
    grid = [[1 for _ in range(width)] for _ in range(height)]
    rooms = []

    for _ in range(room_count):
        w = random.randint(4, 10)
        h = random.randint(4, 10)
        x = random.randint(1, width - w - 1)
        y = random.randint(1, height - h - 1)
        rooms.append((x, y, w, h))
        for ry in range(y, y + h):
            for rx in range(x, x + w):
                grid[ry][rx] = 0

    for i in range(len(rooms) - 1):
        x1, y1 = rooms[i][0] + rooms[i][2] // 2, rooms[i][1] + rooms[i][3] // 2
        x2, y2 = rooms[i+1][0] + rooms[i+1][2] // 2, rooms[i+1][1] + rooms[i+1][3] // 2
        while x1 != x2:
            grid[y1][x1] = 0
            x1 += 1 if x2 > x1 else -1
        while y1 != y2:
            grid[y1][x1] = 0
            y1 += 1 if y2 > y1 else -1

    return {
        "width": width,
        "height": height,
        "grid": grid,
        "rooms": rooms,
        "spawn": (rooms[0][0] + rooms[0][2] // 2, rooms[0][1] + rooms[0][3] // 2) if rooms else (width//2, height//2)
    }

def generate_terrain(width: int, height: int, seed: int = None) -> List[List[float]]:
    if seed is not None:
        random.seed(seed)

    terrain = []
    for y in range(height):
        row = []
        for x in range(width):
            value = random.random() * 2 - 1
            row.append(value)
        terrain.append(row)
    return terrain
