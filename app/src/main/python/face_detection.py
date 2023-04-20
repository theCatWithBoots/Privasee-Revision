import cv2
import numpy as np
from PIL import Image
import base64
import io
#from os import listdir, path

def main(data):
    
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

    decode_data = base64.b64decode(data) #decode the stringe we passed
    np_data = np.fromstring(decode_data, np.uint8) #convert data to numpy data
    img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)

    img_colored = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(500, 500))

    if len(faces) == 0:
        return "No face detected"
    else:
        for(x ,y,w,h) in faces:
            face_img = img_colored[y:y+h, x:x+w]
            face_img_resized = cv2.resize(face_img, (224,224), )
            break


    pil_im = Image.fromarray(face_img_resized)
    
    buff = io.BytesIO()
    pil_im.save(buff,format="JPEG")
    img_str = base64.b64encode(buff.getvalue())
    return ""+str(img_str, 'utf-8')

    
