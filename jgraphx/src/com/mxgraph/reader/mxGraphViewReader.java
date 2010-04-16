/**
 * $Id: mxGraphViewReader.java,v 1.26 2010/01/13 10:43:46 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.reader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;

/**
 * An abstract converter that renders display XML data onto a canvas.
 */
public abstract class mxGraphViewReader extends DefaultHandler
{

	/**
	 * Holds the canvas to be used for rendering the graph.
	 */
	protected mxICanvas canvas;

	/**
	 * Holds the global scale of the graph. This is set just before
	 * createCanvas is called.
	 */
	protected double scale = 1;

	/**
	 * Specifies if labels should be rendered as HTML markup.
	 */
	protected boolean htmlLabels = false;

	/**
	 * Sets the htmlLabels switch.
	 */
	public void setHtmlLabels(boolean value)
	{
		htmlLabels = value;
	}

	/**
	 * Returns the htmlLabels switch.
	 */
	public boolean isHtmlLabels()
	{
		return htmlLabels;
	}

	/**
	 * Returns the canvas to be used for rendering.
	 * 
	 * @param attrs Specifies the attributes of the new canvas.
	 * @return Returns a new canvas.
	 */
	public abstract mxICanvas createCanvas(Map<String, Object> attrs);

	/**
	 * Returns the canvas that is used for rendering the graph.
	 * 
	 * @return Returns the canvas.
	 */
	public mxICanvas getCanvas()
	{
		return canvas;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException
	{
		String tagName = qName.toUpperCase();
		Map<String, Object> attrs = new Hashtable<String, Object>();

		for (int i = 0; i < atts.getLength(); i++)
		{
			String name = atts.getLocalName(i);

			// Workaround for possible null name
			if (name == null)
			{
				name = atts.getQName(i);
			}

			attrs.put(name, atts.getValue(i));
		}

		parseElement(tagName, attrs);
	}

	/**
	 * Parses the given element and paints it onto the canvas.
	 * 
	 * @param tagName Name of the node to be parsed.
	 * @param attrs Attributes of the node to be parsed.
	 */
	public void parseElement(String tagName, Map<String, Object> attrs)
	{
		if (canvas == null && tagName.equalsIgnoreCase("GRAPH"))
		{
			scale = mxUtils.getDouble(attrs, "scale", 1);
			canvas = createCanvas(attrs);

			if (canvas != null)
			{
				canvas.setScale(scale);
			}
		}
		else if (canvas != null)
		{
			boolean drawLabel = false;

			if (tagName.equalsIgnoreCase("VERTEX")
					|| tagName.equalsIgnoreCase("GROUP"))
			{
				drawVertex(attrs);
				drawLabel = true;
			}
			else if (tagName.equalsIgnoreCase("EDGE"))
			{
				drawEdge(attrs);
				drawLabel = true;
			}

			if (drawLabel)
			{
				drawLabel(tagName.equalsIgnoreCase("EDGE"), attrs);
			}
		}
	}

	/**
	 * Draws the specified vertex using the canvas.
	 * 
	 * @param attrs Specifies the attributes of the vertex.
	 */
	public void drawVertex(Map<String, Object> attrs)
	{
		int width = mxUtils.getInt(attrs, "width");
		int height = mxUtils.getInt(attrs, "height");

		if (width > 0 && height > 0)
		{
			int x = (int) Math.round(mxUtils.getDouble(attrs, "x"));
			int y = (int) Math.round(mxUtils.getDouble(attrs, "y"));

			canvas.drawVertex(x, y, width, height, attrs);
		}
	}

	/**
	 * Draws the specified edge using the canvas.
	 * 
	 * @param attrs Specifies the attribute of the edge.
	 */
	public void drawEdge(Map<String, Object> attrs)
	{
		List<mxPoint> pts = parsePoints(mxUtils.getString(attrs, "points"));

		if (pts.size() > 0)
		{
			canvas.drawEdge(pts, attrs);
		}
	}

	/**
	 * Draws the specified label using the canvas.
	 * 
	 * @param attrs Specifies the attributes of the label.
	 */
	public void drawLabel(boolean isEdge, Map<String, Object> attrs)
	{
		String label = mxUtils.getString(attrs, "label");

		if (label != null && label.length() > 0)
		{
			mxPoint offset = new mxPoint(mxUtils.getDouble(attrs, "dx"),
					mxUtils.getDouble(attrs, "dy"));
			mxRectangle vertexBounds = (!isEdge) ? new mxRectangle(mxUtils
					.getDouble(attrs, "x"), mxUtils.getDouble(attrs, "y"),
					mxUtils.getDouble(attrs, "width"), mxUtils.getDouble(attrs,
							"height")) : null;
			mxRectangle bounds = mxUtils.getLabelPaintBounds(label, attrs,
					mxUtils.isTrue(attrs, "html", false), offset, vertexBounds,
					scale);

			canvas
					.drawLabel(label, (int) Math.round(bounds.getX()),
							(int) Math.round(bounds.getY()), (int) Math
									.round(bounds.getWidth()), (int) Math
									.round(bounds.getHeight()), attrs,
							isHtmlLabels());
		}
	}

	/**
	 * Parses the list of points into an object-oriented representation.
	 * 
	 * @param pts String containing a list of points.
	 * @return Returns the points as a list of mxPoints.
	 */
	public static List<mxPoint> parsePoints(String pts)
	{
		List<mxPoint> result = new ArrayList<mxPoint>();

		if (pts != null)
		{
			int len = pts.length();
			String tmp = "";
			String x = null;

			for (int i = 0; i < len; i++)
			{
				char c = pts.charAt(i);

				if (c == ',' || c == ' ')
				{
					if (x == null)
					{
						x = tmp;
					}
					else
					{
						result.add(new mxPoint(Double.parseDouble(x), Double
								.parseDouble(tmp)));
						x = null;
					}
					tmp = "";
				}
				else
				{
					tmp += c;
				}
			}

			result.add(new mxPoint(Double.parseDouble(x), Double
					.parseDouble(tmp)));
		}

		return result;
	}

}
