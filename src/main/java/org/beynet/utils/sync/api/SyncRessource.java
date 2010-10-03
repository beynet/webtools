package org.beynet.utils.sync.api;

public class SyncRessource<T> {
	
	public T getRessource() {
		return ressource;
	}
	public void setRessource(T ressource) {
		this.ressource = ressource;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public long getSequence() {
		return sequence;
	}
	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
	private T    ressource   ;
	private long date     ;
	private long sequence ;
}
