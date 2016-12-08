package com.lab6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;

/**
 * Servlet implementation class UDoperations
 */
@WebServlet("/UDoperations")
public class UDoperations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UDoperations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		try{
			System.out.println("in get method");
		int responseCode = 0;
		String city=request.getParameter("cityname4");
		
		MongoClientURI uri = new MongoClientURI("mongodb://manikanta:manikanta@ds031962.mlab.com:31962/assignmentlab7");
		MongoClient client = new MongoClient(uri);

		DB db = client.getDB(uri.getDatabase());
		DBCollection weather = db.getCollection("weatherdata");

		
		BasicDBObject query = new BasicDBObject();
		query.append("name",city);
		
		weather.remove(query);
		PrintWriter write=response.getWriter();
		write.println("<p> Deleted the Details of "+city+"</p>");
	    }
		catch(Exception e)
		{
			
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		

		// TODO Auto-generated method stub
		//doGet(request, response);
		try{
			System.out.println("in post method");
		int responseCode = 0;
		String city=request.getParameter("cityname3");
		
		
		String api="http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID=1abe9c87c394ef77b86b71f0a1459ec9&mode=json";
		URL url = new URL(api);
		String abd=url.toString();
		String vc=callURL(abd);
		System.out.println("Data"+vc);
		
		
		JSONObject jsonObject  = new JSONObject(vc);
		
	    System.out.println("JSON details"+jsonObject);
	    JSONObject a=jsonObject.getJSONObject("main");
	    JSONArray weathercondition=jsonObject.getJSONArray("weather");
	    System.out.println("please get me"+weathercondition);
	    Double b=(Double)a.get("temp");
	    String name=(String)jsonObject.get("name");
	    //Double tmp=(double) Math.round(b);
	    Double ch=foreign(b);
	    
	    System.out.println("please get me"+a);
	    String desc=(String)weathercondition.getJSONObject(0).getString("description");
	    String b1=b.toString();
	    
	    PrintWriter write=response.getWriter();
	    write.println("<p>Name: "+name+"</p>");
	    write.println("<p>Temperature is: "+ch+" Farenheit</p>");
	    write.println("<p>Description: "+desc+"</p>");
	    
	    
	    String api2="http://maps.googleapis.com/maps/api/geocode/json?address="+city;
	    URL url2 = new URL(api2);
		String abc=url2.toString();
		String vc2=callURL(abc);
		System.out.println("Data"+vc2);
		
		JSONObject jsonObject2  = new JSONObject(vc2);
		JSONArray a2=jsonObject2.getJSONArray("results");
	    String placeid=(String)a2.getJSONObject(0).getString("place_id");
	    //Double lat=(Double)a2.getJSONObject(0).getDouble("lat");
	    System.out.println("place_id: "+placeid);
	    //System.out.println("longitude: "+lat);
	    write.println("<p>Place ID for "+city+" is: "+placeid+" (from google maps api)"+"</p>");
	    
	    
	    //String lon=lat;
	    //String api3="https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=befe37fb0b7ad787f75f5447b60025b1&place_id="+placeid+"&accuracy=1&tags="+desc+"&sort=relevance&extras=url_l&format=json";
	    
	    //URL url3 = new URL(api3);
		//String a3=url3.toString();
		//String vc3=callURL(a3);
		//System.out.println("Data"+vc3);
		
		//JSONObject jsonObject3  = new JSONObject(vc3);
	    //System.out.println("JSON details"+jsonObject3);
	    
	    MongoClientURI uri = new MongoClientURI("mongodb://manikanta:manikanta@ds031962.mlab.com:31962/assignmentlab7");
		MongoClient client = new MongoClient(uri);

		DB db = client.getDB(uri.getDatabase());
		DBCollection weather = db.getCollection("weatherdata");
		
		
		
		System.out.println(weather.getName());
		
		JSONObject jsonObj  = new JSONObject();
		
		jsonObj.put("name",name);
		jsonObj.put("temperature",ch);
		jsonObj.put("description",desc);
		jsonObj.put("placeid",placeid);
		
		
		DBObject dbObject= (DBObject)JSON.parse(jsonObj.toString());
		//DBObject dbObject1= (DBObject)JSON.parse(a2.toString());
		//BasicDBObject newDocument = dbObject;
		System.out.println(jsonObj);
		
		//newDocument.append("$set", new BasicDBObject().append("", false));
		//newDocument.append();
		//System.out.println(newDocument.toString());
		//BasicDBObject searchQuery = new BasicDBObject().append("name", city);
		//System.out.println(searchQuery.toString());
	    //weather.update(searchQuery, newDocument);
		//weather.update(dbObject);
		weather.insert(dbObject);
	    write.println("<p> Update the Details of "+city+"</p>");
	    
		}
		catch(Exception e)
		{
			
		}
	
	}
	
public Double foreign(Double b) {
		
		double x = ((b-273.0) * 9.0/5.0) + 32.0;
		Double fahrenhiet=(double) Math.round(x);
		return fahrenhiet;
	}


public static String callURL(String myURL) {
	//System.out.println("Requested URL:" + myURL);
	StringBuilder sb = new StringBuilder();
	URLConnection urlConn = null;
	InputStreamReader in = null;
	try {
		URL url = new URL(myURL);
		urlConn = url.openConnection();
		if (urlConn != null)
			urlConn.setReadTimeout(60 * 1000);
		if (urlConn != null && urlConn.getInputStream() != null) {
			in = new InputStreamReader(urlConn.getInputStream(),
					Charset.defaultCharset());
			BufferedReader bufferedReader = new BufferedReader(in);
			if(bufferedReader!=null)
			{
				int cp;
				while ((cp = bufferedReader.read()) != -1) {
					sb.append((char) cp);
				}
				bufferedReader.close();
			}
		}
	in.close();
	} catch (Exception e) {
		throw new RuntimeException("Exception while calling URL:"+ myURL, e);
	} 

	return sb.toString();
			}

}
