package org.beynet.utils.sync.impl.tcp;

import java.util.ArrayList;
import java.util.List;

import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncPoolDescriptor;

public class TcpSyncPoolDescriptor implements SyncPoolDescriptor {
	
	public TcpSyncPoolDescriptor() {
		hosts = new ArrayList<SyncHost>();
	}

	@Override
	public List<SyncHost> getHostList() {
		return hosts;
	}
	
	@Override
	public void addHost(SyncHost newHost) {
		hosts.add(newHost);
	}

	private List<SyncHost> hosts ;

	@Override
	/**
	 * in this loop we ping each remote host and we listen to the currenthost
	 */
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
