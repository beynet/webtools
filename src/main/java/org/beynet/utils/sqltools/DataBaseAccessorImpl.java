package org.beynet.utils.sqltools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.beynet.utils.framework.Session;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.sqltools.admin.SqlToolsConnectionFactory;

/**
 * this class should be use to masquerade access to Sql Connection
 * @author beynet
 *
 */
public class DataBaseAccessorImpl implements DataBaseAccessor  {
	/**
	 * to construct DataBaseAccessor with a DataSource
	 * @param s
	 */
	public DataBaseAccessorImpl() {
		dataSource = null ;
		dataBaseDebugUrl = debugDataBaseClass = null ;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#setDataSource(javax.sql.DataSource)
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#getDataSource()
	 */
	public DataSource getDataSource() {
		return(dataSource);
	}


	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#setDataBaseDebugUrl(java.lang.String)
	 */
	public void setDataBaseDebugUrl(String dataBaseDebugUrl) {
		this.dataBaseDebugUrl = dataBaseDebugUrl;
	}



	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#setDebugDataBaseClass(java.lang.String)
	 */
	public void setDebugDataBaseClass(String debugDataBaseClass) {
		this.debugDataBaseClass = debugDataBaseClass;
	}



	
	
	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#getDataBaseDebugUrl()
	 */
	public String getDataBaseDebugUrl() {
		return(dataBaseDebugUrl);
	}
	
	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#getDebugDataBaseClass()
	 */
	public String getDebugDataBaseClass() {
		return(debugDataBaseClass);
	}
	
	private Connection initConnection() throws SQLException {
		Connection result;
		System.err.println("create new connection !!!!!!!!!!!!!!");
		if (dataSource!=null) {
			/* creation de l'objet connection */
			/* ------------------------------ */
			result = dataSource.getConnection();
			if (logger.isDebugEnabled()) logger.debug("Ok connection from datasource done");
		}
		else {
			if (dataBaseDebugUrl!=null) {
				try {
					Class.forName(debugDataBaseClass).newInstance();
					result  = DriverManager.getConnection(dataBaseDebugUrl);
					logger.info("Debug jdbc connection done");
				} catch (InstantiationException e) {
					e.printStackTrace();
					throw new SQLException(e);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new SQLException(e);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new SQLException(e);
				}
			}
			else {
				throw new SQLException("No DataSource found");
			}
		}
		result.setAutoCommit(false);
		return(SqlToolsConnectionFactory.getInstance().makeNewSqlToolsConnection(result));
	}

	/* (non-Javadoc)
	 * @see org.beynet.utils.sqltools.DataBaseAccessor#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		Session session = SessionFactory.instance().getCurrentSession();
		if (session==null) return(initConnection());
		Connection connection = session.getRegisteredConnection(this);
		if (connection!=null) {
			return(connection);
		}
		connection=initConnection();
		session.registerConnection(this, connection);
		return(connection);
	}
	
	private DataSource          dataSource         ;
	private String              dataBaseDebugUrl   ; // url to connect to databse (for junit tests)
	private String              debugDataBaseClass ;
	private static Logger logger = Logger.getLogger(DataBaseAccessorImpl.class);
}
