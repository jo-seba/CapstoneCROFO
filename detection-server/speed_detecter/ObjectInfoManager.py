from collections import deque
import time
import cv2
from pprint import pprint
from copy import deepcopy
from utils import get_center_pos, euclidean_distance

class SpeedState:
    Default = 0
    Slow = -1
    Fast = 1

class SpeedColor:
    LightBlue = (100, 255, 100)
    LightRed = (100, 100, 255)
    White = (255, 255, 255)

    def get_color(self, speed_state):
        if speed_state == SpeedState.Fast:
            return self.LightBlue
        elif speed_state == SpeedState.Slow:
            return self.LightRed
        else:
            return self.White

class ObjectInfoManager:




    def __init__(self):

        self.obj_history = {}




    def clean(self):
        pass


    def insertTrack(self, track):
        track.hist_time = time.time()

        # print(track.track_id)
        if not (track.track_id in self.obj_history.keys()):
            self.obj_history[track.track_id] = deque(maxlen=10)  # 가장 최신의 5개의 데이터만 저장함

        if len(self.obj_history[track.track_id]):
            last_commit_time = self.obj_history[track.track_id][-1].hist_time

            if time.time() - last_commit_time > 0.5: # 1초마다 저장

                prev_pos = (self.obj_history[track.track_id][-1].to_tlbr()[0], self.obj_history[track.track_id][-1].to_tlbr()[1])
                curr_pos = (self.obj_history[track.track_id][-1].to_tlbr()[2], self.obj_history[track.track_id][-1].to_tlbr()[3])

                track.speed_scalar = euclidean_distance(prev_pos, curr_pos) / (track.hist_time - self.obj_history[track.track_id][-1].hist_time)

                if track.speed_scalar - self.obj_history[track.track_id][-1].speed_scalar > 0:
                    track.speed_state = SpeedState.Fast
                elif track.speed_scalar - self.obj_history[track.track_id][-1].speed_scalar < 0:
                    track.speed_state = SpeedState.Slow
                else:
                    track.speed_state = SpeedState.Default

                self.obj_history[track.track_id].append(track)
        else:
            track.speed_scalar = 0
            track.speed_state = SpeedState.Default
            self.obj_history[track.track_id].append(track)

        self.clean()


    def drawOnImage(self, image, scale = 1):

        for track_id, history in self.obj_history.items():


            last_commit_time = history[-1].hist_time
            
            # 기록이 3분전일경우 삭제함

            if time.time() - last_commit_time > 5:
                continue

            # Draw First Circle
            center_pos = get_center_pos(history[0].to_tlbr() * scale)
            cv2.circle(image, center_pos,  1, (255, 255, 255), -1)
            # cv2.putText(image, str(track_id), center_pos, 1, 0.5 * scale, (0, 255, 0), 1)

            for i, h in enumerate(history):
                if i == 0: continue

                center_pos = get_center_pos(h.to_tlbr() * scale)

                cv2.circle(image, center_pos, int((i + 1) / len(history) * 3), (255, 255, 255), -1)

                cv2.line(
                    image,
                    get_center_pos(history[i - 1].to_tlbr() * scale),
                    get_center_pos(history[i].to_tlbr() * scale),
                    SpeedColor().get_color(h.speed_state),
                    1
                )



    def get_object_direction(self, track_id) -> int:


        if track_id not in self.obj_history.keys(): return 0
        if len(self.obj_history[track_id]) < 2: return 0


        now_tlbr = self.obj_history[track_id][-1].to_tlbr()
        prev_tlbr = self.obj_history[track_id][-2].to_tlbr()

        prev_center = (int((prev_tlbr[0] + prev_tlbr[2]) / 2), int((prev_tlbr[1] + prev_tlbr[3]) / 2))
        now_center = (int((now_tlbr[0] + now_tlbr[2]) / 2), int((now_tlbr[1] + now_tlbr[3]) / 2))

        if prev_center[0] < now_center[0]:
            return 1
        elif prev_center[0] > now_center[0]:
            return -1
        else:
            return 0


