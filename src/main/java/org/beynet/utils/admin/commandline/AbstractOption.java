package org.beynet.utils.admin.commandline;

public abstract class AbstractOption implements Option {
    
    public AbstractOption(String name,String description,boolean mandatory) {
        reset();
        this.name = name;
        this.description = description;
        this.mandatory = mandatory;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return(description);
    }
    
    @Override
    public void setOptionFound() {
        this.found = true ;
    }
    
    @Override
    public boolean isOptionFound() {
        return found ;
    }
    
    @Override
    public void reset() {
        this.found = false ;
    }
    
    @Override
    public boolean isMandatory() {
        return mandatory;
    }
    
    protected boolean found = false ;
    private String name,description;
    private boolean mandatory;
}
