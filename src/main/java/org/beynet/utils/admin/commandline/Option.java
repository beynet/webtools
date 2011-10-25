package org.beynet.utils.admin.commandline;
/**
 * Represent an option usable in a commandlineoption see {@link CommandLineOptionsAnalyzer}
 * @author beynet
 *
 */
public interface Option {
    
    /**
     * @return expected option name
     */
    public String getName();
    
    /**
     * @return the description of this option
     */
    public String getDescription();
    
    /**
     * @return the value associated with the option - makes sense only when withValue return true
     */
    public Object getValue();
    
    
    /**
     * @return true if a value must be associated with this option
     */
    public boolean isWithValue();
    
    /**
     * @return true if current option is mandatory
     */
    public boolean isMandatory();
    
    /**
     * called when this option is found
     */
    public void setOptionFound();
    
    /**
     * @return true if the option was found in the command line
     */
    public boolean isOptionFound();
    
    /**
     * define the value of the option from the string found in the command line
     * @param value
     * @throws IllegalArgumentException : if given value is to convertible to expected value type
     */
    public void setValue(String value) throws IllegalArgumentException ;
    
    
    /**
     * reset the internal state of this option
     */
    public void reset();
    
}
