package com.mxgraph.examples.swing.handler;

import java.awt.Component;
import java.awt.event.MouseEvent;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SCXMLConnectionHandler extends mxConnectionHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 592305036573256904L;

	public SCXMLConnectionHandler(SCXMLGraphComponent graphComponent) {
		super(graphComponent);
	}

	@Override
	public Object createTargetVertex(MouseEvent e, Object source) {
		mxGraph graph = graphComponent.getGraph();
		SCXMLGraphEditor editor = getEditor(e);
		SCXMLGraphComponent gc = editor.getGraphComponent();
		mxPoint point = graphComponent.getPointForEvent(e);
		mxCell parent = (mxCell) gc.getCellAt((int) point.getX(),
				(int) point.getY());
		SCXMLNode value = (SCXMLNode) editor.getCurrentFileIO()
				.buildNodeValue();
		mxCell createdCell = (mxCell) graph.insertVertex(parent,
				value.getInternalID(), value, point.getX() - 50,
				point.getY() - 50, 75, 75, value.getStyle());
		
		return createdCell;
	}

	private SCXMLGraphEditor getEditor(MouseEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null
					&& !(component instanceof SCXMLGraphEditor)) {
				component = component.getParent();
			}

			return (SCXMLGraphEditor) component;
		}

		return null;
	}

}
