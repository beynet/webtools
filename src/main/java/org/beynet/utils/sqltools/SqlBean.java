package org.beynet.utils.sqltools;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.SqlBeanInterface;
import org.beynet.utils.sqltools.interfaces.SqlSession;
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
	public void delete(SqlSession session) throws SQLException {
		checkAnnotation();
		if (session==null || session.getCurrentConnection()==null) throw new SQLException(NO_CONNECTION);
		_requestFactory.delete(this,session.getCurrentConnection());
	}
	
	@Override
	public void createTable(SqlSession session) throws SQLException {
		checkAnnotation();
		if (session==null || session.getCurrentConnection()==null) throw new SQLException(NO_CONNECTION);
		_requestFactory.createTable(session.getCurrentConnection());
	}
	
	@Override
	public Integer count(SqlSession session,String request) throws SQLException {
		if (session==null || session.getCurrentConnection()==null) {
			throw new SQLException(NO_CONNECTION);
		}
		return(_requestFactory.count(request.toString(), session.getCurrentConnection()));
	}

	@Override
	public void load(SqlSession session)
			throws SQLException {
		checkAnnotation();
		if (session==null || session.getCurrentConnection()==null) throw new SQLException(NO_CONNECTION);
		_requestFactory.load(this, session.getCurrentConnection());
	}
	
	@Override
	public void load(SqlSession session,String request) throws SQLException {
		checkAnnotation();
		if (session==null || session.getCurrentConnection()==null) throw new SQLException("null connection");
		_requestFactory.load(this,session.getCurrentConnection(),request);
	}

	@Override
	public void save(SqlSession session) throws SQLException {
		checkAnnotation();
		if (session==null || session.getCurrentConnection()==null) throw new SQLException("null connection");
		_requestFactory.save(this,session.getCurrentConnection());
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
	protected static final String  NO_CONNECTION = "null connection" ;
}
