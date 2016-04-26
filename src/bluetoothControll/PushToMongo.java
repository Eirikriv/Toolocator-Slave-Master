package bluetoothControll;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.json.JSONException;
import org.json.JSONObject;


public class PushToMongo{
	MongoClientURI clientURI = new MongoClientURI("mongodb://banana:banana@aws-us-east-1-portal.16.dblayer.com:10205,aws-us-east-1-portal.15.dblayer.com:10195/admin");
    MongoClient client = new MongoClient(clientURI); //that string over is our connectionString to the MongoDB
    String database = "pingBox"; //name of the database
    MongoDatabase db = client.getDatabase(database);//gets the database
	String zone;
	String ID;
  
public JSONObject createJSON(String ID, String zone) throws JSONException{ //make JSONObject of the beaconID (MAC adress) and zone ID
	  this.ID = ID;
	  this.zone = zone;
      JSONObject jo = new JSONObject();
      jo.put("areYouBeacon", true);
      jo.put("beaconID", ID);
      jo.put("zone", zone);     
      return jo;
  }
 
 
  public DBObject convertJSON() throws JSONException{ //method to convert to database object
      DBObject dbObject = (DBObject) JSON.parse(createJSON(ID, zone).toString());
      //System.out.println(dbObject);
      return dbObject;
  }
 
  private String splitBeaconID() throws JSONException{ //method to convert to database object
      String beaconID = (String) convertJSON().get("beaconID");
      return beaconID;
  }
 
  private String splitZone() throws JSONException{ //method to convert to database object
      String zone = (String) convertJSON().get("zone");
      return zone;
  }
 
  private String makeZoneaString() throws JSONException{ //method to convert to database object
      return "zone" + splitZone();
  }
 
  public ArrayList<String> pullIDs(String zoneNr){ //method to convert to database object
	//Pull info from DB:
	  FindIterable<Document> iterable = db.getCollection("beaconStatus").find(new Document("currentZone", zoneNr));
	  //System.out.println("SNAKK TIL MEG DA FAEN:"+db.getCollection("beaconStatus").find(new Document("currentZone", zoneNr)));
	  final ArrayList<String> beaconList = new ArrayList<String>();
	  iterable.forEach(new Block<Document>() {
	      public void apply(final Document document) {
	          beaconList.add((String) document.get("beaconID"));
	          System.out.println(beaconList);
	      }
	  });
	return beaconList;
  } 

  public void pushIt() throws JSONException{
	  
          try {
         
              DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
              Date today = Calendar.getInstance().getTime();
              String reportDate = df.format(today);
      
              //Updating the ZoneArray
              db.getCollection("beaconStatus").updateOne(new Document("beaconID", splitBeaconID()), new Document("$addToSet", new Document(makeZoneaString(), reportDate)));
             
              //Updating currentZone in many documents
              db.getCollection("beaconStatus").updateOne(new Document("beaconID", splitBeaconID()), new Document("$set", new Document("currentZone", splitZone())));
              //System.out.println("I did it nigguh");
             
              //Updating last seen
              db.getCollection("beaconStatus").updateOne(new Document("beaconID", splitBeaconID()), new Document("$set", new Document("lastSeen", reportDate)));
             
          } catch (Exception e) {
              System.out.println("Jeg feiler");
          }
      }
  /*public void pushOnlyStatus() throws JSONException{
	  
	  db.getCollection("beaconStatus").updateOne(new Document("beaconID", splitBeaconID()), new Document("$addToSet", new Document("currentZone", splitBeaconID())));
	  
  }*/
  
  
  
  
}
