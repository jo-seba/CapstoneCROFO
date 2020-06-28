# CROFO Backend (ML/AI) Part
CROFO 어플리케이션의 서버파트를 제공하는 레포지토리 입니다.  
해당 레포지토리에서는 영상을 실시간으로 전송받고 객체인식을 진행한다음, 어플리케이션에 제공하는 기능으로 구성됩니다.



# How to run
이 섹션에서는 백엔드 어플리케이션을 실행시키는 방법을 소개합니다.  
CUDA 10.0이 지원되는 GPU가 내장된 컴퓨터에 Tensorflow(Keras) 라이브러리가 정상적으로 동작하는지 확인이 필요합니다.  
해당 라이브러리가 정상적으로 지원된다면, 아래 방법을 통해서 소스코드를 실행할 수 있습니다.  

## Requrements
### Conda Environment
우리 어플리케이션은 머신러닝 프레임워크를 포함한 여러가지 Python 기반의 프레임워크를 사용하고 있습니다.  
기본적으로 `Anaconda Python 3.5` 버전을 이용하며, 새로운 환경을 구축한 뒤에, 아래 명령어를 이용해 실행에 필요한 여러가지 라이브러리를 자동으로 설치할 수 있습니다.
```shell script
pip install .
```
해당 명령어를 통해 `requirements.txt`에 포함된 프레임워크를 자동으로 설치할 수 있습니다.  
자동으로 설치할 수 없는 경우, 해당 파일을 열람하여 직접 설치를 진행할 수 도 있습니다.



## Command
필수 라이브러리를 설치한 이후에, 아래 명령어를 이용해서 서버를 실행할 수 있습니다.
```shell script
python run.py
```


# Demo
우리의 머신러닝 어플리케이션은 공공CCTV 데이터셋에서 더 좋은 객체인식 결과를 제공하고 있습니다.  
아래 시연 동영상은 기존 pre-trained 된 모델에서 공공CCTV 데이터셋을 전이학습시킨 결과를 보여주고 있습니다.  
기존의 YOLOv3 모델보다 횡단보도내 보행자 인식에서 더 좋은 인식률을 제공하고 있습니다.

## Why we used DeepSORT tracking algorithm?
우리가 추적알고리즘 중 하나인 DeepSORT를 적용한 이유는 아래와 같습니다.
1. 객체 인식이 완벽하지 않기 때문에, 추적알고리즘이 객체 인식에 실패했을 때 이전 프레임의 객체정보를 통해 유추할 수 있음
1. 어플리케이션에서 사람의 보행방향을 제공할 수 있음

실제로 우리 어플리케이션에 적용하였을 때, 더 나은 품질을 객체(보행자 등)정보를 제공할 수 있었습니다.  
이는 객체 인식이 원활하지 않은 환경에서 효과적으로 보완해줄 수 있음을 의미합니다.
# Team (ML/AI)
1. Son Jonathan Sebastian ()
1. In-Chang Baek ([E-mail](mailto:bic4907@gmail.com))


# References
1. [Yolov3 Keras Implements]()
1. [DeepSORT Python Implements]()