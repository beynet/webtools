package org.beynet.utils.sqltools;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.beynet.utils.sqltools.interfaces.SQLHelper;
import org.junit.Test;

public class SQLHelperTest {
    @Test
    public void removeOneQuote() {
        String test1 = "one ' only";
        String rep = SQLHelper.quoteTheQuotes(test1);
        assertThat(rep, is("one '' only"));
        assertThat(SQLHelper.unQuoteTheQuotes(rep), is(test1));
    }
    
    @Test
    public void removeTwoQuotes() {
        String test1 = "two ' or 1=1 ; select * from to be removed '' '''";
        String rep = SQLHelper.quoteTheQuotes(test1) ;
        assertThat(rep, is("two '' or 1=1 ; select * from to be removed '''' ''''''"));
        assertThat(SQLHelper.unQuoteTheQuotes(rep), is(test1));
    }
}
