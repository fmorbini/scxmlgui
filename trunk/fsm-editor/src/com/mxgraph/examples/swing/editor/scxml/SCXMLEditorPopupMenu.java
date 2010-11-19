package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.OutSource;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.DoLayoutAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditEdgeOrderAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditNodeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.OpenAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.RemoveCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsCluster;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsFinal;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsHistory;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsInitial;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsOutsourced;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsParallel;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ToggleDisplayOutsourcedContentInNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ToggleWithTargetAction;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxCellState;

public class SCXMLEditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public SCXMLEditorPopupMenu(SCXMLGraphEditor editor,Point mousePt, Point graphPt, Point screenCoord)
	{
		SCXMLGraphComponent gc = editor.getGraphComponent();
		mxCell c=(mxCell) gc.getCellAt(graphPt.x, graphPt.y);
		SCXMLGraph graph = gc.getGraph();
		mxIGraphModel model = graph.getModel();
		
		boolean inOutsourcedNode=false;
		// if the cell is not editable set it back to null so the menu doesn't allow editing.
		inOutsourcedNode=(c!=null) && !graph.isCellEditable(c);
		
		Point unscaledGraphPoint=gc.unscaledGraphCoordinates(graphPt);

		// edit node/transition (change text accordingly to type of element under cursor)
		if (c!=null) {
			if (c.isEdge()) {
				SCXMLNode source=(SCXMLNode) c.getSource().getValue();				
				if (!inOutsourcedNode) {
					// for an edge, find out if the pointer is on a control point, or not.
					// if on a control point find out if it's the beginning or end of the endge.
					// -add control point if not on a control point
					// -remove control point if on one that is neither the beginning nor the end.
					if (!((source!=null) && (source.isHistoryNode()))) {
						add(editor.bind(mxResources.get("editEdge"), new EditEdgeAction(c,screenCoord)));
						addSeparator();
					}
					// if the edge is not a loop you can add/remove corners
					if (c.getSource()!=c.getTarget()) {
						mxCellState cellState=graph.getView().getState(c);
						int index;
						int lastIndex=cellState.getAbsolutePointCount()-1;
						if ((index=cellState.getIndexOfEdgePointAt(graphPt.x, graphPt.y,gc.getTolerance()))==-1) {
							int indexOfNewPoint=cellState.getIndexOfNewPoint(graphPt.x, graphPt.y,gc.getTolerance())-1;
							if (indexOfNewPoint>=0)
								add(editor.bind(mxResources.get("addCorner"), new AddCornerToEdgeAction(c,unscaledGraphPoint,graphPt,indexOfNewPoint)));
						} else if (index>0 && index<lastIndex)
							add(editor.bind(mxResources.get("removeCorner"), new RemoveCornerToEdgeAction(c,index-1)));
					} else {
						JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("toggleWithTarget"), new ToggleWithTargetAction(c)));
						menuItem.setSelected(((SCXMLEdge)(c.getValue())).isCycleWithTarget());
						add(menuItem);
					}
				}
			} else if (c.isVertex()) {
				boolean nodeIsOutsourced=((SCXMLNode)(c.getValue())).isOutsourcedNode();
				boolean nodeIsFake=((SCXMLNode)(c.getValue())).getFake();
				mxICell parent = c.getParent();
				boolean isHistoryNode=((SCXMLNode)(c.getValue())).isHistoryNode();
				boolean isParallelNode=((SCXMLNode)(c.getValue())).isParallel();
				boolean isFinalNode=((SCXMLNode)(c.getValue())).isFinal();
				boolean isClusterNode=((SCXMLNode)(c.getValue())).isClusterNode();
				if (!inOutsourcedNode) {
					// add node in case the cell under the pointer is a swimlane
					boolean addNodeEnabled=graph.isSwimlane(c) && (editor.getCurrentFileIO()!=null);
					add(editor.bind(mxResources.get("addNode"), new AddAction(mousePt,c))).setEnabled(addNodeEnabled);
					addSeparator();
					mxCell root=SCXMLImportExport.followUniqueDescendantLineTillSCXMLValueIsFound(model);
					add(editor.bind(mxResources.get("editNode"), new EditNodeAction(c,root,screenCoord)));
					if (c!=root) {
						if (!isHistoryNode) {
							if (!nodeIsFake) add(editor.bind(mxResources.get("editOutgoingEdgeOrder"), new EditEdgeOrderAction(c,screenCoord))).setEnabled(graph.getAllOutgoingEdges(c).length>1);
							JMenuItem menuItem2 = new JMenuItem(editor.bind(mxResources.get("editOutsourcedNode"), new SetNodeAsOutsourced(c,screenCoord)));
							menuItem2.setEnabled(!((SCXMLNode)(c.getValue())).isClusterNode() || (c.getChildCount()==0) || ((SCXMLNode)(c.getValue())).isOutsourcedNode());
							add(menuItem2);
						}
						if (!nodeIsFake) {
							if (!isHistoryNode) addSeparator();
							JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsInitialNode"), new SetNodeAsInitial(c)));
							menuItem.setSelected(((SCXMLNode)(c.getValue())).isInitial());
							add(menuItem);
							if (!isHistoryNode) {
								menuItem = new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsFinalNode"), new SetNodeAsFinal(c)));
								menuItem.setSelected(((SCXMLNode)(c.getValue())).isFinal());
								add(menuItem);
								menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsClusterNode"), new SetNodeAsCluster(c)));
								menuItem.setSelected(((SCXMLNode)(c.getValue())).isClusterNode());
								menuItem.setEnabled(!((SCXMLNode)(c.getValue())).isOutsourcedNode());
								add(menuItem);
								menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsParallelNode"), new SetNodeAsParallel(c)));
								menuItem.setSelected(((SCXMLNode)(c.getValue())).isParallel());
								add(menuItem);
							}
							if ((parent!=root) && ((SCXMLNode)parent.getValue()).isClusterNode()) {
								if (!isParallelNode && !isClusterNode && !isFinalNode) {
									addSeparator();
									menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsDeepHistoryNode"), new SetNodeAsHistory(c,true)));
									menuItem.setSelected(((SCXMLNode)(c.getValue())).isDeepHistory());
									add(menuItem);
									menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsShallowHistoryNode"), new SetNodeAsHistory(c,false)));
									menuItem.setSelected(((SCXMLNode)(c.getValue())).isShallowHistory());
									add(menuItem);
								}
							}
						}
					}
				}
				if (nodeIsOutsourced) {
					JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("toggleViewOutsourcedContent"), new ToggleDisplayOutsourcedContentInNode(c)));
					menuItem.setSelected(c.getChildCount()>0);
					add(menuItem);
					if (c.getChildCount()>0) {
						JMenuItem itemMenu = new JMenuItem(editor.bind(mxResources.get("refreshViewOutsourcedContent"), new ToggleDisplayOutsourcedContentInNode(c,true)));
						add(itemMenu);
					}

					SCXMLNode value=((SCXMLNode)(c.getValue()));
					OutSource src=value.getSRC();
					File f=editor.getThisFileInCurrentDirectory(src.getLocation());					
					Action itemAction = editor.bind(mxResources.get("openInNewWindow")+" "+f.getName(), new OpenAction(f,true));
					JMenuItem itemMenu= new JMenuItem(itemAction);
					add(itemMenu);
				}
				addSeparator();
				add(editor.bind(mxResources.get("doRecursiveLayout"), new DoLayoutAction(graph,c,-1)));
				add(editor.bind(mxResources.get("doSimpleLayout"), new DoLayoutAction(graph,c,0)));
			}
		} else {
			add(editor.bind(mxResources.get("editNodeEdge"), null)).setEnabled(false);
		}
	}
}
