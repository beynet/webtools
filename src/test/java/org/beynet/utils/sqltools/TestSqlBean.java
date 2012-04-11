package org.beynet.utils.sqltools;

import java.sql.Blob;


@SqlTable(TestSqlBean.TABLE_NAME)
public class TestSqlBean {
	public TestSqlBean() {
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public void setBin(byte[] bin) {
        this.bin = bin ;
    }
    public byte[] getBin() {
        return(bin) ;
    }
	
	@SqlField(getSequenceName=TABLE_NAME+"_id_seq",isTableUniqueId=true,sqlFieldName=TestSqlBean.FIELD_ID,fieldType=Long.class)
	private Long id ;
	
	@SqlField(sqlFieldName=TestSqlBean.FIELD_NOM,fieldType=String.class)
	private String  name ;
	
	@SqlField(sqlFieldName=TestSqlBean.FIELD_BIN,fieldType = Blob.class)
    private byte[]  bin   ;
    
	
	public final static String TABLE_NAME = "Test" ;
	private final static String FIELD_ID   = "id" ;
	private final static String FIELD_NOM  = "nom" ;
	private final static String FIELD_BIN  = "bin" ;
}
