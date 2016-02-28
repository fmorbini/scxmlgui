package com.mxgraph.examples.swing.editor.scxml;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import com.mxgraph.examples.swing.editor.fileimportexport.OutSource.OUTSOURCETYPE;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode.HISTORYTYPE;
import com.mxgraph.examples.swing.editor.utils.Pair;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

public class SCXMLChangeHandler {

	private static final SCXMLChangeHandler instance=new SCXMLChangeHandler();
	
	public abstract class SCXMLChange implements mxUndoableChange {
		@Override
		public String getInfoString() {
			return this.getClass().getSimpleName();
		}
	}
	
	public class SCXMLInitialStateProperty extends SCXMLChange {
		SCXMLNode node=null;
		Boolean valueToBeRestored=null;
		public SCXMLInitialStateProperty(SCXMLNode node) {
			this.node=node;
			this.valueToBeRestored=node.isInitial();
		}
		
		@Override
		public void execute() {
			boolean currentValue=node.isInitial();
			node.setInitial(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}
	public class SCXMLFinalStateProperty extends SCXMLChange {
		SCXMLNode node=null;
		Boolean valueToBeRestored=null;
		public SCXMLFinalStateProperty(SCXMLNode node) {
			this.node=node;
			this.valueToBeRestored=node.isFinal();
		}
		
		@Override
		public void execute() {
			boolean currentValue=node.isFinal();
			node.setFinal(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}
	public class SCXMLHistoryStateProperty extends SCXMLChange {
		SCXMLNode node=null;
		HISTORYTYPE valueToBeRestored=null;
		public SCXMLHistoryStateProperty(SCXMLNode node) {
			this.node=node;
			this.valueToBeRestored=node.getHistoryType();
		}
		
		@Override
		public void execute() {
			HISTORYTYPE currentValue=node.getHistoryType();
			node.setAsHistory(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}
	public class SCXMLClusterStateProperty extends SCXMLChange {
		SCXMLNode node=null;
		Boolean valueToBeRestored=null;
		public SCXMLClusterStateProperty(SCXMLNode node) {
			this.node=node;
			this.valueToBeRestored=node.isClusterNode();
		}
		
		@Override
		public void execute() {
			boolean currentValue=node.isClusterNode();
			node.setCluster(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}
	public class SCXMLParallelStateProperty extends SCXMLChange {
		SCXMLNode node=null;
		Boolean valueToBeRestored=null;
		public SCXMLParallelStateProperty(SCXMLNode node) {
			this.node=node;
			this.valueToBeRestored=node.isParallel();
		}
		
		@Override
		public void execute() {
			boolean currentValue=node.isParallel();
			node.setParallel(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}

	public class SCXMLGenericTextProperty<T> extends SCXMLChange {
		boolean toBeUndone=true;
		T thing=null;
		String oldString=null;
		Document oldDoc=null;
		private UndoableEdit oldUndoPos=null;
		private UndoableEdit redoPos=null;
		private boolean undoExcludesTarget=false;
		private Method docGetter,undoGetter,stringSetter,stringGetter,docSetter;
		public SCXMLGenericTextProperty(T thing,Method stringReader, Method docReader, Method undoReader,
				Method stringSetter,Method docSetter) {
			this.toBeUndone=true;
			this.thing=thing;

			this.docGetter=docReader;
			this.undoGetter=undoReader;
			this.stringSetter=stringSetter;
			this.stringGetter=stringReader;
			this.docSetter=docSetter;
			
			try {
				this.oldString=(String) stringGetter.invoke(thing);
				this.oldDoc=(Document) docReader.invoke(thing);
				MyUndoManager um = (MyUndoManager) undoReader.invoke(thing);

				this.oldUndoPos=(um!=null)?um.getNextUndoableEdit():null;
				this.undoExcludesTarget=(this.oldUndoPos!=null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void execute() {
			try {
				if (oldDoc==null) oldDoc=(Document) docGetter.invoke(thing);
				docSetter.invoke(thing,oldDoc);
				MyUndoManager um = (MyUndoManager) undoGetter.invoke(thing);
				if ((oldUndoPos==null) && (um!=null)) oldUndoPos=um.getInitialEdit();
				if (toBeUndone && (um!=null) && (oldUndoPos!=null)) {
					toBeUndone=false;
					redoPos=(um!=null)?um.getNextUndoableEdit():null;
					um.undoTo(oldUndoPos,undoExcludesTarget);
				} else if (!toBeUndone && (um!=null) && (redoPos!=null)) {
					toBeUndone=true;
					um.redoTo(redoPos,false);
				}
				stringSetter.invoke(thing, (String)stringGetter.invoke(thing));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public class SCXMLOutsourcingStateProperty extends SCXMLChange {
		private SCXMLGenericTextProperty<SCXMLNode> srcLocationState;
		private SCXMLNode node;
		private OUTSOURCETYPE typeToBeRestored;

		public SCXMLOutsourcingStateProperty(SCXMLNode node) {
			this.node=node;
			srcLocationState=new SCXMLGenericTextProperty<SCXMLNode>(node,getSRC,getSRCDoc,getSRCUndoManager,setSRC,setSRCDoc);
			this.typeToBeRestored=node.getSRC().getType();
		}
		
		@Override
		public void execute() {
			srcLocationState.execute();
			OUTSOURCETYPE currentType = node.getSRC().getType();
			node.getSRC().setType(typeToBeRestored);
			node.setFake(typeToBeRestored==OUTSOURCETYPE.XINC);
			typeToBeRestored=currentType;
		}
	}
	
	public class SCXMLOrderOutgoingEdgesStateProperty extends SCXMLChange {
		ArrayList<Pair<SCXMLEdge,Integer>> oldOrder;
		mxCell node;
		
		public SCXMLOrderOutgoingEdgesStateProperty(mxCell node) {
			this.node=node;
			oldOrder=new ArrayList<Pair<SCXMLEdge,Integer>>();
			saveCurrentOrderOfOutgoingEdges(node,oldOrder);
		}
		
		private void saveCurrentOrderOfOutgoingEdges(mxCell n,
				ArrayList<Pair<SCXMLEdge, Integer>> saveTo) {
			int l=n.getEdgeCount();
			for(int i=0;i<l;i++) {
				mxCell e=(mxCell) n.getEdgeAt(i);
				if (e.getSource()==n) {
					SCXMLEdge v=(SCXMLEdge) e.getValue();
					int o=v.getOrder();
					saveTo.add(new Pair<SCXMLEdge, Integer>(v, o));
				}
			}
		}

		@Override
		public void execute() {
			ArrayList<Pair<SCXMLEdge,Integer>> currentOrder=new ArrayList<Pair<SCXMLEdge,Integer>>();
			saveCurrentOrderOfOutgoingEdges(node,currentOrder);
			for(Pair<SCXMLEdge,Integer> eo:oldOrder) {
				eo.getFirst().setOrder(eo.getSecond());
			}
			oldOrder=currentOrder;
		}
	}
	public class SCXMLWithTargetEdgeProperty extends SCXMLChange {
		SCXMLEdge edge=null;
		Boolean valueToBeRestored=null;
		public SCXMLWithTargetEdgeProperty(SCXMLEdge edge) {
			this.edge=edge;
			this.valueToBeRestored=edge.isCycleWithTarget();
		}
		
		@Override
		public void execute() {
			boolean currentValue=edge.isCycleWithTarget();
			edge.setCycleWithTarget(valueToBeRestored);
			valueToBeRestored=currentValue;
		}
	}
	
	private static Method getSRC,getID,getName,getOnEntry,getOnExit,getOnInitialEntry,getDoneData,getDatamodel,getNamespace,getExe,getCondition,getEvent,getNodeComments,getEdgeComments;
	private static Method getSRCDoc,getIDDoc,getNameDoc,getOnEntryDoc,getOnExitDoc,getOnInitialEntryDoc,getDoneDataDoc,getDatamodelDoc,getNamespaceDoc,getExeDoc,getConditionDoc,getEventDoc,getNodeCommentsDoc,getEdgeCommentsDoc;
	private static Method getSRCUndoManager,getIDUndoManager,getNameUndoManager,getOnEntryUndoManager,getOnExitUndoManager,getOnInitialEntryUndoManager,getDoneDataUndoManager,getDatamodelUndoManager,getNamespaceUndoManager,getExeUndoManager,getConditionUndoManager,getEventUndoManager,getNodeCommentsUndoManager,getEdgeCommentsUndoManager;
	private static Method setSRC,setID,setName,setOnEntry,setOnExit,setOnInitialEntry,setDoneData,setDatamodel,setNamespace,setExe,setCondition,setEvent,setNodeComments,setEdgeComments;
	private static Method setSRCDoc,setIDDoc,setNameDoc,setOnEntryDoc,setOnExitDoc,setOnInitialEntryDoc,setDoneDataDoc,setDatamodelDoc,setNamespaceDoc,setExeDoc,setConditionDoc,setEventDoc,setNodeCommentsDoc,setEdgeCommentsDoc;
	private static Method setSRCUndoManager,setIDUndoManager,setNameUndoManager,setOnEntryUndoManager,setOnExitUndoManager,setOnInitialEntryUndoManager,setDoneDataUndoManager,setDatamodelUndoManager,setNamespaceUndoManager,setExeUndoManager,setConditionUndoManager,setEventUndoManager,setNodeCommentsUndoManager,setEdgeCommentsUndoManager;
	static {
		try {
			//getters
			getSRC=SCXMLNode.class.getDeclaredMethod("getOutsourcedLocation");
			getID=SCXMLNode.class.getDeclaredMethod("getID");
			getName=SCXMLNode.class.getDeclaredMethod("getName");
			getOnEntry=SCXMLNode.class.getDeclaredMethod("getOnEntry");
			getOnExit=SCXMLNode.class.getDeclaredMethod("getOnExit");
			getOnInitialEntry=SCXMLNode.class.getDeclaredMethod("getOnInitialEntry");
			getDoneData=SCXMLNode.class.getDeclaredMethod("getDoneData");
			getDatamodel=SCXMLNode.class.getDeclaredMethod("getDatamodel");
			getNamespace=SCXMLNode.class.getDeclaredMethod("getNamespace");			
			getExe=SCXMLEdge.class.getDeclaredMethod("getExe");			
			getCondition=SCXMLEdge.class.getDeclaredMethod("getCondition");			
			getEvent=SCXMLEdge.class.getDeclaredMethod("getEvent");			
			getNodeComments=SCXMLNode.class.getDeclaredMethod("getComments");
			getEdgeComments=SCXMLEdge.class.getDeclaredMethod("getComments");

			getSRCDoc=SCXMLNode.class.getDeclaredMethod("getSRCDoc");
			getIDDoc=SCXMLNode.class.getDeclaredMethod("getIDDoc");
			getNameDoc=SCXMLNode.class.getDeclaredMethod("getNameDoc");
			getOnEntryDoc=SCXMLNode.class.getDeclaredMethod("getOnEntryDoc");
			getOnExitDoc=SCXMLNode.class.getDeclaredMethod("getOnExitDoc");
			getOnInitialEntryDoc=SCXMLNode.class.getDeclaredMethod("getOnInitialEntryDoc");
			getDoneDataDoc=SCXMLNode.class.getDeclaredMethod("getDoneDataDoc");
			getDatamodelDoc=SCXMLNode.class.getDeclaredMethod("getDatamodelDoc");
			getNamespaceDoc=SCXMLNode.class.getDeclaredMethod("getNamespaceDoc");			
			getExeDoc=SCXMLEdge.class.getDeclaredMethod("getExeDoc");			
			getConditionDoc=SCXMLEdge.class.getDeclaredMethod("getConditionDoc");			
			getEventDoc=SCXMLEdge.class.getDeclaredMethod("getEventDoc");			
			getNodeCommentsDoc=SCXMLNode.class.getDeclaredMethod("getCommentsDoc");
			getEdgeCommentsDoc=SCXMLEdge.class.getDeclaredMethod("getCommentsDoc");
			
			getSRCUndoManager=SCXMLNode.class.getDeclaredMethod("getSRCUndoManager");
			getIDUndoManager=SCXMLNode.class.getDeclaredMethod("getIDUndoManager");
			getNameUndoManager=SCXMLNode.class.getDeclaredMethod("getNameUndoManager");
			getOnEntryUndoManager=SCXMLNode.class.getDeclaredMethod("getOnEntryUndoManager");
			getOnExitUndoManager=SCXMLNode.class.getDeclaredMethod("getOnExitUndoManager");
			getOnInitialEntryUndoManager=SCXMLNode.class.getDeclaredMethod("getOnInitialEntryUndoManager");
			getDoneDataUndoManager=SCXMLNode.class.getDeclaredMethod("getDoneDataUndoManager");
			getDatamodelUndoManager=SCXMLNode.class.getDeclaredMethod("getDatamodelUndoManager");
			getNamespaceUndoManager=SCXMLNode.class.getDeclaredMethod("getNamespaceUndoManager");
			getExeUndoManager=SCXMLEdge.class.getDeclaredMethod("getExeUndoManager");			
			getConditionUndoManager=SCXMLEdge.class.getDeclaredMethod("getConditionUndoManager");			
			getEventUndoManager=SCXMLEdge.class.getDeclaredMethod("getEventUndoManager");			
			getNodeCommentsUndoManager=SCXMLNode.class.getDeclaredMethod("getCommentsUndoManager");
			getEdgeCommentsUndoManager=SCXMLEdge.class.getDeclaredMethod("getCommentsUndoManager");
			//setters
			setSRC=SCXMLNode.class.getDeclaredMethod("setOutsourcedLocation",String.class);
			setID=SCXMLNode.class.getDeclaredMethod("setID",String.class);
			setName=SCXMLNode.class.getDeclaredMethod("setName",String.class);
			setOnEntry=SCXMLNode.class.getDeclaredMethod("setOnEntry",String.class);
			setOnExit=SCXMLNode.class.getDeclaredMethod("setOnExit",String.class);
			setOnInitialEntry=SCXMLNode.class.getDeclaredMethod("setOnInitialEntry",String.class);
			setDoneData=SCXMLNode.class.getDeclaredMethod("setDoneData",String.class);
			setDatamodel=SCXMLNode.class.getDeclaredMethod("setDatamodel",String.class);
			setNamespace=SCXMLNode.class.getDeclaredMethod("setNamespace",String.class);			
			setExe=SCXMLEdge.class.getDeclaredMethod("setExe",String.class);			
			setCondition=SCXMLEdge.class.getDeclaredMethod("setCondition",String.class);			
			setEvent=SCXMLEdge.class.getDeclaredMethod("setEvent",String.class);			
			setNodeComments=SCXMLNode.class.getDeclaredMethod("setComments",String.class);
			setEdgeComments=SCXMLEdge.class.getDeclaredMethod("setComments",String.class);

			setSRCDoc=SCXMLNode.class.getDeclaredMethod("setSRCDoc",Document.class);
			setIDDoc=SCXMLNode.class.getDeclaredMethod("setIDDoc",Document.class);
			setNameDoc=SCXMLNode.class.getDeclaredMethod("setNameDoc",Document.class);
			setOnEntryDoc=SCXMLNode.class.getDeclaredMethod("setOnEntryDoc",Document.class);
			setOnExitDoc=SCXMLNode.class.getDeclaredMethod("setOnExitDoc",Document.class);
			setOnInitialEntryDoc=SCXMLNode.class.getDeclaredMethod("setOnInitialEntryDoc",Document.class);
			setDoneDataDoc=SCXMLNode.class.getDeclaredMethod("setDoneDataDoc",Document.class);
			setDatamodelDoc=SCXMLNode.class.getDeclaredMethod("setDatamodelDoc",Document.class);
			setNamespaceDoc=SCXMLNode.class.getDeclaredMethod("setNamespaceDoc",Document.class);			
			setExeDoc=SCXMLEdge.class.getDeclaredMethod("setExeDoc",Document.class);			
			setConditionDoc=SCXMLEdge.class.getDeclaredMethod("setConditionDoc",Document.class);			
			setEventDoc=SCXMLEdge.class.getDeclaredMethod("setEventDoc",Document.class);			
			setNodeCommentsDoc=SCXMLNode.class.getDeclaredMethod("setCommentsDoc",Document.class);
			setEdgeCommentsDoc=SCXMLEdge.class.getDeclaredMethod("setCommentsDoc",Document.class);

			setSRCUndoManager=SCXMLNode.class.getDeclaredMethod("setSRCUndoManager",MyUndoManager.class);
			setIDUndoManager=SCXMLNode.class.getDeclaredMethod("setIDUndoManager",MyUndoManager.class);
			setNameUndoManager=SCXMLNode.class.getDeclaredMethod("setNameUndoManager",MyUndoManager.class);
			setOnEntryUndoManager=SCXMLNode.class.getDeclaredMethod("setOnEntryUndoManager",MyUndoManager.class);
			setOnExitUndoManager=SCXMLNode.class.getDeclaredMethod("setOnExitUndoManager",MyUndoManager.class);
			setOnInitialEntryUndoManager=SCXMLNode.class.getDeclaredMethod("setOnInitialEntryUndoManager",MyUndoManager.class);
			setDoneDataUndoManager=SCXMLNode.class.getDeclaredMethod("setDoneDataUndoManager",MyUndoManager.class);
			setDatamodelUndoManager=SCXMLNode.class.getDeclaredMethod("setDatamodelUndoManager",MyUndoManager.class);
			setNamespaceUndoManager=SCXMLNode.class.getDeclaredMethod("setNamespaceUndoManager",MyUndoManager.class);			
			setExeUndoManager=SCXMLEdge.class.getDeclaredMethod("setExeUndoManager",MyUndoManager.class);			
			setConditionUndoManager=SCXMLEdge.class.getDeclaredMethod("setConditionUndoManager",MyUndoManager.class);			
			setEventUndoManager=SCXMLEdge.class.getDeclaredMethod("setEventUndoManager",MyUndoManager.class);
			setNodeCommentsUndoManager=SCXMLNode.class.getDeclaredMethod("setCommentsUndoManager",MyUndoManager.class);
			setEdgeCommentsUndoManager=SCXMLEdge.class.getDeclaredMethod("setCommentsUndoManager",MyUndoManager.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	public static void addStateOfNodeInCurrentEdit(mxCell cell,mxIGraphModel model) {
		if ((cell!=null) && (cell.getValue()!=null)) {
			SCXMLNode node=(SCXMLNode) cell.getValue();	
			try {
				model.addChangeToCurrentEdit(instance.new SCXMLParallelStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLClusterStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLInitialStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLFinalStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLOrderOutgoingEdgesStateProperty(cell));
				model.addChangeToCurrentEdit(instance.new SCXMLHistoryStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLOutsourcingStateProperty(node));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getID,getIDDoc,getIDUndoManager,setID,setIDDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getName,getNameDoc,getNameUndoManager,setName,setNameDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getOnEntry,getOnEntryDoc,getOnEntryUndoManager,setOnEntry,setOnEntryDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getOnExit,getOnExitDoc,getOnExitUndoManager,setOnExit,setOnExitDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getOnInitialEntry,getOnInitialEntryDoc,getOnInitialEntryUndoManager,setOnInitialEntry,setOnInitialEntryDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getDoneData,getDoneDataDoc,getDoneDataUndoManager,setDoneData,setDoneDataDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getDatamodel,getDatamodelDoc,getDatamodelUndoManager,setDatamodel,setDatamodelDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getNamespace,getNamespaceDoc,getNamespaceUndoManager,setNamespace,setNamespaceDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLNode>(node,getNodeComments,getNodeCommentsDoc,getNodeCommentsUndoManager,setNodeComments,setNodeCommentsDoc));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void addStateOfEdgeInCurrentEdit(mxCell cell,mxIGraphModel model) {
		if ((cell!=null) && (cell.getValue()!=null)) {
			SCXMLEdge edge=(SCXMLEdge) cell.getValue();
			try {
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLEdge>(edge,getEvent,getEventDoc,getEventUndoManager,setEvent,setEventDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLEdge>(edge,getCondition,getConditionDoc,getConditionUndoManager,setCondition,setConditionDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLEdge>(edge,getExe,getExeDoc,getExeUndoManager,setExe,setExeDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLGenericTextProperty<SCXMLEdge>(edge,getEdgeComments,getEdgeCommentsDoc,getEdgeCommentsUndoManager,setEdgeComments,setEdgeCommentsDoc));
				model.addChangeToCurrentEdit(instance.new SCXMLWithTargetEdgeProperty(edge));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
