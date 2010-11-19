package com.mxgraph.examples.swing.editor.utils;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;

public class CellSelector {
	private mxCell currentSelectedCell=null;
	private String currentSelectedCellPrevStyle=null;
	private JList list;
	protected DefaultListModel listModel;
	private SCXMLGraphComponent gc;
	private mxIGraphModel model;
	
	public CellSelector(JList list,SCXMLGraphComponent gc) {
		this.list=list;
		listModel=(DefaultListModel) list.getModel();
		this.gc=gc;
		this.model=gc.getGraph().getModel();
	}
	
	public mxCell getCellFromListElement(int selectedIndex) {
		return (mxCell) listModel.get(selectedIndex);
	}
	
	public void handleSelectEvent(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int lastIndex = listModel.size()-1;
			int selectedIndex = list.getSelectedIndex();

			if ((selectedIndex>=0) && (selectedIndex<=lastIndex)) {
				mxCell c=getCellFromListElement(selectedIndex);
				updateSelection(c);
			} else {
				unselectAll();
			}
		}
	}
	
	private void updateSelection(mxCell c) {
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
