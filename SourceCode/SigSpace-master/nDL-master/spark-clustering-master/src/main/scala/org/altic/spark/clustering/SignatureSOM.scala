package org.altic.spark.clustering

import java.io.File
import java.nio.file.{Files, Paths}

import org.altic.spark.clustering.som.SomTrainerB
import org.altic.spark.clustering.utils.{LabelVector, IO, SparkReader}
import org.apache.log4j.PropertyConfigurator
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.distribution.MultivariateGaussian
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.{GaussianMixtureModel, GaussianMixture, KMeans}
import org.apache.spark.rdd.RDD
import org.apache.spark.util.Vector

/**
 * Created by pradyumnad on 3/4/16. Edited by Manikanta
 */
object SignatureSOM {
  def context(): SparkContext = {
    val prgName = this.getClass.getSimpleName
    println("## LOCAL ##")

    val sparkConf = new SparkConf().setAppName("SigSpaceImage").setMaster("local[*]").set("spark.executor.memory","10g")
      .set("spark.driver.memory","5g")
      .set("spark.driver.maxResultSize","4g")
    //new SparkContext("local", prgName)
    new SparkContext(sparkConf)
  }

  class Experience(val datasetDir: String, val outputDir: String, val name: String, val nbProtoRow: Int, val nbProtoCol: Int, val nbIter: Int, val ext: String = ".data") {
    def dir: String = datasetDir + "/" + name

    def outDir: String = outputDir + "/" + name

    def dataPath: String = if (ext.length > 1) dir + "/" + name + ext else dir

    def nbTotProto: Int = nbProtoCol * nbProtoRow
  }

  def cleanResults(exp: Experience) {
    val resultDirs = new File(exp.dir).listFiles.filter(_.getName.startsWith(exp.name + ".clustering"))
    resultDirs.foreach(IO.delete)
    println("Removed " + exp.dir)
  }

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }

  def main(args: Array[String]) {
    PropertyConfigurator.configure("log4j.properties")
    val sc = context()

    var globalDatasetDir = "./data"
    // Magi Cluster mode
    if (System.getenv("SPARK_CLUSTERING") != null) {
      globalDatasetDir = System.getenv("SPARK_CLUSTERING") + "/data"
    }

    val globalNbIter = 100
    //    val nbExp = 1

    // Note ; map height/width = round(((5*dlen)^0.54321)^0.5)
    //        dlen is the number of row of the dataset


    val filepath = "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\GeneratedFeatures\\SIFT_"
    val outfilepath = "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\Signatures"
    //    val filepath = "/Users/pradyumnad/Documents/Datasets/MNIST"
    //    val outfilepath = "/Users/pradyumnad/Documents/Generated/MNIST"

    val clusters = Array(20, 30, 50)
      clusters.foreach(K => saveSignatures(sc, globalNbIter, K, filepath, outfilepath))
  }

  def saveSignatures(sc: SparkContext, globalNbIter: Int,
                     K: Int, filepath: String, outfilepath: String): Unit = {
    val dirs = getListOfSubDirectories(filepath)
    //        val dirs = Array("8", "9")
    val experiences = dirs.map(d => new Experience(filepath, outfilepath, d, 1, K, globalNbIter, ext = ""))
    //    println(experiences)

    val start = System.currentTimeMillis()

    experiences.foreach { exp =>
      // Delete old results
      //      cleanResults(exp)

      //SOMSignature(sc, globalNbIter, exp)
      //kmeansSignature(sc, globalNbIter, exp)
      EMSignature(sc, globalNbIter, exp)
    }

    val end = System.currentTimeMillis()

    println((end - start) + "milli secs")

  }

  def clusterCount(total: Long, percent: Int): Int = {
    Math.round(total * percent / 100)
  }

  def SOMSignature(sc: SparkContext, globalNbIter: Int, exp: Experience): Unit = {
    // Read data from fil
    println("\n***** READ : " + exp.name)
    val datas = SparkReader.parseSIFT(sc, exp.dataPath).cache()

    /**
     * Calculate the no of clusters based on the no of data rows in a Data set/Class
     */
    val K = clusterCount(datas.count(), exp.nbTotProto)
    println(exp.name + " : " + exp.nbTotProto + " = " + K)
    //      println("Total data" + datas.count())
    println("\n***** SOM : " + exp.name)
    val som = new SomTrainerB
    val somOptions = Map(
      //      "clustering.som.nbrow" -> exp.nbProtoRow.toString,
      //      "clustering.som.nbcol" -> exp.nbProtoCol.toString
      "clustering.som.nbrow" -> "1",
      "clustering.som.nbcol" -> K.toString
    )
    val somConvergeDist = -0.1
    val x=som.training(datas.asInstanceOf[RDD[Vector]], somOptions, exp.nbIter, somConvergeDist)

    println("x: "+x)

    // Save results
    val afilename = exp.outDir + "/" + exp.name + ".clustering.som_" + exp.nbProtoRow + "x" + exp.nbProtoCol
    val sfilename = exp.outDir + "/" + exp.name + ".clustering.som_centers_" + exp.nbProtoRow + "x" + exp.nbProtoCol
           if (!Files.exists(Paths.get(afilename))) {
              som.associations(datas).map(d => d._1 + "," + d._2 + " - " + d._3)
                .saveAsTextFile(afilename)
            }

    if (!Files.exists(Paths.get(sfilename))) {
      som.signature(sc, datas)
        .map(d => d._1)
        .saveAsTextFile(sfilename)
    }
  }

  def kmeansSignature(sc: SparkContext, globalNbIter: Int, exp: Experience): Unit = {
    val datas = SparkReader.parseForKMeans(sc, exp.dataPath).cache()
    // Cluster the data into two classes using KMeans
    // Load and parse the data
    /**
     * Calculate the no of clusters based on the no of data rows in a Data set/Class
     */
    val K = clusterCount(datas.count(), exp.nbTotProto)
    println(exp.name + " : " + exp.nbTotProto + " = " + K)
    val clusters = KMeans.train(datas, K, globalNbIter)
    //      clusters.save(sc, exp.dir + "/" + exp.name)
    //    println(clusters.clusterCenters.mkString(" "))
    val kMeansCenters = sc.parallelize(clusters.clusterCenters)
    kMeansCenters.map(v => v.toArray.mkString(" ")).saveAsTextFile(exp.outDir + "/" + exp.name + ".clustering.kmeans_centers_" + exp.nbTotProto)
    //    val preds = dataKM.map(p => p.toArray.mkString(" ") + "," + clusters.predict(p))
    //    preds.saveAsTextFile(exp.outDir + "/" + exp.name + ".clustering.kmeans_" + exp.nbTotProto)
  }

  def EMSignature(sc: SparkContext, globalNbIter: Int, exp: Experience): Unit = {
    // Read data from file
    println("\n***** READ : " + exp.name)
    //val datas: RDD[LabelVector] = SparkReader.parseSIFT(sc, exp.dataPath).cache()

    val datas = SparkReader.parseForKMeans(sc, exp.dataPath).cache()

    /**
      * Calculate the no of clusters based on the no of data rows in a Data set/Class
      */
    val K = clusterCount(datas.count(), exp.nbTotProto)

    println(exp.name + " : " + exp.nbTotProto + " = " + K)
    println("Total data" + datas.count())

    val gmm = new GaussianMixture().setK(K).run(datas)
    gmm.save(sc,"data/EMModel")
    val mapClusterIndices =gmm.predict(datas)
    val x: Array[MultivariateGaussian] = gmm.gaussians

    // output parameters of max-likelihood model
    for (i <- 0 until gmm.k) {
      println("weight=%f\nmu=%s\nsigma=\n%s\n" format
        (gmm.weights(i), gmm.gaussians(i).mu, gmm.gaussians(i).sigma))
      val clusterCenter = gmm.gaussians(i).mu;
    }
    
    // Save results
    val sfilename = exp.outDir + "/" + exp.name + ".clustering.em_centers_" + exp.nbTotProto


    if (!Files.exists(Paths.get(sfilename))) {
      mapClusterIndices
        .map(d => d)
        .saveAsTextFile(sfilename)
    }
  }



}
