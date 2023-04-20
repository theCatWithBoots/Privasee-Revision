import numpy as np
from sklearn.decomposition import PCA
from sklearn.metrics import euclidean_distances
import cv2
from PIL import Image
import base64
import io

def train(*ImgArr):

    # Load training images
    training_images = []

    for i in ImgArr:
        decode_data = base64.b64decode(i) #decode the stringe we passed
        np_data = np.frombuffer(decode_data, np.uint8) #convert data to numpy data
        img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
        img_gray =cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        training_images.append(img_gray)

    training_images = np.array(training_images)

    # Flatten images
    training_images = training_images.reshape(training_images.shape[0], -1)

    # Perform PCA on training images
    pca = PCA(n_components=0.95)
    pca.fit(training_images)

    # Project training images and test image onto the PCA space
   # projected_training_images = pca.transform(training_images)

    return pca