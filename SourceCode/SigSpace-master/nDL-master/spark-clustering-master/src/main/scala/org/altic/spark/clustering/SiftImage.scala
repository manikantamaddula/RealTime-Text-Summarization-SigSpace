package org.altic.spark.clustering

import org.apache.hadoop.io.BytesWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.feature.local.list.LocalFeatureList
import org.openimaj.feature.local.matcher.BasicMatcher
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher
import org.openimaj.feature.local.matcher.LocalFeatureMatcher
import org.openimaj.feature.local.matcher.MatchingUtilities
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.DisplayUtilities
import org.openimaj.image.ImageUtilities
import org.openimaj.image.MBFImage
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC
import java.io.File
import java.io.IOException

/**
  * Created by Manikanta on 9/15/2016.
  */
object SiftImage {

  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir", "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\PB\\hadoopforspark");

    // Configuration
    val sparkConf = new SparkConf().setAppName("iHearWOrd2Vec").setMaster("local[*]").set("spark.driver.memory","6g")
    //.set("spark.executor.memory","2g")

    val sc = new SparkContext(sparkConf)

    val inputfilepath = "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\SigSpace Data from Lab\\Generated\\UECFOOD256\\LBP_"

    val image1: MBFImage = ImageUtilities.readMBF(new File("data/1.jpg"))

    val engine: DoGSIFTEngine = new DoGSIFTEngine
    val queryKeypoints: LocalFeatureList[Keypoint] = engine.findFeatures(image1.flatten)

    val dirs = getListOfSubDirectories(inputfilepath)

    dirs.foreach(f=>{

      val folderpath = inputfilepath + "\\" + f + "\\*"

      val images=sc.sequenceFile[String, BytesWritable](folderpath, 2)
      //val images: RDD[(String, String)] =sc.wholeTextFiles(folderpath)





    })






  }



  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }

}
