package org.gd.spark.opendl.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.gd.spark.opendl.downpourSGD.SampleVector;

public class DataInput {
    private static Random rand = new Random(System.currentTimeMillis());

    /**
     * Read sample data from mnist_784 text file
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static List<SampleVector> readMnist(String path, int x_feature, int y_feature) throws Exception {
        List<SampleVector> ret = new ArrayList<SampleVector>();
        String str = null;
        BufferedReader br = new BufferedReader(new FileReader(path));
        while (null != (str = br.readLine())) {
            String[] splits = str.split(",");
            SampleVector xy = new SampleVector(x_feature, y_feature);
            xy.getY()[Integer.valueOf(splits[0])] = 1;
            for (int i = 1; i < splits.length; i++) {
                xy.getX()[i - 1] = Double.valueOf(splits[i]);
            }
            ret.add(xy);
        }
        br.close();
        return ret;
    }

    public static List<SampleVector> readMyMnist(String path, int x_feature, int y_feature) throws Exception {
        List<SampleVector> ret = new ArrayList<SampleVector>();
        String str = null;
        BufferedReader br = new BufferedReader(new FileReader(path));
        while (null != (str = br.readLine())) {
            String[] parts = str.split(",");

            SampleVector xy = new SampleVector(x_feature, y_feature);
            xy.getY()[Integer.valueOf(parts[0])] = 1;
            String[] splits = parts[1].split(" ");
            for (int i = 1; i <= splits.length; i++) {
                xy.getX()[i - 1] = Double.valueOf(splits[i-1]);
            }
            ret.add(xy);
        }
        br.close();
        return ret;
    }

    /**
     * Read sample data from Caltech 101 folder
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<String>();

        if (folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    listFilesForFolder(fileEntry);
                } else if (!fileEntry.isHidden()) {
                    files.add(fileEntry.getName());
                    System.out.println(fileEntry.getName());
                }
            }
        } else {
            System.err.println("Not a directory");
        }
        return files;
    }

    public static List<SampleVector> readCaltech101(String path, int x_feature, int y_feature) throws Exception {
        ArrayList<String> files = listFilesForFolder(new File(path));

        List<SampleVector> ret = new ArrayList<SampleVector>();

        for (String file : files) {
            String str = null;
            BufferedReader br = new BufferedReader(new FileReader(path+"/"+file));
            while (null != (str = br.readLine())) {
                String[] splits = str.split(",");
                String[] features = splits[1].split(" ");
                SampleVector xy = new SampleVector(x_feature, y_feature);
                xy.getY()[Integer.valueOf(splits[0].split(" ")[1])] = 1;
                for (int i = 0; i < features.length; i++) {
                    xy.getX()[i] = Double.valueOf(features[i]);
                }
                ret.add(xy);
            }
            br.close();
        }
        return ret;
    }

    /**
     * Read sample data from mnist_784 text file
     *
     * @return
     * @throws Exception
     */
    public static List<SampleVector> readGoogleStreetData(String featurePath,
                                                          int x_feature, int y_feature) throws Exception {
        List<SampleVector> ret = new ArrayList<SampleVector>();
        String str = null;
        BufferedReader br = new BufferedReader(new FileReader(featurePath));
        while (null != (str = br.readLine())) {
            String[] splits = str.split(",");
            SampleVector xy = new SampleVector(x_feature, y_feature);
            xy.getY()[Float.valueOf(splits[0]).intValue()] = 1;
            for (int i = 1; i < splits.length; i++) {
                xy.getX()[i - 1] = Double.valueOf(splits[i]);
            }
            ret.add(xy);
        }
        br.close();
        return ret;
    }

    /**
     * Parallelize list to RDD
     *
     * @param context
     * @param list
     * @return
     * @throws Exception
     */
    public static JavaRDD<SampleVector> toRDD(JavaSparkContext context, List<SampleVector> list) throws Exception {
        return context.parallelize(list);
    }

    /**
     * Split total list read from file to train and test part
     *
     * @param totalList
     * @param trainList
     * @param testList
     * @param trainRatio
     */
    public static void splitList(List<SampleVector> totalList, List<SampleVector> trainList, List<SampleVector> testList, double trainRatio) {
        for (SampleVector sample : totalList) {
            if (rand.nextDouble() <= trainRatio) {
                trainList.add(sample);
            } else {
                testList.add(sample);
            }
        }
    }
}
