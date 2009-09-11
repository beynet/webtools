package org.beynet.utils;

import org.beynet.utils.sqltools.RequestFactoryImpl;
import org.beynet.utils.sqltools.SqlBean;
import org.beynet.utils.sqltools.SqlField;
import org.beynet.utils.sqltools.SqlTable;
import org.beynet.utils.sqltools.interfaces.RequestFactory;

@SqlTable(TestSqlBean.TABLE_NAME)
public class TestSqlBean extends SqlBean {
	
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
	
	@SqlField(getSequenceName="clients_id_seq",isTableUniqueId=true,sqlFieldName=TestSqlBean.FIELD_ID,fieldType=Integer.class)
	private Integer id ;
	
	@SqlField(sqlFieldName=TestSqlBean.FIELD_NOM,fieldType=String.class)
	private String  name ;
	
	private final static RequestFactory<TestSqlBean> rq = new RequestFactoryImpl<TestSqlBean>(TestSqlBean.class);
	public final static String TABLE_NAME = "Test" ;
	private final static String FIELD_ID   = "id" ;
	private final static String FIELD_NOM  = "nom" ;
}
