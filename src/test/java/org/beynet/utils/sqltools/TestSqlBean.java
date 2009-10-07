package org.beynet.utils.sqltools;


@SqlTable(TestSqlBean.TABLE_NAME)
public class TestSqlBean {
	public TestSqlBean() {
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@SqlField(getSequenceName=TABLE_NAME+"_id_seq",isTableUniqueId=true,sqlFieldName=TestSqlBean.FIELD_ID,fieldType=Integer.class)
	private Integer id ;
	
	@SqlField(sqlFieldName=TestSqlBean.FIELD_NOM,fieldType=String.class)
	private String  name ;
	
	public final static String TABLE_NAME = "Test" ;
	private final static String FIELD_ID   = "id" ;
	private final static String FIELD_NOM  = "nom" ;
}
