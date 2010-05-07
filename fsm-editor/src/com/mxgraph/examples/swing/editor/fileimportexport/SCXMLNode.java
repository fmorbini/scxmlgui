package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import com.mxgraph.examples.swing.editor.utils.StringUtils;
import com.mxgraph.model.mxGeometry;

public class SCXMLNode implements Serializable {
	private static final long serialVersionUID = -2136349535452806563L;

	public static final String GEOX="geomtryX";
	public static final String GEOY="geomtryY";
	public static final String GEOW="geomtryW";
	public static final String GEOH="geomtryH";
	
	public static final String INTERNALID="internalID";
	public static final String ID="id";
	public static final String TYPE="type";
	public static final String INITIAL="initial";
	public static final String FINAL="final";
	public static final String ONENTRYEXE="onentryexe";
	public static final String INITEXE="initexe";
	public static final String FINALDATA="finalData";
	public static final String ONEXITEXE="onexitexe";

	public static final String PARALLEL="parallel";
	public static final String NORMAL="normal";
	public static final String STYLE="style";
	public static final String DATAMODEL="datamodel";
	public static final String DONEDATA="donedata";
	
	public static final String DEFAULTFILLCOLOR="#fcd087";
	public static final String DEFAULTSTROKECOLOR="#000000";
	public static final String DEFAULTSHAPE="rounded=1";
	public static final String PARALLELFILLCOLOR="#c2d200";
	public static final String PARALLELSTROKECOLOR="#c2d200";
	public static final String INITIALFILLCOLOR="#cffc87";
	public static final String FINALSTROKECOLOR="#FF0000";
	public static final String CLUSTERSHAPE="swimlane";

	public static final String DATAMODELUNDO="DMundo";
	public static final String DATAMODELDOC="DMdoc";
	
	public static final String SCXMLIDUNDO="SCXMLIDundo";
	public static final String SCXMLIDDOC="SCXMLIDdoc";
	public static final String ONENTRYUNDO="ENTRYundo";
	public static final String ONENTRYDOC="ENTRYdoc";
	public static final String ONEXITUNDO="EXITundo";
	public static final String ONEXITDOC="EXITdoc";
	// only initial states
	public static final String ONINITIALENTRYUNDO="INITIALundo";
	public static final String ONINITIALENTRYDOC="INITIALdoc";
	// only final states
	public static final String FINALDONEDATAUNDO="FINALundo";
	public static final String FINALDONEDATADOC="FINALdoc";

	// all non root states
	public static final String SRC="src";
	public static final String SRCUNDO="SRCundo";
	public static final String SRCDOC="SRCdoc";
	
	// only root state
	public static final String NAMESPACE="namespace";
	public static final String NAMESPACEUNDO="namespaceundo";
	public static final String NAMESPACEDOC="namespacedoc";
	
	public static final String ROOTID="SCXML";
	
	private HashMap<String,Object> node;
	public SCXMLNode() {
		node=new HashMap<String, Object>();
		node.put(TYPE,NORMAL);
		this.setShape(DEFAULTSHAPE);
		this.setFillColor(DEFAULTFILLCOLOR);
		this.setStrokeColor(DEFAULTSTROKECOLOR);
		this.setInitial(false);
		this.setFinal(false);
		this.setParallel(false);
	}
	public boolean isRoot() {
		return getID().equals(ROOTID);
	}
	public String getInternalID() {
		return (String)node.get(INTERNALID);
	}
	public void setInternalID(String internalID) {
		node.put(INTERNALID, internalID);
	}
	public String getSRC() {
		Document dmd = getSRCDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(SRC);
			}
		}
		else
			return (String)node.get(SRC);
	}
	public void setSRC(String src) {
		node.put(SRC, src);
	}
	// getter and setter for document and undomanager for the SRC field of a node
	public UndoManager getSRCUndoManager() {
		return (UndoManager) node.get(SRCUNDO);
	}
	public UndoManager setSRCUndoManager(UndoManager um) {
		node.put(SRCUNDO,um);
		return um;
	}
	public Document getSRCDoc() {
		return (Document) node.get(SRCDOC);
	}
	public Document setSRCDoc(Document doc) {
		node.put(SRCDOC,doc);
		return doc;
	}
	public String getNAMESPACE() {
		Document dmd = getNAMESPACEDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(NAMESPACE);
			}
		}
		else
			return (String)node.get(NAMESPACE);
	}
	public void setNAMESPACE(String namespace) {
		node.put(NAMESPACE, namespace);
	}
	// getter and setter for document and undomanager for the NAMESPACE field of a node
	public UndoManager getNAMESPACEUndoManager() {
		return (UndoManager) node.get(NAMESPACEUNDO);
	}
	public UndoManager setNAMESPACEUndoManager(UndoManager um) {
		node.put(NAMESPACEUNDO,um);
		return um;
	}
	public Document getNAMESPACEDoc() {
		return (Document) node.get(NAMESPACEDOC);
	}
	public Document setNAMESPACEDoc(Document doc) {
		node.put(NAMESPACEDOC,doc);
		return doc;
	}
	public String getID() {
		Document dmd = getSCXMLIDDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(ID);
			}
		}
		else
			return (String)node.get(ID);
	}
	public void setID(String scxmlID) {
		node.put(ID, scxmlID);
	}
	public String getOnEntry() {
		Document dmd = getOnEntryDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(ONENTRYEXE);
			}
		}
		else
			return (String)node.get(ONENTRYEXE);
	}
	public String getOnExit() {
		Document dmd = getOnExitDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(ONEXITEXE);
			}
		}
		else
			return (String)node.get(ONEXITEXE);
	}
	public String getOnInitialEntry() {
		Document dmd = getInitialEntryDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(INITEXE);
			}
		}
		else
			return (String)node.get(INITEXE);
	}
	public void setFinalData(String scxmlID) {
		node.put(FINALDATA, scxmlID);
	}
	public String getFinalData() {
		Document dmd = getFinalDataDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(FINALDATA);
			}
		}
		else
			return (String)node.get(FINALDATA);
	}
	public void setOnEntry(String exe) {
		node.put(ONENTRYEXE,exe);
	}
	public void setOnExit(String exe) {
		node.put(ONEXITEXE,exe);
	}
	public void setOnInitialEntry(String exe) {
		node.put(INITEXE,exe);
	}
	public void setDoneData(String dd) {
		assert(isFinal());
		node.put(DONEDATA,dd);
	}
	public String getDoneData() {
		assert(isFinal());
		Document dmd = getFinalDataDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(DONEDATA);
			}
		}
		else
			return (String)node.get(DONEDATA);
	}
	public void setDataModel(String dm) {
		node.put(DATAMODEL,dm);
	}
	public void addToDataModel(String dm) {
		String pdm=getDataModel();
		node.put(DATAMODEL,(pdm==null)?dm:pdm+dm);
	}
	public String getDataModel() {
		Document dmd = getDatamodelDoc();
		if (dmd!=null) {
			try {
				return dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				return (String)node.get(DATAMODEL);
			}
		}
		else
			return (String)node.get(DATAMODEL);
	}
	public void setParallel(boolean b) {
		this.setFillColor((isInitial())?INITIALFILLCOLOR:((b)?PARALLELFILLCOLOR:DEFAULTFILLCOLOR));
		this.setStrokeColor((isFinal())?FINALSTROKECOLOR:((b)?PARALLELSTROKECOLOR:DEFAULTSTROKECOLOR));
		node.put(TYPE, (b)?PARALLEL:NORMAL);
		if (b) setCluster(true); // a parallel node must be a cluster
	}
	public boolean isParallel() {
		if (node.get(TYPE).equals(PARALLEL))
			return true;
		else
			return false;
	}
	public void setInitial(Boolean b) {
		this.setFillColor((b)?INITIALFILLCOLOR:((isParallel())?PARALLELFILLCOLOR:DEFAULTFILLCOLOR));
		node.put(INITIAL, b);
	}
	public Boolean isInitial() {
		return (Boolean)node.get(INITIAL);
	}
	public void setCluster(Boolean b) {
		if (b) setShape(CLUSTERSHAPE);
		else setShape(DEFAULTSHAPE);
	}
	public boolean isClusterNode() {
		return getShape().equals(CLUSTERSHAPE);
	}
	public void setFinal(Boolean b) {
		this.setStrokeColor((b)?FINALSTROKECOLOR:((isParallel())?PARALLELSTROKECOLOR:DEFAULTSTROKECOLOR));
		node.put(FINAL, b);
	}
	public Boolean isFinal() {
		return (Boolean)node.get(FINAL);
	}
	@Override
	public String toString() {
		String ret="<";
		for (String i:node.keySet())
			ret+=i+": "+node.get(i)+"; ";
		return ret+">";
	}
	public void setShape(String shape) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		sh.put("root",shape);
	}
	public String getShape() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get("root");
	}
	public void setFillColor(String color) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (color==null)
			sh.remove("fillColor");
		else
			sh.put("fillColor",color);
	}
	public String getFillColor() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get("fillColor");
	}
	public void setStrokeColor(String color) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (color==null)
			sh.remove("strokeColor");
		else
			sh.put("strokeColor",color);
	}
	public String getStrokeColor() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get("strokeColor");
	}
	public String getStyle() {
		HashMap<String, String> sh = (HashMap<String, String>)node.get(STYLE);
		String ret=sh.get("root")+";";
		for (String k:sh.keySet()) {
			if (!k.equals("root"))
				ret+=k+"="+sh.get(k)+";";
		}
		return ret;
	}
	// getter and setter for document and undomanager for the datamodel editor
	public UndoManager getDatamodelUndoManager() {
		return (UndoManager) node.get(DATAMODELUNDO);
	}
	public UndoManager setDatamodelUndoManager(UndoManager um) {
		node.put(DATAMODELUNDO,um);
		return um;
	}
	public Document getDatamodelDoc() {
		return (Document) node.get(DATAMODELDOC);
	}
	public Document setDatamodelDoc(Document doc) {
		node.put(DATAMODELDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the onentry editor
	public UndoManager getOnEntryUndoManager() {
		return (UndoManager) node.get(ONENTRYUNDO);
	}
	public UndoManager setOnEntryUndoManager(UndoManager um) {
		node.put(ONENTRYUNDO,um);
		return um;
	}
	public Document getOnEntryDoc() {
		return (Document) node.get(ONENTRYDOC);
	}
	public Document setOnEntryDoc(Document doc) {
		node.put(ONENTRYDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the onexit editor
	public UndoManager getOnExitUndoManager() {
		return (UndoManager) node.get(ONEXITUNDO);
	}
	public UndoManager setOnExitUndoManager(UndoManager um) {
		node.put(ONEXITUNDO,um);
		return um;
	}
	public Document getOnExitDoc() {
		return (Document) node.get(ONEXITDOC);
	}
	public Document setOnExitDoc(Document doc) {
		node.put(ONEXITDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the executable content for the the editor for the initial entry in an initial node
	public UndoManager getInitialEntryUndoManager() {
		return (UndoManager) node.get(ONINITIALENTRYUNDO);
	}
	public UndoManager setInitialEntryUndoManager(UndoManager um) {
		node.put(ONINITIALENTRYUNDO,um);
		return um;
	}
	public Document getInitialEntryDoc() {
		return (Document) node.get(ONINITIALENTRYDOC);
	}
	public Document setInitialEntryDoc(Document doc) {
		node.put(ONINITIALENTRYDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the donedata field of a final node
	public UndoManager getFinalUndoManager() {
		return (UndoManager) node.get(FINALDONEDATAUNDO);
	}
	public UndoManager setFinalUndoManager(UndoManager um) {
		node.put(FINALDONEDATAUNDO,um);
		return um;
	}
	public Document getFinalDataDoc() {
		return (Document) node.get(FINALDONEDATADOC);
	}
	public Document setFinalDataDoc(Document doc) {
		node.put(FINALDONEDATADOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the SCXML ID field of a node
	public UndoManager getSCXMLIDUndoManager() {
		return (UndoManager) node.get(SCXMLIDUNDO);
	}
	public UndoManager setSCXMLIDUndoManager(UndoManager um) {
		node.put(SCXMLIDUNDO,um);
		return um;
	}
	public Document getSCXMLIDDoc() {
		return (Document) node.get(SCXMLIDDOC);
	}
	public Document setSCXMLIDDoc(Document doc) {
		node.put(SCXMLIDDOC,doc);
		return doc;
	}

	public SCXMLNode cloneNode() {
		SCXMLNode n=new SCXMLNode();
		n.node=(HashMap<String, Object>) this.node.clone();
		// removes the documents in the original value (if there). But get their values (because if there they have the
		// real value of the property they represent (the document)
		n.setDatamodelDoc(null);
		n.setDatamodelUndoManager(null);
		n.setDataModel(getDataModel());
		n.setFinalDataDoc(null);
		n.setFinalUndoManager(null);
		n.setFinalData(getFinalData());
		n.setInitialEntryDoc(null);
		n.setInitialEntryUndoManager(null);
		n.setOnInitialEntry(getOnInitialEntry());
		n.setOnEntryDoc(null);
		n.setOnEntryUndoManager(null);
		n.setOnEntry(getOnEntry());
		n.setOnExitDoc(null);
		n.setOnExitUndoManager(null);
		n.setOnExit(getOnExit());
		n.setSCXMLIDDoc(null);
		n.setSCXMLIDUndoManager(null);
		n.setID(getID());
		return n;
	}
	public void setGeometry(int x, int y, int w, int h) {
		node.put(GEOX, x);
		node.put(GEOY, y);
		node.put(GEOW, w);
		node.put(GEOH, h);
	}
	public mxGeometry getGeometry() {		
		if (node.containsKey(GEOX)) {
			int x,y,h,w;
			x=(Integer)node.get(GEOX);
			y=(Integer)node.get(GEOY);
			w=(Integer)node.get(GEOW);
			h=(Integer)node.get(GEOH);
			return new mxGeometry(x, y, w, h);
		}
		else return null;
	}
	public boolean isOutsourcedNode() {
		return !StringUtils.isEmptyString(getSRC());
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		SCXMLNode newn = cloneNode();
		HashMap<String, Object> hash = node;
		node=newn.node;
		out.defaultWriteObject();
		node=hash;
	}
}

