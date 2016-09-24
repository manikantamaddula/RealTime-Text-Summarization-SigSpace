package org.altic.spark.clustering.utils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
 * Company : Altic - LIPN
 * User: Tugdual Sarazin
 * Date: 07/01/14
 * Time: 12:37
 */
object SparkReader {
  def parse(sc: SparkContext, filePath: String, splitRegex: String): RDD[NamedVector] = {
    sc.textFile(filePath).map { line =>
      val arrayDouble = line.split(splitRegex).map(_.toDouble)
      new NamedVector(arrayDouble.dropRight(1), arrayDouble.last.toInt)
    }
  }

  def parseForKMeans(sc: SparkContext, filePath: String): RDD[Vector] = {
    val data = sc.textFile(filePath).filter(l => l.length > 1)

    data.map {
      line =>
        val parts = line.split(',')
        val feature = parts(parts.length - 1).trim
        val filepath = parts(0).split("/")
        val arrayDouble = feature.split(' ').map(_.toDouble)
        Vectors.dense(arrayDouble.dropRight(1))
    }
  }

  private def parseSIFTLine(line: String): LabelVector = {
    val parts = line.split(',')
    val feature = parts(parts.length - 1).trim
    val filepath = parts(0).split("/")
    // print(feature)

    val features = feature.split(' ').map(_.trim.toDouble)
    //    val label = filepath(filepath.length - 2)
    val label = parts(0)
    new LabelVector(features, label)
  }

  private def parseClass(line: String): (String, Array[Double]) = {
    val parts = line.split(',')
    val feature = parts(parts.length - 1).trim
    val filepath = parts(0).split("/")
    // print(feature)

    val features = feature.split(' ').map(_.trim.toDouble)
    val label = filepath(filepath.length - 2)
    (label, features)
  }


  def parseSIFTClass(sc: SparkContext, filePath: String): RDD[(String, Array[Double])] = {
    val data = sc.textFile(filePath).filter(_.length > 1)
    data.map(parseClass)
  }


  def parseSIFT(sc: SparkContext, filePath: String): RDD[LabelVector] = {
    val data = sc.textFile(filePath).filter(_.length > 1)
    data.map(parseSIFTLine)
  }
}