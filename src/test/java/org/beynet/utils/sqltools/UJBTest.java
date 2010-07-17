package org.beynet.utils.sqltools;

import org.beynet.utils.exception.UtilsException;

public interface UJBTest {
	void createTables();
	void save(TestSqlBean bean) throws UtilsException ;
	void load(TestSqlBean bean) throws UtilsException ;
	void delete(TestSqlBean bean) throws UtilsException ;
}