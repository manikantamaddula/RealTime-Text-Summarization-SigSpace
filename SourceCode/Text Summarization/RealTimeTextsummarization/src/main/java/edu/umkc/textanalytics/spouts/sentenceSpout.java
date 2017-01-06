package edu.umkc.textanalytics.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import edu.umkc.textanalytics.util.CoreNLP;

import java.util.*;

/**
 * Created by manikanta on 12/8/16.
 */
public class sentenceSpout extends BaseRichSpout{
    SpoutOutputCollector _collector;
    KafkaConsumer<String,String> consumer;
    ConsumerRecords<String, String> records;
    String topicname="TextSummarization";


    @Override
    public void open(Map<String, Object> map, TopologyContext topologyContext, SpoutOutputCollector collector) {
        _collector = collector;
        Properties props = new Properties();

        props.put("bootstrap.servers", "172.16.2.241:9092");
        props.put("group.id", "spout");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topicname));


    }

    @Override
    public void nextTuple() {

        records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
            //System.out.println("inside spout next Tuple: xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(), record.key(), record.value());
            String key = record.key();
            String value = record.value();

            List<String> sentlist=new ArrayList();
            sentlist= CoreNLP.returnDocument(value);

            for (int i = 0; i < sentlist.size(); i++) {
                _collector.emit(new Values(key,sentlist.get(i)),sentlist.get(i));
            }

        }



    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("articleid", "sentence"));

    }

    @Override
    public void close(){ }

    @Override
    public void ack(Object msgId) {
        System.out.println("ack on msgId: " + msgId);

    }

    @Override
    public void fail(Object msgId) {

        System.out.println("fail on msgId: " + msgId);
    }
}
