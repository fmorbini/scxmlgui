package com.mxgraph.examples.swing.editor.scxml.search;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.scxml.TextDialog;
import com.mxgraph.examples.swing.editor.utils.CellSelector;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxResources;

public class SCXMLSearchTool extends JDialog implements ListSelectionListener, WindowListener, ActionListener, DocumentListener {

	private static final int defaultNumResults = 1000;
	private SCXMLSearch search;
	private SCXMLGraphEditor editor;
	private JTextField searchBox;
	private JList list;
	private DefaultListModel listModel;
	private mxIGraphModel model;
	private SCXMLGraphComponent gc;
	private CellSelector listSelectorHandler;

	public SCXMLSearchTool(JFrame parent, SCXMLGraphEditor editor) throws CorruptIndexException, LockObtainFailedException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		super(parent,"Find");
		search = new SCXMLSearch(editor,defaultNumResults);

		buildIndex();
		
		this.editor=editor;
		this.gc=editor.getGraphComponent();
		this.model=gc.getGraph().getModel();
		
		addWindowListener(this);
		JPanel contentPane = new JPanel();
		populateGUI(contentPane);
		contentPane.setOpaque(true); //content panes must be opaque
		
		listSelectorHandler=new CellSelector(list, gc);
		
		//Create and set up the window.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(contentPane);

		//Display the window.
		pack();
		setVisible(false);
	}
	
	public void buildIndex() throws CorruptIndexException, LockObtainFailedException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		search.buildIndex();
	}
	public void updateCellInIndex(mxCell c,boolean add) throws CorruptIndexException, IOException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		ArrayList<mxCell> l = new ArrayList<mxCell>();
		l.add(c);
		search.updateIndex(l,add);
	}
	public void updateCellsInIndex(Collection<mxCell> cs,boolean add) throws CorruptIndexException, IOException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		search.updateIndex(cs,add);
	}
	
	private void populateGUI(JPanel contentPane) {
		searchBox = new JTextField();
		searchBox.addActionListener(this);
		searchBox.getDocument().addDocumentListener(this);

		JButton helpButton = new JButton(mxResources.get("help"));
		helpButton.setActionCommand("help");
		helpButton.addActionListener(this);

		JPanel searchAndHelp = new JPanel();
		searchAndHelp.setLayout(new BoxLayout(searchAndHelp,BoxLayout.LINE_AXIS));
		searchAndHelp.add(searchBox);
		searchAndHelp.add(helpButton);
		searchAndHelp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//Create the list and put it in a scroll pane.
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);
		list.setCellRenderer((ListCellRenderer) new SCXMLSearchrenderer());
		JScrollPane listScrollPane = new JScrollPane(list);

		contentPane.setLayout(new GridBagLayout());

		//Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(searchAndHelp, c);

        c=new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        contentPane.add(listScrollPane, c);
	}

	class SCXMLSearchrenderer extends JLabel implements ListCellRenderer {
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{	    	 
			mxCell c=(mxCell)value;
			String s="";
			if (c.isVertex()) {
				SCXMLNode n=(SCXMLNode) c.getValue();
				s=n.getID();
			} else {
				SCXMLEdge e=(SCXMLEdge) c.getValue();
				s=e.getSCXMLSource()+"-["+e.getCondition()+","+e.getEvent()+"]->"+e.getSCXMLTargets();
			}
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
	
	public void showTool(JFrame parent) {
		Rectangle pos=parent.getBounds();
		setLocation((int)pos.getX(), (int)pos.getY());
		setVisible(true);
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
		listSelectorHandler.unselectAll();
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
		String cmd=e.getActionCommand();
		if (cmd.equals("help")) {
			new TextDialog(this,"Search help","The search box accepts Lucene syntax avaialble at:\n" +
					"http://lucene.apache.org/java/2_4_0/queryparsersyntax.html\n" +
					"\n" +
					"By default, if no fields are specified the entered text is searched in all fields.\n"+
					"The available fields are:\n" +
					"-For edges:\n"+
					" -source: search the source of edges\n"+
					" -target: search the target of edges\n"+
					" -eve: search the event string of edges\n"+
					" -cnd: search the condition string of edges\n"+
					" -eexe: search the executable content of edges\n"+
					" -com: search the comments associated with an edge\n"+
					"-For nodes:\n"+
					" -id: search the label of nodes\n"+
					" -inc: search the include string of nodes\n"+
					" -dm: search the datamodel associted with nodes\n"+
					" -ns: search the namespace associated with nodes\n"+
					" -entry: search the content exectuted whena  node is entered\n"+
					" -exit: search the content exectuted whena  node is exited\n"+
					" -init: search the content executed when the node is entered because it's an intial node\n"+
					" -dd: search the data attached when the node is exited\n"+
					" -com: search the comments associated with a node\n"+
					"\n"+
					"To search all element with a non empty datamodel field use the following query:\"dm:[* TO*]\"\n",
					ModalityType.MODELESS);
		}
	}

	public ArrayList<mxCell> findAndUpdateList(Document searchBox) {
		try {
			String query=searchBox.getText(0, searchBox.getLength());
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

	@Override
	public void valueChanged(ListSelectionEvent e) {
		listSelectorHandler.handleSelectEvent(e);
	}
}
