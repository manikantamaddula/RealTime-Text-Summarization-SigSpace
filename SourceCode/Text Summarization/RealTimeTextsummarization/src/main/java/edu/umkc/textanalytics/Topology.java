package edu.umkc.textanalytics;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import edu.umkc.textanalytics.bolts.summarization;
import edu.umkc.textanalytics.bolts.textanalysis;
import edu.umkc.textanalytics.spouts.sentenceSpout;


/**
 * Created by Manikanta on 12/3/2016.
 */
public class Topology {



    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        //Spout to generate sentences
        builder.setSpout("sentencesspout", new sentenceSpout(), 1);
        builder.setBolt("textClassification", new textanalysis(),1).fieldsGrouping("sentencesspout",new Fields("articleid"));
        builder.setBolt("summarization",new summarization(),1).fieldsGrouping("textClassification",new Fields("articleid"));



        Config conf = new Config();
        conf.setDebug(false);

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);

            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }
        else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
        }
    }
}
