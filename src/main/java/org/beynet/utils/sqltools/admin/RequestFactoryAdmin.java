package org.beynet.utils.sqltools.admin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.beynet.utils.sqltools.interfaces.RequestFactory;

public class RequestFactoryAdmin<T>  implements RequestFactoryAdminMBean, RequestFactory<T> {
	
	public RequestFactoryAdmin(RequestFactory<T> factory) {
		this.factory = factory ;
		this.isSuspended = false ;
	}

	@Override
	public synchronized void suspendComponent() {
		isSuspended = true;
	}
	@Override
	public synchronized void unSuspendComponent() {
		isSuspended = false ;
		notifyAll();
	}

	private synchronized void checkSuspend() {
		if (isSuspended) {
			try {
				wait();
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	@Override
	public int count(T sqlBean, Connection connection) throws SQLException {
		return(factory.count(sqlBean, connection));
	}
	
	@Override
	public void createTable(Connection connection) throws SQLException {
		factory.createTable(connection);
	}
	
	@Override
	public void delete(T sqlBean, Connection connection) throws SQLException {
		checkSuspend();
		factory.delete(sqlBean, connection);
	}

	@Override
	public void delete(T sqlBean, Connection connection, String query)
			throws SQLException {
		checkSuspend();
		factory.delete(sqlBean, connection,query);
	}

	@Override
	public void load(T sqlBean, Connection connection) throws SQLException {
		checkSuspend();
		factory.load(sqlBean, connection);
	}

	@Override
	public void load(T sqlBean, Connection connection, String request)
			throws SQLException {
		checkSuspend();
		factory.load(sqlBean, connection, request);
	}

	@Override
	public void loadList(List<T> listResult, Connection connection,
			String request) throws SQLException {
		checkSuspend();
		factory.loadList(listResult, connection, request);
	}

	@Override
	public void save(T sqlBean, Connection connection) throws SQLException {
		checkSuspend();
		factory.save(sqlBean, connection);
	}
	
	private RequestFactory<T> factory     ;
	private boolean           isSuspended ;
	
	
	
}
