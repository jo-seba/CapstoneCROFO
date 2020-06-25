def get_box_info(box):
    return [box[0], box[1], box[0] + box[2], box[1] + box[3]]

def get_iou(box1, box2): # 변환된 좌표
    
    x1 = max(box1[0], box2[0])
    y1 = max(box1[1], box2[1])
    x2 = min(box1[2], box2[2])
    y2 = min(box1[3], box2[3])
    return x1, y1, x2, y2

def isIntersection(box1, box2): # 변환된 좌표
    #print('intersection ', box1, box2)
    if box1[0] >= box2[2] or box1[2] <= box2[0]:
        #print('요거')
        return False
    if box1[1] >= box2[3] or box1[3] <= box2[1]:
        #print('저거')
        return False
    return True

def get_iou_value(box1, box2):
    if not isIntersection(box1, box2):
        return 0
    x1, y1, x2, y2 = get_iou(box1, box2)
    '''
    print('헿')
    print(get_iou(box1, box2))
    print()
    '''
    area_intersection = (x2 - x1) * (y2 - y1)
    area_box1 = (box1[2] - box1[0]) * (box1[3] - box1[1])
    area_box2 = (box2[2] - box2[0]) * (box2[3] - box2[1])
    area_union = area_box1 + area_box2 - area_intersection
    
    if area_box1 <= 0 or area_box2 <= 0 or area_intersection <= 0 or area_union < 0:
        print('get_iou_value algorithm error')
        return 0
    
    if area_union == 0: return 0
    
    iou = area_intersection / area_union
    return iou