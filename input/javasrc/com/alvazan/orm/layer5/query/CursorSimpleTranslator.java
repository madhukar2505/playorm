package com.alvazan.orm.layer5.query;

import com.alvazan.orm.api.z5api.IndexColumnInfo;
import com.alvazan.orm.api.z8spi.action.IndexColumn;
import com.alvazan.orm.api.z8spi.iter.AbstractCursor;
import com.alvazan.orm.api.z8spi.meta.DboColumnMeta;

public class CursorSimpleTranslator extends AbstractCursor<IndexColumnInfo> {

	private AbstractCursor<IndexColumn> cursor;
	private DboColumnMeta info;

	public CursorSimpleTranslator(DboColumnMeta info, AbstractCursor<IndexColumn> scan) {
		this.info = info;
		this.cursor = scan;
	}

	@Override
	public void beforeFirst() {
		cursor.beforeFirst();
	}

	@Override
	public Holder<IndexColumnInfo> nextImpl() {
		Holder<IndexColumn> holder = cursor.nextImpl();
		if(holder == null)
			return null;
		IndexColumn indCol = holder.getValue();
		if(indCol == null)
			return new Holder<IndexColumnInfo>(null);
		IndexColumnInfo info = new IndexColumnInfo();
		info.setPrimary(indCol);
		info.setColumnMeta(this.info);
		return new Holder<IndexColumnInfo>(info);
	}

}