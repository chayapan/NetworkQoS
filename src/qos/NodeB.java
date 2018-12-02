package qos;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NodeB {
	public Date start;
	public Receiver rcv;
	public Forwarder fwd;
	public NodeB() throws SocketException, UnknownHostException, InterruptedException  {
		String addr = "127.0.0.1";
		InetAddress dest = InetAddress.getByName(addr);
		start = new Date();
		
		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>(1024);
		
		rcv = new Receiver(Receiver.PORT, queue);
		rcv.start(); // start receiver thread
		
		InetAddress caddr = InetAddress.getByName(addr);
		// Change caddr to consumer address.
		fwd = new Forwarder(caddr, Consumer.PORT, queue);
		fwd.start();
		
	}

	public static void main(String[] args) {
		System.out.println("==== Node B ====");
		try {
			NodeB B = new NodeB();
			
			while (true) {
				// Stop program automatically after 30 seconds.
				if ((System.currentTimeMillis() - B.start.getTime()) > 30000) {
					System.out.println(B.rcv.getStatistics()); // Dump statistics.
					System.out.println(B.fwd.getStatistics());
					System.out.println(new Date().toString() + " | NodeB shutsdown.");
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
