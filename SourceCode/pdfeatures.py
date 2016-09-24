import cv2
import numpy as np

__author__ = 'pradyumnad'


def SIFT(filepath):
    algo = "SIFT"
    print(filepath)
    img = cv2.imread(filepath)
    # assert img.size > 0

    detector = cv2.FeatureDetector_create(algo)
    descriptor = cv2.DescriptorExtractor_create(algo)

    skp = detector.detect(img)
    skp, sd = descriptor.compute(img, skp)

    featuresString = ""
    for s, sk in zip(sd, skp):
        # print("angel", sk.angle, "octave", sk.octave, "scale", sk.size)
        items = ' '.join(map(str, s))
        items = str(sk.angle) + "," + str(sk.octave) + "," + str(sk.size) + "," + items
        items = filepath + "," + items
        featuresString += items + "\n"
        # print(items)
        # print("\n")

    # string = numpy.array2string(sd)
    # numpy.savetxt(algo + ".csv", sd, delimiter=",")
    return featuresString


def denseSIFT(filepath):
    img = cv2.imread(filepath)

    detector = cv2.FeatureDetector_create("Dense")
    descriptor = cv2.DescriptorExtractor_create("SIFT")

    skp = detector.detect(img)
    skp, sd = descriptor.compute(img, skp)

    featuresstring = ""
    for s in sd:
        items = ' '.join(map(str, s))
        items = filepath + "," + items
        featuresstring += items + "\n"
        # print(items)
        # print("\n")

    # numpy.savetxt("Dense.csv", sd, delimiter=",")
    return featuresstring


def colorHistograms(filepath):
    print(filepath)
    img = cv2.imread(filepath)
    hists = ""
    hists += colorHist(filepath, img, n=1)
    hists += colorHist(filepath, img, n=2)
    hists += colorHist(filepath, img, n=3)
    return hists

def colorHist(filepath, img, n=1):
    # print(img.shape)
    width = img.shape[0]
    height = img.shape[1]

    x_range = range(0, width, width / n)[0:n]
    y_range = range(0, height, height / n)[0:n]

    x_range.append(width)
    y_range.append(height)

    # print(x_range)
    # print(y_range)

    histograms = ""
    for i, xval in enumerate(x_range):
        for j, yval in enumerate(y_range):
            if i < n and j < n:
                # print(x_range[i], x_range[i + 1], y_range[j], y_range[j + 1])
                # create a mask
                mask = np.zeros(img.shape[:2], np.uint8)
                mask[x_range[i]:x_range[i + 1], y_range[j]: y_range[j + 1]] = 255
                masked_img = cv2.bitwise_and(img, img, mask=mask)
                # pyplot.imshow(masked_img, 'gray')
                # pyplot.show()

                hist = cv2.calcHist(masked_img, [0], None, [256], [0, 256])
                # print("Histogram Size", hist.size)
                featuresstring = strFromHist(hist)
                histograms += filepath+","+str(n)+","+str(i)+","+str(j)+","+featuresstring + "\n"
    return histograms


def strFromHist(hist):
    featuresstring = ""
    for s in hist:
        items = ' '.join(map(str, s))
        featuresstring += items + " "
        # print(items)
        # print("\n")
    return featuresstring


if __name__ == '__main__':
    result = colorHistograms("/home/machine1/Documents/Datasets/UECFOOD256/1/1.jpg")
    print(result)
