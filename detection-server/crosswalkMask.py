import os, sys
import cv2
import numpy as np
from skimage.transform import ProjectiveTransform
from shapely.geometry import Point, Polygon

CUR_PATH = os.path.abspath('.')
WEIGHT_PATH = os.path.join(CUR_PATH, os.path.join('model_data', 'cwRCNN.h5'))

import mrcnn.model as modellib
import mrcnn.config as configlib
from mrcnn import visualize

class CrosswalkMask:
    def __init__(self):
        self.config = configlib.Config()
        self.model = modellib.MaskRCNN(mode='inference', config=self.config, model_dir='./model_data')
        self.model.load_weights(WEIGHT_PATH, by_name=True)
        self.class_names = {0:'bg', 1:'crosswalk'}
        self.result = {}

    def detectCrosswalk(self, frame, OPENCV_FORMAT=True):
        if not OPENCV_FORMAT:
            print("crosswalk detect error: Image file is not opened by opencv.")
            exit()
        self.result = self.model.detect([frame])[0]
        return self.result
    
    def getEnoughBox(self, index=-1):
        if index == -1:
            result = []
            for roi in self.result['rois']:
                x1, y1 = np.min(roi, axis=0)
                x2, y2 = np.max(roi, axis=0)
                result.append([x1, y1, x2, y2])
            return reulst
        else:
            roi = self.result['rois'][index]
            x1, y1 = np.min(roi, axis=0)
            x2, y2 = np.max(roi, axis=0)
            return [x1, y1, x2, y2]
        
    def getConvertedCoordinate(self, stdMap, dataList, symmetry=False): # 횡단보도 방향에 따라 대칭 결정해야할듯?
        real_data = checkObjectPosition(stdMap, dataList)
        if stdMap[0][0] > stdMap[2][0]:
            origin = [stdMap[1], stdMap[2], stdMap[3], stdMap[0]]
        else:
            origin = [stdMap[0], stdMap[1], stdMap[2], stdMap[3]]
        trans = ProjectiveTransofrm()
        dst = np.asarray([[0,0], [0,30], [30,30], [30, 0]])
        if not trans.estimate(origin, dst): raise Exception("estimate failed")
        data_local = trans(dataList)
        if symmetry:
            data_local = np.flip(data_local, axis=1)
        return dst, data_local
    
    def checkObjectPosition(self, area, dataList): # x y 만 들어왔다고 가정
        result = []
        polygon = Polygon(area)
        for cord in dataList:
            if polygon.contains(Point([(cord[0], cord[1])])):
                result.appned(cord)
        return reslut