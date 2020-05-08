package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.TransferHandler;

import org.w3c.dom.Document;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.handler.SCXMLConnectionHandler;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

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

		setWheelScrollingEnabled(false);
		
		//addComponentListener(this);
		setDragEnabled(true);
		
		// Sets switches typically used in an editor
		setPageVisible(false);
		setGridVisible(true);
		setToolTips(true);
		getConnectionHandler().setCreateTarget(true);

		// Loads the defalt stylesheet from an external file
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(SCXMLGraphEditor.class.getResource(
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
	
	@Override
	public SCXMLGraph getGraph()
	{
		return (SCXMLGraph) graph;
	}
	
	@Override
	protected mxConnectionHandler createConnectionHandler() {
		return new SCXMLConnectionHandler(this);
	}
	
	/**
	 * Overrides drop behaviour to set the cell style if the target
	 * is not a valid drop target and the cells are of the same
	 * type (eg. both vertices or both edges). 
	 */
	public Object[] importCells(Object[] cells, double dx, double dy,
			Object target, Point location)
	{
		/*
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
		 */
		mxIGraphModel model = getGraph().getModel();
		for (Object cell:cells) {
			Object value=model.getValue(cell);
			if (value instanceof SCXMLNode) {
				model.setStyle(cell, ((SCXMLNode)value).getStyle());
			} else if (value instanceof SCXMLEdge) {
				model.setStyle(cell, ((SCXMLEdge)value).getStyle((mxCell) cell));
			}
		}
		return super.importCells(cells, dx, dy, target, location);
	}

	private HashMap<String,mxCell> scxmlNodes=new HashMap<String, mxCell>();
	public void addSCXMLNode(SCXMLNode n, mxCell node) {
		scxmlNodes.put(n.getID(), node);
	}
	public boolean isSCXMLNodeAlreadyThere(SCXMLNode n) {
		return scxmlNodes.containsKey(n.getID());
	}
	public mxCell getSCXMLNodeForID(String id) {
		return scxmlNodes.get(id);
	}
	public void clearSCXMLNodes() {
		scxmlNodes.clear();
	}
	public void validateGraph()
	{		
		mxGraphModel model = (mxGraphModel) graph.getModel();
		model.fireEvent(new mxEventObject(mxEvent.REQUEST_VALIDATION,"root",model.getRoot()));
	}

	/* (non-Javadoc)
	 * @see com.mxgraph.swing.mxGraphComponent#getSiblingsOfCell(java.lang.Object)
	 */
	@Override
	public Collection<Object> getSiblingsOfCell(Object c) {
		ArrayList<Object> ret=new ArrayList<Object>();
		ret.add(c);
		mxCell cell=(mxCell) c;
		SCXMLEdge value=null;
		ArrayList<String> targets=null;
		if (cell.isEdge() && ((value=(SCXMLEdge)cell.getValue())!=null) && ((targets=value.getSCXMLTargets())!=null) &&
				(targets.size()>1)) {
			mxICell source = cell.getSource();
			if (source!=null) {
				int numEdges=source.getEdgeCount();
				for(int i=0;i<numEdges;i++) {
					mxICell child = source.getEdgeAt(i);
					if ((child!=c) && (child.getValue()==value)) {
						ret.add(child);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	protected TransferHandler createTransferHandler()
	{
		return new SCXMLTransferHandler();
	}
	
	@Override
	protected mxGraphHandler createGraphHandler() {
		return new SCXMLGraphHandler(this);
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

