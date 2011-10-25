package org.beynet.utils.admin.commandline;

/**
 * option with no value
 * @author beynet
 *
 */
public class BooleanOption extends AbstractOption implements Option {

    public BooleanOption(String name,String description) {
        super(name,description,false);
    }
    
    
    @Override
    public boolean isWithValue() {
        return false;
    }
    
    
    @Override
    public Boolean getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

}

