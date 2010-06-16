package com.mxgraph.examples.swing.editor.scxml;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;

public class SCXMLSearchTool extends JDialog implements ListSelectionListener, WindowListener, ActionListener, DocumentListener {

	private static final int defaultNumResults = 1000;
	private SCXMLSearch search;
	private SCXMLGraphEditor editor;
	private JTextField searchBox;
	private JList list;
	private DefaultListModel listModel;
	private mxIGraphModel model;

	public SCXMLSearchTool(JFrame parent, SCXMLGraphEditor editor) throws CorruptIndexException, LockObtainFailedException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		super(parent,"SCXML Listener");
		search = new SCXMLSearch();

		search.buildIndex(editor.getGraphComponent().getGraph(),defaultNumResults);
		
		this.editor=editor;
		this.model=editor.getGraphComponent().getGraph().getModel();
		
		addWindowListener(this);
		JPanel contentPane = new JPanel(new BorderLayout());
		populateGUI(contentPane);
		contentPane.setOpaque(true); //content panes must be opaque
		
		//Create and set up the window.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(contentPane);

		//Display the window.
		pack();
		setVisible(false);
	}
	
	public void buildIndex() throws CorruptIndexException, LockObtainFailedException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		search.buildIndex(editor.getGraphComponent().getGraph(),defaultNumResults);
	}
	public void updateCellInIndex(mxCell c) throws CorruptIndexException, IOException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		ArrayList<mxCell> l = new ArrayList<mxCell>();
		l.add(c);
		search.updateIndex(l);
	}
	
	private void populateGUI(JPanel contentPane) {
		JLabel portLabel = new JLabel("port:");
		
		searchBox = new JTextField();
		searchBox.addActionListener(this);
		searchBox.getDocument().addDocumentListener(this);

		//Create the list and put it in a scroll pane.
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);
		list.setCellRenderer((ListCellRenderer) new SCXMLSearchrenderer());
		JScrollPane listScrollPane = new JScrollPane(list);

		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
		contentPane.add(searchBox);
		contentPane.add(listScrollPane);
	}

	class SCXMLSearchrenderer extends JLabel implements ListCellRenderer {
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{	    	 
			String s = value.toString();
			setText(index+": "+s);
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
	
	public void showTool() {
		setVisible(true);
	}

	private mxCell currentSelectedCell=null;
	private String currentSelectedCellPrevStyle=null;
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int lastIndex = listModel.size()-1;
			int selectedIndex = list.getSelectedIndex();

			if ((selectedIndex>=0) && (selectedIndex<=lastIndex)) {
				mxCell c=(mxCell) listModel.get(selectedIndex);
				updateSelection(c);
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
			} else {
				model.highlightCell(c, "#ff9b88");
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		setVisible(false);
		if (currentSelectedCell!=null) {
			model.setStyleCovert(currentSelectedCell, currentSelectedCellPrevStyle);
			currentSelectedCell=null;
			currentSelectedCellPrevStyle=null;
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<mxCell> findAndUpdateList(Document doc) {
		try {
			String query=doc.getText(0, doc.getLength());
			ArrayList<mxCell> result = search.find(query);
			listModel.clear();
			if (result!=null) {
				for(mxCell c:result) {
					listModel.addElement(c);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		Document doc = e.getDocument();
		findAndUpdateList(doc);
	}
	@Override
	public void insertUpdate(DocumentEvent e) {
		Document doc = e.getDocument();
		findAndUpdateList(doc);
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
		Document doc = e.getDocument();
		findAndUpdateList(doc);
	}
}
