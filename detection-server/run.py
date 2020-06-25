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

from crosswalkMask import CrosswalkMask
from mrcnn import visualize

logging.basicConfig(level=logging.WARN, format='%(asctime)s %(message)s')
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

ShowImage = True

class Daemon:

    recentImage = None
    serverInstance = None
    yoloInstance = None
    maskrcnnInstance = None
    objHistoryManager = None
    bestCrosswalk = None

    def __init__(self):
        # 서버 데몬이 시작할 때 시작
        logger.info('Starting Server Daemon...')

        self.recentImage = []
        self.imageScale = 2

        self.objHistoryManager = ObjectInfoManager()



    def start(self):
        logger.info('Starting ImageStreaming Server')
        self.serverInstance = ServerSocket('0.0.0.0', 4444)
        self.serverInstance.startReceiveRecentImage(self.recentImage)
        logger.info('Started Image Streaming Server')

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


        testImage = cv2.imread('crosswalk14.PNG')
        self.recentImage.append(testImage)

        fps = 0.0
        recent_tracktime = time.time()
        recent_crosswalktime = time.time()
        class_names = {0: 'bg', 1: 'crosswalk'}

        # self.redetected_crosswalk()

        while True:

            t1 = time.time()
            if not len(self.recentImage): continue

            # if self.bestCrosswalk == None: self.redetected_crosswalk()

            #if time.time() - recent_crosswalktime > 30:
            #    recent_crosswalktime = time.time()
            #    self.redetected_crosswalk()

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

            # image = visualize.display_instances(image, self.bestCrosswalk['rois'], self.bestCrosswalk['masks'], self.bestCrosswalk['class_ids'], class_names, self.bestCrosswalk['scores'],
            #                            title="Predictions")

            scaled_frame = cv2.resize(image, dsize=(image.shape[1] * self.imageScale, image.shape[0] * self.imageScale))

            for track in tracker.tracks:
                #if frame_index % 5 == 0:
                if time.time() - recent_tracktime > 0.1:
                    self.objHistoryManager.insertTrack(deepcopy(track))
                    recent_tracktime = time.time()
                bbox = track.to_tlbr() * self.imageScale
                cv2.rectangle(scaled_frame, (int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])), (255, 0, 0), 1)

                if not track.is_confirmed() or track.time_since_update > 1:
                    continue

                # cv2.putText(scaled_frame, str(track.track_id),(int(bbox[0]), int(bbox[1])), 1, 0.5 * scale, (255,255,255), 1)

            self.objHistoryManager.drawOnImage(scaled_frame, self.imageScale)

            for det in detections:
                bbox = det.to_tlbr() * self.imageScale
                cv2.rectangle(scaled_frame, (int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])), (255, 255, 255),
                              1)
                cv2.putText(scaled_frame, str(det.label), (int(bbox[0]), int(bbox[1])), 1, 0.5 * self.imageScale, (255, 255, 255),
                            1)
            cv2.putText(scaled_frame, "FPS %.1f" % (fps), (20, 30), 1, 1 * self.imageScale, (255, 255, 255), 2)



            if ShowImage:
                cv2.imshow('Result Detected Result', scaled_frame)
                cv2.waitKey(1)

            fps = (fps + (1. / (time.time() - t1))) / 2

    def redetected_crosswalk(self):
        logger.info('Re-detecting Crosswalk')

        if len(self.recentImage):
            ret = self.maskrcnnInstance.detectCrosswalk(self.recentImage[0])
            if self.bestCrosswalk == None:
                self.bestCrosswalk = ret
                logger.info('Changed Best Crosswalk')
            elif self.bestCrosswalk['scores'] < ret['scores']:
                self.bestCrosswalk = ret
                logger.info('Changed Best Crosswalk')
            else:
                logger.info('Not Changed Crosswalk')


daemon = Daemon()
daemon.start()












