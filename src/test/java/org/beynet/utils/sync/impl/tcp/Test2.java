package org.beynet.utils.sync.impl.tcp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Test2 {
	public static void main(String[] args){
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.INFO);
	        Logger.getLogger("org.beynet.utils.sync").setLevel(Level.DEBUG);
			TcpSyncPoolDescriptor des = new TcpSyncPoolDescriptor();
			des.addHost(new LocalTcpSyncHost(Integer.valueOf(2),Integer.valueOf(8889)));
			des.addHost(new RemoteTcpSyncHost(Integer.valueOf(1),"localhost", Integer.valueOf(8888)));
			des.addHost(new RemoteTcpSyncHost(Integer.valueOf(3),"localhost", Integer.valueOf(8890)));
			TcpSyncManager manager = new TcpSyncManager(2000);
			manager.initialize(des);
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
}
