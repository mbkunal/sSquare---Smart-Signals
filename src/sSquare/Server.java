package sSquare;

import java.net.*;
import java.io.*;

public class Server extends Thread  {

	static int turn = 0;
	
	public static void main(String[] args) throws IOException, InterruptedException  {
		
		
		TCounter tc = new TCounter();
		Thread.currentThread();
		
		
		
			/*int time = tc.calculate(); 
			System.out.println(time);
			Thread.sleep(5000);
			*/
	
	
	
		ServerSocket[] TL = new ServerSocket[4];
	
		
		try {
			for (int i = 0; i < 4; i++) {
				TL[i] = new ServerSocket(6060 + i);
				System.out.println("port opened " + (6060 + i));
			}
		} 	catch (IOException e) {
				e.printStackTrace();
		}
		
		
		DataInputStream[] in = new DataInputStream[4];
		DataOutputStream[] out = new DataOutputStream[4];
		try {
			Socket[] l = new Socket[4];
			for (int i = 0; i < 4; i++) {
				l[i] = TL[i].accept();
				in[i] = new DataInputStream(l[i].getInputStream());
				out[i] = new DataOutputStream(l[i].getOutputStream());
				String str = in[i].readUTF();
				System.out.println("init" + str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		while (true) {
			try {
				out[turn].writeUTF("green " + (new Integer(tc.calculate())).toString());
				String str = in[turn].readUTF();
				System.out.println(str);

			} catch (Exception e) {
				e.printStackTrace();
			}
			turn = (turn + 1) % 4;
		}
	}
}
