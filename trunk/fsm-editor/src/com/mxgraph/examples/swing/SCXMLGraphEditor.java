package com.mxgraph.examples.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.mxgraph.examples.swing.editor.EditorAboutFrame;
import com.mxgraph.examples.swing.editor.fileimportexport.IImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.ImportExportPicker;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorMenuBar;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorPopupMenu;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.scxml.SCXMLKeyboardHandler;
import com.mxgraph.examples.swing.editor.scxml.SCXMLSearchTool;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.OpenAction;
import com.mxgraph.examples.swing.editor.scxml.listener.SCXMLListener;
import com.mxgraph.examples.swing.editor.utils.AbstractActionWrapper;
import com.mxgraph.examples.swing.editor.utils.StringUtils;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;


public class SCXMLGraphEditor extends JPanel
{
	public Preferences preferences=Preferences.userRoot();
	private JTextArea scxmlErrorsDialog;
	private ImportExportPicker iep;
	public ImportExportPicker getIOPicker() {return iep;}
	public SCXMLEditorMenuBar menuBar;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * Adds required resources for i18n
	 */
	static
	{
		mxResources.add("com/mxgraph/examples/swing/resources/editor");
	}

	/**
	 * the zoomed in view of the graph (just a small portion of the outline (typically))
	 */
	protected SCXMLGraphComponent graphComponent;

	/**
	 * a summary view of the entire graph
	 */
	protected mxGraphOutline graphOutline;

	private SCXMLListener scxmlListener;
	private SCXMLSearchTool scxmlSearchtool;

	/**
	 * 
	 */
	protected mxUndoManager undoManager;

	/**
	 * 
	 */
	protected String appTitle;

	/**
	 * 
	 */
	protected JLabel statusBar;

	/**
	 * 
	 */
	protected File currentFile;
	protected IImportExport currentFileIOMethod;
	protected Long lastModifiedDate;

	/**
	 * 
	 */
	protected boolean modified = false;

	/**
	 * 
	 */
	protected mxRubberband rubberband;

	/**
	 * 
	 */
	protected mxKeyboardHandler keyboardHandler;

	/**
	 * 
	 */
	protected mxIEventListener undoHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
			updateUndoRedoActionState();
			changeTracker.invoke(null, null);
		}
	};
	private Action undo;
	private Action redo;
	public void setUndoMenuAction(Action externalAction) {
		this.undo=externalAction;
		updateUndoRedoActionState();
	}
	public void setRedoMenuAction(Action externalAction) {
		this.redo=externalAction;
		updateUndoRedoActionState();
	}
	public void updateUndoRedoActionState() {
		if (redo!=null)
			redo.setEnabled(undoManager.canRedo());
		if (undo!=null)
			undo.setEnabled(undoManager.canUndo());
	}

	public SCXMLListener getSCXMLListener() {
		return scxmlListener;
	}
	public SCXMLSearchTool getSCXMLSearchTool() {
		return scxmlSearchtool;
	}
	
	private HashMap<String,SCXMLGraph> file2graph=new HashMap<String, SCXMLGraph>();
	private HashMap<String,SCXMLImportExport> file2importer=new HashMap<String, SCXMLImportExport>();
	public void clearDisplayOutsourcedContentStatus() {
		file2graph.clear();
		file2importer.clear();
	}
	public SCXMLGraph attachOutsourcedContentToThisNode(mxCell ond,SCXMLGraph g,boolean display) throws Exception {
		SCXMLGraph rootg=getGraphComponent().getGraph();
		SCXMLNode v=(SCXMLNode) ond.getValue();
		// get the outsourcing url (SRC field)
		String src=v.getSRC();
		// get the file name, the optional namespace and the optional node name
		// syntax handled: filename#namespace:nodename
		// or filename#nodename
		// or filename
		String file,namespace,node;
		int pos=src.indexOf('#',0);
		if (pos>=0) {
			file=src.substring(0, pos);
			int nmpos=src.indexOf(':',pos);
			if (nmpos>=0) {
				namespace=src.substring(pos+1, nmpos);
				node=src.substring(nmpos+1);
			} else {
				namespace=null;
				node=src.substring(pos+1);
			}
		} else {
			file=src;
			namespace=null;
			node=null;
		}
		if ((namespace!=null) && (node==null)) throw new Exception("node name not given but namespace given in: '"+src+"'");
		String SCXMLnodename=((namespace!=null)?namespace+":":"")+node;
		// normalize the file name to the system absolute path of that file
		File f=new File(file);
		String fileName=f.getName();
		// add the base directory information
		String wd=(getCurrentFile()!=null)?getCurrentFile().getParent():System.getProperty("user.dir");
		f=new File(wd+f.separator+fileName);
		while (!f.exists()) {
			JFileChooser fc = new JFileChooser(wd);
			final String inputFileName=fileName;
			fc.setFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return "Find '"+inputFileName+"' file.";
				}
				
				@Override
				public boolean accept(File f) {
					if (f.getName().equals(inputFileName) || f.isDirectory()) return true;
					else return false;
				}
			});
			fc.setAcceptAllFileFilterUsed(false);
			int rc = fc.showDialog(this, mxResources.get("findFile")+" '"+fileName+"'");
			if (rc == JFileChooser.APPROVE_OPTION) {
				System.out.println("trying this file: '"+fc.getSelectedFile()+"'");
				f=fc.getSelectedFile();
			} else {
				throw new Exception("Aborted by the user.");
			}
		}
		fileName=f.getAbsolutePath();
		// check to see if the required file has already been read
		SCXMLImportExport ie = file2importer.get(fileName);
		SCXMLGraph ig = file2graph.get(fileName);
		if (ig==null) {
			// load the required graph
			assert(!file2importer.containsKey(fileName));
			file2importer.put(file, ie=new SCXMLImportExport());								
			// read the graph, this will throw an exception if something goes wrong
			ie.readInGraph(ig=new SCXMLGraph(), f.getAbsolutePath());
			ig.setEditor(this);
			file2graph.put(file, ig);
		}
		assert((ig!=null) && (ie!=null));
		System.out.println("attaching node: '"+SCXMLnodename+"' from file '"+fileName+"'");
		// check that the requested node is there
		SCXMLNode SCXMLn = ie.getNodeFromSCXMLID(SCXMLnodename);
		if(SCXMLn==null) {
			JOptionPane.showMessageDialog(this,mxResources.get("nodeNotFound")+": '"+SCXMLnodename+"'",mxResources.get("error"),JOptionPane.ERROR_MESSAGE);
			return null;
		} else {
			String internalID=SCXMLn.getInternalID();
			assert(!StringUtils.isEmptyString(internalID));
			// get the cell corresponding to that node
			mxCell oc=ie.getCellFromInternalID(internalID);
			assert(oc!=null);
			// check whether ond has children, issue a warning if it has
			int cc=ond.getChildCount();
			if (cc>0) {
				//  remove all children of ond
				if (display) System.out.println("WARNING: the node: "+v+" has "+cc+" child(ren). Removing all of them.");

				// insure first that the cells are deletable
				Set<Object> descendants=new HashSet<Object>();
				rootg.getAllDescendants(ond, descendants);
				// don't change ond (ond is the original node in the graph (the one to which we are adding the outsourced content))
				descendants.remove(ond);
				for(Object d:descendants) rootg.setCellAsDeletable(d, true);

				Object[] children=new Object[cc];
				for (int i=0;i<cc;i++) children[i]=ond.getChildAt(i);
				rootg.removeCells(children);
			}
			if (display) {
				//  attach copy of oc as only children of ond
				v.setCluster(true); rootg.setCellStyle(v.getStyle(),ond);
				HashMap<Object,Object> original2clone=new HashMap<Object, Object>();
				Object[] noc = g.cloneCells(new Object[]{oc}, false,original2clone);

				// loop through the mapping now created while cloning, if there are
				// any cells that are outsourced add this clone to the list of clones
				// for them in the graph ig.
				for (mxCell c:ig.getOutsourcedNodes()) {
					mxCell clone=(mxCell) original2clone.get(c);
					if (clone!=null) {
						HashSet<mxCell> clones4ThisOriginal = ig.getOriginal2Clones().get(c);
						if (clones4ThisOriginal==null) ig.getOriginal2Clones().put(c,clones4ThisOriginal=new HashSet<mxCell>());
						assert(!clones4ThisOriginal.contains(clone));
						clones4ThisOriginal.add(clone);
					}
				}

				assert(noc.length==1);
				mxCell ocCopy=(mxCell) noc[0];
				rootg.addCell(ocCopy, ond);
				//  block all editing for ocCopy and all its children
				Set<Object> descendants=new HashSet<Object>();
				rootg.getAllDescendants(ocCopy, descendants);
				for(Object d:descendants) {
					rootg.setCellAsDeletable(d, false);
					rootg.setCellAsEditable(d, false);
					//rootg.setCellAsMovable(d, false);
				}
			} else {
				v.setCluster(false); rootg.setCellStyle(v.getStyle(),ond);
			}
			return ig;
		}
	}
	public void displayOutsourcedContentInNode(mxCell node, SCXMLGraph g, boolean display) throws Exception {
		attachOutsourcedContentToThisNode(node, g, display);
	}
	public void displayOutsourcedContent(SCXMLGraph g,boolean display,boolean isRoot) throws Exception {
		// get the nodes that are outsourced
		HashSet<mxCell> onds = g.getOutsourcedNodes();
		for(mxCell ond:onds) {
			// ig contains the graph from which the content of ond (or all its clones) is imported
			SCXMLGraph ig=null;
			// if isRoot is true, use the original node.
			// else: check if there are clones for this original node and use those clones
			if (isRoot) {
				ig=attachOutsourcedContentToThisNode(ond, g, display);
			} else {
				HashSet<mxCell> clones4Ond=g.getOriginal2Clones().get(ond);
				if (clones4Ond!=null)
					for (mxCell clonedOnd:clones4Ond)
						ig=attachOutsourcedContentToThisNode(clonedOnd, g, display);
			}
			// recursively call this function on the graph just created
			if (ig!=null) displayOutsourcedContent(ig,display,false);
		}
	}
	private boolean doDisplayOfOutsourcedContent=false;
	private JCheckBoxMenuItem displayOutsourcedContentMenuItem;
	public void setDisplayOutsourcedContentMenuItem(JCheckBoxMenuItem mi) {
		displayOutsourcedContentMenuItem=mi;
	}
	public boolean isDisplayOfOutsourcedContentSelected() {
		return doDisplayOfOutsourcedContent;
	}
	public void setDisplayOfOutsourcedContentSelected(boolean b) {
		doDisplayOfOutsourcedContent=b;
		if (displayOutsourcedContentMenuItem!=null)
			displayOutsourcedContentMenuItem.setSelected(isDisplayOfOutsourcedContentSelected());
	}
	
	/**
	 * 
	 */
	protected mxIEventListener changeTracker = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			if (undoManager.isUnmodifiedState()) setModified(false);
			else setModified(true);
		}
	};
	
	/**
	 * 
	 */
	public SCXMLGraphEditor(String appTitle, SCXMLGraphComponent component)
	{
		iep=new ImportExportPicker();
		// Stores and updates the frame title
		this.appTitle = appTitle;

		// Stores a reference to the graph and creates the command history
		graphComponent = component;
		final SCXMLGraph graph = graphComponent.getGraph();
		undoManager = new mxUndoManager(100);

		// Updates the modified flag if the graph model changes
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		graph.getModel().addListener(mxEvent.UNDO, undoHandler);
		graph.getView().addListener(mxEvent.UNDO, undoHandler);
		
		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener()
		{
			public void invoke(Object source, mxEventObject evt)
			{
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph.getSelectionCellsForChanges(changes));
			}
		};
		
		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);
		
		// Creates the status bar
		statusBar = createStatusBar();

		// Display some useful information about repaint events
		installRepaintListener();

		// Puts everything together
		setLayout(new BorderLayout());
		add(graphComponent, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);

		updateTitle();
		
		graph.setAutoSizeCells(true);
		graph.setEditor(this);
		graph.setMultigraph(true);
		graph.setAllowDanglingEdges(false);
		graph.setConnectableEdges(false);
		// the following 2 lines are required by the graph validation routines,
		// otherwise a null pointer exception is generated.
		mxMultiplicity[] m={};
		graph.setMultiplicities(m);
		
		preferences = Preferences.userRoot();
	}

	/**
	 * 
	 */
	protected void installHandlers()
	{
		rubberband = new mxRubberband(graphComponent);
		keyboardHandler = new SCXMLKeyboardHandler(graphComponent);
	}

	/**
	 * 
	 */
	protected JLabel createStatusBar()
	{
		JLabel statusBar = new JLabel(mxResources.get("ready"));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		return statusBar;
	}

	/**
	 * 
	 */
	protected void installRepaintListener()
	{
		graphComponent.getGraph().addListener(mxEvent.REPAINT,
				new mxIEventListener()
				{
					public void invoke(Object source, mxEventObject evt)
					{
						String buffer = (graphComponent.getTripleBuffer() != null) ? ""
								: " (unbuffered)";
						mxRectangle dirty = (mxRectangle) evt
								.getProperty("region");

						if (dirty == null)
						{
							status("Repaint all" + buffer);
						}
						else
						{
							status("Repaint: x=" + (int) (dirty.getX()) + " y="
									+ (int) (dirty.getY()) + " w="
									+ (int) (dirty.getWidth()) + " h="
									+ (int) (dirty.getHeight()) + buffer);
						}
					}
				});
	}

	/**
	 * 
	 */
	protected void mouseWheelMoved(MouseWheelEvent e)
	{
		Point graphPoint;
		Point mousePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),graphComponent);
		graphPoint=((SCXMLGraphComponent)graphComponent).mouseCoordToGraphMouseCoord(mousePoint);
		if (e.getWheelRotation() < 0)
		{
			graphComponent.zoomIn(graphPoint);
		}
		else
		{
			graphComponent.zoomOut(graphPoint);
		}

		status(mxResources.get("scale") + ": "
				+ (int) (100 * graphComponent.getGraph().getView().getScale())
				+ "%");
	}

	/**
	 * 
	 */
	protected void showOutlinePopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(mxResources
				.get("magnifyPage"));
		item.setSelected(graphOutline.isFitPage());

		item.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				graphOutline.setFitPage(!graphOutline.isFitPage());
				graphOutline.repaint();
			}
		});

		JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(mxResources
				.get("showLabels"));
		item2.setSelected(graphOutline.isDrawLabels());

		item2.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				graphOutline.setDrawLabels(!graphOutline.isDrawLabels());
				graphOutline.repaint();
			}
		});

		JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(mxResources
				.get("buffering"));
		item3.setSelected(graphOutline.isTripleBuffered());

		item3.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				graphOutline
						.setTripleBuffered(!graphOutline.isTripleBuffered());
				graphOutline.repaint();
			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.add(item);
		menu.add(item2);
		menu.add(item3);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}

	/**
	 * 
	 */
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point screenCoord=e.getLocationOnScreen();
		Point mousePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),graphComponent);
		Point graphPoint=((SCXMLGraphComponent)graphComponent).mouseCoordToGraphMouseCoord(mousePoint);
		SCXMLEditorPopupMenu menu = new SCXMLEditorPopupMenu(this,mousePoint,graphPoint,screenCoord);
		menu.show(graphComponent, mousePoint.x, mousePoint.y);

		e.consume();
	}

	/**
	 * 
	 */
	protected void mouseLocationChanged(MouseEvent e)
	{
		status(e.getX() + ", " + e.getY());
	}

	public int getScroollingAmount(JScrollBar hs, MouseWheelEvent e) {
		return (int) (e.getWheelRotation()*hs.getModel().getExtent()*0.3);
	}
	
	/**
	 * 
	 */
	protected void installListeners()
	{
		// Installs mouse wheel listener for zooming
		MouseWheelListener wheelTracker = new MouseWheelListener()
		{
			/**
			 * 
			 */
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getSource() instanceof mxGraphOutline
						|| e.isControlDown())
				{
					SCXMLGraphEditor.this.mouseWheelMoved(e);
				} else {
					JScrollBar s = (e.isShiftDown())?graphComponent.getHorizontalScrollBar():graphComponent.getVerticalScrollBar();
					if (s!=null) {
						int d=getScroollingAmount(s,e);
						s.setValue(s.getValue()+d);
					}
				}
			}

		};

		// Handles mouse wheel events in the outline and graph component
		graphOutline.addMouseWheelListener(wheelTracker);
		graphComponent.addMouseWheelListener(wheelTracker);

		// Installs the popup menu in the outline
		graphOutline.addMouseListener(new MouseAdapter()
		{

			/**
			 * 
			 */
			public void mousePressed(MouseEvent e)
			{
				// Handles context menu on the Mac where the trigger is on mousepressed
				mouseReleased(e);
			}

			/**
			 * 
			 */
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showOutlinePopupMenu(e);
				}
			}

		});

		// Installs the popup menu in the graph component
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{

			/**
			 * 
			 */
			public void mousePressed(MouseEvent e)
			{
				// Handles context menu on the Mac where the trigger is on mousepressed
				mouseReleased(e);
			}

			/**
			 * 
			 */
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showGraphPopupMenu(e);
				}
			}

		});

		// Installs a mouse motion listener to display the mouse location
		graphComponent.getGraphControl().addMouseMotionListener(
				new MouseMotionListener()
				{

					/*
					 * (non-Javadoc)
					 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
					 */
					public void mouseDragged(MouseEvent e)
					{
						mouseLocationChanged(e);
					}

					/*
					 * (non-Javadoc)
					 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
					 */
					public void mouseMoved(MouseEvent e)
					{
						mouseDragged(e);
					}

				});
	}

	/**
	 * 
	 */
	public void setCurrentFile(File file,IImportExport fie)
	{
		File oldValue = currentFile;
		currentFile = file;
		IImportExport oldFie=currentFileIOMethod;
		currentFileIOMethod = fie; 

		firePropertyChange("currentFile", oldValue, file);
		firePropertyChange("currentFileIO", oldFie, fie);

		if (oldValue != file)
		{
			updateTitle();
		}
		setLastModifiedDate();
	}
	
	public void setLastModifiedDate() {
		File file=getCurrentFile();
		if (file!=null) lastModifiedDate=file.lastModified();
		else lastModifiedDate=null;
	}
	public Long getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * 
	 */
	public File getCurrentFile()
	{
		return currentFile;
	}
	public IImportExport getCurrentFileIO()
	{
		return currentFileIOMethod;
	}

	/**
	 * 
	 * @param modified
	 */
	public void setModified(boolean modified)
	{
		boolean oldValue = this.modified;
		this.modified = modified;

		firePropertyChange("modified", oldValue, modified);

		if (oldValue != modified)
		{
			updateTitle();
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * 
	 */
	public SCXMLGraphComponent getGraphComponent()
	{
		return graphComponent;
	}

	/**
	 * 
	 */
	public mxGraphOutline getGraphOutline()
	{
		return graphOutline;
	}

	/**
	 * 
	 */
	public mxUndoManager getUndoManager()
	{
		return undoManager;
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return
	 */
	public Action bind(String name, final Action action)
	{
		return bind(name, action, null);
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return
	 */
	@SuppressWarnings("serial")
	public AbstractActionWrapper bind(String name, final Action a, String iconUrl)
	{
		return new AbstractActionWrapper(getGraphComponent(),name, a,(iconUrl != null) ? new ImageIcon(SCXMLGraphEditor.class.getResource(iconUrl)) : null);
	}

	/**
	 * 
	 * @param msg
	 */
	public void status(String msg)
	{
		statusBar.setText(msg);
	}

	/**
	 * 
	 */
	public void updateTitle()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			String title = (currentFile != null) ? currentFile
					.getAbsolutePath() : mxResources.get("newDiagram");

			if (modified)
			{
				title += "*";
			}

			frame.setTitle(title + " - " + appTitle);
		}
	}

	/**
	 * 
	 */
	public void about()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			EditorAboutFrame about = new EditorAboutFrame(frame);
			about.setModal(true);

			// Centers inside the application frame
			int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
			int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
			about.setLocation(x, y);

			// Shows the modal dialog and waits
			about.setVisible(true);
		}
	}

	/**
	 * 
	 */
	public void exit()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			frame.dispose();
			scxmlListener.dispose();
			scxmlSearchtool.dispose();
		}
	}

	public static class AskToSaveIfRequired {
		public static boolean check(SCXMLGraphEditor editor) {
			AbstractAction saveA=null;
			ActionEvent saveE=null;
			int answer=JOptionPane.NO_OPTION;
			while (editor.isModified() && ((answer=JOptionPane.showConfirmDialog(editor, mxResources.get("saveChanges"))) == JOptionPane.YES_OPTION)) {
				if (saveA==null) {
					saveA = new SCXMLEditorActions.SaveAction(false);
					saveE =new ActionEvent(editor, 0, "");
				}
				saveA.actionPerformed(saveE);
			}
			if ((answer==JOptionPane.NO_OPTION) || (answer==JOptionPane.YES_OPTION)) {
				return true;
			}
			return false;
		}
	}
	
	public class WindowEventDemo extends JFrame implements WindowListener {

		private SCXMLGraphEditor editor;
		
		public WindowEventDemo(SCXMLGraphEditor e){
			super();
			addWindowListener(this);
			editor=e;
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
			if (AskToSaveIfRequired.check(editor)) {
				editor.exit();
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
	}
	
    private TransferHandler handler = new TransferHandler() {
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            } else {
            	return true;
            }
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            Transferable t = support.getTransferable();

            try {
                List<File> l = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);

                int num=l.size();
                if (num>0) {
                    File f=l.get(0);
                	if (num>1) {
                		JOptionPane.showMessageDialog(SCXMLGraphEditor.this,
                				"Importing only first file: "+f,
                				mxResources.get("warning"),
                				JOptionPane.WARNING_MESSAGE);
                	}
                	OpenAction action = new OpenAction(f);
                	action.actionPerformed(new ActionEvent(SCXMLGraphEditor.this, 0, "", 0));
                }
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return true;
        }
    };	

	public JFrame createFrame(SCXMLGraphEditor editor) throws CorruptIndexException, LockObtainFailedException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		WindowEventDemo frame = new WindowEventDemo(this);
		// the contentPane of the JRootPane is a JPanel (that is the FSMGraphEditor)
		//frame.setContentPane(this);

		//frame.getContentPane().add(this);
		// TODO: create menu bar

		// Creates the graph outline component
		graphOutline = new mxGraphOutline(graphComponent,200,200);
		
		scxmlErrorsDialog=new JTextArea();
		scxmlErrorsDialog.setEditable(false);
		JPanel errorStatus=new JPanel();
		errorStatus.setLayout(new BoxLayout(errorStatus, BoxLayout.Y_AXIS));
		errorStatus.add(new JLabel("Validation errors:"));
		errorStatus.add(new JScrollPane(scxmlErrorsDialog));
		
		JPanel inner = new JPanel();
		inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
		inner.add(errorStatus);
		inner.add(graphOutline);
		
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inner,graphComponent);
		outer.setDividerLocation(200);
		outer.setDividerSize(6);
		outer.setBorder(null);	

		// Puts everything together
		setLayout(new BorderLayout());
		add(outer, BorderLayout.CENTER);
		
		scxmlListener=new SCXMLListener(frame,editor);
		scxmlSearchtool=new SCXMLSearchTool(frame,editor);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(menuBar=new SCXMLEditorMenuBar(editor));
		frame.setSize(870, 640);

		graphComponent.setTransferHandler(handler);
		
		// Updates the frame title
		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();
		updateTitle();

		// Installs automatic validation (use editor.validation = true
		// if you are using an mxEditor instance)
		graphComponent.getGraph().getModel().addListener(mxEvent.CHANGE, new mxIEventListener()
		{
			private StringBuffer warnings=new StringBuffer();
			public void invoke(Object sender, mxEventObject evt)
			{
				warnings.setLength(0);
				if (graphComponent.validateGraph(warnings)) scxmlErrorsDialog.setText("");
				else scxmlErrorsDialog.setText(warnings.toString());
			}
		});
		
		frame.getContentPane().add(this);
		
		return frame;
	}

	/**
	 * Creates and executes the specified layout.
	 * 
	 * @param key Key to be used for getting the label from mxResources and also
	 * to create the layout instance for the commercial graph editor example.
	 * @return
	 */
	@SuppressWarnings("serial")
	public Action graphLayout(final String key)
	{
		final mxIGraphLayout layout = createLayout(key);

		if (layout != null)
		{
			return new AbstractAction(mxResources.get(key))
			{
				public void actionPerformed(ActionEvent e)
				{
					if (layout != null)
					{
						Object cell = graphComponent.getGraph()
								.getSelectionCell();

						if (cell == null
								|| graphComponent.getGraph().getModel()
										.getChildCount(cell) == 0)
						{
							cell = graphComponent.getGraph().getDefaultParent();
						}

						long t0 = System.currentTimeMillis();
						layout.execute(cell);
						status("Layout: " + (System.currentTimeMillis() - t0)
								+ " ms");
					}
				}

			};
		}
		else
		{
			return new AbstractAction(mxResources.get(key))
			{

				public void actionPerformed(ActionEvent e)
				{
					JOptionPane.showMessageDialog(graphComponent, mxResources
							.get("noLayout"));
				}

			};
		}
	}

	/**
	 * Creates a layout instance for the given identifier.
	 */
	protected mxIGraphLayout createLayout(String ident)
	{
		mxIGraphLayout layout = null;

		if (ident != null)
		{
			mxGraph graph = graphComponent.getGraph();

			if (ident.equals("verticalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph);
			}
			else if (ident.equals("horizontalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph, JLabel.WEST);
			}
			else if (ident.equals("verticalTree"))
			{
				layout = new mxCompactTreeLayout(graph, false);
			}
			else if (ident.equals("horizontalTree"))
			{
				layout = new mxCompactTreeLayout(graph, true);
			}
			else if (ident.equals("parallelEdges"))
			{
				layout = new mxParallelEdgeLayout(graph);
			}
			else if (ident.equals("placeEdgeLabels"))
			{
				layout = new mxEdgeLabelLayout(graph);
			}
			else if (ident.equals("organicLayout"))
			{
				layout = new mxFastOrganicLayout(graph);
			}
			if (ident.equals("verticalPartition"))
			{
				layout = new mxPartitionLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalPartition"))
			{
				layout = new mxPartitionLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("verticalStack"))
			{
				layout = new mxStackLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalStack"))
			{
				layout = new mxStackLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("circleLayout"))
			{
				layout = new mxCircleLayout(graph);
			}
		}

		return layout;
	}
	
	/**
	 * main of the editor application.
	 * creates the FSMEditor that is a CustomGraphComponent (JScrollPane)
	 *  contains an instance of CustomGraph (mxGraph that is mxEventSourcE))
	 * create the interface containing the CustomGraphComponent: FSMEditor (FSMGraphEditor (JPanel))
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			mxConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
			SCXMLGraphEditor editor = new SCXMLGraphEditor("FSM Editor", new SCXMLGraphComponent(new SCXMLGraph()));		
			editor.createFrame(editor).setVisible(true);
			editor.getGraphComponent().requestFocusInWindow();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
