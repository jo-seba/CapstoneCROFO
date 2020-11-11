import os
import json

CLASS_IDS = {'person':0, 'bike':1, 'car':2, 'bus':3, 'truck':4}
CUR_PATH = os.path.dirname(os.path.abspath(__file__))
ANNO_PATH = os.path.join(CUR_PATH, 'annotations')

# annotation_list = os.listdir(ANNO_PATH)
# json_annotation_list = [annotation for annotation in annotation_list if annotation.endswith('.json')]
    
# IMAGE_PATH = os.path.join(CUR_PATH, 'images')
BIC_PATH_SJ = "C:\\Users\\bic49\\jupyter_data\\images\\sjTrain"
BIC_PATH_726 = "C:\\Users\\bic49\\jupyter_data\\images\\tot_imgs"
BIC_PATH_NEW_TRAIN = 'C:\\Users\\bic49\\jupyter_data\\images\\0610'

def json_to_csv(anno_dir_path, image_dir_path, class_ids, output_filename, EXIST_FILE = False):
    # annotation_list = os.listdir(anno_dir_path)
    json_annotation_list = []
    json_annotation_list.append('0610_new_train.json')
    #json_annotation_list = [annotation for annotation in annotation_list if annotation.endswith('.json')]
    print(json_annotation_list)
    json_datas = []
    
    for json_annotation in json_annotation_list:
        with open(os.path.join(CUR_PATH, json_annotation), "rb") as f:
            json_datas.append(json.load(f))
    if EXIST_FILE == False:
        if os.path.isfile(output_filename):
            os.remove(output_filename)
            print(output_filename, 'is removed')

    with open(output_filename, 'w', encoding='utf-8') as train_csv_file:
        cnt = 0
        for annotation_data in json_datas:
            for image_data in annotation_data:
                full_image_name = os.path.join(image_dir_path, image_data['External ID'])
                #if os.path.isfile(full_image_name) == False:
                    # print('Warning : ', full_image_name, "doesn't exist")
                    # DB Error Table 처리해도 될듯
                try:
                    object_list = image_data['Label']['objects']
                except:
                    continue
                if len(object_list) == 0:
                    print(full_image_name, ' is empty')
                    continue
                bbox_str_list = ' '
                for object_data in object_list:
                    cnt+=1
                    bbox = object_data['bbox']
                    class_id = class_ids[object_data['title']]
                    left = int(bbox['left'])
                    top = int(bbox['top'])
                    right = left + int(bbox['width'])
                    bottom = top + int(bbox['height'])
                    bbox_str = ('{0},{1},{2},{3},{4}').format(left, top, right, bottom, class_id)
                    bbox_str_list = bbox_str_list + bbox_str + ' '
                train_csv_file.write(full_image_name + bbox_str_list + '\n')
    print('Finished converting :', output_filename)
    print('object Num:', cnt)
if __name__ == "__main__":
    json_to_csv(ANNO_PATH, BIC_PATH_NEW_TRAIN, CLASS_IDS, os.path.join(ANNO_PATH, '20200609_new.csv'), EXIST_FILE=True)