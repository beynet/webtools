package org.beynet.utils.tools;

public class Semaphore {
	public Semaphore(int initial) {
		this.value = initial ;
	}
	/**
	 * Acquire one resource
	 * @throws InterruptedException
	 */
	public synchronized void P() throws InterruptedException {
		while(true) {
			if (value > 0) {
				value --;
				return;
			}
			wait();
		}
	}
	/**
	 * release one resource
	 */
	public synchronized void V() {
		value++;
		notify();
	}
	
	private int value ;
}
