package code;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.GregorianCalendar;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.PrefetchCompleteEvent;

// This is the song receiver
// THIS GOES SECOND

public class InitiaterServer implements ControllerListener {
	PrintWriter out;
	BufferedReader in;
	String srcIP;
	int port;
	RTPClient rtpc;
	
	public InitiaterServer(String srcIP, int port) {
		this.srcIP = srcIP;
		this.port = port;
	}
	
	public void initiate() {
		ServerSocket server;
		Socket client;
		long offsetTotal = 0;
		try {
			server = new ServerSocket(port);
			client = server.accept();
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			long timeDiff;			
			
			for (int i = 0; i <= 3; i++) {
				timeDiff = new GregorianCalendar().getTimeInMillis() - Long.parseLong(in.readLine());
				out.println(timeDiff);
				offsetTotal += timeDiff;
				System.out.println(timeDiff);
			}
			
			offsetTotal /= 4;

			System.out.println(offsetTotal);
			System.out.println("Handshake received from server. Socket established!");
			
			rtpc = new RTPClient(srcIP, this);
			Thread t = new Thread(rtpc);
			t.start();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	public synchronized void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof EndOfMediaEvent) {
			System.exit(0);
		} else if (evt instanceof PrefetchCompleteEvent) {
			out.println("GOGOGO");
			out.println("this is pure delay purpose");
			rtpc.p.start();
			//r.pl.start();
		} else {
			System.out.println(evt.toString());
		}
	}
	
	/*public static void startComm(RTPServer r) {
		ServerSocket server;
		Socket client;
		long offsetTotal = 0;
		try {
			server = new ServerSocket(42050);
			client = server.accept();
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			long timeDiff;			
			
			for (int i = 0; i <= 3; i++) {
				timeDiff = new GregorianCalendar().getTimeInMillis() - Long.parseLong(in.readLine());
				out.println(timeDiff);
				offsetTotal += timeDiff;
				System.out.println(timeDiff);
			}
			
			offsetTotal /= 4;

			System.out.println(offsetTotal);
			System.out.println("Handshake received from server. Socket established!");
			RTPClient.entry("172.16.150.122", offsetTotal, r);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}*/
}
