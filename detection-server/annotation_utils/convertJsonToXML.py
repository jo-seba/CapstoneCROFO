import os
import cv2
import json
from xml.etree.ElementTree import Element, dump, ElementTree

CLASS_IDS = {'person':0, 'bike':1, 'car':2, 'bus':3, 'truck':4}

CUR_PATH = os.path.abspath('.')
ANNO_PATH = os.path.join(CUR_PATH, 'TestForYOLO_20200531.json')
NEW_ANNO_PATH = os.path.join(CUR_PATH, 'Test_Anno')

IMAGE_PATH = os.path.join(CUR_PATH, 'test_img_148_jpg')

BIC_PATH = "C:\\Users\\bic49\\jupyter_data\\images\\testdata\\0"

def indent(elem, level=0): #자료 출처 https://goo.gl/J8VoDK
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

def create_xml_from_json(img_name, img_path, object_list, output_filename): # img_name : External ID
    if os.path.isfile(output_filename):
            os.remove(output_filename)
            print(output_filename, 'is removed')
    root = Element("annotation", verified="yes")

    node = Element("filename")
    node.text = img_name
    root.append(node)

    img = cv2.imread(img_path)
    height, width, channel = img.shape

    node = Element("size") # 필요없을듯
    node_2 = Element("width")
    node_2.text = str(width)
    node.append(node_2)

    # level 2
    node_2 = Element("height")
    node_2.text = str(height)
    node.append(node_2)

    node_2 = Element("depth")
    node_2.text = str(channel)
    node.append(node_2)

    root.append(node)
    # level 2 -----
    for object_data in object_list:
        bbox = object_data['bbox']
        node = Element("object")

        # level 2
        node_2 = Element("name")
        node_2.text = object_data['title']
        node.append(node_2)

        node_2 = Element("bndbox")

        # level 3
        left = int(bbox['left'])
        top = int(bbox['top'])
        right = left + int(bbox['width'])
        bottom = top + int(bbox['height'])
        node_3 = Element("xmin")
        node_3.text = str(left)
        node_2.append(node_3)

        node_3 = Element("ymin")
        node_3.text = str(top)
        node_2.append(node_3)

        node_3 = Element("xmax")
        node_3.text = str(right)
        node_2.append(node_3)

        node_3 = Element("ymax")
        node_3.text = str(bottom)
        node_2.append(node_3)

        node.append(node_2)
        # level 3 -----

        root.append(node)
    # level 2 -----

    indent(root)
    tree = ElementTree(root)
    tree.write(output_filename)
    # dump(root)

def json_to_xml(anno_dir_path, image_dir_path, new_anno_dir_path):
    print(anno_dir_path)
    with open(anno_dir_path, "rb") as f:
        json_data = json.load(f)

    for image_data in json_data:
        img_name = image_data['External ID']
        full_image_name = os.path.join(image_dir_path, img_name)
        object_list = image_data['Label']['objects']
        
        if len(object_list) == 0:
            print(full_image_name, ' is empty')
            continue
        output_file = img_name + '.xml'
        create_xml_from_json(img_name, full_image_name, object_list, os.path.join(new_anno_dir_path, output_file))

    print('Finished converting :')
    
if __name__ == "__main__":
    if not os.path.exists(NEW_ANNO_PATH):
        os.makedirs(NEW_ANNO_PATH)
    json_to_xml(ANNO_PATH, IMAGE_PATH, NEW_ANNO_PATH)