package org.beynet.utils.sqltools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.sqltools.interfaces.SqlSession;

public class SqlSessionImpl implements SqlSession {
	
	public SqlSessionImpl(DataBaseAccessor dataBaseAccessor) {
		this.dataBaseAccessor = dataBaseAccessor ;
		connectionList = new HashMap<Thread, Connection>();
		connectionListSt = new HashMap<Thread,Object>();
	}
	
	@Override
	public void connectToDataBase(boolean transacted) throws UtilsException {
		synchronized (connectionList) {
			if (connectionList.get(Thread.currentThread())!=null) {
				throw new UtilsException(UtilsExceptions.Error_Sql,"Already connected");
			}
		}
		try {
			Connection connection = dataBaseAccessor.getConnection();
			if (transacted==true) connection.setAutoCommit(false);
			else connection.setAutoCommit(true);
			synchronized (connectionList) {
				if (connectionList.size()>1) {
					System.err.println("taille="+connectionList.size());
				}
				connectionListSt.put(Thread.currentThread(), Thread.currentThread().getStackTrace());
				connectionList.put(Thread.currentThread(), connection);
			}
			
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@Override
	public void commit() throws UtilsException {
		Connection connection = getCurrentConnection();
		try {
			connection.commit();
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@Override
	public void closeConnection() throws UtilsException {
		synchronized (connectionList) {
			Connection connection = connectionList.get(Thread.currentThread()) ;
			if (connection==null) throw new UtilsException(UtilsExceptions.Error_Param,"No current transaction");
			try {
				connection.close();
			}
			catch(SQLException e) {
				throw new UtilsException(UtilsExceptions.Error_Sql,e);
			}
			finally {
				connectionList.remove(Thread.currentThread());
				connectionListSt.remove(Thread.currentThread());
			}
		}
	}

	

	@Override
	public Connection getCurrentConnection(){
		synchronized (connectionList) {
			return(connectionList.get(Thread.currentThread()));
		}
	}

	@Override
	public void rollback() throws UtilsException {
		Connection connection = getCurrentConnection();
		try {
			connection.rollback();
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		for (Thread t : connectionList.keySet()) {
			connectionList.get(t).close();
			System.err.println("!!! finalize a session with Connection!=null !!!");
			StackTraceElement[] stck = (StackTraceElement[])connectionListSt.get(t);
			StringBuffer message = new StringBuffer();
			message.append("connection created from : class :\n");
			for (StackTraceElement sta : stck) {
				message.append("\t");
				message.append(sta.getClassName());
				message.append(" method ");
				message.append(sta.getMethodName());
				message.append(" line ");
				message.append(sta.getLineNumber());
				message.append("\n");
			}
			System.err.println(message.toString());
			logger.error(message);
		}
		super.finalize();
	}
	
	private DataBaseAccessor       dataBaseAccessor ;
	private Map<Thread,Connection> connectionList   ;
	private Map<Thread,Object> connectionListSt   ;
	private final static Logger logger = Logger.getLogger(SqlSessionImpl.class);
}
