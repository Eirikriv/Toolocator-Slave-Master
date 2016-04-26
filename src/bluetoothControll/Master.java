package bluetoothControll;
import java.io.*; 
import java.net.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.Messaging.SyncScopeHelper;

public class Master {
	
	
	PushToMongo pusher = new PushToMongo();
	ArrayList<String> changedSone = new ArrayList<>();
	public String[] onExit ={"/bin/sh" , "-c", "/usr/bin/python /home/reaktivistene/Toolocator/on_exit.py"}; //path to pythonlight and sound Script uncomment if the berryclip is not on the master
	public String[] onEntry ={"/bin/sh" , "-c", "/usr/bin/python /home/reaktivistene/Toolocator/on_enter.py"}; //path to pythonlight and sound Script uncomment if the berryclip is not on the master
	
	public JSONObject parce(String fromSlave) throws ParseException, JSONException{//parce the incoming JSONObject from slaves
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(fromSlave);
	return json;
	}
	
	public void checkSoneAndUpdateDB(String zone, JSONObject sone1, JSONObject sone2 ) throws ParseException, JSONException, Exception{ //checking zone by zone, pulls first all beacons previously known to be in sone 1,2 or x. 
			//then compares the recieved lists from the sones and updates the DB if canges had been made
		JSONObject fromSlave = new JSONObject();
		JSONObject otherSlave = new JSONObject(); //make it so we only need to pull each sone once
		if(zone.equals("1")){
			fromSlave = sone1;
			otherSlave = sone2;
		} else{
			fromSlave = sone2;
			otherSlave = sone1;
		}

		JSONArray beaconsFromSlave = (JSONArray)fromSlave.get("beaconList");
		JSONArray beaconsFromOtherSlave = (JSONArray)otherSlave.get("beaconList");
		System.out.println("beaconsFromSlave :" + beaconsFromSlave.toJSONString()); //here we do a print 
		ArrayList<String> beaconsFromMongo = pusher.pullIDs(zone);
		System.out.println("beaconsFromMongo :" + beaconsFromMongo);
		System.out.println("her kommer forst" + beaconsFromSlave);
		int counter = 0;
		int counter2 =0;
		while(counter < beaconsFromSlave.size()){
			if(beaconsFromSlave.get(counter).equals("E0:38:55:85:5D:43")){counter++;}else if(beaconsFromSlave.get(counter).equals("E5:63:DF:42:DA:99")){counter++;}else if(beaconsFromSlave.get(counter).equals("E2:D2:E8:EE:79:CD")){counter++;}else{	
				beaconsFromSlave.remove(counter);
			}
		}
		while(counter2 < beaconsFromOtherSlave.size()){
			if(beaconsFromOtherSlave.get(counter2).equals("E0:38:55:85:5D:43")){counter2++;}else if(beaconsFromOtherSlave.get(counter2).equals("E5:63:DF:42:DA:99")){counter2++;}else if(beaconsFromOtherSlave.get(counter2).equals("E2:D2:E8:EE:79:CD")){counter2++;}else{	

				beaconsFromOtherSlave.remove(counter2);
			}
		}
		System.out.println("comparing beacons in sone with beacons registred in the DB"); //here is a print
	
		int flagg = 1;
		
		if(beaconsFromSlave.size() > beaconsFromMongo.size()){
			flagg = 2;
		} else if(beaconsFromSlave.size() < beaconsFromMongo.size()){
			flagg = 3;
		}
		if(flagg==1){
			for(int a = 0; a < beaconsFromSlave.size();a++){
				if(!beaconsFromMongo.contains(beaconsFromSlave.get(a))){
					pusher.createJSON((String) beaconsFromSlave.get(a), zone);
					pusher.pushIt();
					changedSone.add((String) beaconsFromSlave.get(a));
					Runtime.getRuntime().exec(onEntry);
				}
				else{
					continue;
				}
			}
			for(int a = 0; a < beaconsFromMongo.size();a++){
				if(!beaconsFromSlave.contains(beaconsFromMongo.get(a)) && !changedSone.contains(beaconsFromMongo.get(a))){ //her kan det bli feil
					pusher.createJSON((String) beaconsFromMongo.get(a), "X");
					pusher.pushIt();
					Runtime.getRuntime().exec(onExit);
				}
				else{
					continue;
				}
			}
		} else if(flagg==2){
			for(int a = 0; a < beaconsFromSlave.size();a++){
				if(!beaconsFromMongo.contains(beaconsFromSlave.get(a))){
					pusher.createJSON((String) beaconsFromSlave.get(a), zone);
					pusher.pushIt();
					Runtime.getRuntime().exec(onEntry);
					changedSone.add((String) beaconsFromSlave.get(a));
				} 
				else{
					continue;
				}
			}
		} else if(flagg==3){
			for(int a = 0; a < beaconsFromMongo.size();a++){ //her forsvinner en beacon ut fra sonen totalt
				if(!beaconsFromSlave.contains(beaconsFromMongo.get(a)) && !changedSone.contains(beaconsFromMongo.get(a))){ //her kan det bli feil
					pusher.createJSON((String) beaconsFromMongo.get(a), "X");
					Runtime.getRuntime().exec(onExit);
					pusher.pushIt();
				}
				else{
					continue;
				}
			}
		}
		System.out.println("finishing updating the database");
	}
	public void checkAllSonesAndUpdateDB(int numberOfSones) throws ParseException, JSONException, Exception{
		JSONObject sone1 = parce(pingSlave("129.241.208.100", "7924"));
		JSONObject sone2 = parce(pingSlave("129.241.209.106","7924"));
		
		for(int i = 1; i < numberOfSones+1; i ++){
			checkSoneAndUpdateDB(String.valueOf(i),sone1,sone2);
		}
		
	}
	
	public String pingSlave(String slaveIP, String slavePort) throws Exception  {   //the method that the master uses to ping the slave and get list with beacons
		
		String sentence = "request_beacons";   
		Socket clientSocket = new Socket(slaveIP, Integer.parseInt(slavePort));   
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());    
		outToServer.writeBytes(sentence + '\n');
		System.out.println("pingedSlave");
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  
		String bluetoothBeaconID = inFromServer.readLine();
		System.out.println("recieved ping from Slave");
		//System.out.println("list recieved list are: " + bluetoothBeaconID);
		clientSocket.close();
		System.out.println("closed connection");
		return bluetoothBeaconID;
		} 
	
	public static void main(String[] args) throws ParseException, JSONException, Exception { //master main method
		while(true){
		Master me = new Master();
		
		me.checkAllSonesAndUpdateDB(2);
		
		}

	}
	
	
}
	