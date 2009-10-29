package org.beynet.utils.sqltools;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.sqltools.interfaces.RequestManager;


@UJB(name="testujb")
public class TestUJBImpl implements TestUJB {
	public TestUJBImpl() {
		
	}
	
	@Transaction
	@Override
	public void createTables() {
		try {
			manager.createTable(TestSqlBean.class);
		} catch (UtilsException e) {
			e.printStackTrace();
		}
	}
	
	@Transaction
	@Override
	public void save(TestSqlBean bean) throws UtilsException {
		manager.persist(bean);
	}
	
	@Transaction
	@Override
	public void delete(TestSqlBean bean) throws UtilsException {
		manager.delete(bean);
	}
	
	@Transaction
	@Override
	public void load(TestSqlBean bean) throws UtilsException {
		manager.load(bean);
	}
	
	@UJB(name="managertest")
	private RequestManager manager ;
}
