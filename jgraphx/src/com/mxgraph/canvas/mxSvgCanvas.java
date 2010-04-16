/**
 * $Id: mxSvgCanvas.java,v 1.50 2010/01/26 12:54:44 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.canvas;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;

/**
 * An implementation of a canvas that uses SVG for painting. This canvas
 * ignores the STYLE_LABEL_BACKGROUNDCOLOR and
 * STYLE_LABEL_BORDERCOLOR styles due to limitations of SVG.
 */
public class mxSvgCanvas extends mxBasicCanvas
{

	/**
	 * Holds the HTML document that represents the canvas.
	 */
	protected Document document;

	/**
	 * Constructs a new SVG canvas for the specified dimension and scale.
	 */
	public mxSvgCanvas()
	{
		this(null);
	}

	/**
	 * Constructs a new SVG canvas for the specified bounds, scale and
	 * background color.
	 */
	public mxSvgCanvas(Document document)
	{
		setDocument(document);
	}

	/**
	 * 
	 */
	public void appendSvgElement(Element node)
	{
		if (document != null)
		{
			document.getDocumentElement().appendChild(node);
		}
	}

	/**
	 * 
	 */
	public void setDocument(Document document)
	{
		this.document = document;
	}

	/**
	 * Returns a reference to the document that represents the canvas.
	 * 
	 * @return Returns the document.
	 */
	public Document getDocument()
	{
		return document;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawVertex(int, int, int, int, java.util.Hashtable)
	 */
	public Object drawVertex(int x, int y, int w, int h,
			Map<String, Object> style)
	{
		Element elem = null;

		x += translate.x;
		y += translate.y;

		if (!mxUtils.getString(style, mxConstants.STYLE_SHAPE, "").equals(
				mxConstants.SHAPE_SWIMLANE))
		{
			elem = drawShape(x, y, w, h, style);
		}
		else
		{
			int start = (int) Math.round(mxUtils.getInt(style,
					mxConstants.STYLE_STARTSIZE, mxConstants.DEFAULT_STARTSIZE)
					* scale);

			// Removes some styles to draw the content area
			Map<String, Object> cloned = new Hashtable<String, Object>(style);
			cloned.remove(mxConstants.STYLE_FILLCOLOR);
			cloned.remove(mxConstants.STYLE_ROUNDED);

			if (mxUtils.isTrue(style, mxConstants.STYLE_HORIZONTAL, true))
			{
				elem = drawShape(x, y, w, start, style);
				drawShape(x, y + start, w, h - start, cloned);
			}
			else
			{
				elem = drawShape(x, y, start, h, style);
				drawShape(x + start, y, w - start, h, cloned);
			}
		}

		return elem;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawEdge(java.util.List, java.util.Hashtable)
	 */
	public Object drawEdge(List<mxPoint> pts, Map<String, Object> style)
	{
		// Transpose all points by cloning into a new array
		pts = mxUtils.translatePoints(pts, translate.x, translate.y);

		// Draws the line
		Element elem = drawLine(pts, style);

		// Applies opacity
		float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY, 100);

		if (opacity != 100)
		{
			String value = String.valueOf(opacity / 100);
			elem.setAttribute("fill-opacity", value);
			elem.setAttribute("stroke-opacity", value);
		}

		return elem;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawLabel(java.lang.String, int, int, int, int, java.util.Hashtable, boolean)
	 */
	public Object drawLabel(String label, int x, int y, int w, int h,
			Map<String, Object> style, boolean isHtml)
	{
		if (drawLabels)
		{
			x += translate.x;
			y += translate.y;

			return drawText(label, x, y, w, h, style);
		}

		return null;
	}

	/**
	 * Draws the shape specified with the STYLE_SHAPE key in the given style.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param style Style of the the shape.
	 */
	public Element drawShape(int x, int y, int w, int h,
			Map<String, Object> style)
	{
		String fillColor = mxUtils.getString(style,
				mxConstants.STYLE_FILLCOLOR, "none");
		String strokeColor = mxUtils.getString(style,
				mxConstants.STYLE_STROKECOLOR);
		float strokeWidth = (float) (mxUtils.getFloat(style,
				mxConstants.STYLE_STROKEWIDTH, 1) * scale);

		// Draws the shape
		String shape = mxUtils.getString(style, mxConstants.STYLE_SHAPE);
		Element elem = null;
		Element background = null;

		if (shape.equals(mxConstants.SHAPE_IMAGE))
		{
			String img = getImageForStyle(style);

			if (img != null)
			{
				elem = document.createElement("image");

				elem.setAttribute("x", String.valueOf(x));
				elem.setAttribute("y", String.valueOf(y));
				elem.setAttribute("width", String.valueOf(w));
				elem.setAttribute("height", String.valueOf(h));

				elem.setAttributeNS(mxConstants.NS_XLINK, "xlink:href", img);
			}
		}
		else if (shape.equals(mxConstants.SHAPE_LINE))
		{
			String direction = mxUtils.getString(style,
					mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
			String d = null;

			if (direction.equals(mxConstants.DIRECTION_EAST)
					|| direction.equals(mxConstants.DIRECTION_WEST))
			{
				int mid = (int) (y + h / 2);
				d = "M " + x + " " + mid + " L " + (x + w) + " " + mid;
			}
			else
			{
				int mid = (int) (x + w / 2);
				d = "M " + mid + " " + y + " L " + mid + " " + (y + h);
			}

			elem = document.createElement("path");
			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_ELLIPSE))
		{
			elem = document.createElement("ellipse");

			elem.setAttribute("cx", String.valueOf(x + w / 2));
			elem.setAttribute("cy", String.valueOf(y + h / 2));
			elem.setAttribute("rx", String.valueOf(w / 2));
			elem.setAttribute("ry", String.valueOf(h / 2));
		}
		else if (shape.equals(mxConstants.SHAPE_DOUBLE_ELLIPSE))
		{
			elem = document.createElement("g");
			background = document.createElement("ellipse");
			background.setAttribute("cx", String.valueOf(x + w / 2));
			background.setAttribute("cy", String.valueOf(y + h / 2));
			background.setAttribute("rx", String.valueOf(w / 2));
			background.setAttribute("ry", String.valueOf(h / 2));
			elem.appendChild(background);

			int inset = (int) ((3 + strokeWidth) * scale);

			Element foreground = document.createElement("ellipse");
			foreground.setAttribute("fill", "none");
			foreground.setAttribute("stroke", strokeColor);
			foreground
					.setAttribute("stroke-width", String.valueOf(strokeWidth));

			foreground.setAttribute("cx", String.valueOf(x + w / 2));
			foreground.setAttribute("cy", String.valueOf(y + h / 2));
			foreground.setAttribute("rx", String.valueOf(w / 2 - inset));
			foreground.setAttribute("ry", String.valueOf(h / 2 - inset));
			elem.appendChild(foreground);
		}
		else if (shape.equals(mxConstants.SHAPE_RHOMBUS))
		{
			elem = document.createElement("path");

			String d = "M " + (x + w / 2) + " " + y + " L " + (x + w) + " "
					+ (y + h / 2) + " L " + (x + w / 2) + " " + (y + h) + " L "
					+ x + " " + (y + h / 2);

			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_TRIANGLE))
		{
			elem = document.createElement("path");
			String direction = mxUtils.getString(style,
					mxConstants.STYLE_DIRECTION, "");
			String d = null;

			if (direction.equals(mxConstants.DIRECTION_NORTH))
			{
				d = "M " + x + " " + (y + h) + " L " + (x + w / 2) + " " + y
						+ " L " + (x + w) + " " + (y + h);
			}
			else if (direction.equals(mxConstants.DIRECTION_SOUTH))
			{
				d = "M " + x + " " + y + " L " + (x + w / 2) + " " + (y + h)
						+ " L " + (x + w) + " " + y;
			}
			else if (direction.equals(mxConstants.DIRECTION_WEST))
			{
				d = "M " + (x + w) + " " + y + " L " + x + " " + (y + h / 2)
						+ " L " + (x + w) + " " + (y + h);
			}
			else
			// east
			{
				d = "M " + x + " " + y + " L " + (x + w) + " " + (y + h / 2)
						+ " L " + x + " " + (y + h);
			}

			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_HEXAGON))
		{
			elem = document.createElement("path");
			String direction = mxUtils.getString(style,
					mxConstants.STYLE_DIRECTION, "");
			String d = null;

			if (direction.equals(mxConstants.DIRECTION_NORTH)
					|| direction.equals(mxConstants.DIRECTION_SOUTH))
			{
				d = "M " + (x + 0.5 * w) + " " + y + " L " + (x + w) + " "
						+ (y + 0.25 * h) + " L " + (x + w) + " "
						+ (y + 0.75 * h) + " L " + (x + 0.5 * w) + " "
						+ (y + h) + " L " + x + " " + (y + 0.75 * h) + " L "
						+ x + " " + (y + 0.25 * h);
			}
			else
			{
				d = "M " + (x + 0.25 * w) + " " + y + " L " + (x + 0.75 * w)
						+ " " + y + " L " + (x + w) + " " + (y + 0.5 * h)
						+ " L " + (x + 0.75 * w) + " " + (y + h) + " L "
						+ (x + 0.25 * w) + " " + (y + h) + " L " + x + " "
						+ (y + 0.5 * h);
			}

			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_CLOUD))
		{
			elem = document.createElement("path");

			String d = "M " + (x + 0.25 * w) + " " + (y + 0.25 * h) + " C "
					+ (x + 0.05 * w) + " " + (y + 0.25 * h) + " " + x + " "
					+ (y + 0.5 * h) + " " + (x + 0.16 * w) + " "
					+ (y + 0.55 * h) + " C " + x + " " + (y + 0.66 * h) + " "
					+ (x + 0.18 * w) + " " + (y + 0.9 * h) + " "
					+ (x + 0.31 * w) + " " + (y + 0.8 * h) + " C "
					+ (x + 0.4 * w) + " " + (y + h) + " " + (x + 0.7 * w) + " "
					+ (y + h) + " " + (x + 0.8 * w) + " " + (y + 0.8 * h)
					+ " C " + (x + w) + " " + (y + 0.8 * h) + " " + (x + w)
					+ " " + (y + 0.6 * h) + " " + (x + 0.875 * w) + " "
					+ (y + 0.5 * h) + " C " + (x + w) + " " + (y + 0.3 * h)
					+ " " + (x + 0.8 * w) + " " + (y + 0.1 * h) + " "
					+ (x + 0.625 * w) + " " + (y + 0.2 * h) + " C "
					+ (x + 0.5 * w) + " " + (y + 0.05 * h) + " "
					+ (x + 0.3 * w) + " " + (y + 0.05 * h) + " "
					+ (x + 0.25 * w) + " " + (y + 0.25 * h);

			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_ACTOR))
		{
			elem = document.createElement("path");
			double width3 = w / 3;

			String d = " M " + x + " " + (y + h) + " C " + x + " "
					+ (y + 3 * h / 5) + " " + x + " " + (y + 2 * h / 5) + " "
					+ (x + w / 2) + " " + (y + 2 * h / 5) + " C "
					+ (x + w / 2 - width3) + " " + (y + 2 * h / 5) + " "
					+ (x + w / 2 - width3) + " " + y + " " + (x + w / 2) + " "
					+ y + " C " + (x + w / 2 + width3) + " " + y + " "
					+ (x + w / 2 + width3) + " " + (y + 2 * h / 5) + " "
					+ (x + w / 2) + " " + (y + 2 * h / 5) + " C " + (x + w)
					+ " " + (y + 2 * h / 5) + " " + (x + w) + " "
					+ (y + 3 * h / 5) + " " + (x + w) + " " + (y + h);

			elem.setAttribute("d", d + " Z");
		}
		else if (shape.equals(mxConstants.SHAPE_CYLINDER))
		{
			elem = document.createElement("g");
			background = document.createElement("path");

			double dy = Math.min(40, Math.floor(h / 5));
			String d = " M " + x + " " + (y + dy) + " C " + x + " "
					+ (y - dy / 3) + " " + (x + w) + " " + (y - dy / 3) + " "
					+ (x + w) + " " + (y + dy) + " L " + (x + w) + " "
					+ (y + h - dy) + " C " + (x + w) + " " + (y + h + dy / 3)
					+ " " + x + " " + (y + h + dy / 3) + " " + x + " "
					+ (y + h - dy);
			background.setAttribute("d", d + " Z");
			elem.appendChild(background);

			Element foreground = document.createElement("path");
			d = "M " + x + " " + (y + dy) + " C " + x + " " + (y + 2 * dy)
					+ " " + (x + w) + " " + (y + 2 * dy) + " " + (x + w) + " "
					+ (y + dy);

			foreground.setAttribute("d", d);
			foreground.setAttribute("fill", "none");
			foreground.setAttribute("stroke", strokeColor);
			foreground
					.setAttribute("stroke-width", String.valueOf(strokeWidth));

			elem.appendChild(foreground);
		}
		else
		{
			elem = document.createElement("rect");

			elem.setAttribute("x", String.valueOf(x));
			elem.setAttribute("y", String.valueOf(y));
			elem.setAttribute("width", String.valueOf(w));
			elem.setAttribute("height", String.valueOf(h));

			if (mxUtils.isTrue(style, mxConstants.STYLE_ROUNDED, false))
			{
				elem.setAttribute("rx", String.valueOf(w
						* mxConstants.RECTANGLE_ROUNDING_FACTOR));
				elem.setAttribute("ry", String.valueOf(h
						* mxConstants.RECTANGLE_ROUNDING_FACTOR));
			}
		}

		Element bg = background;

		if (bg == null)
		{
			bg = elem;
		}

		bg.setAttribute("fill", fillColor);
		bg.setAttribute("stroke", strokeColor);
		bg.setAttribute("stroke-width", String.valueOf(strokeWidth));

		// Adds the shadow element
		Element shadowElement = null;

		if (mxUtils.isTrue(style, mxConstants.STYLE_SHADOW, false)
				&& !fillColor.equals("none"))
		{
			shadowElement = (Element) bg.cloneNode(true);

			shadowElement.setAttribute("transform",
					mxConstants.SVG_SHADOWTRANSFORM);
			shadowElement.setAttribute("fill", mxConstants.W3C_SHADOWCOLOR);
			shadowElement.setAttribute("stroke", mxConstants.W3C_SHADOWCOLOR);
			shadowElement.setAttribute("stroke-width", String
					.valueOf(strokeWidth));

			appendSvgElement(shadowElement);
		}

		// Applies rotation
		double rotation = mxUtils.getDouble(style, mxConstants.STYLE_ROTATION);

		if (rotation != 0)
		{
			int cx = x + w / 2;
			int cy = y + h / 2;

			elem.setAttribute("transform", "rotate(" + rotation + "," + cx
					+ "," + cy + ")");

			if (shadowElement != null)
			{
				shadowElement.setAttribute("transform", "rotate(" + rotation
						+ "," + cx + "," + cy + ") "
						+ mxConstants.SVG_SHADOWTRANSFORM);
			}
		}

		// Applies opacity
		float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY, 100);

		if (opacity != 100)
		{
			String value = String.valueOf(opacity / 100);
			elem.setAttribute("fill-opacity", value);
			elem.setAttribute("stroke-opacity", value);

			if (shadowElement != null)
			{
				shadowElement.setAttribute("fill-opacity", value);
				shadowElement.setAttribute("stroke-opacity", value);
			}
		}

		appendSvgElement(elem);

		return elem;
	}

	/**
	 * Draws the given lines as segments between all points of the given list
	 * of mxPoints.
	 * 
	 * @param pts List of points that define the line.
	 * @param style Style to be used for painting the line.
	 */
	public Element drawLine(List<mxPoint> pts, Map<String, Object> style)
	{
		Element group = document.createElement("g");
		Element path = document.createElement("path");

		String strokeColor = mxUtils.getString(style,
				mxConstants.STYLE_STROKECOLOR);
		float tmpStroke = (float) (mxUtils.getFloat(style,
				mxConstants.STYLE_STROKEWIDTH, 1));
		float strokeWidth = (float) (tmpStroke * scale);

		if (strokeColor != null && strokeWidth > 0)
		{
			// Draws the start marker
			Object marker = style.get(mxConstants.STYLE_STARTARROW);

			mxPoint pt = pts.get(1);
			mxPoint p0 = pts.get(0);
			mxPoint offset = null;

			if (marker != null)
			{
				float size = (float) (mxUtils.getFloat(style,
						mxConstants.STYLE_STARTSIZE,
						mxConstants.DEFAULT_MARKERSIZE));
				offset = drawMarker(group, marker, pt, p0, size, tmpStroke,
						strokeColor);
			}
			else
			{
				double dx = pt.getX() - p0.getX();
				double dy = pt.getY() - p0.getY();

				double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
				double nx = dx * strokeWidth / dist;
				double ny = dy * strokeWidth / dist;

				offset = new mxPoint(nx / 2, ny / 2);
			}

			// Applies offset to the point
			if (offset != null)
			{
				p0 = (mxPoint) p0.clone();
				p0.setX(p0.getX() + offset.getX());
				p0.setY(p0.getY() + offset.getY());

				offset = null;
			}

			// Draws the end marker
			marker = style.get(mxConstants.STYLE_ENDARROW);

			pt = pts.get(pts.size() - 2);
			mxPoint pe = pts.get(pts.size() - 1);

			if (marker != null)
			{
				float size = (float) (mxUtils.getFloat(style,
						mxConstants.STYLE_ENDSIZE,
						mxConstants.DEFAULT_MARKERSIZE));
				offset = drawMarker(group, marker, pt, pe, size, tmpStroke,
						strokeColor);
			}
			else
			{
				double dx = pt.getX() - p0.getX();
				double dy = pt.getY() - p0.getY();

				double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
				double nx = dx * strokeWidth / dist;
				double ny = dy * strokeWidth / dist;

				offset = new mxPoint(nx / 2, ny / 2);
			}

			// Applies offset to the point
			if (offset != null)
			{
				pe = (mxPoint) pe.clone();
				pe.setX(pe.getX() + offset.getX());
				pe.setY(pe.getY() + offset.getY());

				offset = null;
			}

			// Draws the line segments
			pt = p0;
			String d = "M " + pt.getX() + " " + pt.getY();

			for (int i = 1; i < pts.size() - 1; i++)
			{
				pt = pts.get(i);
				d += " L " + pt.getX() + " " + pt.getY();
			}

			d += " L " + pe.getX() + " " + pe.getY();

			path.setAttribute("d", d);
			path.setAttribute("stroke", strokeColor);
			path.setAttribute("fill", "none");
			path.setAttribute("stroke-width", String.valueOf(strokeWidth));

			group.appendChild(path);
			appendSvgElement(group);
		}

		return group;
	}

	/**
	 * Draws the specified marker as a child path in the given parent.
	 */
	public mxPoint drawMarker(Element parent, Object type, mxPoint p0,
			mxPoint pe, float size, float strokeWidth, String color)
	{
		mxPoint offset = null;

		// Computes the norm and the inverse norm
		double dx = pe.getX() - p0.getX();
		double dy = pe.getY() - p0.getY();

		double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
		double absSize = size * scale;
		double nx = dx * absSize / dist;
		double ny = dy * absSize / dist;

		pe = (mxPoint) pe.clone();
		pe.setX(pe.getX() - nx * strokeWidth / (2 * size));
		pe.setY(pe.getY() - ny * strokeWidth / (2 * size));

		nx *= 0.5 + strokeWidth / 2;
		ny *= 0.5 + strokeWidth / 2;

		Element path = document.createElement("path");
		path.setAttribute("stroke-width", String.valueOf(strokeWidth * scale));
		path.setAttribute("stroke", color);
		path.setAttribute("fill", color);

		String d = null;

		if (type.equals(mxConstants.ARROW_CLASSIC)
				|| type.equals(mxConstants.ARROW_BLOCK))
		{
			d = "M "
					+ pe.getX()
					+ " "
					+ pe.getY()
					+ " L "
					+ (pe.getX() - nx - ny / 2)
					+ " "
					+ (pe.getY() - ny + nx / 2)
					+ ((!type.equals(mxConstants.ARROW_CLASSIC)) ? "" : " L "
							+ (pe.getX() - nx * 3 / 4) + " "
							+ (pe.getY() - ny * 3 / 4)) + " L "
					+ (pe.getX() + ny / 2 - nx) + " "
					+ (pe.getY() - ny - nx / 2) + " z";
		}
		else if (type.equals(mxConstants.ARROW_OPEN))
		{
			nx *= 1.2;
			ny *= 1.2;

			d = "M " + (pe.getX() - nx - ny / 2) + " "
					+ (pe.getY() - ny + nx / 2) + " L " + (pe.getX() - nx / 6)
					+ " " + (pe.getY() - ny / 6) + " L "
					+ (pe.getX() + ny / 2 - nx) + " "
					+ (pe.getY() - ny - nx / 2) + " M " + pe.getX() + " "
					+ pe.getY();
			path.setAttribute("fill", "none");
		}
		else if (type.equals(mxConstants.ARROW_OVAL))
		{
			nx *= 1.2;
			ny *= 1.2;
			absSize *= 1.2;

			d = "M " + (pe.getX() - ny / 2) + " " + (pe.getY() + nx / 2)
					+ " a " + (absSize / 2) + " " + (absSize / 2) + " 0  1,1 "
					+ (nx / 8) + " " + (ny / 8) + " z";
		}
		else if (type.equals(mxConstants.ARROW_DIAMOND))
		{
			d = "M " + (pe.getX() + nx / 2) + " " + (pe.getY() + ny / 2)
					+ " L " + (pe.getX() - ny / 2) + " " + (pe.getY() + nx / 2)
					+ " L " + (pe.getX() - nx / 2) + " " + (pe.getY() - ny / 2)
					+ " L " + (pe.getX() + ny / 2) + " " + (pe.getY() - nx / 2)
					+ " z";
		}

		if (d != null)
		{
			path.setAttribute("d", d);
			parent.appendChild(path);
		}

		return offset;
	}

	/**
	 * Draws the specified text either using drawHtmlString or using drawString.
	 * 
	 * @param text Text to be painted.
	 * @param x X-coordinate of the text.
	 * @param y Y-coordinate of the text.
	 * @param w Width of the text.
	 * @param h Height of the text.
	 * @param style Style to be used for painting the text.
	 */
	public Object drawText(String text, int x, int y, int w, int h,
			Map<String, Object> style)
	{
		Element elem = null;
		String fontColor = mxUtils.getString(style,
				mxConstants.STYLE_FONTCOLOR, "black");
		String fontFamily = mxUtils.getString(style,
				mxConstants.STYLE_FONTFAMILY, mxConstants.DEFAULT_FONTFAMILIES);
		int fontSize = (int) (mxUtils.getInt(style, mxConstants.STYLE_FONTSIZE,
				mxConstants.DEFAULT_FONTSIZE) * scale);

		if (text != null && text.length() > 0)
		{
			elem = document.createElement("text");

			// Applies the opacity
			float opacity = mxUtils.getFloat(style,
					mxConstants.STYLE_TEXT_OPACITY, 100);

			if (opacity != 100)
			{
				String value = String.valueOf(opacity / 100);
				elem.setAttribute("fill-opacity", value);
				elem.setAttribute("stroke-opacity", value);
			}

			elem.setAttribute("text-anchor", "middle");
			elem.setAttribute("font-weight", "normal");
			elem.setAttribute("font-decoration", "none");

			elem.setAttribute("font-size", String.valueOf(fontSize));
			elem.setAttribute("font-family", fontFamily);
			elem.setAttribute("fill", fontColor);

			String[] lines = text.split("\n");
			y += fontSize
					+ (h - lines.length * (fontSize + mxConstants.LINESPACING))
					/ 2 - 2;

			for (int i = 0; i < lines.length; i++)
			{
				Element tspan = document.createElement("tspan");

				tspan.setAttribute("x", String.valueOf(x + w / 2));
				tspan.setAttribute("y", String.valueOf(y));

				tspan.appendChild(document.createTextNode(lines[i]));
				elem.appendChild(tspan);

				y += fontSize + mxConstants.LINESPACING;
			}

			appendSvgElement(elem);
		}

		return elem;
	}

}
