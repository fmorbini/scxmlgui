package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.mxgraph.examples.config.SCXMLConstraints.RestrictedState;
import com.mxgraph.examples.config.SCXMLConstraints.RestrictedState.PossibleEvent;
import com.mxgraph.examples.swing.editor.fileimportexport.OutSource.OUTSOURCETYPE;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode.HISTORYTYPE;
import com.mxgraph.examples.swing.editor.scxml.MyUndoManager;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.StringUtils;
import com.mxgraph.util.mxConstants;

public class SCXMLNode implements Serializable {
	private static final long serialVersionUID = -2136349535452806563L;

	public static final String GEOX="geometryX";
	public static final String GEOY="geometryY";
	public static final String GEOW="geometryW";
	public static final String GEOH="geometryH";
	
	public static final String INTERNALID="internalID";
	public static final String ID="id";
	public static final String NAME="name";
	public static final String TYPE="type";
	public static final String RESTRICTEDSTATE="restrictedState";
	public static final String INITIAL="initial";
	public static final String CLUSTER="cluster";
	public static final String FINAL="final";
	public static final String ONENTRYEXE="onentryexe";
	public static final String INITEXE="initexe";
	public static final String ONEXITEXE="onexitexe";
	public static final String SCRIPT="script";

	public static final String HISTORY="history";
	public static final String PARALLEL="parallel";
	public static final String NORMAL="normal";
	public static final String RESTICTED="restricted";
	public static final String STYLE="style";
	public static final String DATAMODEL="datamodel";
	public static final String DONEDATA="donedata";
	
	public enum HISTORYTYPE {DEEP,SHALLOW};
	
	public static final String DEFAULTFILLCOLOR="#cdd5ff";
	public static final String DEFAULTSTROKECOLOR="#000000";
	public static final String DEFAULTSHAPE="rounded=1";
	public static final String PARALLELFILLCOLOR="#c2d200";
	public static final String PARALLELSTROKECOLOR="#c2d200";
	public static final String INITIALFILLCOLOR="#ffab75";
	public static final String FINALSTROKECOLOR="#FF0000";
	public static final String DEEPHISTORYFILLCOLOR="#bb00a6";
	public static final String SHALLOWHISTORYFILLCOLOR="#dd6fd1";

	public static final String INITIALSHAPE="ellipse";
	public static final String CLUSTERSHAPE="swimlane";

	public static final String COMMENTS="comments";
	public static final String COMMENTSUNDO="COundo";
	public static final String COMMENTSDOC="COdoc";

	public static final String DATAMODELUNDO="DMundo";
	public static final String DATAMODELDOC="DMdoc";
	
	public static final String SCXMLIDUNDO="SCXMLIDundo";
	public static final String SCXMLIDDOC="SCXMLIDdoc";
	public static final String NAMEUNDO="NAMEundo";
	public static final String NAMEDOC="NAMEdoc";
	public static final String ONENTRYUNDO="ENTRYundo";
	public static final String ONENTRYDOC="ENTRYdoc";
	public static final String ONEXITUNDO="EXITundo";
	public static final String ONEXITDOC="EXITdoc";
	public static final String SCRIPTUNDO="SCRIPTundo";
	public static final String SCRIPTDOC="SCRIPTdoc";
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
	private HashSet<OutSource> outSourcingChildren;
	private boolean saveRoot=true;
	private boolean isFake=false;
	public SCXMLNode() {
		node=new HashMap<String, Object>();
		node.put(TYPE,NORMAL);
		setShape(DEFAULTSHAPE);
		setStrokeColor(DEFAULTSTROKECOLOR);
		setInitial(false);
		setCluster(false);
		setFinal(false);
		setParallel(false);
		outSourcingChildren=new HashSet<OutSource>();
		setFillColorFromState();
	}
	public boolean getFake() {return isFake;}
	public void setFake(boolean f) {isFake=f;}
	public boolean shouldThisRootBeSaved() {
		return !isRoot() || saveRoot;
	}
	public void setSaveThisRoot(boolean save) {
		if (isRoot()) saveRoot=save;
		else saveRoot=true;
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
	public void addToOutsourcingChildren(OutSource source) {
		outSourcingChildren.add(source);
	}
	public HashSet<OutSource> getOutsourcingChildren() { return outSourcingChildren;}
	public OutSource getSRC() {
		OutSource ret=null;
		ret=(OutSource)node.get(SRC);
		if (ret==null) node.put(SRC,ret=new OutSource(OUTSOURCETYPE.SRC, ""));
		Document dmd = getSRCDoc();
		if (dmd!=null) {
			try {
				String location=dmd.getText(0, dmd.getLength());
				ret.setLocation(location);
			} catch (BadLocationException e) {}
		}
		return ret;
	}
	public void setSRC(String src,OUTSOURCETYPE type) {
		node.put(SRC, new OutSource(type, src));
	}
	public void setSRC(OutSource src) {
		node.put(SRC, src);
	}
	public String getOutsourcedLocation() {
		return (isOutsourcedNode())?StringUtils.removeLeadingAndTrailingSpaces(getSRC().getLocation()):"";
	}
	public void setOutsourcedLocation(String location) {
		if (isOutsourcedNode())
			getSRC().setLocation(location);
		else setSRC(location, OUTSOURCETYPE.SRC);
	}
	public boolean isOutsourcedNode() {
		OutSource src = getSRC();
		return (src!=null) && (!StringUtils.isEmptyString(src.getLocation()));
	}
	public boolean isOutsourcedNodeUsingSRC() {
		OutSource src = getSRC();
		return (src!=null) && src.getType()==OUTSOURCETYPE.SRC;
	}
	public boolean isOutsourcedNodeUsingXInclude() {
		OutSource src = getSRC();
		return (src!=null) && src.getType()==OUTSOURCETYPE.XINC;
	}
	// getter and setter for document and undomanager for the SRC field of a node
	public MyUndoManager getSRCUndoManager() {
		return (MyUndoManager) node.get(SRCUNDO);
	}
	public MyUndoManager setSRCUndoManager(MyUndoManager um) {
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
	public String getNamespace() {
		String ret=null;
		Document dmd = getNamespaceDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(NAMESPACE);
			}
		}
		else
			ret=(String)node.get(NAMESPACE);
		return (ret==null)?"":ret;
	}
	public void setNamespace(String namespace) {
		node.put(NAMESPACE, namespace);
	}
	// getter and setter for document and undomanager for the NAMESPACE field of a node
	public MyUndoManager getNamespaceUndoManager() {
		return (MyUndoManager) node.get(NAMESPACEUNDO);
	}
	public MyUndoManager setNamespaceUndoManager(MyUndoManager um) {
		node.put(NAMESPACEUNDO,um);
		return um;
	}
	public Document getNamespaceDoc() {
		return (Document) node.get(NAMESPACEDOC);
	}
	public Document setNamespaceDoc(Document doc) {
		node.put(NAMESPACEDOC,doc);
		return doc;
	}
	public String getID() {
		String ret=null;
		Document dmd = getIDDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(ID);
			}
		}
		else
			ret=(String)node.get(ID);
		return (ret==null)?"":ret;
	}
	public void setID(String scxmlID) {
		node.put(ID, scxmlID);
	}
	public String getName() {
		String ret=null;
		Document dmd = getNameDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(NAME);
			}
		}
		else
			ret=(String)node.get(NAME);
		return (ret==null)?"":ret;
	}
	public void setName(String name) {
		node.put(NAME, name);
	}
	public String getOnEntry() {
		String ret=null;
		Document dmd = getOnEntryDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(ONENTRYEXE);
			}
		}
		else
			ret=(String)node.get(ONENTRYEXE);
		return (ret==null)?"":ret;
	}
	public String getOnExit() {
		String ret=null;
		Document dmd = getOnExitDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(ONEXITEXE);
			}
		}
		else
			ret=(String)node.get(ONEXITEXE);
		return (ret==null)?"":ret;
	}
	public String getOnInitialEntry() {
		String ret=null;
		Document dmd = getOnInitialEntryDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(INITEXE);
			}
		}
		else
			ret=(String)node.get(INITEXE);
		return (ret==null)?"":ret;
	}
	public String getScript() {
		String ret=null;
		Document dmd = getScriptDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(SCRIPT);
			}
		}
		else
			ret=(String)node.get(SCRIPT);
		return (ret==null)?"":ret;
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
	public void setScript(String script) {
		node.put(SCRIPT,script);
	}
	public void appendToScript(String script) {
		String existingContent=getScript();
		node.put(SCRIPT,(StringUtils.isEmptyString(existingContent))?script:existingContent+script);
	}
	public void setDoneData(String dd) {
		node.put(DONEDATA,dd);
	}
	public String getDoneData() {
		String ret=null;
		Document dmd = getDoneDataDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(DONEDATA);
			}
		}
		else
			ret=(String)node.get(DONEDATA);
		return (ret==null)?"":ret;
	}
	public void setDatamodel(String dm) {
		node.put(DATAMODEL,dm);
	}
	public void addToDataModel(String dm) {
		String pdm=getDatamodel();
		node.put(DATAMODEL,(pdm==null)?dm:pdm+dm);
	}
	public String getDatamodel() {
		String ret=null;
		Document dmd = getDatamodelDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(DATAMODEL);
			}
		}
		else
			ret=(String)node.get(DATAMODEL);
		return (ret==null)?"":ret;
	}
	public MyUndoManager getCommentsUndoManager() {
		return (MyUndoManager) node.get(COMMENTSUNDO);
	}
	public MyUndoManager setCommentsUndoManager(MyUndoManager um) {
		node.put(COMMENTSUNDO,um);
		return um;
	}
	public Document getCommentsDoc() {
		return (Document) node.get(COMMENTSDOC);
	}
	public Document setCommentsDoc(Document doc) {
		node.put(COMMENTSDOC,doc);
		return doc;
	}
	public void setComments(String cm) {
		node.put(COMMENTS,cm);
	}
	public String getComments() {
		String ret=null;
		Document dmd = getCommentsDoc();
		if (dmd!=null) {
			try {
				ret=dmd.getText(0, dmd.getLength());
			} catch (BadLocationException e) {
				ret=(String)node.get(COMMENTS);
			}
		}
		else
			ret=(String)node.get(COMMENTS);
		return (ret==null)?"":ret;
	}
	public void setParallel(boolean b) {
		setStrokeColor((isFinal())?FINALSTROKECOLOR:((b)?PARALLELSTROKECOLOR:DEFAULTSTROKECOLOR));
		node.put(TYPE, (b)?PARALLEL:NORMAL);
		if (b) setCluster(true); // a parallel node must be a cluster
		setFillColorFromState();
	}
	public boolean isParallel() {
		if (node.get(TYPE).equals(PARALLEL))
			return true;
		else
			return false;
	}
	
	private void setShapeFromState() {
		Boolean cluster=isClusterNode();
		if (cluster!=null && cluster) setShape(CLUSTERSHAPE);
		else if (isInitial()) setShape(INITIALSHAPE);
		else setShape(DEFAULTSHAPE);
	}
	
	public void setInitial(Boolean b) {
		node.put(INITIAL, b);
		setShapeFromState();
		setFillColorFromState();
	}
	public Boolean isInitial() {
		return (Boolean)node.get(INITIAL);
	}
	public void setRestricted(Boolean b, RestrictedState restrictedState) {
		if (isRestricted()) {
			List<RestrictedState> nodeRestrictions = getRestrictedStates();
			if ((isRestricted(restrictedState)) && (!b)) {
				nodeRestrictions.remove(restrictedState);
				if (nodeRestrictions.isEmpty()) {
					setStrokeColor(DEFAULTSTROKECOLOR);
					setStrokeWidth(null);
					node.put(TYPE, NORMAL);
					node.remove(RESTRICTEDSTATE);
				}
			} else if ((b) && (!isRestricted(restrictedState))){
				nodeRestrictions.add(restrictedState);
			}
		} else if (b) {
			setStrokeColor(restrictedState.getColor());
			setStrokeWidth("4");
			node.put(TYPE, RESTICTED);
			List<RestrictedState> nodeRestrictions = new LinkedList<RestrictedState>();
			nodeRestrictions.add(restrictedState);
			node.put(RESTRICTEDSTATE, nodeRestrictions);
		}
	}
	public Boolean isRestricted(){
		if (node.get(TYPE).equals(RESTICTED)) {
			return true;
		} else {
			return false;
		}
	}
	public Boolean isRestricted(RestrictedState restrictedState){
		if (isRestricted()) {
			List<RestrictedState> restrictedStates = getRestrictedStates();
			for(RestrictedState tempState: restrictedStates){
				if (restrictedState.getName().equals(tempState.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	public List<RestrictedState> getRestrictedStates(){
		if (isRestricted()) {
			return (List<RestrictedState>)node.get(RESTRICTEDSTATE);
		} else {
			return null;
		}
	}
	public List<PossibleEvent> getPossibleEvents(){
		List<PossibleEvent> possibleEvents = null;
		if (isRestricted()) {
			possibleEvents = new LinkedList<PossibleEvent>();
	    	for(RestrictedState tempState: getRestrictedStates()){
	    		possibleEvents.addAll(tempState.getPossibleEvent());
	    	}
		}
		return possibleEvents;
	}
	public void setAsHistory(final HISTORYTYPE type) {
		node.put(HISTORY, type);
		setFillColorFromState();
	}
	public Boolean isHistoryNode() {
		return (node.get(HISTORY) instanceof HISTORYTYPE);
	}
	public HISTORYTYPE getHistoryType() {return (HISTORYTYPE) node.get(HISTORY); }
	public Boolean isDeepHistory() {
		return ((node.get(HISTORY)!=null) &&
				(node.get(HISTORY).equals(HISTORYTYPE.DEEP)));
	}
	public Boolean isShallowHistory() {
		return ((node.get(HISTORY)!=null) &&
				(node.get(HISTORY).equals(HISTORYTYPE.SHALLOW)));
	}
	public void setCluster(Boolean b) {
		node.put(CLUSTER, b);
		setShapeFromState();
	}
	public Boolean isClusterNode() {
		return (Boolean)node.get(CLUSTER);
	}
	public void setFinal(Boolean b) {
		setStrokeColor((b)?FINALSTROKECOLOR:((isParallel())?PARALLELSTROKECOLOR:DEFAULTSTROKECOLOR));
		node.put(FINAL, b);
	}
	public Boolean isFinal() {
		return (Boolean)node.get(FINAL);
	}
	@Override
	public String toString() {
		String ret="<"+getID();
		//for (String i:node.keySet()) ret+=i+": "+node.get(i)+"; ";
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
	private void setFillColorFromState() {
		String fillColor=DEFAULTFILLCOLOR;
		String gradientColor;
		boolean isHistory=isHistoryNode();
		boolean isInitial=isInitial();
		boolean isParallel=isParallel();
		assert(!(isHistory && isParallel));

		if (isHistory) {
			HISTORYTYPE historyType = getHistoryType();
			if (historyType.equals(HISTORYTYPE.DEEP)) fillColor=DEEPHISTORYFILLCOLOR;
			else if (historyType.equals(HISTORYTYPE.SHALLOW)) fillColor=SHALLOWHISTORYFILLCOLOR;
		}
		
		if (isParallel) fillColor=PARALLELFILLCOLOR;
		
		gradientColor=null;
		if (isInitial) gradientColor=INITIALFILLCOLOR;

		setFillColor(fillColor);
		setGradientColor(gradientColor);
	}
	public void setFillColor(String color) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (color==null)
			sh.remove(mxConstants.STYLE_FILLCOLOR);
		else
			sh.put(mxConstants.STYLE_FILLCOLOR,color);
	}
	public void setGradientColor(String color) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (color==null)
			sh.remove(mxConstants.STYLE_GRADIENTCOLOR);
		else
			sh.put(mxConstants.STYLE_GRADIENTCOLOR,color);
	}
	public String getFillColor() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get(mxConstants.STYLE_FILLCOLOR);
	}
	public void setStrokeColor(String color) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (color==null)
			sh.remove(mxConstants.STYLE_STROKECOLOR);
		else
			sh.put(mxConstants.STYLE_STROKECOLOR,color);
	}
	public String getStrokeColor() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get(mxConstants.STYLE_STROKECOLOR);
	}
	public void setStrokeWidth(String w) {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		if (sh==null) {
			node.put(STYLE,sh=new HashMap<String, String>());
		}
		if (w==null)
			sh.remove(mxConstants.STYLE_STROKEWIDTH);
		else
			sh.put(mxConstants.STYLE_STROKEWIDTH,w);
	}
	public String getStrokeWidth() {
		HashMap<String,String> sh=(HashMap<String, String>)node.get(STYLE);
		return (sh==null)?null:sh.get(mxConstants.STYLE_STROKEWIDTH);
	}
	public String getStyle() {
		HashMap<String, String> sh = (HashMap<String, String>)node.get(STYLE);
		String ret=sh.get("root")+";";
		boolean outSourced=isOutsourcedNode();
		for (String k:sh.keySet()) {
			if (!k.equals("root")) {
				if (!outSourced || (!k.equals(mxConstants.STYLE_STROKEWIDTH) && !k.equals(mxConstants.STYLE_DASHED)))
					ret+=k+"="+sh.get(k)+";";
			}
		}
		if (outSourced) ret+="strokeWidth=3;dashed=1;";
		return ret;
	}
	// getter and setter for document and undomanager for the datamodel editor
	public MyUndoManager getDatamodelUndoManager() {
		return (MyUndoManager) node.get(DATAMODELUNDO);
	}
	public MyUndoManager setDatamodelUndoManager(MyUndoManager um) {
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
	public MyUndoManager getOnEntryUndoManager() {
		return (MyUndoManager) node.get(ONENTRYUNDO);
	}
	public MyUndoManager setOnEntryUndoManager(MyUndoManager um) {
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
	public MyUndoManager getOnExitUndoManager() {
		return (MyUndoManager) node.get(ONEXITUNDO);
	}
	public MyUndoManager setOnExitUndoManager(MyUndoManager um) {
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
	public MyUndoManager getOnInitialEntryUndoManager() {
		return (MyUndoManager) node.get(ONINITIALENTRYUNDO);
	}
	public MyUndoManager setOnInitialEntryUndoManager(MyUndoManager um) {
		node.put(ONINITIALENTRYUNDO,um);
		return um;
	}
	public Document getOnInitialEntryDoc() {
		return (Document) node.get(ONINITIALENTRYDOC);
	}
	public Document setOnInitialEntryDoc(Document doc) {
		node.put(ONINITIALENTRYDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for extra unknown content (including script)
	public MyUndoManager getScriptUndoManager() {
		return (MyUndoManager) node.get(SCRIPTUNDO);
	}
	public MyUndoManager setScriptUndoManager(MyUndoManager um) {
		node.put(SCRIPTUNDO,um);
		return um;
	}
	public Document getScriptDoc() {
		return (Document) node.get(SCRIPTDOC);
	}
	public Document setScriptDoc(Document doc) {
		node.put(SCRIPTDOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the donedata field of a final node
	public MyUndoManager getDoneDataUndoManager() {
		return (MyUndoManager) node.get(FINALDONEDATAUNDO);
	}
	public MyUndoManager setDoneDataUndoManager(MyUndoManager um) {
		node.put(FINALDONEDATAUNDO,um);
		return um;
	}
	public Document getDoneDataDoc() {
		return (Document) node.get(FINALDONEDATADOC);
	}
	public Document setDoneDataDoc(Document doc) {
		node.put(FINALDONEDATADOC,doc);
		return doc;
	}
	// getter and setter for document and undomanager for the SCXML ID field of a node
	public MyUndoManager getIDUndoManager() {
		return (MyUndoManager) node.get(SCXMLIDUNDO);
	}
	public MyUndoManager setIDUndoManager(MyUndoManager um) {
		node.put(SCXMLIDUNDO,um);
		return um;
	}
	public Document getIDDoc() {
		return (Document) node.get(SCXMLIDDOC);
	}
	public Document setIDDoc(Document doc) {
		node.put(SCXMLIDDOC,doc);
		return doc;
	}
	public MyUndoManager getNameUndoManager() {
		return (MyUndoManager) node.get(NAMEUNDO);
	}
	public MyUndoManager setNameUndoManager(MyUndoManager um) {
		node.put(NAMEUNDO,um);
		return um;
	}
	public Document getNameDoc() {
		return (Document) node.get(NAMEDOC);
	}
	public Document setNameDoc(Document doc) {
		node.put(NAMEDOC,doc);
		return doc;
	}

	public SCXMLNode cloneNode() {
		SCXMLNode n=new SCXMLNode();
		n.node=(HashMap<String, Object>) node.clone();
		// removes the documents in the original value (if there). But get their values (because if there they have the
		// real value of the property they represent (the document)
		n.setDatamodelDoc(null);
		n.setDatamodelUndoManager(null);
		n.setDatamodel(getDatamodel());
		n.setCommentsDoc(null);
		n.setCommentsUndoManager(null);
		n.setComments(getComments());
		n.setDoneDataDoc(null);
		n.setDoneDataUndoManager(null);
		n.setDoneData(getDoneData());
		n.setOnInitialEntryDoc(null);
		n.setOnInitialEntryUndoManager(null);
		n.setOnInitialEntry(getOnInitialEntry());
		n.setOnEntryDoc(null);
		n.setOnEntryUndoManager(null);
		n.setOnEntry(getOnEntry());
		n.setOnExitDoc(null);
		n.setOnExitUndoManager(null);
		n.setOnExit(getOnExit());
		n.setScriptDoc(null);
		n.setScriptUndoManager(null);
		n.setScript(getScript());
		n.setIDDoc(null);
		n.setIDUndoManager(null);
		n.setNameUndoManager(null);
		n.setNameDoc(null);
		n.setSRC(getSRC());
		n.setSRCDoc(null);
		n.setSRCUndoManager(null);
		n.setNamespace(getNamespace());
		n.setNamespaceDoc(null);
		n.setNamespaceUndoManager(null);
		n.setID(getID());
		n.setName(getName());
		n.setFake(getFake());
		return n;
	}
	public void setGeometry(double x, double y, double w, double h) {
		node.put(GEOX, x);
		node.put(GEOY, y);
		node.put(GEOW, w);
		node.put(GEOH, h);
	}
	public mxGeometry getGeometry() {		
		if (node.containsKey(GEOX)) {
			double x,y,h,w;
			x=(Double)node.get(GEOX);
			y=(Double)node.get(GEOY);
			w=(Double)node.get(GEOW);
			h=(Double)node.get(GEOH);
			return new mxGeometry(x, y, w, h);
		}
		else return null;
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

