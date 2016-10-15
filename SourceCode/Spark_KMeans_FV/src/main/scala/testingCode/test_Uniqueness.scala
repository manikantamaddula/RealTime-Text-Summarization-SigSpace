package testingCode

import java.io.{FileWriter, PrintWriter, File, PrintStream}

import kMeansPipeline.WordClusterSOM
import mlpipeline.{CoreNLP, TFIDF}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.param.ParamMap

//import org.apache.spark.mllib.feature.{Word2VecModel, Word2Vec}
import org.apache.spark.ml.feature.{Word2VecModel, StopWordsRemover, Tokenizer, Word2Vec}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Manikanta on 7/24/2016.
  */
object test_Uniqueness {


  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir", "C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\PB\\hadoopforspark");

    // Configuration
    val sparkConf = new SparkConf().setAppName("iHearWOrd2Vec").setMaster("local[*]").set("spark.driver.memory","3g")
      //.set("spark.executor.memory","2g")

    val sc = new SparkContext(sparkConf)

    //val spark = SQLContext.getOrCreate(sc)


    val spark = SparkSession.builder.appName("iHearWOrd2Vec").master("local[*]").getOrCreate()


    // Turn off Info Logger for Consolexxx
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);



    // Read the file into RDD[String]
    val inputfilepath="C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\KDM\\Project Files\\bbcsport-fulltext\\bbcsport2"

    //val inputfilepath="C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\KDM\\Project Files\\bbcsport-fulltext\\bbcsport\\cricket\\*"
    val rddWords =sc.wholeTextFiles(inputfilepath+"\\*",2000)
    val text: RDD[(String, String)] = rddWords.map { case (file, text) => (file.split("/").takeRight(2).head,CoreNLP.returnLemma(text.
      replaceAll("[^a-zA-Z\\s:]", " ")
      .replaceAll("\"\"[\\p{Punct}&&[^.]]\"\"", " ")
      .replaceAll(","," ")
      .replaceAll("\"\"\\b\\p{IsLetter}{1,2}\\b\"\""," "))) }
    //text.foreach(f=>println(f._1.split("/").takeRight(2).head))


    //Creating DataFrame from RDD
    val sentenceData = spark.createDataFrame(text).toDF("labels", "sentence")
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


    /*
    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, remover, word2Vec))
    */
    val model: Word2VecModel = word2Vec.fit(processedWordData)
    println("word2vec model fit is done")

    val model3=model.transform(processedWordData)

    model3.printSchema()
    //model3.select("filteredWords","result").foreach(f=>println(f))
    val z: ParamMap = model.extractParamMap()
    println(z)

    model.write.overwrite().save("data/word2vecmodel")
  //model.save("data/word2vecmodel")

    println("saved word2vec model")

    //TFIDF TopWords
    println("topwords are returned")

    val dirs: Array[String] = getListOfSubDirectories(inputfilepath)

    var toptfidfwords: Array[(String, Double,String)]=Array()
    val y =dirs.foreach(f => {

      val folderpath = inputfilepath + "\\" + f + "\\*"

      val rddWords2 =sc.wholeTextFiles(folderpath)
      val text2: RDD[(String, String)] = rddWords2.map { case (file, text) => (file,CoreNLP.returnLemma(text.
        replaceAll("[^a-zA-Z\\s:]", " ")
        .replaceAll("\"\"[\\p{Punct}&&[^.]]\"\"", " ")
        .replaceAll(","," ")
        .replaceAll("\"\"\\b\\p{IsLetter}{1,2}\\b\"\""," "))) }


      //Creating DataFrame from RDD
      val sentenceData2 = spark.createDataFrame(text2).toDF("labels", "sentence")
      //Tokenizer
      val tokenizer2 = new Tokenizer().setInputCol("sentence").setOutputCol("words")
      val wordsData2 = tokenizer2.transform(sentenceData2)

      //Stop Word Remover
      val remover2 = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filteredWords")
      val processedWordData2 = remover2.transform(wordsData2)
      val x: Array[(String, Double)] =TFIDF.getTopTFIDFWords(sc, processedWordData2.select("filteredWords").rdd)
      val x2 =x.map{ ff=>(ff._1,ff._2,f)}
      //x2.foreach{fff=>println(fff)}
      toptfidfwords=toptfidfwords ++ x2
      })

    println("toptfidf words for all classes:")
    //toptfidfwords.foreach(f=>println(f))

    val topwordterms =toptfidfwords.map(f=>(f._1))
    //sc.parallelize(topwordterms)
    //sc.broadcast(topwordterms)
    val topwordterms2: Array[(String, String)] =toptfidfwords.map(f=>(f._3,f._1))
    val topwordterms3: RDD[(String, String)] =sc.parallelize(topwordterms2)
    val topwordterms4: RDD[(String, Array[String])] =topwordterms3.map{ f=>(f._1,Array(f._2.toString))}
    val topwordsdf=spark.createDataFrame(topwordterms4).toDF("labels", "filteredWords")
    val vec_labled: DataFrame =model.transform(topwordsdf)
    vec_labled.printSchema()
    //vec_labled.foreach(f=>println(f))
    //val vecc =vec_labled.rdd.map{case Row(labels: String,filteredWords:Array[String],result:Vector)=>(labels,filteredWords,result)}
    //vecc.foreach(f=>println(f))
    val labels =vec_labled("labels")+"_"+vec_labled("filteredWords")
    val labels2=vec_labled.selectExpr("explode(filteredWords)","labels","filteredWords","result")
    labels2.printSchema()
    //labels2.foreach(f=>println(f))
    val labels3=labels2.selectExpr("concat_ws('-',labels,col)","labels","filteredWords","result")
    labels3.printSchema()
    labels3.foreach(f=>println(f))




    //val wordvectors=topwordterms.map{f=>model.transform(topwordterms)}


    //val vec =model.getVectors
    //val vec2=vec.select("vector","word")
    //val vec3=  vec2.filter(vec("word").isin(topwordterms.toList:_*)).rdd
    val vec3=labels3.select("result","concat_ws(-, labels, col)").rdd
    vec3.coalesce(1,true).saveAsTextFile("data/wordvector2")



    //input to SOM
    val infile= new File("data/wordvector2/part-00000")
    val outfile=new File("somclusters2.txt")
    new WordClusterSOM(infile,outfile,sc)




    //kMeans on SOM to form signatures for sub categories or topics
    // Load and parse the data for kMeans Input
    val data = sc.textFile("somclusters2.txt")
    val arraydata =data.map{ f=>f.split("\t")}
    val vecdata: RDD[Vector] =arraydata.map{ f=>Vectors.dense(f(1).toDouble,f(2).toDouble)}
    val worddata =arraydata.map{ f=>f(0)}
    val worddata2 =arraydata.map{ f=>f(0).split("-")}
    val writer2 = new PrintStream("som_with_labels.txt")
    val x3=arraydata.map{ f=>(f(0).split("-")(0),f(0).split("-")(0),Vectors.dense(f(1).toDouble,f(2).toDouble))}

    arraydata.collect().foreach{ f=>
      writer2.println((f(0).split("-")(0)+"\t"+f(0).split("-")(1)+"\t"+f(1)+"\t"+f(2)))
    }






    // Cluster the data into 40 classes using KMeans
    val numClusters = 40
    val numIterations = 1000
    val clusters = KMeans.train(vecdata, numClusters, numIterations)
    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(vecdata)
    println("Within Set Sum of Squared Errors = " + WSSSE)
    // Save and load model
    clusters.save(sc, "data/kMeansOnSOMmodel")
    //val sameModel = KMeansModel.load(sc, "data/kMeansOnSOMmodel")

    val mapClusterIndices =clusters.predict(vecdata)
    val x: RDD[((String, Vector), Int)] =worddata.zip(vecdata).zip(mapClusterIndices)

    val writer = new PrintStream("data/somclusters3.txt")
    x.collect.foreach { f =>
      writer.println(f._1._1.toString+"\t"+f._1._2.toString.replace("[","").replace("]","").replace(",","\t")+"\t"+f._2.toString)
    }

    val nvinput=x.map{f=>new LabeledPoint(f._2,f._1._2)}
    nvinput.foreach(f=>println(f))
    nvinput.saveAsTextFile("data/nvinput")

    // Split data into training (70%) and test (30%).
    val splits = nvinput.randomSplit(Array(0.8, 0.2), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    val model2 = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    val predictionAndLabel = test.map(p => (model2.predict(p.features), p.label))
    predictionAndLabel
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

    //predictionAndLabel.foreach{f=>println(f)}

    println("accuracy of naive bayes model: "+accuracy)
    //predictionAndLabel.foreach(f=>println(f))
    val metrics=new MulticlassMetrics(predictionAndLabel)

    val confmatrix: Matrix =metrics.confusionMatrix
    println("ConfusionMatrix; ")
    confmatrix.rowIter.foreach(f=>println(f))
    val wghtprecision: Double =metrics.weightedPrecision
    val wghtrecall: Double =metrics.weightedRecall
    val wghtfmeasure: Double =metrics.weightedFMeasure
    println("Accuracy: "+metrics.accuracy)
    println("Weighted Precision: "+wghtprecision)
    println("Weighted Recall: "+wghtrecall)
    println("Weighted FMeasure: "+wghtfmeasure)


    // Save and load model
    model2.save(sc, "data/myNaiveBayesModel")
    val sameModel = NaiveBayesModel.load(sc, "data/myNaiveBayesModel")



    spark.stop()
    sc.stop()
  }

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.filter(_.isDirectory).map(_.getName)
  }



}
