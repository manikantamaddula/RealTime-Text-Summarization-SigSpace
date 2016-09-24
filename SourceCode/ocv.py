import os
from pyspark import SparkConf, SparkContext
import pdfeatures

__author__ = 'pradyumnad'

algo = "SIFT"
os.environ['SPARK_HOME'] = "/usr/local/spark"

PATH = "/home/machine1/PycharmProjects/imganalysis"


def generateTrainTextFile():
    file = open("images.txt", 'w')

    for dirname, dirnames, filenames in os.walk('/home/machine1/Documents/Datasets/UECFOOD256'):
        for subdirname in dirnames:
            print(os.path.join(dirname, subdirname))

        # print path to all filenames.
        for filename in filenames:
            if filename.startswith("."):
                continue
            elif filename.endswith(".txt"):
                continue
            fullPath = os.path.join(dirname, filename)
            print(fullPath)
            file.write(fullPath + "\n")
    file.close()


if __name__ == '__main__':
    generateTrainTextFile()

    conf = SparkConf() \
        .set("spark.driver.port", "4040") \
        .setAppName("MyApp") \
        .setMaster("local[*]")

    sc = SparkContext(conf=conf)

    files = sc.textFile("images.txt").cache()
    print("Total file : " + str(files.count()))

    # dense_features = files.map(pdfeatures.denseSIFT)
    # sift_features = files.map(pdfeatures.SIFT)
    hists = files.map(pdfeatures.colorHistograms)
    # print(dense_features.count())
    # print(sift_features.count())
    # sift_features.saveAsTextFile(PATH + "/SIFTfeatures")
    # dense_features.saveAsTextFile(PATH + "/Densefeatures")
    hists.saveAsTextFile(PATH + "/Histfeatures")

    sc.stop()
