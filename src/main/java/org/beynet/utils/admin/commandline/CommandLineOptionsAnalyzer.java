package org.beynet.utils.admin.commandline;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class handling all the options found in a command line
 * @author beynet
 *
 */
public class CommandLineOptionsAnalyzer {

    private static final Pattern TOKEN = Pattern.compile("^(\"[^\"]*\"|[^\"\\s]*)\\s*.*");

    /**
     * parse command line into tokens, tokens contained between " are grouped
     * @param commandLine
     * @return
     */
    public static List<String> parseCommandLine(String commandLine) {
        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(commandLine);
        while (matcher.matches()) {
            String match = matcher.group(1);
            String arg = match;
            if (arg.startsWith("\"") && arg.endsWith("\"") && arg.length()>=2) {
                arg = arg.substring(1, arg.length() - 1);
            }
            result.add(arg);
            if (commandLine.length()==match.length()) break;
            commandLine=commandLine.substring(match.length());
            commandLine = commandLine.trim();
            matcher=TOKEN.matcher(commandLine);
        }
        return result;
    }


    /**
     * @param options
     * @throws IllegalArgumentException
     */
    public CommandLineOptionsAnalyzer(List<Option> options) throws IllegalArgumentException {
        this.options = options ;
        optionsByName = new HashMap<String, Option>();
        for (Option option : options) {
            optionsByName.put(option.getName(), option);
        }
    }

    /**
     * analyse a new command line
     * @throws InvalidParameterException
     */
    public void analyseCommandLine(String [] options) throws IllegalArgumentException {
        for (Option option : this.options) {
            option.reset();
        }
        Option previous = null ;
        for (String option : options) {
            // no previous context
            if (previous==null) {
                Option found = optionsByName.get(option);
                if (found==null) throw new IllegalArgumentException("unknown option : "+option);
                found.setOptionFound();
                if (found.isWithValue()) {
                    previous = found ;
                }
            }
            else {
                previous.setValue(option);
                previous=null;
            }
        }
        for (Option option : this.options) {
            if (option.isWithValue() && option.isMandatory()) {
                Object found = option.getValue();
                if (found == null) throw new IllegalArgumentException("no value for option "+option.getName());
                if (found instanceof List) {
                    if ( ((List<?>)found).size() == 0 ) throw new IllegalArgumentException("no value for option "+option.getName());
                }
            }
        }
    }
    
    
    /**
     * print help
     * paralele 
     */
    public void printHelp(PrintStream os) {
        StringBuilder mandatories = new StringBuilder("Mandatory parameters : \n");
                                    mandatories.append("-----------------------\n");
                                    
        StringBuilder optionals = new StringBuilder("\nOptional parameters :\n");
                                   optionals.append("---------------------\n");
                                    
        
        for (Option option : options) {
            StringBuilder builder = null ;
            if (option.isMandatory()) {
                builder = mandatories;
            }
            else {
                builder = optionals;
            }
            builder.append("<");
            builder.append(option.getName());
            builder.append(">\t: ");
            builder.append(option.getDescription());
            builder.append("\n");
        }
                                   
        os.println(mandatories.toString());
        os.println(optionals.toString());
    }


    private List<Option>       options       ;
    private Map<String,Option> optionsByName ;
}
