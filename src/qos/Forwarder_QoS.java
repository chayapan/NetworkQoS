package qos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Forwarder_QoS extends Thread {
	public static int STAT_INTERVAL = 1; // Print statistics every n seconds.
	
	public static final int CONSUMER_PORT = 4568;

	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private boolean running;

	BlockingQueue<Message> queueX;
	BlockingQueue<Message> queueY;

	private Date startTime;
	private long lastStat;
	private long sentTotal;
	private long sentX;
	private long sentY;

	private byte[] buf;

	public Forwarder_QoS(InetAddress dest, int port, BlockingQueue<Message> queueX, BlockingQueue<Message> queueY)
			throws InterruptedException, SocketException, UnknownHostException {
		startTime = new Date();
		sentTotal = 0;
		sentX = 0;
		sentY = 0;
		this.address = dest; // Destination is InetAddress object
		this.port = port;
		this.queueX = queueX;
		this.queueY = queueY;

		// Create socket
		try {
			socket = new DatagramSocket();
		} catch (Exception e) {
			String msg = "Error starting server: " + e.getMessage();
			System.out.println(msg);
		}

		String info = String.format("Forward To: %s:%s", address.getHostAddress(), port);
		System.out.println(info);
	}

	public void run() {
		this.lastStat = System.currentTimeMillis();
		String d;
		running = true;

		// Node B can forwards any types of messages to Node C at the rate of 35 packets
		// per second.
		// A non-discriminating queue must be implemented at node B.

		while (true) {

			// Report statistics every STAT_INTERVAL x 1000 milisec
			if ((System.currentTimeMillis() - this.lastStat) > 1000 * STAT_INTERVAL) {
				System.out.println(new Date().toString() + " | " + getStatistics());
				this.lastStat = System.currentTimeMillis();
			}

			// Every second, send 35 packets.
			// Message type Y to be forwarded first. 
			//  - So we loop until queueY is empty, then start working on X
			try {
				int sendLimit = 35;
				long e1 = System.currentTimeMillis();
				if (queueY.size() > 0) {
					// Forward Y first
					for (int i = 0; i < 35; i++) {
						forward(queueY); // forward
						sendLimit -= 1;
					}
				}
				if ((sendLimit > 0) && (queueX.size() > 0)) {
					// Forward X using rest of quota
					for (int i = 0; i < sendLimit; i++) {
						forward(queueX);
					}
				}
				// reset send limit
				sendLimit = 35;
				
				long e2 = System.currentTimeMillis() - e1; // milliseconds from second starts.
				long slack = 1000 - e2;
				TimeUnit.MILLISECONDS.sleep(slack); // Wait until the second is completed.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void close() {
		socket.close();
	}

	/*
	 * Forward message in queue Y to consumer
	 */
	public String forward(BlockingQueue<Message> queue) throws InterruptedException {
		if (queue.isEmpty()) {
			return "QUEUE-EMPTY";
		}
		
		// Get message from queue
		Message m = queue.take();

		// Update statistics
		sentTotal += 1;
		if (m.getType() == Message.X) {
			sentX += 1;
		}
		if (m.getType() == Message.Y) {
			sentY += 1;
		}

		String msg = m.toString(); // becomes <<___>>
		buf = msg.getBytes();
		DatagramPacket packet;
		String d;
		try {
			// Create packet to send out (outbound packet).
			packet = new DatagramPacket(buf, buf.length, address, port);
			
			// DEBUG
			// d = String.format("Forwarder/ Datagram to %s:%s", packet.getAddress(), packet.getPort());
			// System.out.println(d);

			socket.send(packet);
			
			// DEBUG
			// d = String.format("Forwarder/ Datagram send %s bytes", packet.getLength());
			// System.out.println(d);

			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			String received = new String(packet.getData(), 0, packet.getLength());
			
			// DEBUG
			// d = String.format("Forwarder/ ACK received %s bytes", packet.getLength());
			// System.out.println(d);
			return received;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "NO-DATA";
	}

	public String getStatistics() {
		int queueLenX = this.queueX.size();
		int queueLenY = this.queueY.size();
		String stat = String.format("Forwarder/ %s Forward Total=%s X=%s Y=%s queueX=%s queueY=%s", new Date().toString(),
				this.sentTotal, this.sentX, this.sentY, queueLenX, queueLenY);
		return stat;
	}
}
