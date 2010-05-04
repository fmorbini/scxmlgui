package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.Serializable;
import java.util.HashMap;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
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

	public static final String EVENTUNDO="EVENTundo";
	public static final String EVENTDOC="EVENTdoc";
	public static final String CONDITIONUNDO="CONDundo";
	public static final String CONDITIONDOC="CONDdoc";
	public static final String EXEUNDO="EXEundo";
	public static final String EXEDOC="EXEdoc";

	private HashMap<String,Object> edge;
	public SCXMLEdge() {
		edge=new HashMap<String, Object>();
		setEvent("");
	}
	public String getSCXMLSource() {
		return (String)edge.get(FROM);
	}
	public String getSCXMLTarget() {
		return (String)edge.get(TO);
	}
	public String getInternalID() {
		return (String)edge.get(INTERNALID);
	}
	public void setInternalID(String internalID) {
		edge.put(INTERNALID, internalID);
	}
	public String getEvent() {
		Document dmd = getEventDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)edge.get(EVENT);
			}
		}
		else
			return (String)edge.get(EVENT);
	}
	public void setEvent(String e) {
		edge.put(EVENT, e);
	}
	public String getCondition() {
		Document dmd = getConditionDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)edge.get(CONDITION);
			}
		}
		else
			return (String)edge.get(CONDITION);
	}
	public void setCondition(String c) {
		edge.put(CONDITION, c);
	}
	public String getExe() {
		Document dmd = getExeDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)edge.get(EDGEEXE);
			}
		}
		else
			return (String)edge.get(EDGEEXE);
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
		edge.put(TO, toSCXMLID);
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
	public Object cloneEdge(SCXMLImportExport scxmlImportExport) {
		SCXMLEdge e=(SCXMLEdge) scxmlImportExport.buildEdgeValue();
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
		return null;
	}
	public void setOrder(int o) {
		assert(o>=0);
		edge.put(EDGEORDER, o);
	}
	public Integer getOrder() {
		return (Integer)edge.get(EDGEORDER);
	}
}

