package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Point;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import com.mxgraph.examples.swing.editor.EditorActions.HistoryAction;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditDatamodelAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.RemoveCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditNodeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsCluster;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsFinal;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsInitial;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsParallel;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.DoLayoutAction;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class SCXMLEditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public SCXMLEditorPopupMenu(SCXMLGraphEditor editor,Point mousePt, Point graphPt)
	{
		mxGraphComponent gc = editor.getGraphComponent();
		mxCell c=(mxCell) gc.getCellAt(graphPt.x, graphPt.y);
		mxGraph graph=gc.getGraph();
		
		Point unscaledGraphPoint=gc.unscaledGraphCoordinates(graphPt);

		// add node in case the cell under the pointer is a swimlane
		boolean addNodeEnabled=graph.isSwimlane(c) && (editor.getCurrentFileIO()!=null);
		add(editor.bind(mxResources.get("addNode"), new AddAction(mousePt,c))).setEnabled(addNodeEnabled);

		// edit node/transition (change text accordingly to type of element under cursor)
		if (c!=null) {
			if (c.isEdge()) {
				// for an edge, find out if the pointer is on a control point, or not.
				// if on a control point find out if it's the beginning or end of the endge.
				// -add control point if not on a control point
				// -remove control point if on one that is neither the beginning nor the end.
				add(editor.bind(mxResources.get("editEdge"), new EditEdgeAction(c,mousePt)));
				// if the edge is not a loop you can add/remove corners
				if (c.getSource()!=c.getTarget()) {
					mxCellState cellState=graph.getView().getState(c);
					int index;
					int lastIndex=cellState.getAbsolutePointCount()-1;
					if ((index=cellState.getIndexOfEdgePointAt(graphPt.x, graphPt.y,gc.getTolerance()))==-1) {
						int indexOfNewPoint=cellState.getIndexOfNewPoint(graphPt.x, graphPt.y,gc.getTolerance())-1;
						if (indexOfNewPoint>=0)
							add(editor.bind(mxResources.get("addCorner"), new AddCornerToEdgeAction(c,unscaledGraphPoint,indexOfNewPoint)));
					} else if (index>0 && index<lastIndex)
						add(editor.bind(mxResources.get("removeCorner"), new RemoveCornerToEdgeAction(c,index-1)));
				}
			} else if (c.isVertex()) {
				add(editor.bind(mxResources.get("editNode"), new EditNodeAction(c,mousePt)));
				JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsInitialNode"), new SetNodeAsInitial(c)));
				menuItem.setSelected(((SCXMLNode)(c.getValue())).isInitial());
				add(menuItem);
				menuItem = new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsFinalNode"), new SetNodeAsFinal(c)));
				menuItem.setSelected(((SCXMLNode)(c.getValue())).isFinal());
				add(menuItem);
				menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsClusterNode"), new SetNodeAsCluster(c)));
				menuItem.setSelected(((SCXMLNode)(c.getValue())).isClusterNode());
				add(menuItem);
				menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsParallelNode"), new SetNodeAsParallel(c)));
				menuItem.setSelected(((SCXMLNode)(c.getValue())).isParallel());
				add(menuItem);
			}
		} else {
			add(editor.bind(mxResources.get("editNode/Edge"), null)).setEnabled(false);
		}

		// edit scxml datamodel
		add(editor.bind(mxResources.get("editDataModel"), new EditDatamodelAction(mousePt))).setEnabled((editor.getCurrentFileIO()==null)?false:true);
		add(editor.bind(mxResources.get("doLayout"), new DoLayoutAction(graph,c)));
	}
}
