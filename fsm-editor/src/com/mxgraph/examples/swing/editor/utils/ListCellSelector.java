package com.mxgraph.examples.swing.editor.utils;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;

public class ListCellSelector extends CellSelector {
	private JList list;
	protected DefaultListModel listModel;
	
	public ListCellSelector(JList list,SCXMLGraphComponent gc) {
		super(gc);
		this.list=list;
		listModel=(DefaultListModel) list.getModel();
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
}
