package bluetoothControll;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SimpleHelloWorld {
	
	String e =  "helloWorld";
	
	void print() throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter("fileeee.txt", "UTF-8");
		writer.println("The first line");
		writer.println("The second line");
		writer.close();
	}
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		SimpleHelloWorld t = new SimpleHelloWorld();
		t.print();
		System.out.println("hei");
	}

}



