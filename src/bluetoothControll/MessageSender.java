package bluetoothControll;

import java.io.*; 
import java.net.*;

import org.json.JSONException;
class MessageSender implements Runnable {    
	
	int socket;
	boolean serverUp;
	MessageSender(int socket, boolean serverUp){ //only initialised at the start of the Slave, we set the port (socket and a boolean to keep the server running here)
		this.socket = socket; 
		this.serverUp = serverUp;
	}
	
	@Override
	public void run() { //must be implemented in order to run in a thread
			try {
				startListen(); //starts to listen from pings from master
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	public void startListen() throws IOException, JSONException{ //this method continously listen for pings from master, when it gets a ping, the Slaves "toSlave" JSONObject is sent to the master, containg all beaconIDs	
		
		@SuppressWarnings("resource")
		ServerSocket welcomeSocket = new ServerSocket(socket);
		System.out.println("slaveServerIsUp");
		while(serverUp){
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				if(inFromClient.readLine() != null){
					DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
					System.out.println("Ping Recived from Master, sending BeaconStatus");
					outToClient.writeBytes(SlaveProto.toSlave.toString());
					System.out.println("Now sending Master the list created");
				}
				
		}
		
	}
}

