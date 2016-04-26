package bluetoothControll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class SlaveProto {
	
	

	public static ArrayList<ArrayList<String>> buffer = new ArrayList<>();
	public static int numberOfScans = 10; //number of scans that is done before the buffer starts to replace old scans
	
	public static JSONObject toSlave; //this field is updated every 25 secounds and holds the current list of beacons in the range of the ble reciever
	
	public String[] Openfile(String path) throws IOException { //openfile used to open freshBeaconScan.txt
		FileReader fr = new FileReader (path);
		BufferedReader textReader = new BufferedReader(fr);
		int numberOfLines = readLines(path);
		String[] listWithTextData = new String[numberOfLines];
		for (int i=0 ; i < numberOfLines; i++){
			listWithTextData[i] = textReader.readLine();}
		textReader.close();
		fr.close();
		return listWithTextData;
	}
	public int readLines(String path) throws IOException{				//helps tp read file
		FileReader file_to_read = new FileReader(path);
		BufferedReader bf = new BufferedReader(file_to_read);
		int numberOfLines = 0;
		while ((bf.readLine()) != null){
			numberOfLines++;}
		bf.close();
		file_to_read.close();
		return numberOfLines;
	}
	
	public void addToBuffer(String path) throws IOException{ //here we reduce the 10 scans from the buffer to one list that contains all the registred beacons from the last 25 secounds
		ArrayList<String> temp = new ArrayList<>();
		String[] ListWithMeasurements = Openfile(path);
		for(String x: ListWithMeasurements){
			temp.add(x);
		}
		if(buffer.size()<numberOfScans){
		buffer.add(temp);
		}
		else {
			buffer.remove(0);
			buffer.add(temp);
		}	
	}
	
	
	public  void updateJSONToMaster() throws JSONException{ //updates the JSON object that can be sent to the master
		ArrayList<String> temp = new ArrayList<>();
		JSONObject jSONtoMaster = new JSONObject();
		for(ArrayList<String> scans : buffer){//loops thru all scans 
			for(String bufferScans : scans){//loops thru all elementsin one scan and only adds those not added before
				if(temp.contains(bufferScans)){
					continue;
				}
				else{
					temp.add(bufferScans);
				}
			}
		}					
		jSONtoMaster.put("beaconList", temp);
		toSlave = jSONtoMaster;
	}
	
	public static void main(String[] args) throws InterruptedException, IOException, JSONException {
		List<Integer> taskQueue = new ArrayList<Integer>();
		
		SlaveProto mat = new SlaveProto();
		Thread serverCon = new Thread(new MessageSender(7924 ,true)); //starting the server thread, this listens for pings from master
		Thread bleCon = new Thread(new ConnectionManager(taskQueue)); //starting the blescanner
		bleCon.start();
		serverCon.start();
		while(true){
		synchronized (taskQueue) { //must syncronise the execution of the connectionmanager and the S
			taskQueue.notify();
			taskQueue.wait();
			System.out.println("the scan has now finished and slave can use the created txt file");
			mat.addToBuffer("/home/reaktivistene/newBeaconScan.txt");
			mat.updateJSONToMaster();

			}
		}
	}
	
	
	

}
