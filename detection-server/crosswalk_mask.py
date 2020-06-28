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
            return result
        else:
            roi = self.result['rois'][index]
            x1, y1 = np.min(roi, axis=0)
            x2, y2 = np.max(roi, axis=0)
            return [x1, y1, x2, y2]



    def getConvertedCoordinate(self, stdMap, dataList, crosswalk_index):
        real_data = self.checkObjectPosition(stdMap, dataList)
        # symmetry = False

        if crosswalk_index == 1:
            if stdMap[0][0] < stdMap[2][0]:
                origin = np.asarray([stdMap[0], stdMap[3], stdMap[2], stdMap[1]])
            else:
                origin = np.asarray([stdMap[1], stdMap[0], stdMap[3], stdMap[2]])
        elif crosswalk_index == 0:
            if stdMap[0][0] < stdMap[2][0]:
                origin = np.asarray([stdMap[3], stdMap[2], stdMap[1], stdMap[0]])
            else:
                origin = np.asarray([stdMap[0], stdMap[3], stdMap[2], stdMap[1]])
        elif crosswalk_index == 3:
            if stdMap[0][0] < stdMap[2][0]:
                origin = np.asarray([stdMap[3], stdMap[2], stdMap[1], stdMap[0]])
            else:
                origin = np.asarray([stdMap[2], stdMap[1], stdMap[0], stdMap[3]])
        elif crosswalk_index == 2:
            if stdMap[0][0] < stdMap[2][0]:
                origin = np.asarray([stdMap[1], stdMap[0], stdMap[3], stdMap[2]])
            else:
                origin = np.asarray([stdMap[2], stdMap[1], stdMap[0], stdMap[3]])

        trans = ProjectiveTransform()
        dst = np.asarray([[0,0], [0,300], [500,300], [500, 0]])
        if not trans.estimate(origin, dst): raise Exception("estimate failed")
        data_local = trans(dataList)

        # if symmetry:
            # self.symmetryConvert(data_local)
            
        return data_local.astype(int)
    
    def checkObjectPosition(self, area, dataList): # x y 만 들어왔다고 가정
        result = []
        polygon = Polygon(area)
        for cord in dataList:
            if polygon.contains(Point([(cord[0], cord[1])])):
                result.append(cord)
        return result

    def getAreaValue(self, cordis): # r['cordis'][i][0]
        return cv2.contourArea(cordis)

    def symmetryConvert(self, cordis):
        for i in range(len(cordis)):
            cordis[i][0] = 500 - cordis[i][0]
            cordis[i][1] = 300 - cordis[i][1]