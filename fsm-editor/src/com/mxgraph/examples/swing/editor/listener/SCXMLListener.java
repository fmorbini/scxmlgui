package com.mxgraph.examples.swing.editor.listener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.examples.swing.SCXMLEditor;
import com.mxgraph.examples.swing.editor.DefaultFileFilter;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;

public class SCXMLListener extends JFrame implements ListSelectionListener, WindowListener, ActionListener, DocumentListener {
	private int status;
	private static final int STARTED = 0;
	private static final int STOPPED = 1;
	private static final int WAITING = 2;
	private static final int PRESTARTING = 3;
	private static final int LOADING = 4;

	private JList list;
	private DefaultListModel listModel;
	private JScrollPane listScrollPane;
	private ArrayList<HashSet<mxCell>> highlightedCellsEachInstant;

	private JButton saveButton,loadButton;
	private JButton startStopButton;
	private JTextField port;

	ServerSocket socket = null;
	private Socket clientSocket;
	private SCXMLSocketListener listener;
	private SCXMLGraphComponent graphComponent;
	private mxIGraphModel model;
	private SCXMLEditor editor;

	public SCXMLListener(SCXMLEditor editor) {
		super("SCXML listener");
		
		highlightedCellsEachInstant=new ArrayList<HashSet<mxCell>>();
		
		graphComponent=editor.getGraphComponent();
		model=graphComponent.getGraph().getModel();
		this.editor=editor;
		
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
	
	private void populateGUI(JPanel contentPane) {

		JLabel portLabel = new JLabel("port:");
		
		port = new JTextField(10);
		port.addActionListener(this);
		port.getDocument().addDocumentListener(this);
		
		startStopButton=new JButton(mxResources.get("startSCXMLListener"));
		startStopButton.setActionCommand("start");
		startStopButton.addActionListener(this);
				
		JPanel startStopButtonPane = new JPanel();
		startStopButtonPane.setLayout(new BoxLayout(startStopButtonPane,BoxLayout.LINE_AXIS));
		startStopButtonPane.add(portLabel);
		startStopButtonPane.add(port);
		startStopButtonPane.add(Box.createHorizontalStrut(5));
		startStopButtonPane.add(startStopButton);
		startStopButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		//Create the list and put it in a scroll pane.
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);
		list.setCellRenderer((ListCellRenderer) new SCXMLEventRenderer());
		listScrollPane = new JScrollPane(list);

		saveButton = new JButton(mxResources.get("save"));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		loadButton = new JButton(mxResources.get("openFile"));
		loadButton.setActionCommand("load");
		loadButton.addActionListener(this);

		//Create a panel that uses BoxLayout.
		JPanel loadSaveButtonPane = new JPanel();
		loadSaveButtonPane.setLayout(new BoxLayout(loadSaveButtonPane,BoxLayout.LINE_AXIS));
		loadSaveButtonPane.add(saveButton);
		loadSaveButtonPane.add(Box.createHorizontalGlue());
		loadSaveButtonPane.add(loadButton);
		loadSaveButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		contentPane.add(startStopButtonPane, BorderLayout.PAGE_START);
		contentPane.add(listScrollPane, BorderLayout.CENTER);
		contentPane.add(loadSaveButtonPane, BorderLayout.PAGE_END);

		setStatus(PRESTARTING);
	}

	class SCXMLEventRenderer extends JLabel implements ListCellRenderer {
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
	private int prevSelectedIndex=-1;
	//This method is required by ListSelectionListener.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int lastIndex = listModel.size()-1;
			int selectedIndex = list.getSelectedIndex();
			if ((lastIndex<0) || (selectedIndex>=lastIndex)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JScrollBar vs = listScrollPane.getVerticalScrollBar();
						if (vs!=null) vs.setValue(vs.getMaximum());
					}
				});
			}
			if (selectedIndex>=0) {
				resetAllSCXMLEventExecutions(prevSelectedIndex);
				HashSet<mxCell> set = getHighlightAtIndex(selectedIndex);
				if (set!=null) {
					for(mxCell c:set) {
						if (c!=null) {
							if (c.isEdge()) doEdgeShow(model,c,true,null);
							else doNodeShow(model,c,true,null);
						}
					}
				}
				prevSelectedIndex=selectedIndex;
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

	public void showTool() {
		setVisible(true);
	}

	public void stopTool() {
		stopListener();
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
		stopTool();
		resetAllSCXMLEventExecutions(list.getSelectedIndex());
		setVisible(false);
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

	String lastDir=null;
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd=e.getActionCommand();
		Integer portValue;
		if (cmd.equals("start") && ((portValue=validPort(port.getText()))!=null)) {
			if (initiateListener(portValue)) {
				setStatus(WAITING);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if ((clientSocket=waitForConnection(socket))!=null) {
							setStatus(STARTED);
							listener=new SCXMLSocketListener(SCXMLListener.this,clientSocket);
							listener.start();
						} else {
							setStatus(STOPPED);
						}
					}
				});
			} else {
				setStatus(STOPPED);
			}
		} else if (cmd.equals("stop")) {
			setStatus(STOPPED);
		} else if (cmd.equals("save")) {
			String wd = (lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():System.getProperty("user.dir"));
			JFileChooser fc = new JFileChooser(wd);
			fc.setFileFilter(new DefaultFileFilter(".txt","Event List"));
			int rc = fc.showDialog(this, mxResources.get("save"));
			if (rc == JFileChooser.APPROVE_OPTION) {
				lastDir = fc.getSelectedFile().getParent();
				String filename = fc.getSelectedFile().getAbsolutePath();
				FileFilter selectedFilter = fc.getFileFilter();
				if (selectedFilter instanceof DefaultFileFilter)
				{
					String ext = ((DefaultFileFilter) selectedFilter).getExtension();
					if (!filename.toLowerCase().endsWith(ext))
						filename += ext;
				}
				if ((!(new File(filename).exists())) || JOptionPane.showConfirmDialog(graphComponent,mxResources.get("overwriteExistingFile")) == JOptionPane.YES_OPTION)
				{
					try {
						mxUtils.writeFile(getEventListToString(),filename);
					} catch (IOException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(editor.getGraphComponent(),
								ex.toString(),
								mxResources.get("error"),
								JOptionPane.ERROR_MESSAGE);					}			
				}
			}
		} else if (cmd.equals("load")) {
			String wd = (lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():System.getProperty("user.dir"));
			JFileChooser fc = new JFileChooser(wd);
			fc.setFileFilter(new FileNameExtensionFilter("Event List", "txt"));
			int rc = fc.showDialog(this, mxResources.get("openFile"));
			if (rc == JFileChooser.APPROVE_OPTION) {
				lastDir = fc.getSelectedFile().getParent();
				String filename = fc.getSelectedFile().getAbsolutePath();
				try {
					setStatus(LOADING);
					setEventListFromString(mxUtils.readFile(filename));
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(editor.getGraphComponent(),
							ex.toString(),
							mxResources.get("error"),
							JOptionPane.ERROR_MESSAGE);
				}			
				setStatus(STOPPED);
			}
		}
	}

	private Integer validPort(String text) {
		Integer port=null;
		try {
			port=Integer.parseInt(text);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "'"+text+"' is an invalid TCP port number.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return port;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		handleIDField(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		handleIDField(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		handleIDField(e);
	}
	
	private void handleIDField(DocumentEvent e) {
		if (isEmptyIDField(e))
			startStopButton.setEnabled(false);
		else
			startStopButton.setEnabled(true);
	}
	private boolean isEmptyIDField(DocumentEvent e) {
        if (e.getDocument().getLength() <= 0) {
            return true;
        }
        return false;
    }
	
	private boolean initiateListener(int port) {
		if (socket==null) {
			try {
				socket = new ServerSocket(port);
				return true;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "impossible to listen to port '"+port+"'", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, "illegal port: '"+port+"'", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}
	
	public void setStatus(int newStatus) {
		switch (newStatus) {
		case PRESTARTING:
			status=PRESTARTING;
			startStopButton.setActionCommand("start");
			startStopButton.setText(mxResources.get("startSCXMLListener"));
			startStopButton.setEnabled(false);
			port.setText("");
			port.setEnabled(true);			
			list.setEnabled(false);
			saveButton.setEnabled(false);
			loadButton.setEnabled(false);
			break;
		case STARTED:
			status=STARTED;
			resetEventList();
			startStopButton.setActionCommand("stop");
			startStopButton.setText(mxResources.get("stopSCXMLListener"));
			startStopButton.setEnabled(true);
			port.setEnabled(false);
			list.setEnabled(true);
			saveButton.setEnabled(false);
			loadButton.setEnabled(false);
			break;
		case STOPPED:
			status=STOPPED;
			stopTool();
			startStopButton.setActionCommand("start");
			startStopButton.setText(mxResources.get("startSCXMLListener"));
			startStopButton.setEnabled(true);
			port.setEnabled(true);
			list.setEnabled(true);
			if (listModel.size()>0) {
				saveButton.setEnabled(true);
			}
			loadButton.setEnabled(true);
			break;
		case WAITING:
			status=WAITING;
			startStopButton.setActionCommand("wait");
			startStopButton.setText(mxResources.get("waitForConnection"));
			startStopButton.setEnabled(false);
			port.setEnabled(false);
			list.setEnabled(false);
			saveButton.setEnabled(false);
			loadButton.setEnabled(false);
			break;
		case LOADING:
			status=LOADING;
			resetEventList();
			list.setEnabled(true);
			break;
		}
	}
	
	private void resetEventList() {
		resetAllSCXMLEventExecutions(list.getSelectedIndex());
		listModel.clear();
		highlightedCellsEachInstant.clear();
	}
	private String getEventListToString() {
		String ret="";
		for (Object o:listModel.toArray()) {
			String es=((SCXMLEvent)o).write();
			if (es!=null)
				ret+=es+"\n";
		}
		return ret;
	}
	private void setEventListFromString(String events) throws IOException {
		BufferedReader in = new BufferedReader(new StringReader(events));
		String line;
		while((line=in.readLine())!=null) {
			addEvent(line);
		}
	}

	public int getStatus() {
		return status;
	}

	public void stopListener() {
		try {
			if (listener!=null) listener.halt();
			if (socket!=null) socket.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error while terminating SCXML listener.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		socket=null;
		listener=null;
	}
	
	private Socket waitForConnection(ServerSocket socket) {
		if (socket!=null) {
			try {
				Socket clientSocket=socket.accept();
				return clientSocket;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error while accepting connection.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "The socket is NULL, impossible to wait for a connection.", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		return null;
	}
	class SCXMLSocketListener extends Thread {
		private SCXMLListener gui;
		private Socket socket;
		private BufferedReader in;
		private OutputStream out;
		private static final byte ACK = 1;

		public SCXMLSocketListener(SCXMLListener l,Socket clientSocket) {
			this.gui=l;
			this.socket=clientSocket;
		}

		public void run() {
	        try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out=socket.getOutputStream();
        		out.write(ACK);
		        String inputLine;
		        while (gui.getStatus()==STARTED) {
		        	if (in.ready()) {
		        		inputLine = in.readLine();
		        		out.write(ACK);
		        		if (inputLine!=null) gui.addEvent(inputLine);
		        	}
		        }
			} catch (IOException e) {
				gui.setStatus(STOPPED);
			}
        }
		
		public void halt() throws IOException {
	        if (in!=null) in.close();
	        if (socket!=null) socket.close();
		}
	}

	public void addEvent(String command) {
		try {
			SCXMLEvent event = new SCXMLEvent(command);
			int lastIndex = listModel.size()-1;
			int selectedIndex = list.getSelectedIndex();
			listModel.addElement(event);

			HashSet<mxCell> prevHighlightedCells = getHighlightAtIndex(lastIndex);
			HashSet<mxCell> newHighlightedCells = (prevHighlightedCells==null)? new HashSet<mxCell>():new HashSet<mxCell>(prevHighlightedCells);
			// updates the highlight
			event.execute(model,false,newHighlightedCells);
			// store the new highlight state (without changing the display
			setHighlightAtIndex(lastIndex+1,newHighlightedCells);

			if ((lastIndex<0) || (selectedIndex>=lastIndex)) {
				resetAllSCXMLEventExecutions(selectedIndex);
				list.setSelectedIndex(lastIndex+1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//JOptionPane.showMessageDialog(this, "Unknown command received", "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void resetAllSCXMLEventExecutions(int i) {
		if (i>0) {
			HashSet<mxCell> highlightedCells = getHighlightAtIndex(i);
			for (mxCell c:highlightedCells) {
				if (c!=null) {
					if (c.isEdge()) doEdgeHide(model, c,true,null);
					else doNodeHide(model, c,true,null);
				}
			}
		}
	}

	private void doNodeShow(mxIGraphModel model,mxCell n,boolean show,HashSet<mxCell> highlightedCells) {
		if (show) model.highlightCell(n, "#ffe088");
		if (highlightedCells!=null) highlightedCells.add(n);
	}
	private void doEdgeShow(mxIGraphModel model,mxCell n,boolean show,HashSet<mxCell> highlightedCells) {
		if (show) model.highlightCell(n, "#ff9b88","3");
		if (highlightedCells!=null) highlightedCells.add(n);
	}
	private void doNodeHide(mxIGraphModel model,mxCell n,boolean show,HashSet<mxCell> highlightedCells) {
		if (show) model.highlightCell(n,null);
		if (highlightedCells!=null) highlightedCells.remove(n);
	}
	private void doEdgeHide(mxIGraphModel model,mxCell n,boolean show,HashSet<mxCell> highlightedCells) {
		if (show) model.highlightCell(n,null,null);
		if (highlightedCells!=null) highlightedCells.remove(n);
	}

	public class SCXMLEvent {
		static final int SHOWNODE=1;
		static final int HIDENODE=0;
		static final int SHOWEDGE=3;
		static final int HIDEEDGE=2;
		static final int UNK=20;
		int command;
		mxCell arg1,arg2;
		String arg1n,arg2n;

		public SCXMLEvent(String command) throws Exception {
			Pattern nodep = Pattern.compile("^[\\s]*([01])[\\s]+(.+)[\\s]*$");
			Pattern edgep = Pattern.compile("^[\\s]*([23])[\\s]+(.+)[\\s]+->[\\s]+(.+)[\\s]*$");
			Matcher m = nodep.matcher(command);
			if (m.matches() && (m.groupCount()==2)) {
				this.command=Integer.parseInt(m.group(1));
				arg1n=m.group(2);
				arg1 = graphComponent.getSCXMLNodeForID(arg1n);				
			} else {
				m = edgep.matcher(command);
				if (m.matches() && (m.groupCount()==3)) {
					this.command=Integer.parseInt(m.group(1));
					arg1n=m.group(2);
					arg2n=m.group(3);
					arg1=graphComponent.getSCXMLNodeForID(arg1n);
					arg2=graphComponent.getSCXMLNodeForID(arg2n);
				} else {
					this.command=UNK;
					throw new Exception("error in received command");
				}
			}
		}
		
		public void execute(mxIGraphModel model,boolean show,HashSet<mxCell> highlightedCells) {
			Object[] edges;
			switch (command) {
			case SHOWNODE:
				doNodeShow(model, arg1,show,highlightedCells);
				break;
			case SHOWEDGE:
				edges = mxGraphModel.getEdgesBetween(model, arg1, arg2, true);
				for(Object edge:edges){
					doEdgeShow(model,(mxCell) edge,show,highlightedCells);
				}
				break;
			case HIDENODE:
				doNodeHide(model, arg1,show,highlightedCells);
				break;
			case HIDEEDGE:
				edges = mxGraphModel.getEdgesBetween(model, arg1, arg2, true);
				for(Object edge:edges){
					doEdgeHide(model,(mxCell) edge,show,highlightedCells);
				}
				break;
			}
		}
		
		public String write() {
			switch (command) {
			case SHOWNODE:
				return "1 "+arg1n;
			case SHOWEDGE:
				return "3 "+arg1n+" -> "+arg2n;
			case HIDENODE:
				return "0 "+arg1n;
			case HIDEEDGE:
				return "2 "+arg1n+" -> "+arg2n;
			}
			return null;
		}

		@Override
		public String toString() {
			switch (command) {
			case SHOWNODE:
				return "show node "+arg1n;
			case SHOWEDGE:
				return "show edge "+arg1n+"->"+arg2n;
			case HIDENODE:
				return "hide node "+arg1n;
			case HIDEEDGE:
				return "hide edge "+arg1n+"->"+arg2n;
			}
			return "[SCXMLEvent error]";
		}
	}
}
