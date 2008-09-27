package org.beynet.utils.tools;

public class Semaphore {
	public Semaphore(int initial) {
		this.value = initial ;
	}
	public synchronized void P() throws InterruptedException {
		while(true) {
			if (value > 0) {
				value --;
				return;
			}
			wait();
		}
	}
	public synchronized void V() {
		value++;
		notify();
	}
	
	private int value ;
}
