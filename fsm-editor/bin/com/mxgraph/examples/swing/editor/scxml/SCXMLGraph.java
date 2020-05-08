package com.mxgraph.examples.swing.editor.scxml;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.mxgraph.examples.config.SCXMLConstraints.RestrictedState;
import com.mxgraph.examples.config.SCXMLConstraints.RestrictedState.PossibleEvent;
import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.SCXMLGraphEditor.EditorStatus;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.StringUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge.
 */
public class SCXMLGraph extends mxGraph
{
	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();
	private SCXMLGraphEditor editor;
	private HashSet<Object> immovable=new HashSet<Object>();
	private HashSet<Object> undeletable=new HashSet<Object>();
	private HashSet<Object> uneditable=new HashSet<Object>();
	private HashSet<mxCell> outsourced=new HashSet<mxCell>();
	private HashMap<mxCell,HashSet<mxCell>> original2clones=new HashMap<mxCell, HashSet<mxCell>>();
	private HashMap<String,SCXMLImportExport> ourced=new HashMap<String, SCXMLImportExport>();

	public void addToOutsourced(mxCell n) {
		assert(((SCXMLNode)n.getValue()).isOutsourcedNode());
		outsourced.add(n);
	}
	public void removeFromOutsourced(mxCell n) {
		outsourced.remove(n);
	}
	public HashSet<mxCell> getOutsourcedNodes() {
		return outsourced;
	}
	public HashMap<mxCell,HashSet<mxCell>> getOriginal2Clones() {
		return original2clones;
	}
	public void clearOutsourcedIndex() {
		outsourced.clear();
	}
	
	public void setCellAsMovable(Object cell,Boolean m) {
		if (m) immovable.remove(cell);
		else immovable.add(cell);
	}
	public void setCellAsDeletable(Object cell,Boolean d) {
		if (d) undeletable.remove(cell);
		else undeletable.add(cell);
	}
	public void setCellAsEditable(Object cell,boolean e) {
		if (e) uneditable.remove(cell);
		else uneditable.add(cell);
	}
	public void setCellAsConnectable(Object cell,boolean c) {
		if (cell instanceof mxCell) ((mxCell) cell).setConnectable(c);
	}
	@Override
	public mxRectangle getPaintBounds(Object[] cells)
	{
		return getBoundsForCells(cells, false, true, true);
	}
	@Override
	public boolean isCellFoldable(Object cell, boolean collapse)
	{
		return isSwimlane(cell);
	}
	@Override
	public boolean isValidDropTarget(Object cell, Object[] cells)
	{
		return (cell != null) && isSwimlane(cell);
	}
	@Override
	public String validateCell(Object cell, Hashtable<Object, Object> context)
	{
		EditorStatus status=getEditor().getStatus();
		SCXMLGraphComponent gc = getEditor().getGraphComponent();		
		String warnings="";
		if (isCellEditable(cell) && (status==EditorStatus.EDITING)) {
			if (model.isVertex(cell)) {			
				mxCell node=(mxCell)cell;

				mxICell parent=node.getParent();
				if ((parent!=null) && (parent.getValue() instanceof SCXMLNode)) {
					mxCellState stateChild = view.getState(cell);
					//mxCellState stateParent = view.getState(parent);
					//System.out.println(node+" "+parent+" "+stateChild+" "+stateParent);
					Object container=gc.getCellAt((int)stateChild.getCenterX(), (int)stateChild.getCenterY(),true,null,cell,true);
					//System.out.println(container);
					if (container!=parent) warnings+=node+" is not graphically contained in its parent "+parent+".\n";
				}

				SCXMLNode nodeValue = (SCXMLNode)node.getValue();
				String nodeValueID=nodeValue.getID();
				if (nodeValueID.matches(".*[\\s]+.*")) warnings+="node name contains spaces.\n";
				// check if the executable content is parsable xml
				String error=XMLUtils.isParsableXMLString(nodeValue.getOnEntry());
				if (error!=null) warnings+="OnEntry content of node "+nodeValueID+" caused a parser error: "+error+"\n";
				error=XMLUtils.isParsableXMLString(nodeValue.getOnExit());
				if (error!=null) warnings+="OnExit content of node "+nodeValueID+" caused a parser error: "+error+"\n";
				error=XMLUtils.isParsableXMLString(nodeValue.getOnInitialEntry());
				if (error!=null) warnings+="On initial content of node "+nodeValueID+" caused a parser error: "+error+"\n";
				error=XMLUtils.isParsableXMLString(nodeValue.getDoneData());
				if (error!=null) warnings+="Done data of node "+nodeValueID+" caused a parser error: "+error+"\n";
				error=XMLUtils.isParsableXMLString(nodeValue.getDatamodel());
				if (error!=null) warnings+="Data model of node "+nodeValueID+" caused a parser error: "+error+"\n";
				if (!nodeValue.isOutsourcedNode()) {
					// check if the namespace has been included
					String SCXMLid=nodeValueID;
					int pos=SCXMLid.indexOf(':');
					boolean namespaceGood=true;
					String namespace="";
					if (pos>0) {
						namespaceGood=false;
						namespace=SCXMLid.substring(0,pos);
						mxIGraphModel model = getModel();
						mxCell root = SCXMLImportExport.followUniqueDescendantLineTillSCXMLValueIsFound(model);
						SCXMLNode rootValue=(SCXMLNode) root.getValue();
						String[] namespaces=rootValue.getNamespace().split("\n");

						Pattern p = Pattern.compile("^[\\s]*xmlns:([^\\s=:]+)[\\s]*=.*$");
						for(String ns:namespaces) {
							Matcher m = p.matcher(ns);
							if (m.matches() && (m.groupCount()==1)) {
								ns=m.group(1);
								if (namespace.equals(ns)) {
									namespaceGood=true;
									break;
								}
							}
						}
					}
					if (!namespaceGood) warnings+="Namespace '"+namespace+"' is used but not defined.\n";
				}
				if (!StringUtils.isEmptyString(nodeValueID)) {
					SCXMLNode parentValue=null;
					if (parent==null || ((parentValue=(SCXMLNode)parent.getValue())==null) || !parentValue.getFake() || !nodeValueID.equals(SCXMLNode.ROOTID)) {
						if (gc.isSCXMLNodeAlreadyThere(nodeValue)) warnings+="duplicated node name: "+nodeValueID+"\n";
						else gc.addSCXMLNode(nodeValue,node);
					}
				}
				//Restricted node should have at least one related event
				if (nodeValue.isRestricted()) {
					List<RestrictedState> restrictionsOnNode = nodeValue.getRestrictedStates();
					Object[] allOutgoingEdges = editor.getGraphComponent().getGraph().getAllOutgoingEdges(node);
			    	List<String> existingEventsOnSourceNode = new LinkedList<String>();
			    	for(Object object : allOutgoingEdges){
			    		SCXMLEdge tempEdge = (SCXMLEdge)((mxCell)object).getValue();
			    		existingEventsOnSourceNode.add(tempEdge.getEvent());
			    	}
			    	for(RestrictedState restrictedState : restrictionsOnNode){
			    		List<String> possibleEventNames = new LinkedList<String>();
			    		for(PossibleEvent possibleEvent : restrictedState.getPossibleEvent()){
			    			possibleEventNames.add(possibleEvent.getName());
			    		}
			    		possibleEventNames.retainAll(existingEventsOnSourceNode);
			    		if (possibleEventNames.isEmpty()) {
							warnings += "There is no event for restriction [" + restrictedState.getName() + "] on node [" + nodeValue.getID() + "]\n";
						}
			    	}
				}
				if (nodeValue.isClusterNode()) {
					int numInitialChildren=0;
					int numOutGoingTransitions=0;
					int numChildren=node.getChildCount();			
					for (int i=0;i<numChildren;i++) {
						mxCell c=(mxCell) node.getChildAt(i);
						if (c.isVertex()) {
							SCXMLNode cValue = (SCXMLNode)c.getValue();
							if (cValue.isInitial()) {
								numInitialChildren++;
							}
							if ((numInitialChildren>0) && nodeValue.isParallel()) warnings+="Parallel nodes ("+nodeValueID+") don't support a child marked as intiial.\n";
							if (numInitialChildren>1) warnings+="More than 1 children of "+nodeValueID+" is marked as initial.\n";
						} else {
							if (nodeValue.isHistoryNode()) {
								if (c.getSource().equals(node)) {
									numOutGoingTransitions++;
									if (numOutGoingTransitions>1) warnings+="History node '"+nodeValueID+"' has more than 1 outgoing transition.\n";
									if (!StringUtils.isEmptyString(((SCXMLEdge)c.getValue()).getCondition()) ||
											!StringUtils.isEmptyString(((SCXMLEdge)c.getValue()).getEvent())) {
										warnings+="Outgoing transition of history node has non null event or condition.\n";
									}
								}
							}
						}
					}
				}
			} else if (model.isEdge(cell)) {
				// check that source and target have non null SCXML ids.
				mxCell edge=(mxCell)cell;
				SCXMLEdge edgeValue=(SCXMLEdge) edge.getValue();
				if ((edge.getSource()==null) || (edge.getTarget()==null)) warnings+="unconnected edge.\n";
				String error=XMLUtils.isParsableXMLString(edgeValue.getExe());
				SCXMLNode source=(SCXMLNode)edge.getSource().getValue();
				SCXMLNode target=(SCXMLNode)edge.getTarget().getValue();
				if (error!=null) warnings+="Executable content of one edge from "+source.getID()+" to "+target.getID()+" caused a parser error: "+error+"\n";
				if (StringUtils.isEmptyString(source.getID()) || StringUtils.isEmptyString(target.getID())) {
					warnings+="target and source of a transition must have not empty name.\n";
				}
				Object lca = model.getNearestCommonAncestor(edge.getSource(), edge.getTarget());
				if (lca!=null && lca instanceof mxCell) {
					SCXMLNode scxmlLCA = (SCXMLNode) ((mxCell)lca).getValue();
					if (scxmlLCA.isParallel()) warnings+=source.getID()+" and "+target.getID()+" are (descendats of) siblings of a parallel node ("+scxmlLCA.getID()+").\n";
				}

				String edgeEventName = edgeValue.getEvent();
				//check that edge event is allowed by the restriction on the source node
				if (source.isRestricted()){
					boolean isEventPossible = false;
					for(PossibleEvent possibleEvent: source.getPossibleEvents()){
						if (possibleEvent.getName().equals(edgeEventName)) {
							isEventPossible = true;
						}
					}
					if (!isEventPossible) {
						warnings += "Invalid event from " + source.getID() + " to " + target.getID() + "!\n";
					}
				}
				
				//check if the source node is final
				if (source.isFinal()) {
					warnings += "Outgoing edge from a final node!\n";
				}
			}
		}
		if (StringUtils.isEmptyString(warnings)) return null;
		else return warnings;
	}
	@Override
	public boolean isCellMovable(Object cell)
	{			
		return isCellsMovable() && !isCellLocked(cell) && !immovable.contains(cell);
	}
	@Override
	public boolean isCellDeletable(Object cell)
	{			
		return isCellsDeletable() && !undeletable.contains(cell);
	}
	@Override
	public boolean isCellEditable(Object cell) {
		return isCellsEditable() && !uneditable.contains(cell);
	}
	@Override
	public RootStrength vertexShouldBeRoot(Object cell, Object parent,boolean invert) {
		if (cell instanceof mxCell) {
			mxCell c=(mxCell)cell;
			Object v=c.getValue();
			if ((v!=null) && (v instanceof SCXMLNode)) {
				return new RootStrength((invert)?((SCXMLNode)v).isFinal():((SCXMLNode)v).isInitial(),0);
			} else return super.vertexShouldBeRoot(cell, parent, invert);
		} else return super.vertexShouldBeRoot(cell, parent, invert);
	}
	@Override
	public Object insertEdge(Object parent, String id, Object value,Object source, Object target)
	{
		//System.out.println("insert edge: parent:"+parent+" value:"+value+" source:"+source+" target:"+target);
		try {
			int size=getAllOutgoingEdges(source).length;
			if (value==null) {
				value=getEditor().getCurrentFileIO().buildEdgeValue();
			} else if (!(value instanceof SCXMLEdge)) {
				System.out.println("WARNING: non NULL and non SCXMLEdge value passed for new edge (insertEdge in SCXMLGraph)");
				value=getEditor().getCurrentFileIO().buildEdgeValue();
			}
			updateConnectionOfSCXMLEdge((SCXMLEdge) value,source,target,null);
			if (((SCXMLEdge)value).getOrder()==null) ((SCXMLEdge)value).setOrder(size);
			Object edge = insertEdge(parent, ((SCXMLEdge)value).getInternalID(), value, source, target, "");
			setCellStyle(((SCXMLEdge) value).getStyle((mxCell) edge),edge);
			return edge;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void askToUseThisEdgeValue(Object clone, Object otherEdgeValue) {
		int answer = JOptionPane.showConfirmDialog(editor, mxResources.get("createAsNewTargetForMultitarget"),mxResources.get("edgeCreationOption"),JOptionPane.YES_NO_OPTION);
		if (answer==JOptionPane.YES_OPTION) {
			model.setValue(clone, otherEdgeValue);
		} else {
			SCXMLEdge value=(SCXMLEdge) model.getValue(clone);
			value.getSCXMLTargets().clear();
		}
	}
	@Override
	public Object[] cloneCells(Object[] cells, boolean allowInvalidEdges,Map<Object,Object> mapping)
	{
		Object[] clones = null;

		if (cells != null)
		{
			Collection<Object> tmp = new LinkedHashSet<Object>(cells.length);
			tmp.addAll(Arrays.asList(cells));

			if (!tmp.isEmpty())
			{
				double scale = view.getScale();
				mxPoint trans = view.getTranslate();
				clones = model.cloneCells(cells, true,mapping);
				
				for (int i = 0; i < cells.length; i++)
				{
					Object newValue = ((SCXMLImportExport)getEditor().getCurrentFileIO()).cloneValue(((mxCell)clones[i]).getValue());
					((mxCell)clones[i]).setValue(newValue);
					if (!allowInvalidEdges
							&& model.isEdge(clones[i])
							&& getEdgeValidationError(clones[i], model
									.getTerminal(clones[i], true), model
									.getTerminal(clones[i], false)) != null)
					{
						clones[i] = null;
					}
					else
					{
						mxGeometry g = model.getGeometry(clones[i]);

						if (g != null)
						{
							mxCellState state = view.getState(cells[i]);
							mxCellState pstate = view.getState(model
									.getParent(cells[i]));

							if (state != null && pstate != null)
							{
								double dx = pstate.getOrigin().getX();
								double dy = pstate.getOrigin().getY();

								if (model.isEdge(clones[i]))
								{
									// Checks if the source is cloned or sets the terminal point
									Object src = model.getTerminal(cells[i],
											true);

									while (src != null && !tmp.contains(src))
									{
										src = model.getParent(src);
									}

									if (src == null)
									{
										mxPoint pt = state.getAbsolutePoint(0);
										g.setTerminalPoint(new mxPoint(pt
												.getX()
												/ scale - trans.getX(), pt
												.getY()
												/ scale - trans.getY()), true);
									}

									// Checks if the target is cloned or sets the terminal point
									Object trg = model.getTerminal(cells[i],
											false);

									while (trg != null && !tmp.contains(trg))
									{
										trg = model.getParent(trg);
									}

									if (trg == null)
									{
										mxPoint pt = state
												.getAbsolutePoint(state
														.getAbsolutePointCount() - 1);
										g.setTerminalPoint(new mxPoint(pt
												.getX()
												/ scale - trans.getX(), pt
												.getY()
												/ scale - trans.getY()), false);
									}

									// Translates the control points
									List<mxPoint> points = g.getPoints();

									if (points != null)
									{
										Iterator<mxPoint> it = points
												.iterator();

										while (it.hasNext())
										{
											mxPoint pt = it.next();
											pt.setX(pt.getX() + dx);
											pt.setY(pt.getY() + dy);
										}
									}
								}
								else
								{
									g.setX(g.getX() + dx);
									g.setY(g.getY() + dy);
								}
							}
						}
					}
				}
			}
			else
			{
				clones = new Object[] {};
			}
		}

		return clones;
	}
	@Override
	public void cellsRemoved(Object[] cells)
	{
		if (cells != null && cells.length > 0)
		{
			double scale = view.getScale();
			mxPoint tr = view.getTranslate();

			model.beginUpdate();
			try
			{
				Collection<Object> cellSet = new HashSet<Object>();
				cellSet.addAll(Arrays.asList(cells));
				for (int i = 0; i < cells.length; i++)
				{
					mxCell cell=(mxCell) cells[i];
					// Disconnects edges which are not in cells
					Object[] edges = getConnections(cell);

					for (int j = 0; j < edges.length; j++)
					{
						if (!cellSet.contains(edges[j]))
						{
							mxGeometry geo = model.getGeometry(edges[j]);

							if (geo != null)
							{
								mxCellState state = view.getState(edges[j]);

								if (state != null)
								{
									geo = (mxGeometry) geo.clone();
									boolean source = view.getVisibleTerminal(
											edges[j], true) == cell;
									int n = (source) ? 0 : state
											.getAbsolutePointCount() - 1;
									mxPoint pt = state.getAbsolutePoint(n);

									geo.setTerminalPoint(new mxPoint(pt.getX()
											/ scale - tr.getX(), pt.getY()
											/ scale - tr.getY()), source);
									model.setTerminal(edges[j], null, source);
									model.setGeometry(edges[j], geo);
								}
							}
						}
					}
					model.remove(cell);
					if (cell.isEdge()) {
						// check if this edge has a source with other outgoing edges and
						// the source is not going to be deleted. In that case reorder the
						// remaining outgoing edges closing the potential hole that
						// removing this edge may be causing.
						mxCell source=(mxCell) cell.getSource();						
						if (!cellSet.contains(source) && getAllOutgoingEdges(source).length>0) {
							SCXMLChangeHandler.addStateOfNodeInCurrentEdit(source, model);
							reOrderOutgoingEdges(source);
						}
						// if deleted edge was part of multitarget edge, remove the target pointed by this deleted edge.
						Collection<Object> siblings = getEditor().getGraphComponent().getSiblingsOfCell(cell);
						if (siblings.size()>1) {
							SCXMLEdge edgeValue = (SCXMLEdge) cell.getValue();
							SCXMLNode targetNode=(SCXMLNode) cell.getTarget().getValue();
							assert(edgeValue.getSCXMLTargets().contains(targetNode.getID()));
							edgeValue.getSCXMLTargets().remove(targetNode.getID());
						}
					}
				}
				fireEvent(new mxEventObject(mxEvent.CELLS_REMOVED, "cells",cells));
			}
			finally
			{
				model.endUpdate();
			}
		}
	}
	public void reOrderOutgoingEdges(mxCell source) {
		HashMap<Integer,ArrayList<SCXMLEdge>> pos=new HashMap<Integer, ArrayList<SCXMLEdge>>();
		int min=0,max=0;
		for(Object s:getAllOutgoingEdges(source)) {
			mxCell c=(mxCell) s;
			SCXMLEdge v = (SCXMLEdge) c.getValue();
			int o=v.getOrder();
			ArrayList<SCXMLEdge> l = pos.get(o);
			if (l==null) pos.put(o, l=new ArrayList<SCXMLEdge>());
			l.add(v);
			if (o<min) min=o;
			if (o>max) max=o;			
		}
		int neworder=0;
		for(int i=min;i<=max;i++) {
			if (pos.containsKey(i)) {
				for (SCXMLEdge e:pos.get(i)) {
					e.setOrder(neworder++);
				}
			}
		}
	}
	@Override
	public Object connectCell(Object edge, Object terminal, boolean source)
	{
		//System.out.println("connect cell: edge:"+edge+" terminal:"+terminal+" source:"+source);
		model.beginUpdate();
		try
		{
			SCXMLChangeHandler.addStateOfEdgeInCurrentEdit((mxCell) edge, model);
			SCXMLGraphComponent gc = (SCXMLGraphComponent) getEditor().getGraphComponent();
			Collection<Object> siblings = gc.getSiblingsOfCell(edge);
			if (siblings.size()>1) {
				if (source) {
					JOptionPane.showMessageDialog(editor,
							"Detaching edge from multitarget edge.",
							mxResources.get("warning"),
							JOptionPane.WARNING_MESSAGE);
					SCXMLEdge oldValue=(SCXMLEdge) ((mxCell)edge).getValue();
					SCXMLEdge newValue = (SCXMLEdge) ((SCXMLImportExport)getEditor().getCurrentFileIO()).cloneValue(oldValue);
					((mxCell)edge).setValue(newValue);
					SCXMLNode targetNodeValue=(SCXMLNode) model.getValue(model.getTerminal(edge, false));
					oldValue.getSCXMLTargets().remove(targetNodeValue.getID());
					ArrayList<String> targets = newValue.getSCXMLTargets();
					targets.clear();
					targets.add(targetNodeValue.getID());
				}
			}
			// connect edge to new terminal (source or target)
			Object previous = model.getTerminal(edge, source);			
			cellConnected(edge, terminal, source);
			fireEvent(new mxEventObject(mxEvent.CONNECT_CELL, "edge", edge,
					"terminal", terminal, "source", source, "previous",
					previous));
			// update the order of edges in case we move the source of an edge from one node to another.
			if (source) {
				SCXMLChangeHandler.addStateOfNodeInCurrentEdit((mxCell) previous, model);
				reOrderOutgoingEdges((mxCell) previous);
				SCXMLChangeHandler.addStateOfNodeInCurrentEdit((mxCell) terminal, model);
				reOrderOutgoingEdges((mxCell) terminal);
			}
			SCXMLEdge edgeValue=(SCXMLEdge) ((mxCell)edge).getValue();
			// Synchronize the source and targets stored in the value of the modified edge with the graphical properties here updated.
			updateConnectionOfSCXMLEdge(edgeValue,(source)?terminal:null,(source)?null:terminal,previous);
			// update edge style
			setCellStyle(edgeValue.getStyle((mxCell) edge),edge);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			model.endUpdate();
		}

		return edge;
	}
	private void updateConnectionOfSCXMLEdge(SCXMLEdge value, Object source, Object target, Object previous) throws Exception {
		//System.out.println("update connectiopn: value:"+value+" source:"+source+" target:"+target+" previous:"+previous);
		String sourceID=null,targetID=null;
		if (source!=null) {
			sourceID=((SCXMLNode)((mxCell)source).getValue()).getID();
			value.setSCXMLSource(sourceID);
		}
		if (target!=null) {
			targetID=((SCXMLNode)((mxCell)target).getValue()).getID();			
			if (previous==null) {
				// add a target to an edge (new edge without any previous target)
				ArrayList<String> targets = value.getSCXMLTargets();
				if (!targets.contains(targetID)) value.getSCXMLTargets().add(targetID);
			} else {
				// update an edge belonging to a multitarget edge 
				String previousTargetID=((SCXMLNode)((mxCell)previous).getValue()).getID();
				if (!value.getSCXMLTargets().contains(previousTargetID)) throw new Exception("updateConnectionOfSCXMLEdge: Error while moving target of edge with multiple targets. Old target not found.");
				value.getSCXMLTargets().remove(previousTargetID);
				value.getSCXMLTargets().add(targetID);
			}
		}
	}
	public void setEditor(SCXMLGraphEditor scxmlGraphEditor) {
		this.editor=scxmlGraphEditor;
	}
	public SCXMLGraphEditor getEditor() {
		return this.editor;
	}
	public mxCell findCellContainingAllOtherCells() {
		
		return null;
	}
	
	@Override
	public String convertValueToString(Object cell)
	{
		Object v = model.getValue(cell);
		if (v instanceof SCXMLNode) {
			SCXMLNode node=((SCXMLNode)v);
			if (!StringUtils.isEmptyString(node.getName()))
				return node.getID()+"["+node.getName()+"]";
			else
				return node.getID();
		} else if (v instanceof SCXMLEdge) {
			SCXMLEdge edge=((SCXMLEdge)v);
			return edge.getEvent();
		} else {
			return "";
		}
	}

	/**
	 * Holds the edge to be used as a template for inserting new edges.
	 */
	protected Object edgeTemplate;

	/**
	 * Custom graph that defines the alternate edge style to be used when
	 * the middle control point of edges is double clicked (flipped).
	 */
	public SCXMLGraph()
	{
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		setAutoSizeCells(true);
		setAllowLoops(true);
	}

	/**
	 * Sets the edge template to be used to inserting edges.
	 */
	public void setEdgeTemplate(Object template)
	{
		edgeTemplate = template;
	}

	/**
	 * Prints out some useful information about the cell in the tooltip.
	 */
	public String getToolTipForCell(Object cell)
	{
		String tip = null;
		if (cell instanceof mxCell) {
			if (((mxCell)cell).isEdge()) {
				tip="<html>";
				SCXMLEdge v=(SCXMLEdge) ((mxCell)cell).getValue();
				tip+="order: "+v.getOrder()+"<br>";
				tip+="event: "+v.getEvent()+"<br>";
				tip+="condition: <pre>"+XMLUtils.escapeStringForXML(v.getCondition())+"</pre><br>";
				tip+="exe: <pre>"+XMLUtils.escapeStringForXML(v.getExe())+"</pre><br>";
				tip += "</html>";
			} else if (((mxCell)cell).isVertex()) {
				SCXMLNode v=(SCXMLNode) ((mxCell)cell).getValue();
				String src=v.getOutsourcedLocation();
				if (!StringUtils.isEmptyString(src)) {
					tip="<html>";
					tip+="src: "+src+"<br>";
					tip+="type: "+v.getSRC().getType()+"<br>";
					tip += "</html>";
				} else {
					String tipBody="";
					if (v.isRestricted()) {
						tipBody += "Restrictions:<br><pre>";
						for(RestrictedState restriction: v.getRestrictedStates()){
							tipBody += restriction.getName() + "<br>";
						}
						tipBody += "</pre><br>";
					}
					if (v.isInitial()) tipBody+="onInitialEntry: <pre>"+XMLUtils.escapeStringForXML(v.getOnInitialEntry())+"</pre><br>";
					String onEntry = v.getOnEntry();
					if ((onEntry!=null) && (!(onEntry.isEmpty()))) {
						tipBody+="onEntry:<br><pre>"+XMLUtils.escapeStringForXML(onEntry)+"</pre><br>";
					}
					String onExit = v.getOnExit();
					if ((onExit!=null) && (!(onExit.isEmpty()))) {
						tipBody+="onExit:<br><pre>"+XMLUtils.escapeStringForXML(onExit)+"</pre><br>";
					}
					if (v.isFinal()) tipBody+="exitData: "+v.getDoneData()+"<br>";
					if (!tipBody.isEmpty()) {
						tip = "<html>";
						tip += tipBody;
						tip += "</html>";
					}
				}
			}
		}
		return tip;
	}
//	public String getToolTipForCell(Object cell)
//	{
//		String tip = "<html>";
//		mxGeometry geo = getModel().getGeometry(cell);
//		mxCellState state = getView().getState(cell);
//
//		if (getModel().isEdge(cell))
//		{
//			tip += "points={";
//
//			if (geo != null)
//			{
//				List<mxPoint> points = geo.getPoints();
//
//				if (points != null)
//				{
//					Iterator<mxPoint> it = points.iterator();
//
//					while (it.hasNext())
//					{
//						mxPoint point = it.next();
//						tip += "[x=" + numberFormat.format(point.getX())
//								+ ",y=" + numberFormat.format(point.getY())
//								+ "],";
//					}
//
//					tip = tip.substring(0, tip.length() - 1);
//				}
//			}
//
//			tip += "}<br>";
//			tip += "absPoints={";
//
//			if (state != null)
//			{
//
//				for (int i = 0; i < state.getAbsolutePointCount(); i++)
//				{
//					mxPoint point = state.getAbsolutePoint(i);
//					tip += "[x=" + numberFormat.format(point.getX())
//							+ ",y=" + numberFormat.format(point.getY())
//							+ "],";
//				}
//
//				tip = tip.substring(0, tip.length() - 1);
//			}
//
//			tip += "}";
//		}
//		else
//		{
//			tip += "geo=[";
//
//			if (geo != null)
//			{
//				tip += "x=" + numberFormat.format(geo.getX()) + ",y="
//						+ numberFormat.format(geo.getY()) + ",width="
//						+ numberFormat.format(geo.getWidth()) + ",height="
//						+ numberFormat.format(geo.getHeight());
//			}
//
//			tip += "]<br>";
//			tip += "state=[";
//
//			if (state != null)
//			{
//				tip += "x=" + numberFormat.format(state.getX()) + ",y="
//						+ numberFormat.format(state.getY()) + ",width="
//						+ numberFormat.format(state.getWidth())
//						+ ",height="
//						+ numberFormat.format(state.getHeight());
//			}
//
//			tip += "]";
//		}
//
//		mxPoint trans = getView().getTranslate();
//
//		tip += "<br>scale=" + numberFormat.format(getView().getScale())
//				+ ", translate=[x=" + numberFormat.format(trans.getX())
//				+ ",y=" + numberFormat.format(trans.getY()) + "]";
//		tip += "</html>";
//
//		return tip;
//	}

	/**
	 * Overrides the method to use the currently selected edge template for
	 * new edges.
	 * 
	 * @param graph
	 * @param parent
	 * @param id
	 * @param value
	 * @param source
	 * @param target
	 * @param style
	 * @return
	 */
	public Object createEdge(Object parent, String id, Object value,
			Object source, Object target, String style)
	{
		if (edgeTemplate != null)
		{
			mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
			edge.setId(id);

			return edge;
		}

		return super.createEdge(parent, id, value, source, target, style);
	}
	
	public void clearUndeletable(){
		undeletable.clear();
	}

}

