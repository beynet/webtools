package org.beynet.utils.sync.impl.tcp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Test1 {
	public static void main(String[] args){
		try {
			BasicConfigurator.configure();
	        Logger.getRootLogger().setLevel(Level.INFO);
	        Logger.getLogger("org.beynet.utils.sync").setLevel(Level.DEBUG);
			TcpSyncPoolDescriptor des = new TcpSyncPoolDescriptor();
			des.addHost(new LocalTcpSyncHost(1,8888));
			des.addHost(new RemoteTcpSyncHost(2,"localhost", 8889));
			des.addHost(new RemoteTcpSyncHost(3,"localhost", 8890));
			TcpSyncManager manager = new TcpSyncManager(2000);
			manager.initialize(des);
			Thread.sleep(1000*50);
			manager.stop();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
}
