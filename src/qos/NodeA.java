package qos;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class NodeA {
	public NodeA() {
		String addr = "127.0.0.1"; // address of node B
		InetAddress dest;
		try {
			dest = InetAddress.getByName(addr);
			// int s = 3; // Run for 3 seconds.
			int s = 10; // Run for 10 seconds.
			Producer A = new Producer(dest, Receiver.PORT, s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("==== Node A ====");
		NodeA A = new NodeA();
	}

}
