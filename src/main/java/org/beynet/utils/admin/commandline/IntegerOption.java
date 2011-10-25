package org.beynet.utils.admin.commandline;

/**
 * A StringOption is always associated with a value.
 * @author beynet
 *
 */
public class IntegerOption extends AbstractOption implements Option {
    
    public IntegerOption(String name,String description,boolean mandatory) {
        super(name,description,mandatory);
        this.value     = null ;
    }
    
    @Override
    public boolean isWithValue() {
        return true;
    }

    @Override
    public Integer getValue() {
        return value ;
    }
    
    @Override
    public void setValue(String value) throws IllegalArgumentException {
        try {
            this.value = Integer.valueOf(value);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("could not convert "+value+" to an integer");
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        this.value = null ;
    }
    

    private Integer  value ;
}
