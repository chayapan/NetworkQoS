package qos.helper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class PeriodicStatusPrinter extends TimerTask {
	final Semaphore s = new Semaphore(1);
	final Timer timer = new Timer(true);
	
	public PeriodicStatusPrinter() {
		timer.schedule(this, 0, 10 * 1000); //schedule this every 10 seconds
	}
	
	public void run() { //called by timer
		s.release(1 - s.availablePermits());
	}
	
	public void makeCall() throws InterruptedException {
		s.acquire();
		System.out.println(new Date().toString() + " | Status ...");
	}
	
}
