package org.beynet.utils.sqltools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beynet.utils.exception.NoResultException;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class RequestManagerImpl implements RequestManager {
	
	@SuppressWarnings("rawtypes")
	public RequestManagerImpl() {
		_requestFactories = new HashMap<Class, RequestFactory>();
		accessor=null;
	}
	
	

	/**
	 * return requestfactory associated with obj
	 * @param <T>
	 * @param obj
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private RequestFactory getAssociatedFactory(Class cl) {
		RequestFactory requestFactorie =null ;
		synchronized(_requestFactories) {
			if (_requestFactories.get(cl)==null) {
				requestFactorie = new RequestFactoryImpl(cl);
				_requestFactories.put(cl,requestFactorie);
			}
			else {
				requestFactorie = _requestFactories.get(cl);
			}
		}
		return(requestFactorie);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void delete(T obj) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(obj.getClass());
		try {
			requestFactorie.delete(obj, accessor.getConnection());
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@Override
	public <T> void execute(Class<T> cl, String query) throws UtilsException {
	    @SuppressWarnings("unchecked")
        RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
        try {
            requestFactorie.execute(query, accessor.getConnection());
        } catch(SQLException e) {
            throw new UtilsException(UtilsExceptions.Error_Sql,e);
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void delete(Class<T> cl,String query) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
		try {
			requestFactorie.delete(accessor.getConnection(), query);
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Integer count(Class<T> cl,String request) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
		try {
			return(requestFactorie.count(request, accessor.getConnection()));
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Integer count(Class<T> cl) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
		try {
			return(requestFactorie.count(accessor.getConnection()));
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	/**
	 * persist obj
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> void persist(T obj) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(obj.getClass());
		try {
			requestFactorie.save(obj,  accessor.getConnection());
		} catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void load(T obj,String request) throws UtilsException,NoResultException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(obj.getClass());
		try {
			requestFactorie.load(obj,  accessor.getConnection(),request);
		}catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> void createTable(Class<T> cl) throws UtilsException  {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
		try {
			requestFactorie.createTable(accessor.getConnection());
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	/**
	 * load obj
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> void load(T obj) throws UtilsException,NoResultException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(obj.getClass());
		try {
			requestFactorie.load(obj,  accessor.getConnection());
		}catch(SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> void loadList(Class<T> cl,String request,List<T> result) throws UtilsException {
		RequestFactory<T> requestFactorie =(RequestFactory<T>)getAssociatedFactory(cl);
		if (result==null) throw new UtilsException(UtilsExceptions.Error_Param,"List param is null");
		try {
			requestFactorie.loadList(result, accessor.getConnection(), request);
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	@Override
	public <T> List<T> loadList(Class<T> cl,String request) throws UtilsException {
		ArrayList<T> result = new ArrayList<T>();
		loadList(cl, request,result);
		return(result);
	}
	
	private DataBaseAccessor accessor ;
	@SuppressWarnings("unused")
	private String accessorName;
	
	@SuppressWarnings({ "rawtypes" })
	private Map<Class, RequestFactory> _requestFactories ;
}
