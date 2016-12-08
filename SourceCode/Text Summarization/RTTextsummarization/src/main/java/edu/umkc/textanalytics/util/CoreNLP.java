package edu.umkc.textanalytics.util;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mayanka on 27-Jun-16.
 */
public class CoreNLP {
    public static String returnLemma(String sentence) {
        //System.out.println("inside lemma: ");

        Document doc = new Document(sentence);
        //System.out.println("after reading doc");
        String lemma="";
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            //System.out.println("inside for loop");
            //System.out.println(sent);
            List<String> l=sent.lemmas();
            for (int i = 0; i < l.size() ; i++) {
                lemma+= l.get(i) +" ";
            }
         //   System.out.println(lemma);
        }

        return lemma;
    }

    public static List<String> returnDocument(String sentence) {

        List<String> sentlist=new ArrayList();
        Document doc = new Document(sentence);

        for (Sentence sent : doc.sentences()) {

            //System.out.println("sentences inside corenlp class: "+sent);
            sentlist.add(sent.toString());
        }

        sentlist.add("end");


            return sentlist;
    }

}
