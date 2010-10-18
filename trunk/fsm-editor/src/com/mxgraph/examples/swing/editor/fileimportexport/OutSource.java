package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.Serializable;

public class OutSource implements Serializable {

	private static final long serialVersionUID = 1L;
	public enum OUTSOURCETYPE {SRC,XINC};

	private OUTSOURCETYPE type;
	private String location;
	public OutSource(OUTSOURCETYPE t,String l) {
		setLocation(l);
		setType(t);
	}
	public OUTSOURCETYPE getType() {
		return type;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setType(OUTSOURCETYPE type) {
		this.type = type;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OutSource) {
			OutSource objOutSource=(OutSource)obj;
			return (obj!=null && ((objOutSource.location!=null && objOutSource.location.equals(location)) ||
					(objOutSource.location==null))
					&& ((objOutSource.type!=null && objOutSource.type.equals(type)) ||
							(objOutSource.type==null)));
		} else return super.equals(obj);
	}
}
