package org.beynet.utils.sqltools;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.framework.Constructor;
import org.beynet.utils.framework.UJB;

public class TestSql extends TestCase {

	public TestSql(String name) {
		super(name);
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        accessorBaseTest = new DataBaseAccessorImpl();
        accessorBaseTest.setDebugDataBaseClass("org.postgresql.Driver");
        accessorBaseTest.setDataBaseDebugUrl("jdbc:postgresql://localhost/test?user=beynet&password=sec2DBUser");
        try {
			Constructor.instance("/");
			Constructor.instance("/").configure(this);
		}catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testTransaction() {
		assertNotNull(testeur);
		testeur.print();
	}
	@UJB(name="test")
	TestUJB testeur ;
	private DataBaseAccessor accessorBaseTest ;
	private static final Logger logger = Logger.getLogger(TestSqlBean.class);
	

}
