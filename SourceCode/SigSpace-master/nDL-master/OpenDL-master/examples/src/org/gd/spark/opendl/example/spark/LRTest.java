package org.gd.spark.opendl.example.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.gd.spark.opendl.downpourSGD.SGDTrainConfig;
import org.gd.spark.opendl.downpourSGD.SampleVector;
import org.gd.spark.opendl.downpourSGD.Softmax.LR;
import org.gd.spark.opendl.example.ClassVerify;
import org.gd.spark.opendl.example.DataInput;
import org.gd.spark.opendl.downpourSGD.train.DownpourSGDTrain;

public class LRTest {
	private static final Logger logger = Logger.getLogger(LRTest.class);
	
	public static void main(String[] args) {
		try {
//			int x_feature = 480;
//			int y_feature = 101;
//			List<SampleVector> samples = DataInput.readCaltech101("/Users/pradyumnad/Documents/Generated/MCDD/Hists", x_feature, y_feature);

			int x_feature = 128;
			int y_feature = 101;
			List<SampleVector> samples = DataInput.readCaltech101("/Users/pradyumnad/Documents/Generated/MCDD/SIFT500DL", x_feature, y_feature);

			List<SampleVector> trainList = new ArrayList<SampleVector>();
			List<SampleVector> testList = new ArrayList<SampleVector>();
			DataInput.splitList(samples, trainList, testList, 0.8);
			
			JavaSparkContext context = SparkContextBuild.getContext(args);
			JavaRDD<SampleVector> rdds = context.parallelize(trainList);
			rdds.count();
			logger.info("RDD ok.");
			
			LR lr = new LR(x_feature, y_feature);
            SGDTrainConfig config = new SGDTrainConfig();
            config.setUseCG(true);
            config.setCgEpochStep(100);
            config.setCgTolerance(0);
            config.setCgMaxIterations(30);
            config.setMaxEpochs(100);
            config.setNbrModelReplica(4);
            config.setMinLoss(0.01);
            config.setUseRegularization(true);
            config.setMrDataStorage(StorageLevel.MEMORY_ONLY());
            config.setPrintLoss(true);
            config.setLossCalStep(3);
            config.setParamOutput(true);
            config.setParamOutputStep(3);
            config.setParamOutputPath("wb.bin");
            
            logger.info("Start to train lr.");
            DownpourSGDTrain.train(lr, rdds, config);
            
            int trueCount = 0;
            int falseCount = 0;
            double[] predict_y = new double[y_feature];
            for(SampleVector test : testList) {
            	lr.predict(test.getX(), predict_y);
            	if(ClassVerify.classTrue(test.getY(), predict_y)) {
            		trueCount++;
            	}
            	else {
            		falseCount++;
            	}
            }
            logger.info("trueCount-" + trueCount + " falseCount-" + falseCount);
		} catch(Throwable e) {
			logger.error("", e);
		}
	}

}
