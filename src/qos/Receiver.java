package qos;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class Receiver extends Thread {
	public static int STAT_INTERVAL = 1; // Print statistics every n seconds.
	
	public static final int PORT = 4567;
	
	private DatagramSocket socket;
	private boolean running;
	private int port;
	private byte[] buf = new byte[256];
	
	private Date startTime;
	private long lastStat;
	private long receivedTotal;
	private long receivedX;
	private long receivedY;
	
	BlockingQueue<Message> queue;

	public Receiver(int port, BlockingQueue<Message> queue) throws SocketException, UnknownHostException {
			System.out.println("Receiver " + port);
			this.port = port;
			this.queue = queue;
			
			startTime = new Date();
			receivedTotal = 0;
			receivedX = 0;
			receivedY = 0;
			
			// Instead of specific IP address, we listen to 0.0.0.0
			//InetAddress laddr = InetAddress.getLocalHost();
			InetAddress laddr = InetAddress.getByName("0.0.0.0");
			
	        socket = new DatagramSocket(port, laddr);
	        String info = String.format("Listen: %s:%s", socket.getLocalAddress(), socket.getLocalPort());
	        System.out.println(info);
	    }

	public void run() {
		System.out.println("Receiver start..." + "(port="+port+")");
		this.lastStat = System.currentTimeMillis();
		String d;
		running = true;

		while (running) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket(buf, buf.length, address, port);
				String received = new String(packet.getData(), 0, packet.getLength());
				
				// Packet received and decoded.
				receivedTotal += 1;
				
				// Decode
				Message m = Message.fromString(received);
				
				// Put message to queue for forward thread
				try {
					this.queue.put(m);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Keep statistics
				if (m.getType() == Message.X) {
					receivedX += 1;
				}
				if (m.getType() == Message.Y) {
					receivedY += 1;
				}
				
				// Report statistics every STAT_INTERVAL x 1000 milisec
				if ((System.currentTimeMillis() - this.lastStat) > 1000 * STAT_INTERVAL) {
					System.out.println(new Date().toString() + " | " + getStatistics());
					this.lastStat = System.currentTimeMillis();
				}
				
				// DEBUG
				//d = String.format("Receiver/ Receive at %s: Data: %s From: %s:%s", System.currentTimeMillis(), received, address, port);
				// System.out.println(d);
				
				if (received.equals("end")) {
					running = false;
					continue;
				}
				
				// Send ACK
				int bytesReceived = buf.length;
				byte[] ackMsg = String.format("OK %s", bytesReceived).getBytes();
				//packet = new DatagramPacket(buf, buf.length, address, port);
				packet = new DatagramPacket(ackMsg, ackMsg.length, address, port);
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		socket.close();
		
		System.out.println("Receiver/ END.");
		System.out.println(getStatistics());
	}
	
	public String getStatistics() {
		int queueLength = this.queue.size();
		String stat = String.format("Receiver/ %s Recieve Total=%s X=%s Y=%s queue=%s", new Date().toString(), this.receivedTotal, this.receivedX, this.receivedY, queueLength);
		return stat;
	}
}
