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
        img_colored =cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        training_images.append(img_colored)
    training_images = np.array(training_images)

    # Flatten images
    training_images = training_images.reshape(training_images.shape[0], -1)

    # Perform PCA on training images
    pca = PCA(n_components=0.95)
    pca.fit(training_images)

    # Project training images and test image onto the PCA space
    projected_training_images = pca.transform(training_images)

    return projected_training_images


def project_testing(image, project_training_images, pca, threshold):

    decode_data = base64.b64decode(image) #decode the stringe we passed
    np_data = np.frombuffer(decode_data, np.uint8) #convert data to numpy data
    img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
    img_colored =cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    # Flatten images
    test_images = img_colored.reshape(1, -1) 

    # Perform PCA
    projected_test_image = pca.transform(test_images)

    # Compare distances between projected test image and projected training images
    distances = euclidean_distances(projected_test_image, project_training_images)
    min_distance = np.min(distances)

    # Define threshold
    #threshold = 8000


    if min_distance < threshold:
        return True
    else:
        return False


