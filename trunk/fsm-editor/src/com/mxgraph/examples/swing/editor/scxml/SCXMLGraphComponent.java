package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Hashtable;

import org.w3c.dom.Document;

import com.mxgraph.examples.swing.SCXMLEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

/**
 * 
 */
public class SCXMLGraphComponent extends mxGraphComponent //implements ComponentListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6833603133512882012L;

	/**
	 * 
	 * @param graph
	 */
	public SCXMLGraphComponent(mxGraph graph)
	{
		super(graph);

		//addComponentListener(this);
		setDragEnabled(false);
		
		// Sets switches typically used in an editor
		setPageVisible(false);
		setGridVisible(true);
		setToolTips(true);
		getConnectionHandler().setCreateTarget(true);

		// Loads the defalt stylesheet from an external file
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(SCXMLEditor.class.getResource(
				"/com/mxgraph/examples/swing/resources/default-style.xml")
				.toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		// Sets the background to white
		getViewport().setOpaque(false);
		setBackground(Color.WHITE);
	}

	@Override
	// disable double click editing event 
	public boolean isEditEvent(MouseEvent e)
	{
		return false;
	}
	
	/**
	 * Overrides drop behaviour to set the cell style if the target
	 * is not a valid drop target and the cells are of the same
	 * type (eg. both vertices or both edges). 
	 */
	public Object[] importCells(Object[] cells, double dx, double dy,
			Object target, Point location)
	{
		if (target == null && cells.length == 1 && location != null)
		{
			target = getCellAt(location.x, location.y);

			if (target instanceof mxICell && cells[0] instanceof mxICell)
			{
				mxICell targetCell = (mxICell) target;
				mxICell dropCell = (mxICell) cells[0];

				if (targetCell.isVertex() == dropCell.isVertex()
						|| targetCell.isEdge() == dropCell.isEdge())
				{
					mxIGraphModel model = graph.getModel();
					model.setStyle(target, model.getStyle(cells[0]));
					graph.setSelectionCell(target);

					return null;
				}
			}
		}

		return super.importCells(cells, dx, dy, target, location);
	}

	private HashMap<String,Boolean> nodeNames=new HashMap<String, Boolean>();
	public void addSCXMLNode(SCXMLNode n) {
		nodeNames.put(n.getID(), null);
	}
	public boolean isSCXMLNodeAlreadyThere(SCXMLNode n) {
		return nodeNames.containsKey(n.getID());
	}
	public boolean validateGraph(StringBuffer warnings)
	{		
		nodeNames.clear();
		return (validateGraph(graph.getModel().getRoot(),new Hashtable<Object, Object>(),warnings)==null);
	}

	/*
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		System.out.println("resize??");
		FSMEditor editor = ((CustomGraph)this.getGraph()).getEditor();			
		mxGraphComponent gc = editor.getGraphComponent();
		if ((gc!=null) && (editor.getCurrentFileIO()!=null)) {
			mxIGraphModel model = gc.getGraph().getModel();
			mxCell root=((SCXMLImportExport)editor.getCurrentFileIO()).followUniqueDescendantLineTillSCXMLValueIsFound(model);
				mxGeometry g = root.getGeometry();
				model.setGeometry(root, new mxGeometry(g.getX(), g.getY(), gc.getSize().width/graph.getView().getScale(),gc.getSize().height/graph.getView().getScale()));
				//Object[] a={root};
				//mxRectangle[] b={root.getGeometry()};
				//graph.cellsResized(a,b);
		}
		zoomAndCenter();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
*/

}

