package org.beynet.utils.framework.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.exception.UtilsRuntimeException;
import org.beynet.utils.framework.Ressource;
import org.beynet.utils.framework.Session;
import org.beynet.utils.sqltools.DataBaseAccessor;

public class SessionImpl implements Session {
	
	public SessionImpl(boolean autoCommit) {
		registeredConnection = new HashMap<DataBaseAccessor, Connection>();
		registeredRessources = new HashMap<DataBaseAccessor, List<Ressource>>();
		this.autoCommit = autoCommit ;
	}
	
	public void registerConnection(DataBaseAccessor a,Connection connection) {
		try {
			connection.setAutoCommit(autoCommit);
		} catch (SQLException e) {
		}
		registeredConnection.put(a, connection);
	}
	
	public void releaseConnection(DataBaseAccessor a) {
		Connection connection = getRegisteredConnection(a);
		if (connection!=null)
			try {
				connection.close();
			} catch (SQLException e) {
			}
		registeredConnection.remove(a);
	}
	
	public void registerRessource(DataBaseAccessor a,Ressource ressource) {
		List<Ressource> lst = registeredRessources.get(a);
		if (lst==null) {
			lst=new ArrayList<Ressource>();
			registeredRessources.put(a, lst);	
		}
		lst.add(ressource);
	}
	
	public Connection getRegisteredConnection(DataBaseAccessor a) {
		return(registeredConnection.get(a));
	}
	
	@Override
	public void close() {
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.close();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
		for (DataBaseAccessor accessor : registeredRessources.keySet()) {
			List<Ressource> ressources = registeredRessources.get(accessor);
			for (int i=1;i<=ressources.size();i++) {
				Ressource ressource = ressources.get(ressources.size()-i);
				ressource.close();
			}
		}
		registeredConnection.clear();
		registeredRessources.clear();
	}
	
	@Override
	public void commit() {
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.commit();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
		for (DataBaseAccessor accessor : registeredRessources.keySet()) {
			List<Ressource> ressources = registeredRessources.get(accessor);
			for (int i=1;i<=ressources.size();i++) {
				Ressource ressource = ressources.get(ressources.size()-i);
				ressource.commit();
			}
		}
		
	}
	@Override
	public void rollback() {
		for (DataBaseAccessor accessor : registeredRessources.keySet()) {
			List<Ressource> ressources = registeredRessources.get(accessor);
			for (int i=1;i<=ressources.size();i++) {
				Ressource ressource = ressources.get(ressources.size()-i);
				ressource.rollback();
			}
		}
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
	}
	private boolean autoCommit ;
	private Map<DataBaseAccessor,Connection>   registeredConnection ;
	private Map<DataBaseAccessor,List<Ressource>> registeredRessources ;
}
