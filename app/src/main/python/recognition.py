import face_recognition
import cv2
import numpy as np
import math

# Helper
def face_confidence(face_distance, face_match_threshold=0.6):
    range = (1.0 - face_match_threshold)
    linear_val = (1.0 - face_distance) / (range * 2.0)

    if face_distance > face_match_threshold:
        return str(round(linear_val * 100, 2)) + '%'
    else:
        value = (linear_val + ((1.0 - linear_val) * math.pow((linear_val - 0.5) * 2, 0.2))) * 100
        return str(round(value, 2)) + '%'

def android_kotlin (stringReference, stringIdentify):
    global imgReference  
    global imgIdentify
    imgReference  = stringReference
    imgIdentify = stringIdentify
    fr = FaceRecognition()
    result = fr.run_recognition()

    return  result


class FaceRecognition:
    face_locations = []
    face_encodings = []
    known_face_encodings = []

    def __init__(self, ):
        self.encode_faces()

    def encode_faces(self):
        face_image = face_recognition.load_image_file(imgReference)
        face_encoding = face_recognition.face_encodings(face_image)[0]
        self.known_face_encodings.append(face_encoding)
    
    def run_recognition(self):
            img_indentify = cv2.imread(imgIdentify, cv2.IMREAD_COLOR)

            small_frame = cv2.resize(img_indentify, (0, 0), fx=0.25, fy=0.25)

            # Convert the image from BGR color (which OpenCV uses) to RGB color (which face_recognition uses)
            rgb_small_frame = small_frame[:, :, ::-1]

            # Find all the faces and face encodings in the current frame of video
            self.face_locations = face_recognition.face_locations(rgb_small_frame)
            self.face_encodings = face_recognition.face_encodings(rgb_small_frame, self.face_locations)
            #face_recognition.face_encodings(self.register_new_user_capture)[0]
      
            for face_encoding in self.face_encodings:
                # See if the face is a match for the known face(s)
                matches = face_recognition.compare_faces(self.known_face_encodings, face_encoding)
                confidence = 0.00

                # Calculate the shortest distance to face
                face_distances = face_recognition.face_distance(self.known_face_encodings, face_encoding)

                best_match_index = np.argmin(face_distances)
                if matches[best_match_index]:
                    confidence = face_confidence(face_distances[best_match_index])
                    return confidence
            
            return confidence
               

 


    



