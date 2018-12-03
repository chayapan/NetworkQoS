package qos;
/*
 * Sends packet to receiver.
 * 
 * */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Producer {
	private int port;
    private DatagramSocket socket;
    private InetAddress address;

	private Date startTime;
	private long lastStat;
	private long sentTotal;
	private long sentX;
	private long sentY;
    
    private byte[] buf;
 
    public Producer(InetAddress dest, int port, int seconds) throws InterruptedException {
    		Date start = new Date();
    		startTime = start;
    		sentTotal = 0;
    		sentX = 0;
    		sentY = 0;
    	
        System.out.println("Producer start...");
        // Set destination
    		this.port = port;
        address = dest;
    	
    		// Create socket
    		try {
    			socket = new DatagramSocket();
    		} catch (Exception e) {
    			String msg = "Error starting server: " + e.getMessage();
    			// this.activityLog.insert(msg, 0);
    			System.out.println(msg);
    		}
        
        // Send packets for a number of seconds...
        for (int i = 1; i <= seconds; i++) {
        	
        		// In each second, sends 50 messages. The messages compose of type X and type Y.
        		// Ratio: X 20 messages, Y 30 messages.
			System.out.println("Second " + i);
			System.out.println(this.getStatistics());
        		long e1 = System.currentTimeMillis();
        		
        		
        		/* 
        		 * DEBUG
        		 * Send X and Y in pre-determine order.  Send 20 X, then send 30 Y.
        		for (int j = 1; j <= 20; j++) {
        			sendX("foo" + i + "_" + j + "_WWWWWWWWWWWW");
        			sentX += 1;
        		}

        		for (int j = 1; j <= 30; j++) {
        			sendY("bar" + i + "_" + j + "_WWWWWWWWWWWW");
        			sentY += 1;
        		}
        		*/
        		
        		/* Send X and Y in random order */
        		int x_count = 20;
        		int y_count = 30;
        		List<Character> sendSequence = new ArrayList<>();
        		for (int sx = 0; sx < x_count; sx++ ) {
        			sendSequence.add('X');
        		}
        		for (int sy = 0; sy < y_count; sy++ ) {
        			sendSequence.add('Y');
        		}
        		// System.out.println(sendSequence);
        		Collections.shuffle(sendSequence); // Randomize sending order.
        		// System.out.println(sendSequence);

        		for (int j = 0; j < x_count + y_count; j++) {
        			if (sendSequence.get(j) == 'X') {
            			sendX("foo" + i + "_" + j + "_WWWWWWWWWWWW"); // Send type X
            			sentX += 1;
        			}
            		if (sendSequence.get(j) == 'Y') {
	        			sendY("bar" + i + "_" + j + "_WWWWWWWWWWWW"); // Send type Y
	        			sentY += 1;
            		}
        		}
        		
    			long e2 = System.currentTimeMillis() - e1; // milliseconds from second starts.
    			long slack = 1000 - e2;
    			TimeUnit.MILLISECONDS.sleep(slack);
        		 
        }
        close();
        
        Date end = new Date();
        System.out.println("Producer finished. Took " + ((end.getTime() - start.getTime()) / 1000 ) + " sec.");
        System.out.println(this.getStatistics());
    }
 
    public String sendEcho(String msg) {
    		// msg = msg + "__________"; // Add padding or append characters to message
        buf = msg.getBytes();
        DatagramPacket packet;
        String d;
		try {
			packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);
			sentTotal += 1; // Send and update stat.
			
			// Debug outbound data packet:
			// d = String.format("Producer/ Datagram send %s bytes", packet.getLength());
			// System.out.println(d);
	        		
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
	        String received = new String(
	                packet.getData(), 0, packet.getLength());
	        
	        // DEBUG
	        // d = String.format("Producer/ Datagram received %s bytes", packet.getLength());
	        // System.out.println(d);
	        return received;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "NO-DATA";
    }
    
    public String sendX(String value) {
    		String result = sendEcho(Message.X(value).toString());
			return result;
    }

    public String sendY(String value) {
		String result = sendEcho(Message.Y(value).toString());
		return result;
    }
    
    public void close() {
        socket.close();
    }
    
	public String getStatistics() {
		String stat = String.format("Producer/ %s Sent Total=%s X=%s Y=%s", new Date().toString(), this.sentTotal, this.sentX, this.sentY);
		return stat;
	}
}

