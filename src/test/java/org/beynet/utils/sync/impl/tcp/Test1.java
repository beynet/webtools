package org.beynet.utils.sync.impl.tcp;

import java.io.File;
import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.tools.FileUtils;

public class Test1 {
	public static void main(String[] args){
		try {
			BasicConfigurator.configure();
	        Logger.getRootLogger().setLevel(Level.INFO);
	        Logger.getLogger("org.beynet.utils.sync").setLevel(Level.DEBUG);
			TcpSyncPoolDescriptor des = new TcpSyncPoolDescriptor();
			des.addHost(new LocalTcpSyncHost(Integer.valueOf(1),Integer.valueOf(8888)));
			des.addHost(new RemoteTcpSyncHost(Integer.valueOf(2),"localhost", Integer.valueOf(8889)));
			des.addHost(new RemoteTcpSyncHost(Integer.valueOf(3),"localhost", Integer.valueOf(8890)));
			TcpSyncManager manager = new TcpSyncManager(2000);
			manager.initialize(des);
			try {
				String p =new String( FileUtils.loadFile(new File("/etc/passwd")));
				manager.syncRessource(p);
			}catch(Exception e) {
				
			}
			while (true) {
				Date d = new Date();
				manager.syncRessource("essai de ressource :"+d.getTime());
				Thread.sleep(1000*5);
			}
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
}
