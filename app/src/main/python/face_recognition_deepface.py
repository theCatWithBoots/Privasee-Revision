import cv2
from deepface import DeepFace
import base64
import numpy as np


def check_face(testImg,*referenceImg):
    global face_match

    for i in referenceImg:
        decode_data = base64.b64decode(i) #decode the stringe we passed
        np_data = np.frombuffer(decode_data, np.uint8) #convert data to numpy data
        img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
        img_colored =cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        try:
            if DeepFace.verify(testImg, img_colored.copy())['verified']:
                return True
        except ValueError:
            return False
            
    return False
              
    
cv2.destroyAllWindows()