package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class SCXMLEdge implements Serializable {
	private static final long serialVersionUID = -136975794270718480L;
	
	public static final String FROM="from";
	public static final String TO="to";
	public static final String INTERNALID="internalID";
	public static final String CONDITION="cond";
	public static final String EVENT="event";
	public static final String EDGEEXE="edgeexe";
	public static final String TARGET="target";
	public static final String SOURCE="source";
	public static final String EDGEORDER="edgeOrder";
	public static final String WITHTARGET="withTarget";

	public static final String EVENTUNDO="EVENTundo";
	public static final String EVENTDOC="EVENTdoc";
	public static final String CONDITIONUNDO="CONDundo";
	public static final String CONDITIONDOC="CONDdoc";
	public static final String EXEUNDO="EXEundo";
	public static final String EXEDOC="EXEdoc";

	private HashMap<String,Object> edge;
	public SCXMLEdge() {
		edge=new HashMap<String, Object>();
		edge.put(WITHTARGET, false);
		setEvent("");
	}

	public boolean isCycle() {
		return getSCXMLSource().equals(getSCXMLTarget());
	}
	public boolean isCycleWithTarget() {
		return isCycle() && (Boolean)edge.get(WITHTARGET);
	}
	public void setCycleWithTarget(boolean withTarget) {
		edge.put(WITHTARGET, withTarget);
	}
	public String getSCXMLSource() {
		return (String)edge.get(FROM);
	}
	public void setSCXMLSource(String sourceID) {
		edge.put(FROM, sourceID);
	}
	public String getSCXMLTarget() {
		return (String)edge.get(TO);
	}
	public void setSCXMLTarget(String targetID) {
		edge.put(TO, targetID);		
	}
	public String getInternalID() {
		return (String)edge.get(INTERNALID);
	}
	public void setInternalID(String internalID) {
		edge.put(INTERNALID, internalID);
	}
	public String getEvent() {
		String ret=null;
		Document dmd = getEventDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)edge.get(EVENT);
			}
		}
		else
			ret=(String)edge.get(EVENT);
		return (ret==null)?"":ret;
	}
	public void setEvent(String e) {
		edge.put(EVENT, e);
	}
	public String getCondition() {
		String ret=null;
		Document dmd = getConditionDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)edge.get(CONDITION);
			}
		}
		else
			ret=(String)edge.get(CONDITION);
		return (ret==null)?"":ret;
	}
	public void setCondition(String c) {
		edge.put(CONDITION, c);
	}
	public String getExe() {
		String ret=null;
		Document dmd = getExeDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)edge.get(EDGEEXE);
			}
		}
		else
			ret=(String)edge.get(EDGEEXE);
		return (ret==null)?"":ret;
	}
	public void setExe(String e) {
		edge.put(EDGEEXE, e);
	}
	public SCXMLEdge(String fromSCXMLID,String toSCXMLID,String cond,String event, String content) {
		edge=new HashMap<String, Object>();
		edge.put(CONDITION,cond);
		edge.put(EVENT,event);
		edge.put(EDGEEXE,content);
		edge.put(FROM, fromSCXMLID);
		if (toSCXMLID==null) {
			edge.put(TO, fromSCXMLID);
			edge.put(WITHTARGET, false);
		} else {
			edge.put(TO, toSCXMLID);
			edge.put(WITHTARGET, true);
		}
	}
	// getter and setter for document and undomanager for editing event 
	public UndoManager getEventUndoManager() {
		return (UndoManager) edge.get(EVENTUNDO);
	}
	public UndoManager setEventUndoManager(UndoManager um) {
		edge.put(EVENTUNDO,um);
		return um;
	}
	public Document getEventDoc() {
		return (Document) edge.get(EVENTDOC);
	}
	public Document setEventDoc(Document doc) {
		edge.put(EVENTDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for editing condition 
	public UndoManager getConditionUndoManager() {
		return (UndoManager) edge.get(CONDITIONUNDO);
	}
	public UndoManager setConditionUndoManager(UndoManager um) {
		edge.put(CONDITIONUNDO,um);
		return um;
	}
	public Document getConditionDoc() {
		return (Document) edge.get(CONDITIONDOC);
	}
	public Document setConditionDoc(Document doc) {
		edge.put(CONDITIONDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for editing executable content 
	public UndoManager getExeUndoManager() {
		return (UndoManager) edge.get(EXEUNDO);
	}
	public UndoManager setExeUndoManager(UndoManager um) {
		edge.put(EXEUNDO,um);
		return um;
	}
	public Document getExeDoc() {
		return (Document) edge.get(EXEDOC);
	}
	public Document setExeDoc(Document doc) {
		edge.put(EXEDOC,doc);
		return doc;
	}
	public SCXMLEdge cloneEdge() {
		SCXMLEdge e=new SCXMLEdge();
		e.edge=(HashMap<String, Object>) this.edge.clone();
		// as for the node, set all documents to null, but gets the values they contain because it's the most updated.
		e.setConditionDoc(null);
		e.setConditionUndoManager(null);
		e.setCondition(getCondition());
		e.setEventDoc(null);
		e.setEventUndoManager(null);
		e.setEvent(getEvent());
		e.setExeDoc(null);
		e.setExeUndoManager(null);
		e.setExe(getExe());
		return e;
	}
	public void setOrder(int o) {
		assert(o>=0);
		edge.put(EDGEORDER, o);
	}
	public Integer getOrder() {
		if (edge.containsKey(EDGEORDER)) return (Integer)edge.get(EDGEORDER);
		else return null;
	}
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		SCXMLEdge newe = cloneEdge();
		HashMap<String, Object> hash = edge;
		edge=newe.edge;
		out.defaultWriteObject();
		edge=hash;
	}
	public String toString() {
		return getSCXMLSource()+"-["+getCondition()+","+getEvent()+"]->"+getSCXMLTarget();
	}
	
	public String getStyle() {
		String ret="straight;strokeColor=#888888;";
		if (isCycle() && (!isCycleWithTarget())) ret+="strokeWidth=3;dashed=1;";
		return ret;
	}
}

