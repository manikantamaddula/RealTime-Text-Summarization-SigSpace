package edu.umkc.textanalytics.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.mongodb.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Created by Manikanta on 12/7/2016.
 */
public class summarization extends BaseRichBolt {
    OutputCollector _collector;
    String taskName;
    HashMap<String, String> Sentences = new HashMap<String, String>();
    HashMap<String, Integer> Counts = new HashMap<String, Integer>();
    int classCount = 0;
    JSONArray ja = new JSONArray();

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector collector) {

        _collector = collector;
        taskName = context.getThisComponentId() + "_" + context.getThisTaskId();
        System.out.println("prepare statement in summarization bolt");
    }

    @Override
    public void execute(Tuple tuple) {

        System.out.println("execution statement in summarization bolt");
        System.out.println("sentence inside summarization bolt "+tuple.getStringByField("sentence")+" its prediction: "+tuple.getStringByField("prediction"));

        if(!tuple.getStringByField("sentence").equals("end") ) {

            String pred = tuple.getStringByField("prediction");
            String sentence = tuple.getStringByField("sentence");

            Sentences.put(sentence,pred );

            if (Counts.containsKey(pred)) {
                classCount = Counts.get(pred);
                classCount++;
                Counts.put(pred, classCount);
            } else {
                Counts.put(pred, 1);
            }
        }
        else
        {
            System.out.println("Inside else statement");
            List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(Counts.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>(){
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
                    return(o2.getValue()).compareTo(o1.getValue());
                }
            });

            Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
            for(Map.Entry<String, Integer> e1 : list){
                sortedMap.put(e1.getKey(), e1.getValue());
            }


            Map.Entry<String,Integer> entry=sortedMap.entrySet().iterator().next();
            String highpred= entry.getKey();
            System.out.println("high predictions: "+highpred);
            //Integer value=entry.getValue();

            List<String> Sentenceslist = new ArrayList<String>();
            for (Map.Entry<String, String> e : Sentences.entrySet()){
                if (e.getValue().equals(highpred)){

                    Sentenceslist.add(e.getKey());
                    System.out.println("summarized sentences: "+e.getKey());
                }
            }


            //Saveto MongoDB
            MongoClientURI uri = new MongoClientURI("mongodb://manikanta:manikanta@ds127878.mlab.com:27878/rbdtextsummarization");
            MongoClient client = new MongoClient(uri);

            DB db = client.getDB(uri.getDatabase());
            DBCollection location = db.getCollection("summarytext");

            JSONObject jsonObj  = new JSONObject();



            BasicDBObject searchQuery2 = new BasicDBObject().append("textsummarization", "summarizedtext");
            //DBObject olddocument=location.findOne(searchQuery2);

            //Object oldlocations =olddocument.get("lattitude");
            //oldlocations.toString();
            //olddocument.put("lattitude",lattitude);

            BasicDBObject locations = new BasicDBObject();

            JSONObject jo = new JSONObject();
            jo.put("text", Sentenceslist);



            ja.add(jo);

            System.out.println("updated array: "+Arrays.deepToString(ja.toArray()));


            //olddocument.put("locations",jo);

            locations.put("text",Sentenceslist);



            BasicDBObject updateDocument = new BasicDBObject();
            updateDocument.append("$set", new BasicDBObject().append("text", Sentenceslist));

            location.update(searchQuery2, updateDocument);

        }



    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
