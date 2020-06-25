from math import *

def get_center_pos(pos):
    return (
        int(pos[0] + (pos[2] - pos[0]) / 2),
        int(pos[1] + (pos[3] - pos[1]) / 2)
    )


def euclidean_distance(x,y):
    return sqrt(sum(pow(a-b,2) for a, b in zip(x, y)))