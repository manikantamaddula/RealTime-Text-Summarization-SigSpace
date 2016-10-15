package ImageClassification

import java.io.File

import org.apache.hadoop.mapreduce.task.reduce.Shuffle
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by Manikanta on 10/5/2016.
  */
object test {

  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\PB\\hadoopforspark");

    // Configuration
    val sparkConf = new SparkConf().setAppName("SigSpace").setMaster("local[*]").set("spark.driver.memory","3g")
    //.set("spark.executor.memory","2g")

    val sc = new SparkContext(sparkConf)

    //val spark = SQLContext.getOrCreate(sc)


    val spark = SparkSession.builder.appName("SigSpace").master("local[*]").getOrCreate()
    import spark.implicits._


    // Turn off Info Logger for Consolexxx
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);

    val start = System.currentTimeMillis()

    val signaturespath="C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\Signatures_classification"

    val inputsignatures =sc.wholeTextFiles("C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\Signatures_classification\\*\\*")
    println("Input Data: ")
    //inputsignatures.take(1).foreach(f=>println(f))
    inputsignatures.repartition(8)
    inputsignatures.foreach(f=>println(f._1))
    val x2=inputsignatures.map(f=>f._1.split("/"))
    x2.foreach(f=>println(f.mkString(" ")))

    val subdir = getListOfSubDirectories(signaturespath)

    subdir.foreach(f=>println(f))

    subdir.indexOf("anchor")

    val classes=subdir.toList
    classes.foreach(f=>println(f))

    val inputclassification =inputsignatures.map {
      case (file,text)=>
        val label=file.split('/')

        //val vecarray =text.split(" ").map(f=>f.toDouble)
        val vecs =text.split("\n").map(f=>f.split(" ").map(ff=>ff.toDouble))
          vecs.map(f=>
        new LabeledPoint(subdir.indexOf(label(9)),Vectors.dense(f)))
    }
    val x: RDD[LabeledPoint] =inputclassification.flatMap(f=>f)
    //x.foreach(f=>println(f))








  }

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }

}
