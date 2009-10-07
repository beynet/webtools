package org.beynet.utils.sqltools;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public interface DataBaseAccessor {

	public abstract void setDataSource(DataSource dataSource);

	public abstract DataSource getDataSource();

	public abstract void setDataBaseDebugUrl(String dataBaseDebugUrl);

	public abstract void setDebugDataBaseClass(String debugDataBaseClass);

	public abstract String getDataBaseDebugUrl();

	public abstract String getDebugDataBaseClass();

	/**
	 * return a connection to associated dataBase
	 * @return
	 * @throws SQLException
	 */
	public abstract Connection getConnection() throws SQLException;

}