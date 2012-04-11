package org.beynet.utils.sqltools.interfaces;

/**
 * Helper to use to avoid sql injection
 * @author beynet
 *
 */
public class SQLHelper {
    public static String quoteTheQuotes(String sqlString) {
        if (sqlString==null) return null;
        return(sqlString.replaceAll("'", "''"));
    }
    
    public static String unQuoteTheQuotes(String sqlString) {
        if (sqlString==null) return null;
        return(sqlString.replaceAll("''", "'"));
    }
}
