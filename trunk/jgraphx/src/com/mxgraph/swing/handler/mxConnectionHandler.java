/**
 * $Id: mxConnectionHandler.java,v 1.20 2010/02/04 08:10:23 gaudenz Exp $
 * Copyright (c) 2008, Gaudenz Alder
 */
package com.mxgraph.swing.handler;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxGraphControl;
import com.mxgraph.swing.util.mxMouseControl;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxPerimeter.mxPerimeterFunction;

/**
 * Connection handler creates new connections between cells. This control is used to display the connector
 * icon, while the preview is used to draw the line.
 */
public class mxConnectionHandler extends mxMouseControl
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2543899557644889853L;

	/**
	 * 
	 */
	public static Cursor DEFAULT_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	/**
	 * 
	 */
	protected mxGraphComponent graphComponent;

	/**
	 * Specifies the icon to be used for creating new connections. If this is
	 * specified then it is used instead of the handle. Default is null.
	 */
	protected ImageIcon connectIcon = null;

	/**
	 * Specifies the size of the handle to be used for creating new
	 * connections. Default is mxConstants.CONNECT_HANDLE_SIZE. 
	 */
	protected int handleSize = mxConstants.CONNECT_HANDLE_SIZE;

	/**
	 * Specifies if a handle should be used for creating new connections. This
	 * is only used if no connectIcon is specified. If this is false, then the
	 * source cell will be highlighted when the mouse is over the hotspot given
	 * in the marker. Default is mxConstants.CONNECT_HANDLE_ENABLED.
	 */
	protected boolean handleEnabled = mxConstants.CONNECT_HANDLE_ENABLED;

	/**
	 * 
	 */
	protected boolean select = true;

	/**
	 * Specifies if the source should be cloned and used as a target if no
	 * target was selected. Default is false.
	 */
	protected boolean createTarget = false;

	/**
	 * Appearance and event handling order wrt subhandles.
	 */
	protected boolean keepOnTop = true;

	/**
	 * 
	 */
	protected transient Point start, current;

	/**
	 * 
	 */
	protected transient mxCellState source;

	/**
	 * 
	 */
	protected transient mxCellMarker marker;

	/**
	 * 
	 */
	protected transient String error;

	/**
	 * 
	 */
	protected transient JPanel preview = new JPanel()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6401041861368362818L;

		public void paint(Graphics g)
		{
			super.paint(g);
			((Graphics2D) g).setStroke(mxConstants.PREVIEW_STROKE);

			if (start != null && current != null)
			{
				if (marker.hasValidState() || createTarget
						|| graphComponent.getGraph().isAllowDanglingEdges())
				{
					g.setColor(mxConstants.DEFAULT_VALID_COLOR);
				}
				else
				{
					g.setColor(mxConstants.DEFAULT_INVALID_COLOR);
				}

				g.drawLine(start.x - getX(), start.y - getY(), current.x
						- getX(), current.y - getY());
			}
		}
	};

	/**
	 * 
	 */
	protected transient mxIEventListener resetHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			reset();
		}
	};

	/**
	 * 
	 * @param graphComponent
	 */
	public mxConnectionHandler(mxGraphComponent graphComponent)
	{
		this.graphComponent = graphComponent;

		mxGraphControl graphControl = graphComponent.getGraphControl();
		graphControl.add(this, 0);
		graphControl.addMouseListener(this);
		graphControl.addMouseMotionListener(this);

		mxGraphView view = graphComponent.getGraph().getView();
		view.addListener(mxEvent.SCALE, resetHandler);
		view.addListener(mxEvent.TRANSLATE, resetHandler);
		view.addListener(mxEvent.SCALE_AND_TRANSLATE, resetHandler);

		mxIGraphModel model = graphComponent.getGraph().getModel();
		model.addListener(mxEvent.CHANGE, resetHandler);

		marker = new mxCellMarker(graphComponent)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 103433247310526381L;

			// Overrides to return cell at location only if valid (so that
			// there is no highlight for invalid cells that have no error
			// message when the mouse is released)
			protected Object getCell(MouseEvent e)
			{
				Object cell = super.getCell(e);

				if (isConnecting())
				{
					if (source != null)
					{
						error = validateConnection(source.getCell(), cell);

						if (error != null && error.length() == 0)
						{
							cell = null;

							// Enables create target inside groups
							if (createTarget)
							{
								error = null;
							}
						}
					}
				}
				else if (!isValidSource(cell))
				{
					cell = null;
				}

				return cell;
			}

			// Sets the highlight color according to isValidConnection
			protected boolean isValidState(mxCellState state)
			{
				if (isConnecting())
				{
					return error == null;
				}
				else
				{
					return super.isValidState(state);
				}
			}

			// Overrides to use marker color only in highlight mode or for
			// target selection
			protected Color getMarkerColor(MouseEvent e, mxCellState state,
					boolean isValid)
			{
				return (isHighlighting() || isConnecting()) ? super
						.getMarkerColor(e, state, isValid) : null;
			}

			// Overrides to use hotspot only for source selection otherwise
			// intersects always returns true when over a cell
			protected boolean intersects(mxCellState state, MouseEvent e)
			{
				if (!isHighlighting() || isConnecting())
				{
					return true;
				}

				return super.intersects(state, e);
			}
		};

		marker.setHotspotEnabled(true);
		setCursor(DEFAULT_CURSOR);
	}

	/**
	 * Returns true if the source terminal has been clicked and a new
	 * connection is currently being previewed.
	 */
	public boolean isConnecting()
	{
		return start != null && preview != null && preview.isVisible();
	}

	/**
	 * Returns true if no connectIcon is specified and handleEnabled is false.
	 */
	public boolean isHighlighting()
	{
		return connectIcon == null && !handleEnabled;
	}

	/**
	 * 
	 */
	public boolean isKeepOnTop()
	{
		return keepOnTop;
	}

	/**
	 * 
	 */
	public void setKeepOnTop(boolean keepOnTop)
	{
		this.keepOnTop = keepOnTop;
	}

	/**
	 * 
	 */
	public void setConnectIcon(ImageIcon connectIcon)
	{
		this.connectIcon = connectIcon;
	}

	/**
	 * 
	 */
	public ImageIcon getConnecIcon()
	{
		return connectIcon;
	}

	/**
	 * 
	 */
	public void setHandleEnabled(boolean handleEnabled)
	{
		this.handleEnabled = handleEnabled;
	}

	/**
	 * 
	 */
	public boolean isHandleEnabled()
	{
		return handleEnabled;
	}

	/**
	 * 
	 */
	public void setHandleSize(int size)
	{
		this.handleSize = size;
	}

	/**
	 * 
	 */
	public int getHandleSize()
	{
		return handleSize;
	}

	/**
	 * 
	 */
	public mxCellMarker getMarker()
	{
		return marker;
	}

	/**
	 * 
	 */
	public void setMarker(mxCellMarker marker)
	{
		this.marker = marker;
	}

	/**
	 * 
	 */
	public void setCreateTarget(boolean createTarget)
	{
		this.createTarget = createTarget;
	}

	/**
	 * 
	 */
	public boolean isCreateTarget()
	{
		return createTarget;
	}

	/**
	 * 
	 */
	public void setSelect(boolean select)
	{
		this.select = select;
	}

	/**
	 * 
	 */
	public boolean isSelect()
	{
		return select;
	}

	/**
	 * 
	 */
	public void reset()
	{
		if (preview.getParent() != null)
		{
			preview.setVisible(false);
			preview.getParent().remove(preview);
		}

		setVisible(false);
		marker.reset();
		source = null;
		start = null;
		error = null;
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @param e
	 */
	protected void connect(Object source, Object target, MouseEvent e)
	{
		mxGraph graph = graphComponent.getGraph();
		mxIGraphModel model = graph.getModel();

		Object newTarget = null;
		Object edge = null;

		if (target == null && createTarget)
		{
			newTarget = createTargetVertex(e, source);
			target = newTarget;
		}

		if (target != null || graph.isAllowDanglingEdges())
		{
			model.beginUpdate();
			try
			{
				Object dropTarget = graph.getDropTarget(
						new Object[] { newTarget }, e.getPoint(),
						graphComponent.getCellAt(e.getX(), e.getY()));

				if (newTarget != null)
				{
					// Disables edges as drop targets if the target cell was created
					if (dropTarget == null
							|| !graph.getModel().isEdge(dropTarget))
					{
						mxCellState pstate = graph.getView().getState(
								dropTarget);

						if (pstate != null)
						{
							mxGeometry geo = model.getGeometry(newTarget);

							mxPoint origin = pstate.getOrigin();
							geo.setX(geo.getX() - origin.getX());
							geo.setY(geo.getY() - origin.getY());
						}
					}
					else
					{
						dropTarget = graph.getDefaultParent();
					}

					graph.addCells(new Object[] { newTarget }, dropTarget);
				}

				Object parent = graph.getDefaultParent();

				if (model.getParent(source) == model.getParent(target))
				{
					parent = model.getParent(source);
				}

				edge = insertEdge(parent, null, "", source, target);

				if (edge != null)
				{
					// Makes sure the edge has a non-null, relative geometry
					mxGeometry geo = model.getGeometry(edge);

					if (geo == null)
					{
						geo = new mxGeometry();
						geo.setRelative(true);

						model.setGeometry(edge, geo);
					}

					if (target == null)
					{
						mxPoint pt = graphComponent.getPointForEvent(e);
						geo.setTerminalPoint(pt, false);
					}
				}

			}
			finally
			{
				model.endUpdate();
			}
		}

		if (select)
		{
			graph.setSelectionCell(edge);
		}
	}

	/**
	 * Creates, inserts and returns a new edge using mxGraph.insertEdge. 
	 */
	protected Object insertEdge(Object parent, String id, Object value,
			Object source, Object target)
	{
		return graphComponent.getGraph().insertEdge(parent, id, value, source,
				target);
	}

	/**
	 * 
	 */
	public Object createTargetVertex(MouseEvent e, Object source)
	{
		mxGraph graph = graphComponent.getGraph();
		Object clone = graph.cloneCells(new Object[] { source })[0];
		mxIGraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(clone);

		if (geo != null)
		{
			mxPoint point = graphComponent.getPointForEvent(e);
			geo.setX(graph.snap(point.getX() - geo.getWidth() / 2));
			geo.setY(graph.snap(point.getY() - geo.getHeight() / 2));
		}

		return clone;
	}

	/**
	 * 
	 */
	public boolean isValidSource(Object cell)
	{
		return graphComponent.getGraph().isValidSource(cell);
	}

	/**
	 * Returns true. The call to mxGraph.isValidTarget is implicit by calling
	 * mxGraph.getEdgeValidationError in validateConnection. This is an
	 * additional hook for disabling certain targets in this specific handler.
	 */
	public boolean isValidTarget(Object cell)
	{
		return true;
	}

	/**
	 * Returns the error message or an empty string if the connection for the
	 * given source target pair is not valid. Otherwise it returns null.
	 */
	public String validateConnection(Object source, Object target)
	{
		if (target == null && createTarget)
		{
			return null;
		}

		if (!isValidTarget(target))
		{
			return "";
		}

		return graphComponent.getGraph().getEdgeValidationError(null, source,
				target);
	}

	/**
	 * 
	 */
	public void mousePressed(MouseEvent e)
	{
		if (!graphComponent.isForceMarqueeEvent(e) &&
			!graphComponent.isPanningEvent(e))
		{
			// Subhandles in the graph handler have precedence over this
			// if keepOnTop is false, otherwise this has precendence
			if (!e.isConsumed() && source != null && !e.isPopupTrigger())
			{
				if ((isHighlighting() && marker.hasValidState())
						|| (!isHighlighting() && getBounds().contains(
								e.getPoint())))
				{
					start = e.getPoint();
					preview.setOpaque(false);
					preview.setVisible(false);
					graphComponent.getGraphControl().add(preview, 0);
					getParent().setComponentZOrder(this, 0);
					graphComponent.getGraphControl().setCursor(DEFAULT_CURSOR);
					marker.reset();
					e.consume();
				}
			}
		}
	}

	/**
	 * 
	 */
	public void mouseDragged(MouseEvent e)
	{
		if (!e.isConsumed() && source != null && start != null)
		{
			//System.out.println("mouse dragged in mxConnectionHandler");
			int dx = e.getX() - start.x;
			int dy = e.getY() - start.y;

			if (!preview.isVisible() && graphComponent.isSignificant(dx, dy))
			{
				preview.setVisible(true);
				marker.reset();
			}

			current = e.getPoint();
			mxGraph graph = graphComponent.getGraph();
			mxGraphView view = graph.getView();
			double scale = view.getScale();
			mxPoint trans = view.getTranslate();

			current.x = (int) Math.round((graph.snap(current.x / scale
					- trans.getX()) + trans.getX())
					* scale);
			current.y = (int) Math.round((graph.snap(current.y / scale
					- trans.getY()) + trans.getY())
					* scale);

			marker.process(e);

			// Checks if a color was used to highlight the state
			mxCellState state = marker.getValidState();

			if (state != null)
			{
				current.x = (int) state.getCenterX();
				current.y = (int) state.getCenterY();

				// Computes the target perimeter point
				mxPerimeterFunction targetPerimeter = view
						.getPerimeterFunction(state);

				if (targetPerimeter != null)
				{
					mxPoint next = new mxPoint(source.getCenterX(), source
							.getCenterY());
					mxPoint tmp = targetPerimeter.apply(view
							.getPerimeterBounds(state, null, false), null,
							state, false, next);

					if (tmp != null)
					{
						current = tmp.getPoint();
					}
				}
			}

			// Computes the source perimeter point
			mxPerimeterFunction sourcePerimeter = view
					.getPerimeterFunction(source);

			if (sourcePerimeter != null)
			{
				mxPoint pt = sourcePerimeter.apply(view.getPerimeterBounds(
						source, null, true), null, source, true, new mxPoint(
						current));

				if (pt != null)
				{
					start = pt.getPoint();
				}
			}
			else
			{
				start = new Point((int) Math.round(source.getCenterX()),
						(int) Math.round(source.getCenterY()));
			}

			// Hides the connect icon or handle
			setVisible(false);

			// Updates the bounds of the previewed line
			Rectangle bounds = new Rectangle(current);
			bounds.add(start);
			bounds.grow(1, 1);
			preview.setBounds(bounds);

			e.consume();
		}
	}

	/**
	 * 
	 */
	public void mouseReleased(MouseEvent e)
	{
		if (!e.isConsumed() && isConnecting())
		{
			// Does not connect if there is an error
			if (error != null)
			{
				if (error.length() > 0)
				{
					JOptionPane.showMessageDialog(graphComponent, error);
				}
			}
			else if (source != null)
			{
				Object src = source.getCell();
				Object trg = (marker.hasValidState()) ? marker.getValidState()
						.getCell() : null;
				connect(src, trg, e);
			}

			e.consume();
		}
		else if (source != null && !e.isPopupTrigger())
		{
			graphComponent.selectCellForEvent(source.getCell(), e);
		}

		reset();
	}

	/**
	 * 
	 */
	public void mouseMoved(MouseEvent e)
	{
		if (!e.isConsumed() && graphComponent.isEnabled() && isEnabled())
		{
			source = marker.process(e);

			if (isHighlighting() && !marker.hasValidState())
			{
				source = null;
			}

			if (source != null)
			{
				if (isHighlighting())
				{
					// Displays the connect icon on the complete highlighted area
					setBounds(source.getRectangle());
				}
				else
				{
					int imgWidth = handleSize;
					int imgHeight = handleSize;

					if (connectIcon != null)
					{
						imgWidth = connectIcon.getIconWidth();
						imgHeight = connectIcon.getIconHeight();
					}

					int x = (int) source.getCenterX() - imgWidth / 2;
					int y = (int) source.getCenterY() - imgHeight / 2;

					if (graphComponent.getGraph().isSwimlane(source.getCell()))
					{
						mxRectangle size = graphComponent.getGraph()
								.getStartSize(source.getCell());

						if (size.getWidth() > 0)
						{
							x = (int) (source.getX() + size.getWidth() / 2 - imgWidth / 2);
						}
						else
						{
							y = (int) (source.getY() + size.getHeight() / 2 - imgHeight / 2);
						}
					}

					setBounds(x, y, imgWidth, imgHeight);
				}

				if (keepOnTop)
				{
					getParent().setComponentZOrder(this, 0);
				}
			}

			setVisible(source != null);
		}
	}

	/**
	 * 
	 */
	public void paint(Graphics g)
	{
		if (start == null)
		{
			if (connectIcon != null)
			{
				g.drawImage(connectIcon.getImage(), 0, 0, getWidth(),
						getHeight(), this);
			}
			else if (handleEnabled)
			{
				g.setColor(Color.BLACK);
				g.draw3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
				g.setColor(Color.GREEN);
				g.fill3DRect(1, 1, getWidth() - 2, getHeight() - 2, true);
				g.setColor(Color.BLUE);
				g.drawRect(getWidth() / 2 - 1, getHeight() / 2 - 1, 1, 1);
			}
		}
	}
}
