package org.beynet.utils.admin.commandline;

import java.util.ArrayList;
import java.util.List;

/**
 * A StringOption is always associated with a value.
 * @author beynet
 *
 */
public class ListOfStringOption extends AbstractOption implements Option {
    
    public ListOfStringOption(String name,String description,boolean mandatory) {
        super(name,description,mandatory);
        this.value = new ArrayList<String>();
    }
    
    @Override
    public boolean isWithValue() {
        return true;
    }
    
    @Override
    public List<String> getValue() {
        return value ;
    }
    
    @Override
    public void setValue(String value) throws IllegalArgumentException {
        this.value.add((String)value ) ;
    }

    @Override
    public void reset() {
        super.reset();
        if (value!=null) value.clear();
    }
    
    private List<String> value ;
}
