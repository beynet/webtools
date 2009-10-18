package org.beynet.utils.sqltools;



public class TestUJBImpl implements TestUJB {
	public TestUJBImpl() {
		
	}
	
	@Transaction
	@Override
	public void print() {
		System.out.println("test!");
	}
	
}
