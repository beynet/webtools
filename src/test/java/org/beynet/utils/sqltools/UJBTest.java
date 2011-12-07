package org.beynet.utils.sqltools;

import org.beynet.utils.exception.NoResultException;
import org.beynet.utils.exception.UtilsException;

public interface UJBTest {
	void createTables();
	void save(TestSqlBean bean) throws UtilsException ;
	void load(TestSqlBean bean) throws UtilsException,NoResultException ;
	void delete(TestSqlBean bean) throws UtilsException ;
}