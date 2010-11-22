package com.mxgraph.examples.swing.editor.utils;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;

public class CellSelector {
	protected mxCell currentSelectedCell=null;
	protected String currentSelectedCellPrevStyle=null;
	protected SCXMLGraphComponent gc;
	protected mxIGraphModel model;
	
	public CellSelector(SCXMLGraphComponent gc) {
		this.gc=gc;
		this.model=gc.getGraph().getModel();
	}
	
	public void updateSelection(mxCell c) {
		if ((c!=null) && (c!=currentSelectedCell)) {
			model.setStyleCovert(currentSelectedCell, currentSelectedCellPrevStyle);
			currentSelectedCell=c;
			currentSelectedCellPrevStyle=model.getStyle(c);
			if (c.isEdge()) {
				model.highlightCell(c, "#ff9b88","3");
				gc.scrollCellToVisible(c, true);
			} else {
				model.highlightCell(c, "#ff9b88");
				gc.scrollCellToVisible(c, true);
			}
		} else unselectAll();
	}
	
	public void unselectAll() {
		if (currentSelectedCell!=null) {
			model.setStyleCovert(currentSelectedCell, currentSelectedCellPrevStyle);
			currentSelectedCell=null;
			currentSelectedCellPrevStyle=null;
		}
	}
}
