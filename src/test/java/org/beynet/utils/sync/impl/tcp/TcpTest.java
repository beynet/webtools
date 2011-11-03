package org.beynet.utils.sync.impl.tcp;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.impl.CommandMounter;
import org.beynet.utils.sync.impl.GetStateCommand;
import org.beynet.utils.sync.impl.SyncRessourceCommand;
import org.junit.Ignore;

@Ignore
public class TcpTest extends TestCase {
	
	public TcpTest( String testName )
    {
        super( testName );
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }
	
	public void testCommandMounter() {
		SyncCommand mountedCommand= null ;
		CommandMounter mounter = new CommandMounter(null);
		
		GetStateCommand getStateCommand = new GetStateCommand();
		try {
			mountedCommand=mounter.getCommand(getStateCommand.generate().toString().getBytes());
			assertTrue(mountedCommand!=null);
			assertTrue (mountedCommand instanceof GetStateCommand);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		mounter.reset();
		SyncRessourceCommand<String> syncString = new SyncRessourceCommand<String>(null,"abcdefgh");
		try {
			mountedCommand=mounter.getCommand(syncString.generate().toString().getBytes());
			assertTrue(mountedCommand!=null);
			assertTrue (mountedCommand instanceof SyncRessourceCommand<?>);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	public void testThreads() {
		try {
			
			Integer newWeight = Integer.valueOf(1238) ;
			LocalTcpSyncHost local = new LocalTcpSyncHost(Integer.valueOf(1),Integer.valueOf(8888));
			Thread l = new Thread(local);
			l.start();
			local.setWeight(newWeight);
			RemoteTcpSyncHost remote = new RemoteTcpSyncHost(Integer.valueOf(1),"localhost", Integer.valueOf(8888));
			remote.getState();
			System.out.println("weight="+remote.getWeight());
			assertTrue(newWeight.equals(remote.getWeight()));
			
			newWeight = new Integer(18);
			local.setWeight(newWeight);
			remote.getState();
			System.out.println("weight="+remote.getWeight());
			assertTrue(newWeight.equals(remote.getWeight()));
			
			l.interrupt();
			l.join();
			local.stopChilds();
		}catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	public void testTcpSyncManager() {
		
		try {
			TcpSyncPoolDescriptor des = new TcpSyncPoolDescriptor();
			des.addHost(new LocalTcpSyncHost(Integer.valueOf(1),Integer.valueOf(8888)));
			des.addHost(new RemoteTcpSyncHost(Integer.valueOf(1),"localhost", Integer.valueOf(8888)));
			assertTrue(des.getHostList().size()==2);
			TcpSyncManager manager = new TcpSyncManager(2000);
			manager.initialize(des);
			Thread.sleep(1000*5);
			manager.stop();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
