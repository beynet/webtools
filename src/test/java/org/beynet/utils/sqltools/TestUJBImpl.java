package org.beynet.utils.sqltools;

import org.beynet.utils.framework.UJB;

@UJB(name="test")
public class TestUJBImpl implements TestUJB {
	public TestUJBImpl() {
		
	}
	
	@Transaction
	@Override
	public void print() {
		System.out.println("test!");
	}
	
}
