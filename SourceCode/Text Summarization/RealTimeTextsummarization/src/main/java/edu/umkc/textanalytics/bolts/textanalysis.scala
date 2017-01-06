package edu.umkc.textanalytics.bolts

import java.io.{File, PrintStream}
import java.util

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Fields, Tuple, Values}
import edu.umkc.textanalytics.util.{WordClusterSOM, WordClusterSOM2}
import edu.umkc.textanalytics.util.{CoreNLP, TFIDF}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.{StopWordsRemover, Tokenizer, Word2Vec, Word2VecModel}
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.encog.mathutil.matrices.Matrix
import org.encog.ml.data.basic.{BasicMLData, BasicMLDataSet}
import org.encog.neural.som.SOM

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io.{File, FileWriter, PrintStream, PrintWriter}

import org.apache.spark


/**
  * Created by Manikanta on 12/3/2016.
  */
class textanalysis extends BaseRichBolt {
  private[bolts] var _collector: OutputCollector = null
  private[bolts] var taskName: String = null



  override def declareOutputFields(outputFieldsDeclarer: OutputFieldsDeclarer): Unit = {
    outputFieldsDeclarer.declare(new Fields("prediction","sentence"))

  }

  override def prepare(map: util.Map[_, _], topologyContext: TopologyContext, outputCollector: OutputCollector): Unit = {
    _collector = outputCollector
    taskName = topologyContext.getThisComponentId + "_" + topologyContext.getThisTaskId
    System.out.println("bolt prepare statement");
  }

  override def execute(tuple: Tuple){

    if(tuple.getStringByField("sentence")!="end") {

      System.setProperty("hadoop.home.dir", "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\PB\\hadoopforspark");
      val sparkConf = new SparkConf().setAppName("textSummarization").setMaster("local[*]")
      val sc = new SparkContext(sparkConf)
      val spark = SparkSession.builder.appName("iHearWOrd2Vec").master("local[*]").getOrCreate()

           // Turn off Info Logger for Consolexxx
           Logger.getLogger("org").setLevel(Level.OFF);
           Logger.getLogger("akka").setLevel(Level.OFF);

      val sentence = tuple.getStringByField("sentence")
      //System.out.println("inside bolt execute statement" + sentence)

      val text3: RDD[String] = sc.parallelize(Seq(sentence), 1)
      //text3.foreach(f => println(f))
      val text4 = text3.map { f => ("0", CoreNLP.returnLemma(f.
        replaceAll("[^a-zA-Z\\s:]", " ")
      ))
      }

      //Creating DataFrame from RDD
      val sentenceData = spark.createDataFrame(text4).toDF("labels", "sentence")

      //Tokenizer
      val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
      val wordsData = tokenizer.transform(sentenceData)

      //Stop Word Remover
      val remover = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filteredWords")
      val processedWordData = remover.transform(wordsData)

      //Word2Vec Model Generation
      val word2Vec = new Word2Vec()
        .setInputCol("filteredWords")
        .setOutputCol("result")
        .setVectorSize(100)
        .setMinCount(0)

      val model = word2Vec.fit(processedWordData)

      //model.getVectors.select("vector", "word").toDF.printSchema()

      val wordvectors = model.getVectors.select("vector", "word").toDF.rdd

      val model_w2v = Word2VecModel.load("data/word2vecmodel")
      //val newNames = Seq("vector", "word")
      //model_w2v.transform(processedWordData).select("result","labels").toDF(newNames:_*).rdd.coalesce(1,true).saveAsTextFile("data/testwordvector3")

      //val dataforMLdataset =model_w2v.transform(processedWordData).select("result","labels").toDF(newNames:_*).rdd.coalesce(1,true)

      val mldf = model_w2v.transform(processedWordData).select("result", "labels")

      val cols = mldf.first().get(0).toString.replaceAll("\\[", "").replaceAll("\\]", "").split(",")
      val word = mldf.first().get(1).toString
      //println("firstrow element1; "+cols+" firstrowelement 2: "+mldf.first().get(1))

      // read input data and build dataset
      val words = ArrayBuffer[String]()
      val dataset = new BasicMLDataSet()

      val vec: Array[Double] = cols.slice(0, cols.length - 1)
        .map(e => e.toDouble)
      dataset.add(new BasicMLData(vec))
      words += word


      val som = new SOM(100, 100 * 100)
      val line: RDD[String] = sc.textFile("data/matrix/part-00000")
      val rddDouble: RDD[Double] = line.map { f => f.toDouble }
      val arrayDouble: Array[Double] = rddDouble.collect()
      var settingMatrix = new Matrix(100, 100)

      var i = 0
      var j = 0
      for (i <- 0 to 99) {
        for (j <- 0 to 99) {
          settingMatrix.set(i, j, arrayDouble(i * 100 + j))
        }
      }

      som.setWeights(settingMatrix)

      val writer = new PrintStream("somtestoutput.txt")

      dataset.getData().zip(words)
        .foreach(dw => {
          val xy = convertToXY(som.classify(dw._1.getInput())) // find BMU id/coords
          writer.println("%s\t%d\t%d".format(dw._2, xy._1, xy._2))
        })
      def convertToXY(pos: Int): (Int, Int) = {
        val x = Math.floor(pos / 100).toInt
        val y = pos - (100 * x)
        (x, y)
      }

      writer.flush()
      writer.close()


      // Load and parse the data for kMeans classification
      val data = sc.textFile("somtestoutput.txt")
      val parsedData: RDD[Vector] = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()
      val arraydata = data.map { f => f.split("\t") }
      val vecdata: RDD[Vector] = arraydata.map { f => Vectors.dense(f(1).toDouble, f(2).toDouble) }
      val worddata = arraydata.map { f => f(0) }

      vecdata.foreach { f => println(f) }


      val kmeansmodel = KMeansModel.load(sc, "data/kMeansOnSOMmodel")
      val signatures: RDD[Int] = kmeansmodel.predict(vecdata)
     // val x: RDD[((String, Vector), Int)] = worddata.zip(vecdata).zip(signatures)
     // val nvinput = x.map { f => new LabeledPoint(f._2, f._1._2) }
      //val nvinput=vecdata.map{f=>new LabeledPoint(2,f)}
      //nvinput.foreach(f => println(f))


      //val classifmodel=NaiveBayesModel.load(sc,"data/myNaiveBayesModel")
      val classifmodel = DecisionTreeModel.load(sc, "data/myDecisionTreeModel")

      val result = classifmodel.predict(vecdata)
      //val result2=classifmodel.predictProbabilities(vecdata)
      //result.zip(worddata).zip(result2).foreach(f=>println(f))
      //val predictions: RDD[((Double, String), Vector)] =result.zip(worddata).zip(result2)
      val predictions: RDD[(Double, String)] = result.zip(worddata)
      //predictions.foreach(f=>println(f))
      val predictions2 = predictions.collect().zip(text3.collect())

      //predictions2.foreach(f => println(f))
      //result.collect().zip(text3.collect()).foreach(f=>println(f))
      val result_tuple = result.collect();
      spark.stop()
      sc.stop()
      println("spark context stopped")

      _collector.emit(tuple, new Values(result_tuple(0).toString,sentence))
      _collector.ack(tuple)
    }
    else {
      _collector.emit(tuple, new Values("999","end"))
      _collector.ack(tuple)
    }

  }

  /*override def close {
    spark.stop()
    sc.stop()
    println("spark context stopped")
  }*/

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }


}
