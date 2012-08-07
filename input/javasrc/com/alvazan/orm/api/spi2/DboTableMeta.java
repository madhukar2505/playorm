package com.alvazan.orm.api.spi2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alvazan.orm.api.base.anno.Id;
import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.base.anno.OneToMany;
import com.alvazan.orm.api.base.anno.OneToOne;

@NoSqlEntity
public class DboTableMeta {

	@Id(usegenerator=false)
	private String columnFamily;
	
	private String colNamePrefixType = null;
	private String colNameType = String.class.getName();
//	/**
//	 * A special case where the table has rows with names that are not Strings.  This is done frequently for indexes like
//	 * indexes by time for instance where the name of the column might be a byte[] representing a long value or an int value
//	 */
//	private String columnNameType = String.class.getName();
//	private String 
//	private String valueType = void.class.getName();
	
	@OneToMany(entityType=DboColumnMeta.class, keyFieldForMap="columnName")
	private Map<String, DboColumnMeta> nameToField = new HashMap<String, DboColumnMeta>();
	@OneToOne
	private DboColumnMeta idColumn;
	
	private String foreignKeyToExtensions;
	
	public String getColumnFamily() {
		return columnFamily;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}
	
	public void setRowKeyMeta(DboColumnMeta idMeta) {
		this.idColumn = idMeta;
		this.idColumn.setOwner(this);
	}
	
	public void addColumnMeta(DboColumnMeta fieldDbo) {
		fieldDbo.setOwner(this);
		nameToField.put(fieldDbo.getColumnName(), fieldDbo);
	}
	
	public DboColumnMeta getColumnMeta(String columnName) {
		return nameToField.get(columnName);
	}

	@SuppressWarnings("rawtypes")
	public void setColNameType(Class c) {
		Class objType = DboColumnMeta.translateType(c);
		this.colNameType = objType.getName();
	}
	@SuppressWarnings("rawtypes")
	public StorageTypeEnum getNameStorageType() {
		Class clazz = DboColumnMeta.classForName(colNameType);
		return DboColumnMeta.getStorageType(clazz);
	}
//	
//	@SuppressWarnings("rawtypes")
//	public Class getColumnNameType() {
//		return DboColumnMeta.classForName(columnNameType);
//	}
	
	public StorageTypeEnum getColNamePrefixType() {
		return StorageTypeEnum.lookupValue(colNamePrefixType);
	}

	public void setColNamePrefixType(StorageTypeEnum colNamePrefixType) {
		if(colNamePrefixType == null) {
			this.colNamePrefixType = null;
			return;
		}
		this.colNamePrefixType = colNamePrefixType.getDbValue();
	}

	@Override
	public String toString() {
		return "[tablename="+columnFamily+" indexedcolumns="+nameToField.values()+" pk="+idColumn+"]";
	}

	public DboColumnMeta getIdColumnMeta() {
		return idColumn;
	}

	public String getForeignKeyToExtensions() {
		return foreignKeyToExtensions;
	}

	public void setForeignKeyToExtensions(String foreignKeyToExtensions) {
		this.foreignKeyToExtensions = foreignKeyToExtensions;
	}

	public Collection<DboColumnMeta> getAllColumns() {
		return nameToField.values();
	}
}