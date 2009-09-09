package org.beynet.utils.sqltools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.beynet.utils.sqltools.admin.SqlToolsConnectionFactory;

/**
 * this class should be use to masquerade access to Sql Connection
 * @author beynet
 *
 */
public class DataBaseAccessor {
	/**
	 * to construct DataBaseAccessor with a DataSource
	 * @param s
	 */
	public DataBaseAccessor(DataSource s) {
		dataSource = s ;
		dataBaseDebugUrl = debugDataBaseClass = null ;
	}
	
	/**
	 * to construct DataBaseAccessor without a DataSource ie for example for junit tests
	 * @param debugDataBaseClass
	 * @param dataBaseDebugUrl
	 */
	public DataBaseAccessor(String debugDataBaseClass,String dataBaseDebugUrl) {
		dataSource = null ;
		this.debugDataBaseClass=debugDataBaseClass;
		this.dataBaseDebugUrl = dataBaseDebugUrl;
	}
	
	public DataSource getDataSource() {
		return(dataSource);
	}
	
	public String getDataBaseDebugUrl() {
		return(dataBaseDebugUrl);
	}
	
	public String getDebugDataBaseClass() {
		return(debugDataBaseClass);
	}
	
	public synchronized Connection getConnection() throws SQLException {
		Connection result;
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
		return(SqlToolsConnectionFactory.getInstance().makeNewSqlToolsConnection(result));
	}
	
	private DataSource          dataSource         ;
	private String              dataBaseDebugUrl   ; // url to connect to databse (for junit tests)
	private String              debugDataBaseClass ;
	private static Logger logger = Logger.getLogger(DataBaseAccessor.class);
}
