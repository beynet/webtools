package org.beynet.utils.admin.commandline;

/**
 * A StringOption is always associated with a value.
 * @author beynet
 *
 */
public class StringOption extends AbstractOption implements Option {
    
    public StringOption(String name,String description,boolean mandatory) {
        super(name,description,mandatory);
        this.value     = null ;
    }
    
    @Override
    public boolean isWithValue() {
        return true;
    }

    @Override
    public String getValue() {
        return value ;
    }
    
    @Override
    public void setValue(String value) throws IllegalArgumentException {
        this.value = (String)value ;
    }
    
    @Override
    public void reset() {
        super.reset();
        this.value = null ;
    }
    

    private String  value ;
}
