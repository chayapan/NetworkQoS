package qos;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NodeC {

	public NodeC() throws SocketException, UnknownHostException  {
		String addr = "127.0.0.1";
		InetAddress dest = InetAddress.getByName(addr);
		
		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>(1024);
		Consumer consume = new Consumer(Consumer.PORT, queue);
		consume.start(); // start consumer thread
		
		// poll Consumer thread every 1 seconds for statistics
		while (true) {
			// Report statistics every 1,000 milisec
			if ((System.currentTimeMillis() - consume.lastStat) > 1000) {
				System.out.println(new Date().toString() + " | " + consume.getStatistics());
				consume.lastStat = System.currentTimeMillis();
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("==== Node C ====");
		try {
			NodeC C = new NodeC();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
