package org.beynet.utils.sqltools;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.Interceptors;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.sqltools.interfaces.RequestManager;


@UJB(name="testujb")
@Interceptors(InterceptorTest.class)
public class UJBTestImpl implements UJBTest {
	public UJBTestImpl() {
		
	}
	
	@Transaction
	@Override
	public void createTables() {
		logger.debug("Creating tables");
		try {
			manager.createTable(TestSqlBean.class);
		} catch (UtilsException e) {
			e.printStackTrace();
		}
	}
	
	@Transaction
	@Override
	public void save(TestSqlBean bean) throws UtilsException {
		logger.debug("avant save");
		manager.persist(bean);
		logger.debug("apres save");
	}
	
	@Transaction
	@Override
	public void delete(TestSqlBean bean) throws UtilsException {
		logger.debug("deleting");
		manager.delete(bean);
	}
	
	@Transaction
	@Override
	public void load(TestSqlBean bean) throws UtilsException {
		logger.debug("loading");
		manager.load(bean);
	}
	
	@UJB(name="managertest")
	private RequestManager manager ;
	
	private final static Logger logger = Logger.getLogger(UJBTestImpl.class);
}
