package org.beynet.utils.admin.commandline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class CheckCommandLine {
    
    @Test(expected=IllegalArgumentException.class)
    public void mandatoryMissing() {
        Option opt1 = new StringOption("--opt1", "this is the first expected option", true);
        Option opt2 = new StringOption("--opt2", "this is the second expected option", true);
        Option opt3 = new BooleanOption("--opt3", "this is the third expected option");
        Option opt4 = new BooleanOption("--opt4", "this is the 4eme expected option");
        Option opt5 = new IntegerOption("--opt5", "this is the 5eme expected option",true);
        CommandLineOptionsAnalyzer cmd = new CommandLineOptionsAnalyzer(Arrays.asList(opt1,opt2,opt3,opt4,opt5));
        String [] line = {"--opt1","val1","--opt4","--opt2","val2"};
        cmd.analyseCommandLine(line);
    }
    
    @Test
    public void ok1() {
        Option opt1 = new StringOption("--opt1", "this is the first expected option", true);
        Option opt2 = new StringOption("--opt2", "this is the second expected option", true);
        Option opt3 = new BooleanOption("--opt3", "this is the third expected option");
        Option opt4 = new BooleanOption("--opt4", "this is the 4eme expected option");
        Option opt5 = new IntegerOption("--opt5", "this is the 5eme expected option",true);
        CommandLineOptionsAnalyzer cmd = new CommandLineOptionsAnalyzer(Arrays.asList(opt1,opt2,opt3,opt4,opt5));
        cmd.printHelp(System.err);
        String [] line = {"--opt1","val1","--opt4","--opt5","1234","--opt2","val2"};
        cmd.analyseCommandLine(line);
        
        assertThat(opt1.isOptionFound(), is(true));
        assertThat(opt1.getValue(), is((Object)"val1"));
        
        assertThat(opt2.isOptionFound(), is(true));
        assertThat(opt2.getValue(), is((Object)"val2"));
        
        assertThat(opt3.isOptionFound(), is(false));
        
        assertThat(opt4.isOptionFound(), is(true));
        
        assertThat(opt5.isOptionFound(), is(true));
        assertThat(opt5.getValue(), is((Object)new Integer(1234)));
        
        String [] line2 = {"--opt2","val1","--opt3","--opt1","val2","--opt5","1977",};
        
        cmd.analyseCommandLine(line2);
        
        assertThat(opt1.isOptionFound(), is(true));
        assertThat(opt1.getValue(), is((Object)"val2"));
        
        assertThat(opt2.isOptionFound(), is(true));
        assertThat(opt2.getValue(), is((Object)"val1"));
        
        assertThat(opt3.isOptionFound(), is(true));
        
        assertThat(opt4.isOptionFound(), is(false));
        
        assertThat(opt5.isOptionFound(), is(true));
        assertThat(opt5.getValue(), is((Object)new Integer(1977)));
        
        
    }
}
