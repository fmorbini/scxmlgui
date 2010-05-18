package com.mxgraph.examples.swing.editor.scxml;

import java.awt.BorderLayout;
import java.awt.Point;
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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

import com.mxgraph.examples.swing.SCXMLEditor;
import com.mxgraph.examples.swing.editor.EditorAboutFrame;
import com.mxgraph.examples.swing.editor.fileimportexport.IImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.ImportExportPicker;
import com.mxgraph.examples.swing.editor.listener.SCXMLListener;
import com.mxgraph.examples.swing.editor.utils.AbstractActionWrapper;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

public class SCXMLGraphEditor extends JPanel
{
	private JTextArea scxmlErrorsDialog;
	private ImportExportPicker iep;
	public ImportExportPicker getIOPicker() {return iep;}
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
		final mxGraph graph = graphComponent.getGraph();
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
		Point mousePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),graphComponent);
		Point graphPoint=((SCXMLGraphComponent)graphComponent).mouseCoordToGraphMouseCoord(mousePoint);
		SCXMLEditorPopupMenu menu = new SCXMLEditorPopupMenu(this,mousePoint,graphPoint);
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
		return (int) (e.getWheelRotation()*hs.getModel().getExtent()*0.7);
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

	public JFrame createFrame(SCXMLEditor editor)
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
		
		scxmlListener=new SCXMLListener(editor);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(new SCXMLEditorMenuBar(editor));
		frame.setSize(870, 640);

		
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
}
