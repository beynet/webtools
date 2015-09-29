package org.beynet.utils.sqltools;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.NoResultException;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.framework.Session;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.junit.Test;

public class TestFramework {

	public TestFramework( ) {
	        BasicConfigurator.configure();
	        Logger.getRootLogger().setLevel(Level.DEBUG);
	        testUJB = (UJBTest)ConstructorFactory.instance(".").getService("testujb");
	        testUJB.createTables();
	}
	@Test
	public void sqlBean() throws NoResultException, UtilsException {
		Session session = SessionFactory.instance().createSession();
		try {
			TestSqlBean bean1 = new TestSqlBean();
			TestSqlBean bean2 = new TestSqlBean();
			bean1.setId(Long.valueOf(0));
			bean1.setName("TEST");
			bean1.setBin("test".getBytes());
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
			} catch(NoResultException e) {
				
			}
		}
		finally {
			if (session!=null) {
				session.rollback();
				session.close();
				SessionFactory.instance().removeSession();
			}
		}
	}
	
	@Test
	public void withQuotes() throws UtilsException, NoResultException, UnsupportedEncodingException {
	    Session session = SessionFactory.instance().createSession();
	    try {
	        TestSqlBean bean1 = new TestSqlBean();
	        TestSqlBean bean2 = new TestSqlBean();
	        bean1.setId(Long.valueOf(0));
	        bean1.setName("TEST ' with quotes '' \"");
	        bean1.setBin("TEST ' with quotes '' \"".getBytes("UTF-8"));
	        testUJB.save(bean1);
	        bean2.setId(bean1.getId());
	        testUJB.load(bean2);
	        assertEquals(bean1.getName(), bean2.getName());
	        assertThat(Boolean.valueOf(Arrays.equals(bean1.getBin()
                    , bean2.getBin())), is(Boolean.TRUE));
	        bean2.setName("2TEST 'd");
	        testUJB.save(bean2);
	        testUJB.load(bean1);
	        assertEquals(bean1.getName(), bean2.getName());
	        assertThat(Boolean.valueOf(Arrays.equals(bean1.getBin()
	                , bean2.getBin())), is(Boolean.TRUE));
	        testUJB.delete(bean1);
	        try {
	            testUJB.load(bean2);
	            assertTrue(false);
	        } catch(NoResultException e) {

	        }
	    }
	    finally {
	        if (session!=null) {
	            session.commit();
	            session.close();
	            SessionFactory.instance().removeSession();
	        }
	    }
}
	
	@UJB(name="testujb")
	UJBTest testUJB ;
}
