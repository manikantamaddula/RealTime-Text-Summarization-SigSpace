package edu.umkc.textanalytics.spouts


import java.util

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.tuple.Fields


/**
  * Created by Manikanta on 12/3/2016.
  */
class wordCount extends BaseRichSpout {


  def open(map: util.Map[String, Object], topologyContext: TopologyContext, spoutOutputCollector: SpoutOutputCollector): Unit = {

    println("inside spout: xxxxxxxxxxxxxxxxxxxxxxxxxxxx")

  }


  override def nextTuple(){


  }


  override def declareOutputFields(outputFieldsDeclarer: _root_.backtype.storm.topology.OutputFieldsDeclarer): Unit ={
    outputFieldsDeclarer.declare(new Fields("articleid", "sentence"))
  }

}
