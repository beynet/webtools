package org.beynet.utils.framework;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.exception.UtilsRuntimeException;
import org.beynet.utils.messages.impl.MessageQueueImpl;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.DataBaseAccessorImpl;
import org.beynet.utils.sqltools.RequestManagerImpl;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public class ConstructorImpl implements Constructor {
	
	
	
	@Override
	public Object getService(String name) {
		return(ujbList.get(name));
	}
	
	/**
	 * default constructor
	 * @param contextPath
	 */
	public ConstructorImpl(String contextPath) {
		ujbList = new HashMap<String, Object>();
		originalList = new HashMap<String, Object>();
		try {
			localContext= InitialContext.doLookup("java:comp/env");
			if (logger.isInfoEnabled()) logger.info("Container context found");
			
		}
		catch(NamingException e) {
			
		}
		if (localContext==null) {
			try {
				localContext = new InitialContext();
				if (logger.isInfoEnabled()) logger.info("Context created");
			} catch (NamingException e) {
				logger.error("Error creating context");
			}
		}
		init(contextPath);
	}
	/**
	 * construct a class name from it's filename
	 * @param current
	 * @return
	 */
	private String getClassNameFromFile(File current) {
		String classesPattern = "classes"+File.separator ;
		String path = current.getPath();
		int offset = path.indexOf(classesPattern) ;
		String className = path.substring(offset+classesPattern.length(), path.length());
		className=className.replace(File.separatorChar, '.');
		className=className.replaceAll("\\.class", "");
		return(className);
	}
	/**
	 * create an UJB called name
	 * @param classFound
	 * @param name
	 * @return
	 */
	private Object createUJB(Class<?> classFound,String name) {
		try {
			Object ujb      = classFound.newInstance();
			List<Class<? extends Object>> interceptors = null ;
			Interceptors interceptorsAnnotation = classFound.getAnnotation(Interceptors.class);
			if (interceptorsAnnotation!=null) {
				interceptors = new ArrayList<Class<? extends Object>>() ;
				interceptors.add(interceptorsAnnotation.value());
			}
			Object ujbProxy = UtilsClassUJBProxy.newInstance(ujb,interceptors);
			if (logger.isDebugEnabled()) logger.debug("!!!!!!!!!!!!!! Adding new UJB "+name+" to list");
			ujbList.put(name, ujbProxy);
			originalList.put(name, ujb);
			try {
				localContext.addToEnvironment(name, ujbProxy);
			}catch(NamingException e) {
				
			}
			return(ujb);
			
		} catch (Exception e) {
			throw new UtilsRuntimeException(UtilsExceptions.Error_Param,"Class instanciation "+classFound.getName(),e);
		}
	}
	/**
	 * create an UJB from a class
	 * @param classFound
	 */
	private void createUJB(Class<?> classFound) {
		UJB ujbAnnotation = classFound.getAnnotation(UJB.class);
		createUJB(classFound,ujbAnnotation.name());
	}
	
	private void configureManager(RequestManagerImpl result) {
		Field f ;
		try {
			f= result.getClass().getDeclaredField("accessorName") ;
			f.setAccessible(true);
			String name = (String)f.get(result);
			f.setAccessible(false);
			f = result.getClass().getDeclaredField("accessor");
			f.setAccessible(true);
			f.set(result, ujbList.get(name));
			f.setAccessible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private void configureQueue(MessageQueueImpl result) {
		Field f ;
		try {
			f= result.getClass().getDeclaredField("accessorName") ;
			f.setAccessible(true);
			String name = (String)f.get(result);
			f.setAccessible(false);
			f = result.getClass().getDeclaredField("accessor");
			f.setAccessible(true);
			f.set(result, ujbList.get(name));
			f.setAccessible(false);
			
			f= result.getClass().getDeclaredField("managerName") ;
			f.setAccessible(true);
			name = (String)f.get(result);
			f.setAccessible(false);
			f = result.getClass().getDeclaredField("manager");
			f.setAccessible(true);
			f.set(result, ujbList.get(name));
			f.setAccessible(false);
			
			
			Method m = result.getClass().getDeclaredMethod("createTables");
			m.setAccessible(true);
			m.invoke(result );
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.beynet.utils.framework.impl.Constructor#configure(java.lang.Object)
	 */
	public void configure(Object e) {
		if (e instanceof RequestManagerImpl) {
			configureManager((RequestManagerImpl)e);
		}
		if (e instanceof MessageQueueImpl) {
			configureQueue((MessageQueueImpl)e);
		}
		
		Field[] fields = e.getClass().getDeclaredFields();
		for (Field f: fields) {
			UJB ujb = f.getAnnotation(UJB.class);
			if (ujb!=null) {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				try {
					if ("root".equals(ujb.name())) {
						f.set(e, this);
					}
					else {
						f.set(e,ujbList.get(ujb.name()));
					}
				} catch (Exception e1) {
					logger.error("Error injecting ujb="+ujb.name()+" for target class="+e.getClass().getCanonicalName());
					throw new UtilsRuntimeException(UtilsExceptions.Error_Param,e1);
				}
				f.setAccessible(accessible);
			}
		}
	}
	
	private void inspectClass(Class<?> classFound) {
		if (classFound.isAnnotationPresent(UJB.class)) {
			createUJB(classFound);
		}
	}
	/**
	 * check if current is a class or a config file
	 * @param current
	 */
	private void processFile(File current) {
		
		if (current.getName().endsWith(".class")) {
			String className = getClassNameFromFile(current);
			try {
				if (logger.isDebugEnabled()) logger.debug("Try to load class : "+className);
				Class<?> loadedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
				inspectClass(loadedClass);
			} catch (ClassNotFoundException e) {
				logger.error("Fail to load class"+className);
			}
		}
		else if (current.getName().equals("RequestsManagers.xml")) {
			/*
			 * this is a manager config file
			 */
			loadManagersUJB(current);
		}
		else if (current.getName().equals("DataBaseAccessors.xml")) {
			/*
			 * this is a definition of database
			 */
			loadDataBaseAccessors(current);
		}
		else if (current.getName().equals("Queues.xml")) {
			/*
			 * this is a queue definition file
			 */
			loadQueues(current);
		}
	}
	
	
	/**
	 * construct a MessageQueue associated with a DataBaseAccessor accessorName and
	 * with a RequestManager called managerName
	 * @param name
	 * @param accessorName
	 * @param managerName
	 */
	protected void constructQueue(String name, String accessorName,String managerName) {
		MessageQueueImpl result = (MessageQueueImpl)createUJB(MessageQueueImpl.class, name);
		//injecting database accessor
		try {
			Field f = result.getClass().getDeclaredField("accessorName");
			f.setAccessible(true);
			f.set(result, accessorName);
			f.setAccessible(false);
			
			
			f = result.getClass().getDeclaredField("queueName");
			f.setAccessible(true);
			f.set(result, name);
			f.setAccessible(false);
			
			
			f = result.getClass().getDeclaredField("managerName");
			f.setAccessible(true);
			f.set(result, managerName);
			f.setAccessible(false);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * construct queues define into xml file current
	 * exemple :
	 * <queues>
           <queue name="queuetest" manager="managertest" datasource="datatest"/>
       </queues>
	 * @param current
	 */
	private void loadQueues(File current) {
		class QueuesCallBackImpl implements XmlCallBack {

			public QueuesCallBackImpl() {
				
			}
			@Override
			public void onTagContent(List<String> tags, String content)
					throws UtilsException {	
			}
			@Override
			public void onCloseTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTagAttributs(List<String> parents, String tagName,
					Map<String, String> tagValues) throws UtilsException {
				if ("queue".equals(tagName)) {
					if (logger.isDebugEnabled()) logger.debug("creating new Queue name="+tagValues.get("name"));
					constructQueue(tagValues.get("name"),tagValues.get("datasource"),tagValues.get("manager"));
				}
			}
		};
		
		QueuesCallBackImpl callBack = new QueuesCallBackImpl();
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(callBack);
		try {
    		FileInputStream is = new FileInputStream(current);
    		byte[] b = new byte[1024];
    		int readed ;
    		while ( (readed=is.read(b))==1024) {
    			reader.addChars(b, readed);
    		}
    		if (readed>0) reader.addChars(b, readed);
    	} catch(Exception e) {
    		logger.error("Error parsing manager file",e);
    	}
	}
	
	
	protected void constructManagerUJB(String name,String dataSourceName) {
		RequestManagerImpl result = (RequestManagerImpl)createUJB(RequestManagerImpl.class, name);
		//injecting database accessor
		try {
			Field f = result.getClass().getDeclaredField("accessorName");
			f.setAccessible(true);
			f.set(result, dataSourceName);
			f.setAccessible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	protected void constructDataBaseAccessor(String name,String dataSource,String url,String cl) {
		DataBaseAccessor accessor =(DataBaseAccessor)createUJB(DataBaseAccessorImpl.class, name);
		if (dataSource!=null && !"".equals(dataSource) ) {
			try {
				accessor.setDataSource((DataSource)localContext.lookup(dataSource));
			}catch(NamingException e) {
				if (url==null || "".equals(url) || "".equals(cl) || cl==null) {
					throw new RuntimeException(e);
				}
			}
		}
		accessor.setDataBaseDebugUrl(url);
		accessor.setDebugDataBaseClass(cl);
	}
	
	
	
	/**
	 * construct database accessors defined into xml file current
	 * should be constructed from a datasource or from a default classname combined with an url
	 * example :
	 * <databaseaccessors>
         <databaseaccessor name="datatest" datasource="xarch/jdbc/ds" url="jdbc:postgresql://localhost/test?user=beynet&password=sec2DBUser"  class="org.postgresql.Driver"/>
       </databaseaccessors>
	 * @param current
	 */
	private void loadDataBaseAccessors(File current) {
		class DataBaseAccessorCallBackImpl implements XmlCallBack {

			public DataBaseAccessorCallBackImpl() {
				
			}
			@Override
			public void onTagContent(List<String> tags, String content)
					throws UtilsException {	
			}
			@Override
			public void onCloseTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTagAttributs(List<String> parents, String tagName,
					Map<String, String> tagValues) throws UtilsException {
				if ("databaseaccessor".equals(tagName)) {
					if (logger.isDebugEnabled()) logger.debug("creating new DataBaseAccessor name="+tagValues.get("name"));
					constructDataBaseAccessor(tagValues.get("name"),tagValues.get("datasource"),tagValues.get("url"),tagValues.get("class"));
				}
			}
		};
		
		DataBaseAccessorCallBackImpl callBack = new DataBaseAccessorCallBackImpl();
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(callBack);
		try {
    		FileInputStream is = new FileInputStream(current);
    		byte[] b = new byte[1024];
    		int readed ;
    		while ( (readed=is.read(b))==1024) {
    			reader.addChars(b, readed);
    		}
    		if (readed>0) reader.addChars(b, readed);
    	} catch(Exception e) {
    		logger.error("Error parsing manager file",e);
    	}
	}
	
	/**
	 * construct QueryManager (s) defined into xmlfile current
	 * example :
	 * <managers>
          <manager name="managertest" datasource="datatest"/>
       </managers>
	 * @param current
	 */
	private void loadManagersUJB(File current) {
		class ManagerCallBackImpl implements XmlCallBack {

			public ManagerCallBackImpl() {
				
			}
			@Override
			public void onCloseTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}
			@Override
			public void onTagContent(List<String> tags, String content)
					throws UtilsException {				
			}

			@Override
			public void onNewTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTagAttributs(List<String> parents, String tagName,
					Map<String, String> tagValues) throws UtilsException {
				if ("manager".equals(tagName)) {
					if (logger.isDebugEnabled()) logger.debug("creating new manager name="+tagValues.get("name")+" datasource jni name="+tagValues.get("datasource"));
					constructManagerUJB(tagValues.get("name"),tagValues.get("datasource"));
				}
			}
		};
		
		ManagerCallBackImpl callBack = new ManagerCallBackImpl();
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(callBack);
		try {
    		FileInputStream is = new FileInputStream(current);
    		byte[] b = new byte[1024];
    		int readed ;
    		while ( (readed=is.read(b))==1024) {
    			reader.addChars(b, readed);
    		}
    		if (readed>0) reader.addChars(b, readed);
    	} catch(Exception e) {
    		logger.error("Error parsing manager file",e);
    	}
	}
	
	/**
	 * search for UJB
	 * @param dir
	 */
	private void processDir(File dir) {
		File[] files = dir.listFiles();
		for (File current : files) {
			if (current.isDirectory()) {
				processDir(current);
			}
			else {
				processFile(current);
			}
		}
	}
	
	/**
	 * browse workingDir and search for UJB
	 * @param workingDir
	 */
	private void init(String workingDir) {
		File first = new File(workingDir);
		/*
		 * search for ujb
		 */
		processDir(first);
		/*
		 * post configure all instances
		 */
		for (String key : originalList.keySet()) {
			configure(originalList.get(key));
		}
	}
	
	
	private Context             localContext ;
	private Map<String, Object> ujbList      ;
	private Map<String, Object> originalList ;
	

	private static final Logger logger = Logger.getLogger(ConstructorImpl.class);
}
