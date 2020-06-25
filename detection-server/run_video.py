from server import ServerSocket
from yolo import YOLO
from deep_sort import preprocessing
from deep_sort import nn_matching
from deep_sort.detection import Detection
from deep_sort.tracker import Tracker
from tools import generate_detections as gdet
from deep_sort.detection import Detection as ddet

from speed_detecter.ObjectInfoManager import ObjectInfoManager

import logging
import cv2
import time
import numpy as np
from PIL import Image
from copy import deepcopy
import pymysql
import json
from datetime import datetime

from shapely.geometry import Point, Polygon

from crosswalkMask import CrosswalkMask
from mrcnn import visualize


logging.basicConfig(level=logging.WARN, format='%(asctime)s %(message)s')
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

ShowImage = True


TypeCode = {
    'person': 0,
    'car': 1,
    'bike': 2,
    'bus': 3,
    'truck': 4
}

class Daemon:

    recentImage = None
    serverInstance = None
    yoloInstance = None
    maskrcnnInstance = None
    objHistoryManager = None

    dbConn = None
    crosswalks = {}
    unmatched_crosswalks = []
    crosswalk_tgt = []

    def __init__(self):
        # 서버 데몬이 시작할 때 시작
        logger.info('Starting Server Daemon...')

        self.recentImage = []
        self.imageScale = 1

        self.objHistoryManager = ObjectInfoManager()



    def start(self):
        # logger.info('Starting ImageStreaming Server')
        #self.serverInstance = ServerSocket('0.0.0.0', 4444)
        #self.serverInstance.startReceiveRecentImage(self.recentImage)
        #logger.info('Started Image Streaming Server')

        logger.info('Starting Yolo Instance')
        self.yoloInstance = YOLO()
        logger.info('Started Yolo Instance')

        logger.info('Starting DeepSort Instance')
        encoder = gdet.create_box_encoder('model_data/mars-small128.pb', batch_size=1)
        metric = nn_matching.NearestNeighborDistanceMetric("cosine", 0.3, None)
        tracker = Tracker(metric)
        logger.info('Started DeepSort Instance')

        logger.info('Starting MaskRCNN Instance')
        self.maskrcnnInstance = CrosswalkMask()
        logger.info('Starting MaskRCNN Instance')


        logger.info('Connecting to Database')
        self.dbConn = pymysql.connect(
            user='capstone',
            passwd='capstone2020',
            host='127.0.0.1',
            db='capstone',
            charset='utf8'
        )
        logger.info('Connected to Database')

        cap = cv2.VideoCapture('./sample_video/project(15h40m49s).mp4')
        fourcc = cv2.VideoWriter_fourcc(*'DIVX')
        out = cv2.VideoWriter('sample_output.avi', fourcc, 10.0, (480, 316))


        fps = 0.0
        recent_tracktime = time.time()
        recent_crosswalktime = time.time()
        class_names = {0: 'bg', 1: 'crosswalk'}

        # img = cv2.imread('img3734.jpg')
        #self.recentImage.append(img)

        # self.redetected_crosswalk()
        crosswalk_redetected_count = 0
        fps_count = 0

        print(cap.isOpened())
        while cap.isOpened():
            fps_count += 1
            logger.info("%d Frame Processing"%(fps_count))
            ret, frame = cap.read()
            if ret is False: break

            if fps_count == 0:
                self.recentImage.append(frame)
            else:
                self.recentImage = [frame]



            t1 = time.time()
            if not len(self.recentImage): continue


            if fps_count % 300 is 0 and crosswalk_redetected_count < 5:
                self.redetected_crosswalk()
                recent_crosswalktime = time.time()
                crosswalk_redetected_count += 1


            image = self.recentImage[0]

            ### 객체인식 파트
            image_pil = Image.fromarray(image)
            boxs, labels = self.yoloInstance.detect_image(image_pil)

            # print("box_num",len(boxs))
            features = encoder(image, boxs)

            # score to 1.0 here).
            detections = [Detection(bbox, 0.5, feature, label) for bbox, feature, label in zip(boxs, features, labels)]

            # Run non-maxima suppression.
            boxes = np.array([d.tlwh for d in detections])
            scores = np.array([d.confidence for d in detections])
            indices = preprocessing.non_max_suppression(boxes, 1.0, scores)
            detections = [detections[i] for i in indices]

            # Call the tracker
            tracker.predict()
            tracker.update(detections)


            scaled_frame = cv2.resize(image, dsize=(image.shape[1] * self.imageScale, image.shape[0] * self.imageScale))

            for track in tracker.tracks:
                #if frame_index % 5 == 0:

                self.objHistoryManager.insertTrack(deepcopy(track))
                recent_tracktime = time.time()
                bbox = track.to_tlbr() * self.imageScale
                cv2.rectangle(scaled_frame, (int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])), (255, 0, 0), 1)

                if not track.is_confirmed() or track.time_since_update > 1:
                    continue

            '''
                횡단보도에 있는 객체들을 반환합니다.
            '''
            # self.maskrcnnInstance.getConvertedCoordinate(stdMap, dataList, crosswalk_index):

            for cw_id, cw in self.crosswalks.items():
                obj_infos = []

                polygon = Polygon(cw['rects'])
                for track in tracker.tracks:
                    if not track.is_confirmed() or track.time_since_update > 100:
                        continue

                    tlbr = track.to_tlbr()
                    obj_center = (int((tlbr[0] + tlbr[2]) / 2), int((tlbr[1] + tlbr[3]) / 2))

                    if polygon.contains(Point([obj_center])):
                        # 만약 물체가 해당 행단보도에 위치한 경우
                        conv_coord = self.maskrcnnInstance.getConvertedCoordinate(cw['rects'], [np.array(obj_center)], cw['id'])[0]

                        if track.label not in TypeCode.keys():
                            continue

                        detect_info = {
                            'type': TypeCode[track.label],
                            'x': conv_coord[0],
                            'y': conv_coord[1],
                        }
                        # 방향 추출 코드 (jonathan)
                        if track.label == 'person':
                            if len(track.convertCordi) == 0:
                                track.convertCordi = conv_coord
                                now_track_direction = 0
                            else:
                                old_track_convert_cordi = track.convertCordi
                                if old_track_convert_cordi[0] == conv_coord[0]:
                                    now_track_direction = 0
                                else:
                                    now_track_direction = 1 if conv_coord[0] - old_track_convert_cordi[0] > 0 else -1
                        # --------
                            # detect_info['direction'] = self.objHistoryManager.get_object_direction(track.track_id)
                            detect_info['direction'] = now_track_direction

                        obj_infos.append(detect_info)



            self.objHistoryManager.drawOnImage(scaled_frame, self.imageScale)

            for det in detections:
                bbox = det.to_tlbr() * self.imageScale
                cv2.rectangle(scaled_frame, (int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])), (255, 255, 255), 1)
                cv2.putText(scaled_frame, str(det.label), (int(bbox[0]), int(bbox[1])), 1, 0.5 * self.imageScale, (255, 255, 255), 1)

            for cw_id, cw in self.crosswalks.items():
                color = (0, 255 ,0)
                center = cw['center']
                xy = np.array(cw['rects'], dtype=np.int32) * self.imageScale
                scaled_frame = cv2.polylines(scaled_frame, [xy], True, color)
                # scaled_frame = cv2.putText(scaled_frame, str(cw['id']), center, cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 1, color, 2)

            for cw in self.unmatched_crosswalks:
                color = (0, 0, 255)
                xy = np.array(cw['rects'], dtype=np.int32) * self.imageScale
                scaled_frame = cv2.polylines(scaled_frame, [xy], True, color)


            for cw_info in self.crosswalk_tgt:
                if cw_info['cctv_x'] is None or cw_info['cctv_y'] is None:
                    continue

                color = (0, 0, 255)

                img_w = self.recentImage[0].shape[1]
                img_h = self.recentImage[0].shape[0]
                abs_x = int(cw_info['cctv_x'] * img_w * self.imageScale)
                abs_y = int(cw_info['cctv_y'] * img_h * self.imageScale)

                center = (abs_x, abs_y)
                # scaled_frame = cv2.putText(scaled_frame, str(cw_info['id']), center, cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 1, color, 2)

            out.write(scaled_frame)
            if ShowImage:
                cv2.imshow('Result Detected Result', scaled_frame)
                cv2.waitKey(1)



        cap.release()
        out.release()

    def redetected_crosswalk(self):
        logger.info('Re-detecting Crosswalk')

        '''
            Get All Crosswalk List
        '''
        sql = "SELECT * FROM `crosswalk` WHERE intersection_id=3;"
        cursor = self.dbConn.cursor(pymysql.cursors.DictCursor)
        cursor.execute(sql)
        cw_infos = cursor.fetchall()

        self.crosswalk_tgt = cw_infos


        if len(self.recentImage):
            img_w = self.recentImage[0].shape[1]
            img_h = self.recentImage[0].shape[0]

            self.unmatched_crosswalks = []

            info = self.maskrcnnInstance.detectCrosswalk(self.recentImage[0])
            logger.info(str(len(info['rects'])) + " of Crosswalk Detected!")
            for id_, cw in enumerate(info['rects']):
                polygon = Polygon(cw)

                cw_item = {
                    'rects': cw,
                    'center': None,
                    'intersection_id': 3,
                    'id': None,
                }


                for idx, cw_info in enumerate(cw_infos):

                    if cw_info['cctv_x'] is None or cw_info['cctv_y'] is None: continue

                    abs_x = int(cw_info['cctv_x'] * img_w)
                    abs_y = int(cw_info['cctv_y'] * img_h)

                    if polygon.contains(Point([(abs_x, abs_y)])):
                        cw_item['id'] = cw_info['id']
                        cw_item['center'] = (abs_x, abs_y)
                        cw_item['cordis'] = deepcopy(info['cordis'][id_][0])
                        break


                if cw_item['id'] is None:
                    self.unmatched_crosswalks.append(cw_item)
                    continue



                if cw_item['id'] not in self.crosswalks.keys():
                    self.crosswalks[cw_item['id']] = deepcopy(cw_item)
                    logger.info('Crosswalk Registered! - %s'%(cw_item['id']))
                elif cw_item['id'] in self.crosswalks.keys() and cw_item['id'] is not None:
                    oldArea = self.maskrcnnInstance.getAreaValue(self.crosswalks[cw_item['id']]['cordis'])
                    newArea = self.maskrcnnInstance.getAreaValue(cw_item['cordis'])
                    if newArea > oldArea:
                        self.crosswalks[cw_item['id']] = deepcopy(cw_item)
                        logger.info('Crosswalk Refresh Accept! - %s (Old: %d, New: %d)' % (cw_item['id'], oldArea, newArea))
                    else:
                        logger.info('Crosswalk Refresh Rejected! - %s (Old: %d, New: %d)' % (cw_item['id'], oldArea, newArea))




daemon = Daemon()
daemon.start()












