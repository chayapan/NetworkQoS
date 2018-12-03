package qos;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class Consumer extends Thread {
	public static final int PORT = 4568;
	
	private DatagramSocket socket;
	private boolean running;
	private int port;
	private byte[] buf = new byte[256];
	
	public Date startTime;
	public long lastStat;
	private long receivedTotal;
	private long receivedX;
	private long receivedY;
	
	BlockingQueue<Message> queue;

	public Consumer(int port, BlockingQueue<Message> queue) throws SocketException, UnknownHostException {
			System.out.println("Consumer initizing... " + port);
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
		System.out.println("Consumer start..." + "(port="+port+")");
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
				
				// Debug to see content.
				// System.out.println(new Date().toString() + " | Consumer/ Received:" + received);
				
				// Decode
				Message m = Message.fromString(received);
				if (m.getType() == Message.X) {
					receivedX += 1;
				}
				if (m.getType() == Message.Y) {
					receivedY += 1;
				}
				
				if (received.equals("end")) {
					running = false;
					continue;
				}
				
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		socket.close();
		
		System.out.println("Consumer/ END.");
		System.out.println(getStatistics());
	}
	
	public String getStatistics() {
		int queueLength = this.queue.size();
		long elapse = (System.currentTimeMillis() - startTime.getTime()) / 1000;
		String stat = String.format("Consumer/ %s Total=%s X=%s Y=%s elapse=%s sec.", new Date().toString(), this.receivedTotal, this.receivedX, this.receivedY, elapse);
		return stat;
	}
}
