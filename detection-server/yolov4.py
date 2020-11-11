import sys
import os
CUR_PATH = os.path.abspath('.')
YOLOV4_PATH = os.path.join(CUR_PATH, 'pytorchYOLOv4')
sys.path.append(YOLOV4_PATH)

from pytorchYOLOv4.cap_models import Yolov4
import torch
from torch import nn
import torch.nn.functional as F
from tool.torch_utils import *
from tool.yolo_layer import YoloLayer
from tool.utils import load_class_names, plot_boxes_cv2
from tool.torch_utils import do_detect
import cv2

# init code
WEIGHT_FILE_PATH = os.path.join(CUR_PATH, 'pytorchYOLOv4', 'weight', 'yolov4.pth')
CLASSES_NAME_FILE_PATH = os.path.join(CUR_PATH, 'pytorchYOLOv4', 'data', 'coco.names')

class YOLO:
    def __init__(self, weightfile=WEIGHT_FILE_PATH, classes_name_file=CLASSES_NAME_FILE_PATH):
        self.model = Yolov4()
        pretrained_dict = torch.load(weightfile, map_location=torch.device('cuda')) # cuda
        self.model.load_state_dict(pretrained_dict)

        self.use_cuda = True # cuda
        if self.use_cuda:
            self.model.cuda()
        
        self.class_names = load_class_names(classes_name_file)

    def detect(self, cv_image, IS_BGR=True): # cv_imaeg <- cv2.imread / numpy image(BGR)
        resized_image = cv2.resize(cv_image, (416, 416))
        if IS_BGR:
            resized_image = cv2.cvtColor(resized_image, cv2.COLOR_BGR2RGB)
        
        boxes = do_detect(self.model, resized_image, 0.4, 0.6, self.use_cuda)

        return boxes
    
    def get_class_name(self, box):
        return self.class_names[box[6]]

    def visualize_boxes_cv2(self, boxes, cv_image, output_file_name):
        plot_boxes_cv2(cv_image, boxes[0], output_file_name, self.class_names)