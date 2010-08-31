package org.beynet.utils.sync.impl.tcp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Test3 {
	public static void main(String[] args){
		try {
			BasicConfigurator.configure();
	        Logger.getRootLogger().setLevel(Level.INFO);
			TcpSyncPoolDescriptor des = new TcpSyncPoolDescriptor();
			des.addHost(new LocalTcpSyncHost(3,8890));
			des.addHost(new RemoteTcpSyncHost(1,"localhost", 8888));
			des.addHost(new RemoteTcpSyncHost(2,"localhost", 8889));
			TcpSyncManager manager = new TcpSyncManager(2000);
			manager.initialize(des);
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
}
