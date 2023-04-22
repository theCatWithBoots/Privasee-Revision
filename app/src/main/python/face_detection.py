import cv2
import numpy as np
from PIL import Image
import base64
import io
import os
#from os import listdir, path

def main(data, pathLocation):
    
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

    face_img = cv2.imread(data, cv2.IMREAD_COLOR)
    gray = cv2.cvtColor(face_img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(500, 500))

    if (len(faces) == 0):
        return False      
    else:
       if(pathLocation != ""):
            for(x ,y,w,h) in faces:
                face_img = face_img[y:y+h, x:x+w]
                face_img_resized = cv2.resize(face_img, (224,224), )
                filename = os.path.basename(data)
                cv2.imwrite(pathLocation + "{}".format(filename), face_img_resized)
                return True
       else:
           return True




    
