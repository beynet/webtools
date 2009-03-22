package org.beynet.utils.sqltools;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.SqlBeanInterface;

public class SqlBean implements SqlBeanInterface {

	@Override
	public void delete(Connection transaction) throws SQLException {
		checkAnnotation();
		_requestFactory.delete(this,transaction);
	}
	
	public void createTable(Connection transaction) throws SQLException {
		checkAnnotation();
		_requestFactory.createTable(transaction);
	}

	@Override
	public void load(Connection transaction)
			throws SQLException {
		checkAnnotation();
		_requestFactory.load(this, transaction);
	}
	
	@Override
	public void load(Connection transaction,String request) throws SQLException {
		checkAnnotation();
		_requestFactory.load(this,transaction,request);
	}

	@Override
	public void save(Connection transaction) throws SQLException {
		checkAnnotation();
		_requestFactory.save(this,transaction);
	}

	@SuppressWarnings("unchecked")
	private void checkAnnotation() throws SQLException {
		if (_requestFactory!=null) {
			return;
		}
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field f : fields) {
			Object cur = null ;
			
			try {
				boolean accessible = f.isAccessible();
				if (!accessible) f.setAccessible(true);
				cur = f.get(this);
				if (!accessible) f.setAccessible(false);
				if (cur instanceof RequestFactory) {
					_requestFactory = (RequestFactory<SqlBeanInterface>)cur ;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if (_requestFactory==null) {
			throw new SQLException("No Request Factory found !!");
		}
	}
	
	private RequestFactory<SqlBeanInterface> _requestFactory=null ;
}
