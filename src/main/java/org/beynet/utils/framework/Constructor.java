package org.beynet.utils.framework;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
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
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.DataBaseAccessorImpl;
import org.beynet.utils.sqltools.RequestManagerImpl;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public class Constructor {
	
	
	public synchronized static Constructor instance(String contextPath) {
		Constructor instance = instances.get(contextPath) ;
		if (instance==null) {
			instance = new Constructor(contextPath);
			instances.put(contextPath, instance);
		}
		return(instance);
	}
	
	
	public Object getService(String name) {
		return(ujbList.get(name));
	}
	
	public Constructor(String contextPath) {
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
	
	private String getClassNameFromFile(File current) {
		String classesPattern = "classes/" ;
		String path = current.getPath();
		int offset = path.indexOf(classesPattern) ;
		String className = path.substring(offset+classesPattern.length(), path.length());
		className=className.replace(File.separatorChar, '.');
		className=className.replaceAll("\\.class", "");
		return(className);
	}
	
	private Object createUJB(Class<?> classFound,String name) {
		try {
			Object u = classFound.newInstance();
			Object p = UtilsClassUJBProxy.newInstance(u);
			if (logger.isDebugEnabled()) logger.debug("!!!!!!!!!!!!!! Adding new UJB "+name+" to list");
			ujbList.put(name, p);
			originalList.put(name, u);
			try {
				localContext.addToEnvironment(name, p);
			}catch(NamingException e) {
				
			}
			return(u);
			
		} catch (Exception e) {
			throw new UtilsRuntimeException(UtilsExceptions.Error_Param,"Class instanciation "+classFound.getName(),e);
		}
	}
	
	private void createUJB(Class<?> classFound) {
		UJB ujbAnnotation = classFound.getAnnotation(UJB.class);
		createUJB(classFound,ujbAnnotation.name());
	}
	
	public void configure(Object e) {
		Field[] fields = e.getClass().getDeclaredFields();
		for (Field f: fields) {
			UJB ujb = f.getAnnotation(UJB.class);
			if (ujb!=null) {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				try {
					f.set(e,ujbList.get(ujb.name()));
				} catch (Exception e1) {
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
			loadManagersUJB(current);
		}
	}
	
	
	protected void constructManagerUJB(String name,String dataSource) {
		try {
			DataBaseAccessor accessor = new DataBaseAccessorImpl();
			accessor.setDataSource((DataSource)localContext.lookup(dataSource));
			RequestManagerImpl result = (RequestManagerImpl)createUJB(RequestManagerImpl.class, name);
			//injecting database accessor
			try {
				Field f = result.getClass().getDeclaredField("accessor");
				f.setAccessible(true);
				f.set(result, accessor);
				f.setAccessible(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
		}catch(NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private void loadManagersUJB(File current) {
		class ManagerCallBackImpl implements XmlCallBack {

			public ManagerCallBackImpl() {
				
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
				if ("manager".equals(tagName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("creating new manager name="+tagValues.get("name")+" datasource jni name="+tagValues.get("datasource"));
						constructManagerUJB(tagValues.get("name"),tagValues.get("datasource"));
					}
				}
			}
			
			private List<String> _documents;
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
	
	private void init(String contextPath) {
		File first = new File(contextPath);
		processDir(first);
		for (String key : originalList.keySet()) {
			configure(originalList.get(key));
		}
	}
	
	
	private Context             localContext ;
	private Map<String, Object> ujbList      ;
	private Map<String, Object> originalList ;
	
	
	private static Map<String,Constructor> instances = new HashMap<String, Constructor>();
	private static final Logger logger = Logger.getLogger(Constructor.class);
}
