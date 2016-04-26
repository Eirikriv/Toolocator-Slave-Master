package bluetoothControll;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager implements Runnable {
	
	public ConnectionManager(List<Integer> sharedQueue){
		this.taskQueue = sharedQueue;
	}
	private final List<Integer> taskQueue;//syncronises the connectionmanager thread in the slave MAIN 
	
	public String[] listenCommand = {"/bin/sh", "-c", "/usr/bin/hcitool lescan > /home/reaktivistene/rawBeaconScan.txt & sleep 3; pkill --signal SIGINT hcitool"};
	//String[] listenCommand is the command that is sent to the LINUX shell on the raspberries
	public String[] deleteLogAndMakeNew = {"/bin/sh" , "-c", "/usr/bin/rm -f  /home/reaktivistene/rawBeaconScan.txt & /usr/bin/touch /home/reaktivistene/rawBeaconScan.txt"};
	//String[] deleteLogAndMakeNew replaces the old rawBeaconScan.txt with an empty one so that it is ready for the scans
	public String[] hcireset = {"/bin/sh", "-c", "/usr/bin/hciconfig sudo hci0 down & sleep 2; /usr/bin/hciconfig sudo hci0 up"};
	//resets hcitool (bluetooth scanner, unless you get the error "i/o parameters not set"
	public boolean keepAlive = true;
	//constant for keeping the connectionManager running
	public String rawScannPath = "/home/reaktivistene/rawBeaconScan.txt";
	//path to the txt file with the fresh scans
	public void run() { // is run about 20 times / minute, this scans for nearby beacons and saves the recieved pings as MAC adresses in the txt file
		try {
			Runtime.getRuntime().exec(hcireset); //resets before each Scan
			Thread.sleep(5); //sleep it so the hcireset is finnished before the thread continues
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(keepAlive){
			try{
				Scan(); //the scanning function
						
		} catch (InterruptedException e) {
			System.out.println("wrong in ConnectionManagerClass");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		}
	}
	public void Scan() throws IOException, InterruptedException{
		synchronized (taskQueue) {
		Runtime.getRuntime().exec(deleteLogAndMakeNew);
		Thread.sleep(3); //sleep it so the files are created before the scanner starts to write to it
		
		
		Runtime.getRuntime().exec(listenCommand); 
		Thread.sleep(2500); //sleep it here while the Slave reads from the scanner
		CreateShortListOfBeacons(reduceList(rawScannPath), "/home/reaktivistene/newBeaconScan.txt"); //a function that removes redundant beacons and saves them in the newBeaconScan.txt File
		taskQueue.notify(); //thread managment
		taskQueue.wait(); //thread managment

	
		System.out.println("Raw file processed, list ready to be used");
		taskQueue.notifyAll();
		}
	}
	
	
	public String[] Openfile(String path) throws IOException { //method to read file
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
	int readLines(String path) throws IOException{ //method to help the method Openfile
		FileReader file_to_read = new FileReader(path);
		BufferedReader bf = new BufferedReader(file_to_read);
		int numberOfLines = 0;
		while ((bf.readLine()) != null){
			numberOfLines++;}
		bf.close();
		file_to_read.close();
		return numberOfLines;
	}
	ArrayList<String> reduceList(String filepathToBeaconsInSone) throws IOException{ //this method removes all redundant beaconIDs (many pings from the same will only be counted once)
		String[] BeaconsInSone = Openfile(filepathToBeaconsInSone); 
		ArrayList <String> reducedList = new ArrayList<>();
		for(int x = 1; x < BeaconsInSone.length -1; x++){
			if(reducedList.contains(BeaconsInSone[x].substring(0, 17))){
			} else {
				reducedList.add(BeaconsInSone[x].substring(0, 17));
			}
		}
		return reducedList;	
	}
	public void CreateShortListOfBeacons(ArrayList<String> output , String path) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(path, "UTF-8"); //this method just writes the input to the given path
		for(String x: output){
			writer.println(x);
		}
		writer.close();
	}
	
}


