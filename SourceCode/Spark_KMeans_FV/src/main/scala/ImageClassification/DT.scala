package ImageClassification

import java.io.File

import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.{Vectors, Matrix}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by Manikanta on 10/5/2016.
  */
object DT {

  def main(args: Array[String]) {

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

    //val signaturespath="C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\Signatures_classification"

    //101 object categories path
    val signaturespath="C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\DR\\Manikanta_data\\Old Signatures\\256_Object_Categories"

    //kmeans_centers_100

    val inputsignatures: RDD[(String, String)] =sc.wholeTextFiles(signaturespath+"\\*\\*som_centers_1x50*")
    println("Input Data: ")
    //inputsignatures.foreach(f=>println(f))
    val subdir =getListOfSubDirectories(signaturespath)

    subdir.foreach(f=>println(f))

    val classes=subdir.toList

    val inputclassification =inputsignatures.map {
      case (file,text)=>
        val label=file.split('/')

        //val vecarray =text.split(" ").map(f=>f.toDouble)
        val vecs =text.split("\n").map(f=>f.split(" ").map(ff=>ff.toDouble))
        vecs.map(f=>
          new LabeledPoint(classes.indexOf(label(label.size-3)),Vectors.dense(f)))
    }.flatMap(f=>f)
    //inputclassification.foreach(f=>println(f))



    // Split data into training (80%) and test (20%).
    val splits = inputclassification.randomSplit(Array(0.8, 0.2), seed = 12L)
    val training: RDD[LabeledPoint] = splits(0)
    val test = splits(1)

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 20
    val categoricalFeaturesInfo = Map[Int, Int]()
    //val impurity = "entropy"
    val impurity = "gini"
    val maxDepth = 30
    val maxBins = 15000

    //val model2 = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    val model2 = DecisionTree.trainClassifier(training, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    val predictionAndLabel = test.map(p => (model2.predict(p.features), p.label))

    val end = System.currentTimeMillis()
    println("Time to train the Decision Tree model: "+(end - start) + " milli secs")


    //val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

    //predictionAndLabel.foreach{f=>println(f)}

    //println("accuracy of Decision Tree: "+accuracy)
    //predictionAndLabel.foreach(f=>println(f))
    val metrics=new MulticlassMetrics(predictionAndLabel)

    val confmatrix: Matrix =metrics.confusionMatrix
    println("Confusion Matrix: ")
    confmatrix.rowIter.foreach(f=>println(f))
    val wghtprecision: Double =metrics.weightedPrecision
    val wghtrecall: Double =metrics.weightedRecall
    val wghtfmeasure: Double =metrics.weightedFMeasure
    println("Accuracy: "+metrics.accuracy)
    println("Weighted Precision: "+wghtprecision)
    println("Weighted Recall: "+wghtrecall)
    println("Weighted FMeasure: "+wghtfmeasure)


    // Save and load model
    //model2.save(sc, "data/myDecisionTreeModel")
    //val sameModel = DecisionTreeModel.load(sc, "data/myDecisionTreeModel")



    spark.stop()
    sc.stop()
  }

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }


  }
