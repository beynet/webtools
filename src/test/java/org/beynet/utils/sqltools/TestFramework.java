package org.beynet.utils.sqltools;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.framework.Session;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;

public class TestFramework extends TestCase {

	public TestFramework( String testName ) {
	        super( testName );
	        BasicConfigurator.configure();
	        Logger.getRootLogger().setLevel(Level.DEBUG);
	        testUJB = (UJBTest)ConstructorFactory.instance(".").getService("testujb");
	        testUJB.createTables();
	}
	
	public void testSqlBean() {
		Session session = SessionFactory.instance().createSession();
		try {
			TestSqlBean bean1 = new TestSqlBean();
			TestSqlBean bean2 = new TestSqlBean();
			bean1.setId(0L);
			bean1.setName("TEST");
			testUJB.save(bean1);
			bean2.setId(bean1.getId());
			testUJB.load(bean2);
			assertEquals(bean1.getName(), bean2.getName());
			bean2.setName("2TEST");
			testUJB.save(bean2);
			testUJB.load(bean1);
			assertEquals(bean1.getName(), bean2.getName());
			testUJB.delete(bean1);
			try {
				testUJB.load(bean2);
				assertTrue(false);
			} catch(UtilsException e) {
				
			}
		}
		catch(UtilsException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		finally {
			if (session!=null) {
				session.rollback();
				SessionFactory.instance().removeSession();
			}
		}
	}
	
	@UJB(name="testujb")
	UJBTest testUJB ;
}
