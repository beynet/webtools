package org.beynet.utils.sqltools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.Session;

/**
 * the aim of that class is to generate sql request
 *  - if a uniq id is declared (annotation SqlFiel) it is use for requests
 *  - if not ; every fields of bean must be given
 * @author beynet
 *
 * @param <T>
 */
public class RequestFactoryImpl<T> implements RequestFactory<T> {
	
	public RequestFactoryImpl(Class<T> cl) {
		logger.debug("entering constructor for class type="+cl.toString());
		beanClass = cl;
		Field[] tmpFields = cl.getDeclaredFields();
		uniqIdField = null ;
		fields = new ArrayList<Field>();
		getMethods = new ArrayList<Method>();
		setMethods = new ArrayList<Method>();
		
		// storing current table name
		// --------------------------
		SqlTable s = beanClass.getAnnotation(SqlTable.class);
		tableName = s.value();
		
		
		// searching uniq id field
		// ------------------------
		int i;
		for (i=0;i<tmpFields.length;i++) {
			logger.debug("field number "+i+" name="+tmpFields[i].getName());
			if (tmpFields[i].isAnnotationPresent(SqlField.class)) {
				logger.debug("Adding sql field "+tmpFields[i].getName()+" class="+tmpFields[i].getType().toString()+" to list");
				fields.add(tmpFields[i]);
				SqlField f = tmpFields[i].getAnnotation(SqlField.class);
				
				// searching current method accessors
				// ----------------------------------
				String setMethod="set";
				String getMethod="get";
				char[] fieldName = tmpFields[i].getName().toCharArray();
				fieldName[0] = new String(""+fieldName[0]).toUpperCase().charAt(0);
				
				getMethod+=new String(fieldName);
				setMethod+=new String(fieldName);
				
				Method get=null,set=null;
				// looking for get method
				try {
					get = cl.getMethod(getMethod,(Class<?>[])null) ;
				} catch (SecurityException e) {
					
				} catch (NoSuchMethodException e) {
					
				}
				this.getMethods.add(get);
				// looking for set method
				try {
					set = cl.getMethod(setMethod,tmpFields[i].getType()) ;
				} catch (SecurityException e) {
					
				} catch (NoSuchMethodException e) {
					
				}
				this.setMethods.add(set);
				
				// is current field table uniq id ?
				// --------------------------------
				if (f.isTableUniqueId()) {
					if (f.fieldType().toString().equals(Integer.class.toString())) {
						uniqIdField = tmpFields[i] ;
						uniqIdGetMethod = get ;
						uniqIdSetMethod = set ;
						uniqIdAnnot = f;
						uniqIdSequence = f.getSequenceName();
					}
				}
			}
		}
		
		// looking for default constructor
		// --------------------------------
		try {
			defaultConstructor = cl.getConstructor((Class<?>[])null);
		} catch (SecurityException e) {
			defaultConstructor = null ;
		} catch (NoSuchMethodException e) {
			defaultConstructor = null;
		}
	}
	
	@Override
	public void createTable(Connection connection) throws SQLException {
		DatabaseMetaData dmd        = connection.getMetaData();
		ResultSet        tables     = null ;
		boolean          tableFound = false ;
		
		try {
			tables = dmd.getTables(connection.getCatalog(),null,"%",null);
			while(tables.next()){
				for(int i=0; i<tables.getMetaData().getColumnCount();i++){
					String nomColonne = tables.getMetaData().getColumnName(i+1);
					Object valeurColonne = tables.getObject(i+1);
					/*if ("table_type".equals(nomColonne)) {
						type = (String)valeurColonne;
					}*/
					if ("table_name".equals(nomColonne)) {
						String name = (String)valeurColonne;
						if (name.compareToIgnoreCase(tableName)==0) {
							tableFound = true;
						}
					}
				}
				if (tableFound==true) {
					logger.info("Table already exist");
					return;
				}
			}
		} finally {
			if (tables!=null) tables.close();
		}
		StringBuffer query = new StringBuffer(" ");
		if (uniqIdAnnot!=null && !"".equals(uniqIdSequence)) {
			query.append("CREATE SEQUENCE ");
			query.append(uniqIdSequence);
			query.append(" start with 1 MINVALUE 1;\n");
		}
		query.append("create table ");
		query.append(tableName);
		query.append(" ( \n");
		// creating table
		// --------------
		for ( int i=0;i<fields.size();i++) {
			Field f = fields.get(i);
			SqlField sqlField = f.getAnnotation(SqlField.class);
			if (i>0) query.append(",\n");
			query.append(sqlField.sqlFieldName());
			query.append(" ");
			if (sqlField.equals(uniqIdAnnot)) {
				query.append("integer NOT NULL UNIQUE DEFAULT ");
				if (!"".equals(uniqIdSequence)) {
					query.append(" nextval('");
					query.append(uniqIdSequence);
					query.append("')");
				}
			}
			else if (sqlField.fieldType().equals(String.class)) {
				if (sqlField.maxLength()==0) {
					query.append(" text NOT NULL");
				}
				else {
					query.append(" varchar(");
					query.append(sqlField.maxLength());
					query.append(") NOT NULL");
				}
			}
			else if (sqlField.fieldType().equals(Integer.class)) {
				query.append(" integer NOT NULL");
			}
			else if (sqlField.fieldType().equals(Long.class)) {
				query.append(" bigint NOT NULL");
			}
			else if (sqlField.fieldType().equals(Blob.class)) {
				query.append(" bytea NOT NULL");
			}
		}
		query.append("\n);\n");
		if (uniqIdAnnot!=null && !"".equals(uniqIdSequence)) {
			query.append("ALTER SEQUENCE ");
			query.append(uniqIdSequence);
			query.append(" OWNED BY ");
			query.append(tableName);
			query.append(".");
			query.append(uniqIdAnnot.sqlFieldName());
			query.append(";\n");
		}
		System.out.println(query.toString());
		connection.createStatement().execute(query.toString());
	}
	
	
	/**
	 * load a bean from database with default request (from uniq id)
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 */
	public void load(T sqlBean,Connection connection) throws SQLException {
		Integer id = getUniqIdValue(sqlBean);
		if (id != null) {
			String request = makeConsultFromIdQuery(sqlBean.getClass(),id);
			load(sqlBean,connection,request);
		}
		else {
			String request = makeConsultFromAllFieldsQuery(sqlBean);
			load(sqlBean,connection,request);
		}
	}
	/**
	 * load a bean from database with request = param request
	 * @param sqlBean
	 * @param connection
	 * @param request
	 * @throws SQLException
	 */
	public void load(T sqlBean,Connection connection,String request) throws SQLException{
		Statement stmt =  null;
		ResultSet rs = null;
		logger.debug(request);
		try {
			stmt =  connection.createStatement();
			stmt.execute(request);
			rs = stmt.getResultSet();
			if (rs!=null && rs.next()) {
				readFromResultSet(sqlBean,rs);
				logger.debug("Ok bean loaded");
				rs.close();
				rs=null;
			}
			else {
				throw new SQLException(NO_RESULT);
			}
		}
		finally {
			if (rs!=null) {
				rs.close();
			}
			if (stmt!=null) {
				stmt.close();
			}
		}
	}
	
	/**
	 * read a bean from current resultset
	 * @param sqlBean
	 * @param rs
	 * @throws SQLException
	 */
	private void readFromResultSet(T sqlBean,ResultSet rs) throws SQLException {
		for (int i=0 ; i<fields.size();i++) {
			Field  field = fields.get(i);
			Method set   = setMethods.get(i);
			
			logger.debug("Loading current field "+field.getName());
			SqlField f = field.getAnnotation(SqlField.class);
			logger.debug("loading field sqlname="+f.sqlFieldName()+" var name="+field.getName());
			try {
				set.invoke(sqlBean, rs.getObject(f.sqlFieldName()));				
			}
			catch (IllegalAccessException e) {
				throw new SQLException(e);
			}
			catch (IllegalArgumentException e) {
				throw new SQLException(e);
			}
			catch (InvocationTargetException e) {
				throw new SQLException(e);
			}
		}
	}
	
	/**
	 * Read results of query=request - listResult must be filled
	 * @param listResult
	 * @param connection
	 * @param request
	 */
	@Override
	public void loadList(List<T> listResult,Connection connection,String request) throws SQLException{
		Statement stmt =  null;
		ResultSet rs   = null;
		logger.debug("executing request="+request);
		try {
			stmt =  connection.createStatement();
			stmt.execute(request);
			rs = stmt.getResultSet();
			if (rs==null) {
				throw new SQLException(NO_RESULT);
			}
			
			
			while (rs.next()) {
				try {
					T element = defaultConstructor.newInstance(new Object[0]);
					readFromResultSet(element,rs);
					listResult.add(element);
				}
				catch (IllegalAccessException e) {
					throw new SQLException(e);
				}
				catch (InvocationTargetException e) {
					throw new SQLException(e);
				}
				catch (InstantiationException e) {
					throw new SQLException(e);
				}
			}
		}
		finally {
			if (stmt!=null) {
				stmt.close();
			}
		}
	}
	
	
	/**
	 * create/update current bean into database
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 * @throws  
	 */
	public void save(T sqlBean,Connection connection) throws SQLException {
		Integer nextIdVal    = null ;
		Integer currentIdVal = getUniqIdValue(sqlBean);
		
		/* if a sequence is associated with uniq id        */
		/* we ask for a new id value (only if it is a new  */
		/* recor - ie if currentIdVal<=0                   */
		/* ----------------------------------------------- */
		if (uniqIdSequence!=null && uniqIdSequence.length()>0 && currentIdVal.intValue()<=0) {
			String request = "select nextval('"+uniqIdSequence+"');";
			Statement stmt =  null;
			ResultSet rs = null;
			logger.debug(request );
			try {
				stmt =  connection.createStatement();
				stmt.execute(request);
				rs = stmt.getResultSet();
				if (rs!=null && rs.next()) {
					nextIdVal = rs.getInt("nextval");
					logger.debug("Ok bean loaded");
					rs.close();
					rs=null;
				}
				else {
					throw new SQLException(NO_RESULT);
				}
			}
			finally {
				if (stmt!=null) {
					stmt.close();
				}
			}
		}
		
		PreparedStatement stmt =  null;
		try {
			stmt = makeSaveQuery(connection,sqlBean,nextIdVal);
			

			// executing request
			// -----------------
			stmt.executeUpdate();
			// if it was a create statement
			// we update uniq id field
			if (currentIdVal!=null && currentIdVal.intValue()<=0) {
				if (uniqIdSequence!=null && uniqIdSequence.length()>0) {
					logger.debug("New id created = "+nextIdVal+" for class "+sqlBean.getClass());
					uniqIdSetMethod.invoke(sqlBean, nextIdVal);
				}
				else {
					ResultSet rs = stmt.getGeneratedKeys(); 
					//ResultSet rs = stmt.getResultSet();
					if (rs!=null && rs.next()) {
						Integer n = new Integer(rs.getInt(1));
						logger.debug("New id created = "+n+" for class "+sqlBean.getClass());
						uniqIdSetMethod.invoke(sqlBean, n);
						rs.close();
					}
				}
			}
		}
		catch (IllegalAccessException e) {
			logger.warn(e);
			throw new SQLException(e);
		}
		catch (IllegalArgumentException e) {
			logger.warn(e);
			throw new SQLException(e);
		}
		catch (InvocationTargetException e) {
			logger.warn(e);
			throw new SQLException(e);
		}
		finally {
			if (stmt!=null && !(connection instanceof Session)) {
				stmt.close();
			}
		}
	}
	
	/**
	 * delete current bean into database
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 */
	public void delete(T sqlBean,Connection connection) throws SQLException {
		checkSqlTableAnnotation(sqlBean.getClass());
		PreparedStatement stmt = null;
		/* if connection is a SqlTool Session */
		if (connection instanceof Session ) {
			 stmt = ((Session)connection).getDeleteBeanPreparedStatement(beanClass);
			 if (stmt!=null) {
				 logger.debug("Bean delete's prepared statement found in cache for class="+sqlBean.getClass());
			 }
		}
		
		
		// first we check if table has one unique id
		if (uniqIdField!=null) {
			Integer val = getUniqIdValue(sqlBean);
			if (stmt==null) {
				StringBuffer query = new StringBuffer("delete from ");
				query.append(tableName);
				query.append(" where (");
				query.append(uniqIdAnnot.sqlFieldName());
				query.append("=?)");

				stmt = connection.prepareStatement(query.toString());
				if (connection instanceof Session ) {
					((Session)connection).setDeleteBeanPreparedStatement(beanClass, stmt);
				}
			}
			stmt.setInt(1, val.intValue());
			stmt.execute();
		}
		else {
			if (stmt==null) {
				StringBuffer query = new StringBuffer("delete from ");
				query.append(tableName);
				query.append(" where (");
				boolean firstField = true ;
				for (Field field : fields) {
					SqlField f = field.getAnnotation(SqlField.class);
					if (!firstField) {
						query.append(" and ");
					}
					query.append(f.sqlFieldName());
					query.append("=?");
					firstField=false;
				}
				query.append(")");
				logger.debug(query);
				stmt = connection.prepareStatement(query.toString());
				if (connection instanceof Session ) {
					((Session)connection).setDeleteBeanPreparedStatement(beanClass, stmt);
				}
			}
			for ( int i=0;i<fields.size();i++) {
				Method get  = getMethods.get(i);
				try {
					stmt.setObject(i+1, get.invoke(sqlBean,(Object[])null));
				}
				catch (IllegalAccessException e) {
					throw new SQLException(e);
				}
				catch (IllegalArgumentException e) {
					throw new SQLException(e);
				} catch (InvocationTargetException e) {
					throw new SQLException(e);
				}
			}
			stmt.execute();
		}
	}
	
	public void delete(Connection connection,String query) throws SQLException {
		Statement stmt =  null;
		logger.debug(query);
		try {
			stmt =  connection.createStatement();
			stmt.execute(query);
		}
		finally {
			if (stmt!=null) {
				stmt.close();
			}
		}
	}
	
	
	
	/**
	 * return savequery string (witch could be an insert into or an update query)
	 * @param sqlBean
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement makeSaveQuery(Connection connection,T sqlBean,Integer nextIdVal) throws SQLException {
		checkSqlTableAnnotation(sqlBean.getClass());
		boolean newRecord = true ;
		// first we check if table has one unique id
		// else we throw an exception
		
		Integer val = getUniqIdValue(sqlBean);
		if (val==null || val.intValue()<=0) {
			logger.debug("sqlBean is a new record");
		}
		else {
			logger.debug("sqlBean is an update");
			newRecord = false ;
		}
		
		if (newRecord) {
			return(create(connection,sqlBean,nextIdVal));
		}
		else {
			return(update(connection,sqlBean));
		}
	}
	
	
	private String makeConsultFromAllFieldsQuery(T sqlBean) throws SQLException {
		StringBuffer request=new StringBuffer("select * from ");
		request.append(tableName);
		request.append(" where ");
		boolean firstField=true;
		for ( int i=0;i<fields.size();i++) {
			Field field = fields.get(i);
			SqlField f = field.getAnnotation(SqlField.class);
			Method get  = getMethods.get(i);
			if (!firstField) {
				request.append(" and ");
			}
			firstField=false;
			request.append(f.sqlFieldName());
			request.append("=");
			if (f.fieldType().equals(String.class)) {
				request.append("'");
			}
			try {
				request.append(get.invoke(sqlBean,(Object[])null));
			} catch (Exception e) {
				throw new SQLException(e);
			}
			if (f.fieldType().equals(String.class)) {
				request.append("'");
			}
		}
		return(request.toString());
	}
	
	
	/**
	 * generate consultfromid query
	 * @param cl
	 * @param val
	 * @return
	 * @throws SQLException
	 */
	private String makeConsultFromIdQuery(Class<?> cl,Integer val) throws SQLException {
		if (uniqIdField==null) {
			throw new SQLException("Field Unique Id not found");
		}
		StringBuffer request = new StringBuffer("select * from ");
		request.append(tableName);
		request.append(" where ");
		request.append(uniqIdAnnot.sqlFieldName());
		request.append("=");
		request.append(val);
		
		logger.debug("makeConsultFromIdQuery -> "+request);
		return(request.toString());
	}
	
	
	private String makeUpdateQueryString(T sqlBean) throws SQLException{
		StringBuffer request=new StringBuffer("update ");
		request.append(tableName);
		request.append(" set ");
		boolean firstField=true;
		for (Field field : fields) {
			SqlField f = field.getAnnotation(SqlField.class);
			if (field!=uniqIdField) {
				if (!firstField) {
					request.append(",");
				}
				request.append(f.sqlFieldName());
				request.append("=?");
				firstField=false;
			}
		}
		request.append(" where ");
		SqlField f =uniqIdField.getAnnotation(SqlField.class);
		request.append(f.sqlFieldName());
		request.append("=?");
		logger.debug(request);
		return(request.toString());
	}
	/**
	 * return 'update ' query
	 * @param fields
	 * @param sqlBean
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement update(Connection connection,T sqlBean) throws SQLException {
		PreparedStatement stmt = null;
		/* if connection is a SqlTool Session */
		if (connection instanceof Session ) {
			 stmt = ((Session)connection).getUpdateBeanPreparedStatement(beanClass);
			 if (stmt!=null) {
				 logger.debug("Bean update's prepared statement found in cache");
			 }
		}
		if (stmt==null) {
			String query = makeUpdateQueryString(sqlBean);
			stmt = connection.prepareStatement(query);
			if (connection instanceof Session ) {
				((Session)connection).setUpdateBeanPreparedStatement(beanClass, stmt);
			}
		}
		int k=0;
		for (int i=0;i<getMethods.size();i++) {
			Field field = fields.get(i);
			Method get = getMethods.get(i);
			if (field!=uniqIdField) {
				k++;
				try {
					stmt.setObject(k, get.invoke(sqlBean,(Object[])null));
				}
				catch (IllegalAccessException e) {
					throw new SQLException(e);
				}
				catch (IllegalArgumentException e) {
					throw new SQLException(e);
				} catch (InvocationTargetException e) {
					throw new SQLException(e);
				}
			}
		}
		k++;
		stmt.setObject(k, getUniqIdValue(sqlBean));
		return(stmt);
	}
	
	/**
	 * make query string for insertion of bean into database
	 * @param sqlBean
	 * @param nextIdVal
	 * @return
	 */
	private String makeCreateQueryString(T sqlBean) {
		StringBuffer request=new StringBuffer("insert into ");
		StringBuffer fieldNames = null;
		StringBuffer fieldValues = null;
		request.append(tableName);
		for (Field field : fields) {
			SqlField f = field.getAnnotation(SqlField.class);
			if (fieldNames==null) {
				fieldNames=new StringBuffer(f.sqlFieldName());
				fieldValues=new StringBuffer("?");
			}
			else {
				fieldNames.append(",");
				fieldNames.append(f.sqlFieldName());
				fieldValues.append(",?");
			}
		}
		request.append(" (");
		request.append(fieldNames);
		request.append(") values (");
		request.append(fieldValues);
		request.append(")");
		logger.debug(request);
		return(request.toString());
	}
	
	/**
	 * return prepared statement for insertion of a new bean into database
	 * @param fields
	 * @param sqlBean
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement create(Connection connection,T sqlBean,Integer nextIdVal) throws SQLException {
		PreparedStatement stmt=null;
		
		String request ;
		/* if connection is a SqlTool Session */
		if (connection instanceof Session ) {
			 stmt = ((Session)connection).getSaveBeanPreparedStatement(beanClass);
			 if (stmt!=null) {
				 logger.debug("Bean creation's prepared statement found in cache");
				 stmt.clearParameters();
			 }
		}
		if (stmt==null) {
			request = makeCreateQueryString(sqlBean);
			stmt = connection.prepareStatement(request);
			if (connection instanceof Session ) {
				((Session)connection).setSaveBeanPreparedStatement(beanClass, stmt);
			}
		}
		int k=0;
		for ( int i=0;i<fields.size();i++) {
			k++;
			Method get  = getMethods.get(i);
			if (fields.get(i)==uniqIdField) {
				if (uniqIdSequence!=null && uniqIdSequence.length()>0) {
					stmt.setInt(k,nextIdVal);
				} else {
					stmt.setString(k, "DEFAULT");
				}
			}
			else {
				try {
					stmt.setObject(k, get.invoke(sqlBean,(Object[])null));
				}
				catch (IllegalAccessException e) {
					throw new SQLException(e);
				}

				catch (IllegalArgumentException e) {
					throw new SQLException(e);
				} catch (InvocationTargetException e) {
					throw new SQLException(e);
				}
			}
		}
		return(stmt);
	}
	
	@Override
	public Integer count(Connection connection) throws SQLException {
		String request = "select count(1) from "+tableName;
		return(count(request,connection));
	}
	
	@Override
	public Integer count(String request, Connection connection) throws SQLException {
		Statement stmt =  null;
		ResultSet rs = null;
		Integer res = 0;
		logger.debug(request);
		try {
			stmt =  connection.createStatement();
			stmt.execute(request);
			rs = stmt.getResultSet();
			if (rs!=null && rs.next()) {
				res = rs.getInt(1);
				rs.close();
				rs=null;
			}
		}
		finally {
			if (stmt!=null) {
				stmt.close();
			}
		}
		return(res);
	}
	
	
	/**
	 * check if class T has annotation SqlTable
	 * @param SqlBean
	 * @throws SQLException
	 */
	private void checkSqlTableAnnotation(Class<?> cl) throws SQLException {
		if (!cl.isAnnotationPresent(SqlTable.class)) {
			throw new SQLException("No annotation "+SqlTable.class.toString());
		}
	}
	
	private Integer getUniqIdValue(T sqlBean) throws SQLException {
		//Integer val = (Integer)uniqIdField.get(sqlBean);
		if (uniqIdField==null) return(null);
		try {
			Integer val = (Integer)uniqIdGetMethod.invoke(sqlBean, (Object[])null);
			return(val);
		}
		catch (IllegalArgumentException e) {
			throw new SQLException(e);
		}
		catch (IllegalAccessException e) {
			throw new SQLException(e);
		}
		catch (InvocationTargetException e) {
			throw new SQLException(e);
		}
	}
	
	
	private ArrayList<Field>  fields             ;
	private ArrayList<Method> getMethods         ;
	private ArrayList<Method> setMethods         ;
	
	private Field             uniqIdField        ;
	private String            uniqIdSequence     ;
	private Method            uniqIdGetMethod    ;
	private Method            uniqIdSetMethod    ;
	private SqlField          uniqIdAnnot        ;
	private Constructor<T>    defaultConstructor ;
	private Class<T>          beanClass          ;
	private String            tableName          ;
	
	
	private static Logger logger = Logger.getLogger(RequestFactoryImpl.class);
	private static final String NO_RESULT = "NO RESULT";
	
}
