package org.beynet.utils.sqltools;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.SqlBeanInterface;
/**
 * To extend this class, user must declare a static field of type RequestFactory
 * example : 
 * class T extends SqlBean {
 * ....
 * private static RequestFactory<T> rq = new RequestFactoryImpl<T>(T.class	);
 * 
 * using a static is a better approach for performance reasons (underlying class introspection
 * will just be done one time)
 * }
 * @author beynet
 *
 */
public class SqlBean implements SqlBeanInterface {

	@Override
	public void delete(Connection transaction) throws SQLException {
		checkAnnotation();
		if (transaction==null) throw new SQLException("null connection");
		_requestFactory.delete(this,transaction);
	}
	
	public void createTable(Connection transaction) throws SQLException {
		checkAnnotation();
		if (transaction==null) throw new SQLException("null connection");
		_requestFactory.createTable(transaction);
	}

	@Override
	public void load(Connection transaction)
			throws SQLException {
		checkAnnotation();
		if (transaction==null) throw new SQLException("null connection");
		_requestFactory.load(this, transaction);
	}
	
	@Override
	public void load(Connection transaction,String request) throws SQLException {
		checkAnnotation();
		if (transaction==null) throw new SQLException("null connection");
		_requestFactory.load(this,transaction,request);
	}

	@Override
	public void save(Connection transaction) throws SQLException {
		checkAnnotation();
		if (transaction==null) throw new SQLException("null connection");
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
