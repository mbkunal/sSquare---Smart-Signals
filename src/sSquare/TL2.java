package sSquare;

import java.net.*;
import java.io.*;

public class TL2 {

	public static void main(String[] args) {
		String serverName = "localhost"; // TO BE TAKE AS COMMAND LINE ARGUMENT
		int port = 6061; // TO BE TAKE AS COMMAND LINE ARGUMENT
		String me = "TL2";
		String[] ms = new String[2];
		int time = 10; // TO BE CALCLATED USING M.L
		try {
			System.out.println("connecting to server on port" + port);
			Socket client = new Socket(serverName, port);

			System.out.println("conneted to" + client.getRemoteSocketAddress() + " me = " + me);

			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			System.out.println("RED");
			out.writeUTF(me);
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			while (true) {
				// for(int i=0;i<3;i++) {

				String mess = in.readUTF();
				String green = "green";
				String exit = "exit";
				ms = mess.split(" ");
				time = Integer.parseInt(ms[1]);
				if (green.equals(ms[0])) {
					System.out.println(mess);

					try {
						Thread.sleep(1000 * time);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					System.out.println("RED");

					out.writeUTF("done");
				} else if (exit.equals(ms[0])) {
					System.out.println("EXITING! PROGRAM EXECUTED SUCCESSFULLY!! ;) ");
					out.writeUTF("exit-ack");
					break;
				}
			}

			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}