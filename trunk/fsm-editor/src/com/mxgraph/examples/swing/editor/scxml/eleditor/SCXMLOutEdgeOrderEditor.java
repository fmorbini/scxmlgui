package com.mxgraph.examples.swing.editor.scxml.eleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.mxgraph.examples.swing.SCXMLEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphEditor;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class SCXMLOutEdgeOrderEditor extends JDialog implements ListSelectionListener, WindowListener, ActionListener {
	private JList list;
	private DefaultListModel listModel;
	private JScrollPane listScrollPane;
	private ArrayList<HashSet<mxCell>> highlightedCellsEachInstant;
	
	private ArrayList<mxCell> originalOrder=new ArrayList<mxCell>();
	private boolean modified=false;

	private JButton okButton,cancelButton;
	private JButton upButton,downButton;
	private SCXMLEditor editor;
	private SCXMLGraphComponent gc;
	private SCXMLGraph graph;
	
	private static final String title="Edge order editor";
	
	public SCXMLOutEdgeOrderEditor(mxCell source, SCXMLEditor e) {
		editor=e;
		gc=editor.getGraphComponent();
		graph=gc.getGraph();
		
		highlightedCellsEachInstant=new ArrayList<HashSet<mxCell>>();
		
		addWindowListener(this);
		JPanel contentPane = new JPanel(new BorderLayout());
		populateGUI(contentPane);
		populateEdgeList(source);
		contentPane.setOpaque(true); //content panes must be opaque
		
		//save original order in case we cancel the modifications
		for(Object edge:graph.getAllOutgoingEdges(source)) originalOrder.add((mxCell) edge);
		setModified(false);
		
		//Create and set up the window.
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(contentPane);

		//Display the window.
		pack();
		setModal(true);
		setVisible(true);
	}
	
	private void populateEdgeList(mxCell source) {
		Object[] edges = graph.getAllOutgoingEdges(source);
		listModel.setSize(edges.length);
		for(Object e:edges) {
			mxCell edge=(mxCell) e;
			SCXMLEdge scxmlEdge = (SCXMLEdge) edge.getValue();
			listModel.set(scxmlEdge.getOrder(), edge);
		}
	}
	
	private void populateGUI(JPanel contentPane) {
		upButton = new JButton(mxResources.get("moveEdgeUp"));
		upButton.setActionCommand("up");
		upButton.addActionListener(this);
		downButton = new JButton(mxResources.get("moveEdgeDown"));
		downButton.setActionCommand("down");
		downButton.addActionListener(this);

		//Create a panel that uses BoxLayout.
		JPanel moveButtonPane = new JPanel();
		moveButtonPane.setLayout(new BoxLayout(moveButtonPane,BoxLayout.LINE_AXIS));
		moveButtonPane.add(upButton);
		moveButtonPane.add(Box.createHorizontalGlue());
		moveButtonPane.add(downButton);
		moveButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		upButton.setEnabled(false);
		downButton.setEnabled(false);
		
		//Create the list and put it in a scroll pane.
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);
		list.setCellRenderer((ListCellRenderer) new EdgeRenderer());
		listScrollPane = new JScrollPane(list);

		okButton = new JButton(mxResources.get("ok"));
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton(mxResources.get("cancel"));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		//Create a panel that uses BoxLayout.
		JPanel closeButtonPane = new JPanel();
		closeButtonPane.setLayout(new BoxLayout(closeButtonPane,BoxLayout.LINE_AXIS));
		closeButtonPane.add(okButton);
		closeButtonPane.add(Box.createHorizontalGlue());
		closeButtonPane.add(cancelButton);
		closeButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		contentPane.add(moveButtonPane, BorderLayout.PAGE_START);
		contentPane.add(listScrollPane, BorderLayout.CENTER);
		contentPane.add(closeButtonPane, BorderLayout.PAGE_END);
	}
	
	class EdgeRenderer extends JLabel implements ListCellRenderer {
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{
			String text="";
			if (value!=null) {
				mxCell e=(mxCell) value;			
				SCXMLEdge v=(SCXMLEdge) e.getValue();
				text=v.getOrder()+": -> "+graph.convertValueToString(e.getTarget())+" ["+graph.convertValueToString(e)+"]";
			}
			setText(text);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}
	
	private Integer max(int[] array) {
		int length=array.length;
		if (length<=0) return null;
		else {
			int max=array[0];
			for (int i=1;i<length;i++) {
				if (array[i]>max) max=array[i];
			}
			return max;
		}
	}
	private Integer min(int[] array) {
		int length=array.length;
		if (length<=0) return null;
		else {
			int min=array[0];
			for (int i=1;i<length;i++) {
				if (array[i]<min) min=array[i];
			}
			return min;
		}
	}
	
	//This method is required by ListSelectionListener.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if(list.isSelectionEmpty()) {
				upButton.setEnabled(false);
				downButton.setEnabled(false);
			} else {
				int[] sis=list.getSelectedIndices();
				Integer max=max(sis);
				Integer min=min(sis);
				if ((max!=null) && (max<listModel.size()-1)) downButton.setEnabled(true);
				else downButton.setEnabled(false);
				if ((min!=null) && (min>0)) upButton.setEnabled(true);
				else upButton.setEnabled(false);
			}
		}
	}

	private void setHighlightAtIndex(int index,HashSet<mxCell> highlightedCells) {
		highlightedCellsEachInstant.add(index, highlightedCells);
	}
	private HashSet<mxCell> getHighlightAtIndex(int index) {
		if ((index>=0) && (highlightedCellsEachInstant.size()>index)) return highlightedCellsEachInstant.get(index);
		else return null;
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		actionPerformed(new ActionEvent(this, 0, "cancel"));
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd=e.getActionCommand();
		boolean isup=cmd.equals("up");
		boolean isdown=cmd.equals("down");
		if (isup || isdown) {
			int[] sis=list.getSelectedIndices();
			Integer max=max(sis);
			Integer min=min(sis);
			int j=0;
			
			int inc=(isup)?-1:+1;
			int swapEl=(isup)?min:max;
			int swapPos=(isup)?max:min;
			
			for(int i=min;i<=max;i++) {
				mxCell c=(mxCell)listModel.get(i);
				SCXMLEdge v=(SCXMLEdge) c.getValue();
				v.setOrder(i+inc);
				sis[j++]=i+inc;
			}
			mxCell c=(mxCell)listModel.get(swapEl+inc);
			SCXMLEdge v=(SCXMLEdge) c.getValue();
			v.setOrder(swapPos);
			
			//update list model
			listModel=new DefaultListModel();
			populateEdgeList((mxCell) c.getSource());
			list.setModel(listModel);

			// update selection
			list.setSelectedIndices(sis);

			setModified(true);
		} else if (cmd.equals("ok")) {
			exitTool();
		} else if (cmd.equals("cancel")) {
			if (!getModified())
				exitTool();
			else {
				if (JOptionPane.showConfirmDialog(editor, mxResources.get("saveChanges")) == JOptionPane.NO_OPTION) {
					int i=0;
					for (mxCell edge:originalOrder) {
						SCXMLEdge se=(SCXMLEdge)edge.getValue();
						se.setOrder(i++);
					}
					exitTool();
				}
			}
		}
	}

	private void exitTool() {
		dispose();
	}

	private void setModified(boolean m) {
		if (m) {
			modified=true;
			setTitle(title+"*");
		} else {
			modified=false;
			setTitle(title);
		}
	}
	private boolean getModified() {
		return modified;
	}
}
