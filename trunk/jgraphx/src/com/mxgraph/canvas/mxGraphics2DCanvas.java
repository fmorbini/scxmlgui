/**
 * $Id: mxGraphics2DCanvas.java,v 1.158 2010/02/26 08:54:13 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.canvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxLighweightLabel;
import com.mxgraph.util.mxLighweightTextPane;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;

/**
 * An implementation of a canvas that uses Graphics2D for painting.
 */
public class mxGraphics2DCanvas extends mxBasicCanvas
{

	/**
	 * Cache for images.
	 */
	protected Hashtable<String, Image> imageCache = new Hashtable<String, Image>();

	/**
	 * Specifies if linefeeds should be replaced with breaks in HTML markup.
	 * Default is true.
	 */
	protected boolean replaceHtmlLinefeeds = true;

	/**
	 * Optional renderer pane to be used for HTML label rendering.
	 */
	protected CellRendererPane rendererPane;

	/**
	 * Global graphics handle to the image.
	 */
	protected Graphics2D g;

	/**
	 * Constructs a new graphics canvas with an empty graphics object.
	 */
	public mxGraphics2DCanvas()
	{
		this(null);
	}

	/**
	 * Constructs a new graphics canvas for the given graphics object.
	 */
	public mxGraphics2DCanvas(Graphics2D g)
	{
		this.g = g;

		// Initializes the cell renderer pane for drawing HTML markup
		try
		{
			rendererPane = new CellRendererPane();
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	/**
	 * Returns replaceHtmlLinefeeds
	 */
	public boolean isReplaceHtmlLinefeeds()
	{
		return replaceHtmlLinefeeds;
	}

	/**
	 * Returns replaceHtmlLinefeeds
	 */
	public void setReplaceHtmlLinefeeds(boolean value)
	{
		replaceHtmlLinefeeds = value;
	}

	/**
	 * Returns the graphics object for this canvas.
	 */
	public Graphics2D getGraphics()
	{
		return g;
	}

	/**
	 * Sets the graphics object for this canvas.
	 */
	public void setGraphics(Graphics2D g)
	{
		this.g = g;
	}

	/**
	 * Returns an image instance for the given URL. If the URL has
	 * been loaded before than an instance of the same instance is
	 * returned as in the previous call.
	 */
	protected Image loadImage(String image)
	{
		Image img = imageCache.get(image);

		if (img == null)
		{
			img = mxUtils.loadImage(image);

			if (img != null)
			{
				imageCache.put(image, img);
			}
		}

		return img;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawVertex(int, int, int, int, java.util.Hashtable)
	 */
	public Object drawVertex(int x, int y, int w, int h,
			Map<String, Object> style)
	{
		if (g != null)
		{
			x += translate.x;
			y += translate.y;

			// Applies the rotation on the graphics object and stores
			// the previous transform so that it can be restored
			AffineTransform transform = null;
			double rotation = mxUtils.getDouble(style,
					mxConstants.STYLE_ROTATION, 0);

			if (rotation != 0)
			{
				transform = g.getTransform();
				g.rotate(Math.toRadians(rotation), x + w / 2, y + h / 2);
			}

			Composite composite = null;
			float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY,
					100);

			// Applies the opacity to the graphics object
			if (opacity != 100)
			{
				composite = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, opacity / 100));
			}

			// Saves the stroke
			Stroke stroke = g.getStroke();

			if (!mxUtils.getString(style, mxConstants.STYLE_SHAPE, "").equals(
					mxConstants.SHAPE_SWIMLANE))
			{
				drawShape(x, y, w, h, style);
			}
			else
			{
				int start = (int) Math.round(mxUtils.getInt(style,
						mxConstants.STYLE_STARTSIZE,
						mxConstants.DEFAULT_STARTSIZE)
						* scale);

				// Removes some styles to draw the content area
				Map<String, Object> cloned = new Hashtable<String, Object>(
						style);
				cloned.remove(mxConstants.STYLE_FILLCOLOR);
				cloned.remove(mxConstants.STYLE_ROUNDED);

				if (mxUtils.isTrue(style, mxConstants.STYLE_HORIZONTAL, true))
				{
					drawShape(x, y, w, Math.min(h, start), style);
					drawShape(x, y + start, w, h - start, cloned);
				}
				else
				{
					drawShape(x, y, Math.min(w, start), h, style);
					drawShape(x + start, y, w - start, h, cloned);
				}
			}

			// Restores the stroke
			g.setStroke(stroke);

			// Restores the composite rule on the graphics object
			if (composite != null)
			{
				g.setComposite(composite);
			}

			// Restores the affine transformation
			if (transform != null)
			{
				g.setTransform(transform);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawEdge(java.util.List, java.util.Hashtable)
	 */
	public Object drawEdge(List<mxPoint> pts, Map<String, Object> style)
	{
		if (g != null)
		{
			// Transpose all points by cloning into a new array
			pts = mxUtils.translatePoints(pts, translate.x, translate.y);
			float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY,
					100);
			Composite composite = null;

			// Applies the opacity to the graphics object
			if (opacity != 100)
			{
				composite = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, opacity / 100));
			}

			// Saves the stroke
			Stroke stroke = g.getStroke();
			drawLine(pts, style);

			// Restores the stroke
			g.setStroke(stroke);

			// Resets the composite rule on the graphics object
			if (composite != null)
			{
				g.setComposite(composite);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mxgraph.canvas.mxICanvas#drawLabel(java.lang.String, int, int, int, int, java.util.Hashtable, boolean)
	 */
	public Object drawLabel(String label, int x, int y, int w, int h,
			Map<String, Object> style, boolean isHtml)
	{
		if (g != null && drawLabels)
		{
			x += translate.x;
			y += translate.y;

			if (label != null && label.length() > 0)
			{
				Composite composite = null;
				float opacity = mxUtils.getFloat(style,
						mxConstants.STYLE_TEXT_OPACITY, 100);

				// Applies the opacity to the graphics object
				if (opacity != 100)
				{
					composite = g.getComposite();
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, opacity / 100));
				}

				// Draws the label background and border
				Color bg = mxUtils.getColor(style,
						mxConstants.STYLE_LABEL_BACKGROUNDCOLOR);
				Color border = mxUtils.getColor(style,
						mxConstants.STYLE_LABEL_BORDERCOLOR);

				// Draws the label background
				if (bg != null)
				{
					g.setColor(bg);
					g.fillRect(x, y, w, h);
				}

				// Draws the label border
				if (border != null)
				{
					g.setColor(border);
					g.drawRect(x, y, w, h);
				}

				if (isHtml)
				{
					drawHtmlText(mxUtils.getBodyMarkup(label,
							isReplaceHtmlLinefeeds()), x, y, w, h, style);
				}
				else
				{
					Font font = mxUtils.getFont(style, scale);

					if (font.getSize() > 0)
					{
						g.setFont(font);
						drawPlainText(label, x, y, w, h, style);
					}
				}

				if (composite != null)
				{
					g.setComposite(composite);
				}
			}
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
	public void drawShape(int x, int y, int w, int h, Map<String, Object> style)
	{
		Color penColor = mxUtils.getColor(style, mxConstants.STYLE_STROKECOLOR);
		float penWidth = mxUtils.getFloat(style, mxConstants.STYLE_STROKEWIDTH,
				1);
		int pw = (int) Math.ceil(penWidth * scale);

		if (g.hitClip(x - pw, y - pw, w + 2 * pw, h + 2 * pw))
		{
			// Prepares the background
			boolean shadow = mxUtils.isTrue(style, mxConstants.STYLE_SHADOW,
					false);
			Color fillColor = mxUtils.getStyleFillColor(style);
			Paint fillPaint = getFillPaint(new Rectangle(x, y, w, h),
					fillColor, style);

			if (penWidth > 0)
			{
				setStroke(penWidth, style);
			}

			// Draws the shape
			String shape = mxUtils
					.getString(style, mxConstants.STYLE_SHAPE, "");

			if (shape.equals(mxConstants.SHAPE_IMAGE))
			{
				String img = getImageForStyle(style);

				if (img != null)
				{
					drawImage(x, y, w, h, img);
				}
			}
			else if (shape.equals(mxConstants.SHAPE_LINE))
			{
				if (penColor != null)
				{
					g.setColor(penColor);
					String direction = mxUtils.getString(style,
							mxConstants.STYLE_DIRECTION,
							mxConstants.DIRECTION_EAST);

					if (direction.equals(mxConstants.DIRECTION_EAST)
							|| direction.equals(mxConstants.DIRECTION_WEST))
					{
						int mid = (int) (y + h / 2);
						drawLine(x, mid, x + w, mid);
					}
					else
					{
						int mid = (int) (x + w / 2);
						drawLine(mid, y, mid, y + h);
					}
				}
			}
			else if (shape.equals(mxConstants.SHAPE_ELLIPSE))
			{
				drawOval(x, y, w, h, fillColor, fillPaint, penColor, shadow);
			}
			else if (shape.equals(mxConstants.SHAPE_DOUBLE_ELLIPSE))
			{
				drawOval(x, y, w, h, fillColor, fillPaint, penColor, shadow);

				int inset = (int) ((3 + penWidth) * scale);
				x += inset;
				y += inset;
				w -= 2 * inset;
				h -= 2 * inset;
				drawOval(x, y, w, h, null, null, penColor, false);
			}
			else if (shape.equals(mxConstants.SHAPE_RHOMBUS))
			{
				drawRhombus(x, y, w, h, fillColor, fillPaint, penColor, shadow);
			}
			else if (shape.equals(mxConstants.SHAPE_CYLINDER))
			{
				drawCylinder(x, y, w, h, fillColor, fillPaint, penColor, shadow);
			}
			else if (shape.equals(mxConstants.SHAPE_ACTOR))
			{
				drawActor(x, y, w, h, fillColor, fillPaint, penColor, shadow);
			}
			else if (shape.equals(mxConstants.SHAPE_CLOUD))
			{
				drawCloud(x, y, w, h, fillColor, fillPaint, penColor, shadow);
			}
			else if (shape.equals(mxConstants.SHAPE_TRIANGLE))
			{
				String direction = mxUtils.getString(style,
						mxConstants.STYLE_DIRECTION, "");
				drawTriangle(x, y, w, h, fillColor, fillPaint, penColor,
						shadow, direction);
			}
			else if (shape.equals(mxConstants.SHAPE_HEXAGON))
			{
				String direction = mxUtils.getString(style,
						mxConstants.STYLE_DIRECTION, "");
				drawHexagon(x, y, w, h, fillColor, fillPaint, penColor, shadow,
						direction);
			}
			else
			{
				drawRect(x, y, w, h, fillColor, fillPaint, penColor, shadow,
						mxUtils.isTrue(style, mxConstants.STYLE_ROUNDED));

				// Draws the image inside the label shape
				if (shape.equals(mxConstants.SHAPE_LABEL))
				{
					String img = getImageForStyle(style);

					if (img != null)
					{
						String imgAlign = mxUtils.getString(style,
								mxConstants.STYLE_IMAGE_ALIGN,
								mxConstants.ALIGN_CENTER);
						String imgValign = mxUtils.getString(style,
								mxConstants.STYLE_IMAGE_VERTICAL_ALIGN,
								mxConstants.ALIGN_MIDDLE);
						int imgWidth = (int) (mxUtils.getInt(style,
								mxConstants.STYLE_IMAGE_WIDTH,
								mxConstants.DEFAULT_IMAGESIZE) * scale);
						int imgHeight = (int) (mxUtils.getInt(style,
								mxConstants.STYLE_IMAGE_HEIGHT,
								mxConstants.DEFAULT_IMAGESIZE) * scale);
						int spacing = (int) (mxUtils.getInt(style,
								mxConstants.STYLE_SPACING, 2) * scale);

						int imgX = x;

						if (imgAlign.equals(mxConstants.ALIGN_LEFT))
						{
							imgX += spacing;
						}
						else if (imgAlign.equals(mxConstants.ALIGN_RIGHT))
						{
							imgX += w - imgWidth - spacing;
						}
						else
						// CENTER
						{
							imgX += (w - imgWidth) / 2;
						}

						int imgY = y;

						if (imgValign.equals(mxConstants.ALIGN_TOP))
						{
							imgY += spacing;
						}
						else if (imgValign.equals(mxConstants.ALIGN_BOTTOM))
						{
							imgY += h - imgHeight - spacing;
						}
						else
						// MIDDLE
						{
							imgY += (h - imgHeight) / 2;
						}

						drawImage(imgX, imgY, imgWidth, imgHeight, img);
					}
				}
			}
		}
	}

	/**
	 * Draws a a polygon for the given parameters.
	 * 
	 * @param polygon Points of the polygon.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawPolygon(Polygon polygon, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		if (fillColor != null || fillPaint != null)
		{
			if (shadow)
			{
				g.setColor(mxConstants.SHADOW_COLOR);
				g.translate(mxConstants.SHADOW_OFFSETX,
						mxConstants.SHADOW_OFFSETY);
				g.fillPolygon(polygon);
				g.translate(-mxConstants.SHADOW_OFFSETX,
						-mxConstants.SHADOW_OFFSETY);
			}

			if (fillPaint != null)
			{
				g.setPaint(fillPaint);
			}
			else
			{
				g.setColor(fillColor);
			}

			g.fillPolygon(polygon);
		}

		if (penColor != null)
		{
			g.setColor(penColor);
			g.drawPolygon(polygon);
		}
	}

	/**
	 * Draws a path for the given parameters.
	 * 
	 * @param path Path object to be drawn.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawPath(GeneralPath path, Color fillColor, Paint fillPaint,
			Color penColor, boolean shadow)
	{
		if (fillColor != null || fillPaint != null)
		{
			if (shadow)
			{
				g.setColor(mxConstants.SHADOW_COLOR);
				g.translate(mxConstants.SHADOW_OFFSETX,
						mxConstants.SHADOW_OFFSETY);
				g.fill(path);
				g.translate(-mxConstants.SHADOW_OFFSETX,
						-mxConstants.SHADOW_OFFSETY);
			}

			if (fillPaint != null)
			{
				g.setPaint(fillPaint);
			}
			else
			{
				g.setColor(fillColor);
			}

			g.fill(path);
		}

		if (penColor != null)
		{
			g.setColor(penColor);
			g.draw(path);
		}
	}

	/**
	 * Draws a rectangle for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 * @param rounded Boolean indicating if the rectangle is rounded.
	 */
	protected void drawRect(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow, boolean rounded)
	{
		int radius = (rounded) ? getArcSize(w, h) : 0;

		if (fillColor != null || fillPaint != null)
		{
			if (shadow)
			{
				g.setColor(mxConstants.SHADOW_COLOR);

				if (rounded)
				{
					g.fillRoundRect(x + mxConstants.SHADOW_OFFSETX, y
							+ mxConstants.SHADOW_OFFSETY, w, h, radius, radius);
				}
				else
				{
					g.fillRect(x + mxConstants.SHADOW_OFFSETX, y
							+ mxConstants.SHADOW_OFFSETY, w, h);
				}
			}

			if (fillPaint != null)
			{
				g.setPaint(fillPaint);
			}
			else
			{
				g.setColor(fillColor);
			}

			if (rounded)
			{
				g.fillRoundRect(x, y, w, h, radius, radius);
			}
			else
			{
				// Only draws the filled region within the clipping bounds
				if (g.getClipBounds() != null)
				{
					Rectangle rect = new Rectangle(x, y, w, h);
					g.fill(rect.intersection(g.getClipBounds()));
				}
				else
				{
					g.fillRect(x, y, w, h);
				}
			}
		}

		if (penColor != null)
		{
			g.setColor(penColor);

			if (rounded)
			{
				g.drawRoundRect(x, y, w, h, radius, radius);
			}
			else
			{
				g.drawRect(x, y, w, h);
			}
		}
	}

	/**
	 * Draws an image for the given parameters.
	 * 
	 * @param x X-coordinate of the image.
	 * @param y Y-coordinate of the image.
	 * @param w Width of the image.
	 * @param h Height of the image.
	 * @param image URL of the image.
	 */
	protected void drawImage(int x, int y, int w, int h, String image)
	{
		Image img = loadImage(image);

		if (img != null)
		{
			g.drawImage(img, x, y, w, h, null);
		}
	}

	/**
	 * Draws an oval for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawOval(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		if (fillColor != null || fillPaint != null)
		{
			if (shadow)
			{
				g.setColor(mxConstants.SHADOW_COLOR);
				g.fillOval(x + mxConstants.SHADOW_OFFSETX, y
						+ mxConstants.SHADOW_OFFSETY, w, h);
			}

			if (fillPaint != null)
			{
				g.setPaint(fillPaint);
			}
			else
			{
				g.setColor(fillColor);
			}

			g.fillOval(x, y, w, h);
		}

		if (penColor != null)
		{
			g.setColor(penColor);
			g.drawOval(x, y, w, h);
		}
	}

	/**
	 * Draws a rhombus (aka. diamond) for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawRhombus(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		int halfWidth = w / 2;
		int halfHeight = h / 2;

		Polygon rhombus = new Polygon();
		rhombus.addPoint(x + halfWidth, y);
		rhombus.addPoint(x + w, y + halfHeight);
		rhombus.addPoint(x + halfWidth, y + h);
		rhombus.addPoint(x, y + halfHeight);

		drawPolygon(rhombus, fillColor, fillPaint, penColor, shadow);
	}

	/**
	 * Draws a cylinder for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param isShadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawCylinder(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean isShadow)
	{
		int h4 = h / 4;
		int r = w - 1;

		if (fillColor != null || fillPaint != null)
		{
			Area area = new Area(new Rectangle(x, y + h4 / 2, r, h - h4));
			area.add(new Area(new Rectangle(x, y + h4 / 2, r, h - h4)));
			area.add(new Area(new Ellipse2D.Double(x, y, r, h4)));
			area.add(new Area(new Ellipse2D.Double(x, y + h - h4, r, h4)));

			if (isShadow)
			{
				g.setColor(mxConstants.SHADOW_COLOR);
				g.translate(mxConstants.SHADOW_OFFSETX,
						mxConstants.SHADOW_OFFSETY);
				g.fill(area);
				g.translate(-mxConstants.SHADOW_OFFSETX,
						-mxConstants.SHADOW_OFFSETY);
			}

			if (fillPaint != null)
			{
				g.setPaint(fillPaint);
			}
			else
			{
				g.setColor(fillColor);
			}

			g.fill(area);
		}

		if (penColor != null)
		{
			g.setColor(penColor);
			int h2 = h4 / 2;

			g.drawOval(x, y, r, h4);
			g.drawLine(x, y + h2, x, y + h - h2);
			g.drawLine(x + w - 1, y + h2, x + w - 1, y + h - h2);
			g.drawArc(x, y + h - h4, r, h4, 0, -180);
		}
	}

	/**
	 * Draws an actor shape for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawActor(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		float width = w * 2 / 6;

		GeneralPath path = new GeneralPath();

		path.moveTo(x, y + h);
		path.curveTo(x, y + 3 * h / 5, x, y + 2 * h / 5, x + w / 2, y + 2 * h
				/ 5);
		path.curveTo(x + w / 2 - width, y + 2 * h / 5, x + w / 2 - width, y, x
				+ w / 2, y);
		path.curveTo(x + w / 2 + width, y, x + w / 2 + width, y + 2 * h / 5, x
				+ w / 2, y + 2 * h / 5);
		path.curveTo(x + w, y + 2 * h / 5, x + w, y + 3 * h / 5, x + w, y + h);
		path.closePath();

		drawPath(path, fillColor, fillPaint, penColor, shadow);
	}

	/**
	 * Draws a cloud shape for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawCloud(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		GeneralPath path = new GeneralPath();

		path.moveTo((float) (x + 0.25 * w), (float) (y + 0.25 * h));
		path.curveTo((float) (x + 0.05 * w), (float) (y + 0.25 * h), (float) x,
				(float) (y + 0.5 * h), (float) (x + 0.16 * w),
				(float) (y + 0.55 * h));
		path.curveTo((float) x, (float) (y + 0.66 * h), (float) (x + 0.18 * w),
				(float) (y + 0.9 * h), (float) (x + 0.31 * w),
				(float) (y + 0.8 * h));
		path.curveTo((float) (x + 0.4 * w), (float) (y + h),
				(float) (x + 0.7 * w), (float) (y + h), (float) (x + 0.8 * w),
				(float) (y + 0.8 * h));
		path.curveTo((float) (x + w), (float) (y + 0.8 * h), (float) (x + w),
				(float) (y + 0.6 * h), (float) (x + 0.875 * w),
				(float) (y + 0.5 * h));
		path.curveTo((float) (x + w), (float) (y + 0.3 * h),
				(float) (x + 0.8 * w), (float) (y + 0.1 * h),
				(float) (x + 0.625 * w), (float) (y + 0.2 * h));
		path.curveTo((float) (x + 0.5 * w), (float) (y + 0.05 * h),
				(float) (x + 0.3 * w), (float) (y + 0.05 * h),
				(float) (x + 0.25 * w), (float) (y + 0.25 * h));
		path.closePath();

		drawPath(path, fillColor, fillPaint, penColor, shadow);
	}

	/**
	 * Draws a triangle shape for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawTriangle(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow, String direction)
	{
		Polygon triangle = new Polygon();

		if (direction.equals(mxConstants.DIRECTION_NORTH))
		{
			triangle.addPoint(x, y + h);
			triangle.addPoint(x + w / 2, y);
			triangle.addPoint(x + w, y + h);
		}
		else if (direction.equals(mxConstants.DIRECTION_SOUTH))
		{
			triangle.addPoint(x, y);
			triangle.addPoint(x + w / 2, y + h);
			triangle.addPoint(x + w, y);
		}
		else if (direction.equals(mxConstants.DIRECTION_WEST))
		{
			triangle.addPoint(x + w, y);
			triangle.addPoint(x, y + h / 2);
			triangle.addPoint(x + w, y + h);
		}
		else
		// east
		{
			triangle.addPoint(x, y);
			triangle.addPoint(x + w, y + h / 2);
			triangle.addPoint(x, y + h);
		}

		drawPolygon(triangle, fillColor, fillPaint, penColor, shadow);
	}

	/**
	 * Draws a hexagon shape for the given parameters.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param fillColor Optional fill color of the shape.
	 * @param fillPaint Optional paint of the shape.
	 * @param penColor Optional stroke color.
	 * @param shadow Boolean indicating if a shadow should be painted.
	 */
	protected void drawHexagon(int x, int y, int w, int h, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow, String direction)
	{
		Polygon hexagon = new Polygon();

		if (direction.equals(mxConstants.DIRECTION_NORTH)
				|| direction.equals(mxConstants.DIRECTION_SOUTH))
		{
			hexagon.addPoint(x + (int) (0.5 * w), y);
			hexagon.addPoint(x + w, y + (int) (0.25 * h));
			hexagon.addPoint(x + w, y + (int) (0.75 * h));
			hexagon.addPoint(x + (int) (0.5 * w), y + h);
			hexagon.addPoint(x, y + (int) (0.75 * h));
			hexagon.addPoint(x, y + (int) (0.25 * h));
		}
		else
		{
			hexagon.addPoint(x + (int) (0.25 * w), y);
			hexagon.addPoint(x + (int) (0.75 * w), y);
			hexagon.addPoint(x + w, y + (int) (0.5 * h));
			hexagon.addPoint(x + (int) (0.75 * w), y + h);
			hexagon.addPoint(x + (int) (0.25 * w), y + h);
			hexagon.addPoint(x, y + (int) (0.5 * h));
		}

		drawPolygon(hexagon, fillColor, fillPaint, penColor, shadow);
	}

	/**
	 * Computes the arc size for the given dimension.
	 * 
	 * @param w Width of the rectangle.
	 * @param h Height of the rectangle.
	 * @return Returns the arc size for the given dimension.
	 */
	public static int getArcSize(int w, int h)
	{
		int arcSize;

		if (w <= h)
		{
			arcSize = (int) Math.round(h
					* mxConstants.RECTANGLE_ROUNDING_FACTOR);

			if (arcSize > (w / 2))
			{
				arcSize = w / 2;
			}
		}
		else
		{
			arcSize = (int) Math.round(w
					* mxConstants.RECTANGLE_ROUNDING_FACTOR);

			if (arcSize > (h / 2))
			{
				arcSize = h / 2;
			}
		}
		return arcSize;
	}

	/**
	 * Creates a polygon that represents an arrow.
	 * 
	 * @param p0 Start point of the arrow.
	 * @param pe End point of the arrow.
	 * @return Returns the polygon to be painted.
	 */
	protected Polygon createArrow(mxPoint p0, mxPoint pe)
	{
		// Geometry of arrow
		double spacing = mxConstants.ARROW_SPACING * scale;
		double width = mxConstants.ARROW_WIDTH * scale;
		double arrow = mxConstants.ARROW_SIZE * scale;

		double dx = pe.getX() - p0.getX();
		double dy = pe.getY() - p0.getY();
		double dist = Math.sqrt(dx * dx + dy * dy);
		double length = dist - 2 * spacing - arrow;

		// Computes the norm and the inverse norm
		double nx = dx / dist;
		double ny = dy / dist;
		double basex = length * nx;
		double basey = length * ny;
		double floorx = width * ny / 3;
		double floory = -width * nx / 3;

		// Computes points
		double p0x = p0.getX() - floorx / 2 + spacing * nx;
		double p0y = p0.getY() - floory / 2 + spacing * ny;
		double p1x = p0x + floorx;
		double p1y = p0y + floory;
		double p2x = p1x + basex;
		double p2y = p1y + basey;
		double p3x = p2x + floorx;
		double p3y = p2y + floory;
		// p4 not required
		double p5x = p3x - 3 * floorx;
		double p5y = p3y - 3 * floory;

		Polygon poly = new Polygon();
		poly.addPoint((int) Math.round(p0x), (int) Math.round(p0y));
		poly.addPoint((int) Math.round(p1x), (int) Math.round(p1y));
		poly.addPoint((int) Math.round(p2x), (int) Math.round(p2y));
		poly.addPoint((int) Math.round(p3x), (int) Math.round(p3y));
		poly.addPoint((int) Math.round(pe.getX() - spacing * nx), (int) Math
				.round(pe.getY() - spacing * ny));
		poly.addPoint((int) Math.round(p5x), (int) Math.round(p5y));
		poly.addPoint((int) Math.round(p5x + floorx), (int) Math.round(p5y
				+ floory));

		return poly;
	}

	/**
	 * Draws the given arrow shape.
	 * 
	 * @param pts List of points that define the line.
	 */
	protected void drawArrow(List<mxPoint> pts, Color fillColor,
			Paint fillPaint, Color penColor, boolean shadow)
	{
		// Base vector (between end points)
		mxPoint p0 = pts.get(0);
		mxPoint pe = pts.get(pts.size() - 1);
		Polygon poly = createArrow(p0, pe);

		if (g.getClipBounds() == null
				|| g.getClipBounds().intersects(poly.getBounds()))
		{
			drawPolygon(poly, fillColor, fillPaint, penColor, shadow);
		}
	}

	/**
	 * Draws the given connector shape.
	 * 
	 * @param pts List of points that define the line.
	 */
	protected void drawConnector(List<mxPoint> pts, float penWidth,
			Color penColor, Object startMarker, float startSize,
			Object endMarker, float endSize, boolean rounded,
			Map<String, Object> style)
	{
		g.setStroke(new BasicStroke((float) (penWidth * scale)));
		g.setColor(penColor);

		// Draws the start marker
		mxPoint p0 = pts.get(0);
		mxPoint pt = pts.get(1);
		mxPoint offset = null;

		if (startMarker != null)
		{
			offset = drawMarker(startMarker, pt, p0, startSize, penWidth);
		}
		else
		{
			double dx = pt.getX() - p0.getX();
			double dy = pt.getY() - p0.getY();

			double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
			double nx = dx * penWidth * scale / dist;
			double ny = dy * penWidth * scale / dist;

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
		mxPoint pe = pts.get(pts.size() - 1);
		pt = pts.get(pts.size() - 2);

		if (endMarker != null)
		{
			offset = drawMarker(endMarker, pt, pe, endSize, penWidth);
		}
		else
		{
			double dx = pt.getX() - p0.getX();
			double dy = pt.getY() - p0.getY();

			double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
			double nx = dx * penWidth * scale / dist;
			double ny = dy * penWidth * scale / dist;

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

		setStroke(penWidth, style);

		// Draws the line segments
		double arcSize = mxConstants.LINE_ARCSIZE * scale;
		pt = p0;

		for (int i = 1; i < pts.size() - 1; i++)
		{
			mxPoint tmp = pts.get(i);
			double dx = pt.getX() - tmp.getX();
			double dy = pt.getY() - tmp.getY();

			if ((rounded && i < pts.size() - 1) && (dx != 0 || dy != 0)
					&& scale > 0.05)
			{
				// Draws a line from the last point
				// to the current point with a spacing
				// of size off the current point into
				// direction of the last point
				double dist = Math.sqrt(dx * dx + dy * dy);
				double nx1 = dx * Math.min(arcSize, dist / 2) / dist;
				double ny1 = dy * Math.min(arcSize, dist / 2) / dist;
				drawLine((int) Math.round(pt.getX()), (int) Math.round(pt
						.getY()), (int) Math.round(tmp.getX() + nx1),
						(int) Math.round(tmp.getY() + ny1));

				// Draws a curve from the last point
				// to the current point with a spacing
				// of size off the current point into
				// direction of the next point
				mxPoint next = pts.get(i + 1);
				dx = next.getX() - tmp.getX();
				dy = next.getY() - tmp.getY();
				dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
				double nx2 = dx * Math.min(arcSize, dist / 2) / dist;
				double ny2 = dy * Math.min(arcSize, dist / 2) / dist;

				QuadCurve2D.Float curve = new QuadCurve2D.Float((int) Math
						.round(tmp.getX() + nx1), (int) Math.round(tmp.getY()
						+ ny1), (int) Math.round(tmp.getX()), (int) Math
						.round(tmp.getY()), (int) Math.round(tmp.getX() + nx2),
						(int) Math.round(tmp.getY() + ny2));

				Rectangle bounds = curve.getBounds();
				int sw = (int) Math.ceil(penWidth * scale);
				bounds.grow(sw, sw);

				if (g.getClipBounds() == null
						|| g.getClipBounds().intersects(bounds))
				{
					g.draw(curve);
				}

				tmp = new mxPoint(tmp.getX() + nx2, tmp.getY() + ny2);
			}
			else
			{
				drawLine((int) Math.round(pt.getX()), (int) Math.round(pt
						.getY()), (int) Math.round(tmp.getX()), (int) Math
						.round(tmp.getY()));
			}

			pt = tmp;
		}

		drawLine((int) Math.round(pt.getX()), (int) Math.round(pt.getY()),
				(int) Math.round(pe.getX()), (int) Math.round(pe.getY()));
	}

	/**
	 * 
	 * @param fillColor
	 * @param style
	 * @return Returns the paint to be used for filling.
	 */
	protected Paint getFillPaint(Rectangle bounds, Color fillColor,
			Map<String, Object> style)
	{
		Paint fillPaint = null;

		if (fillColor != null)
		{
			Color gradientColor = mxUtils.getColor(style,
					mxConstants.STYLE_GRADIENTCOLOR);

			if (gradientColor != null)
			{
				String gradientDirection = mxUtils.getString(style,
						mxConstants.STYLE_GRADIENT_DIRECTION);

				float x1 = bounds.x;
				float y1 = bounds.y;
				float x2 = bounds.x;
				float y2 = bounds.y;

				if (gradientDirection == null
						|| gradientDirection
								.equals(mxConstants.DIRECTION_SOUTH))
				{
					y2 = bounds.y + bounds.height;
				}
				else if (gradientDirection.equals(mxConstants.DIRECTION_EAST))
				{
					x2 = bounds.x + bounds.width;
				}
				else if (gradientDirection.equals(mxConstants.DIRECTION_NORTH))
				{
					y1 = bounds.y + bounds.height;
				}
				else if (gradientDirection.equals(mxConstants.DIRECTION_WEST))
				{
					x1 = bounds.x + bounds.width;
				}

				fillPaint = new GradientPaint(x1, y1, fillColor, x2, y2,
						gradientColor, true);
			}
		}

		return fillPaint;
	}

	/**
	 * Draws the given lines as segments between all points of the given list
	 * of mxPoints.
	 * 
	 * @param pts List of points that define the line.
	 * @param style Style to be used for painting the line.
	 */
	public void drawLine(List<mxPoint> pts, Map<String, Object> style)
	{
		Color penColor = mxUtils.getStyleStrokeColor(style,Color.BLACK);
		float penWidth = mxUtils.getStyleStrokeWidth(style,(float)1);

		if (penColor != null && penWidth > 0)
		{
			// Draws the shape
			String shape = mxUtils
					.getString(style, mxConstants.STYLE_SHAPE, "");

			if (shape.equals(mxConstants.SHAPE_ARROW))
			{
				setStroke(penWidth, style);

				// Base vector (between end points)
				mxPoint p0 = pts.get(0);
				mxPoint pe = pts.get(pts.size() - 1);

				Rectangle bounds = new Rectangle(p0.getPoint());
				bounds.add(pe.getPoint());

				Color fillColor = mxUtils.getStyleFillColor(style);
				Paint fillPaint = getFillPaint(bounds, fillColor, style);
				boolean shadow = mxUtils.isTrue(style,
						mxConstants.STYLE_SHADOW, false);

				drawArrow(pts, fillColor, fillPaint, penColor, shadow);
			}
			else
			{
				Object startMarker = style.get(mxConstants.STYLE_STARTARROW);
				Object endMarker = style.get(mxConstants.STYLE_ENDARROW);
				float startSize = (float) (mxUtils.getFloat(style,
						mxConstants.STYLE_STARTSIZE,
						mxConstants.DEFAULT_MARKERSIZE));
				float endSize = (float) (mxUtils.getFloat(style,
						mxConstants.STYLE_ENDSIZE,
						mxConstants.DEFAULT_MARKERSIZE));
				boolean rounded = mxUtils.isTrue(style,
						mxConstants.STYLE_ROUNDED, false);

				drawConnector(pts, penWidth, penColor, startMarker, startSize,
						endMarker, endSize, rounded, style);
			}
		}
	}

	/**
	 * Draws the given lines as segments between all points of the given list
	 * of mxPoints.
	 * 
	 * @param penWidth Specifies the width of the pen.
	 * @param style Style to be used for painting the line.
	 */
	protected void setStroke(float penWidth, Map<String, Object> style)
	{
		if (mxUtils.isTrue(style, mxConstants.STYLE_DASHED, false))
		{
			g.setStroke(new BasicStroke((float) (penWidth * scale),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
					new float[] { (float) (3 * scale), (float) (3 * scale) },
					0.0f));
		}
		else
		{
			g.setStroke(new BasicStroke((float) (penWidth * scale)));
		}
	}

	/**
	 * Draws the given line if the line is inside the clipping area. This
	 * method assumes that the stroke on the graphics object is already set.
	 */
	public void drawLine(int x0, int y0, int x1, int y1)
	{
		Line2D line = new Line2D.Float(x0, y0, x1, y1);

		if (g.getClipBounds() == null || line.intersects(g.getClipBounds()))
		{
			g.draw(line);
		}
	}

	/**
	 * Draws the given type of marker.
	 * 
	 * @param type
	 * @param p0
	 * @param pe
	 * @param size
	 * @param strokeWidth
	 * @return Return the mxPoint that defines the offset.
	 */
	public mxPoint drawMarker(Object type, mxPoint p0, mxPoint pe, float size,
			float strokeWidth)
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

		if (type.equals(mxConstants.ARROW_CLASSIC)
				|| type.equals(mxConstants.ARROW_BLOCK))
		{
			Polygon poly = new Polygon();
			poly.addPoint((int) Math.round(pe.getX()), (int) Math.round(pe
					.getY()));
			poly.addPoint((int) Math.round(pe.getX() - nx - ny / 2), (int) Math
					.round(pe.getY() - ny + nx / 2));

			if (type.equals(mxConstants.ARROW_CLASSIC))
			{
				poly.addPoint((int) Math.round(pe.getX() - nx * 3 / 4),
						(int) Math.round(pe.getY() - ny * 3 / 4));
			}

			poly.addPoint((int) Math.round(pe.getX() + ny / 2 - nx), (int) Math
					.round(pe.getY() - ny - nx / 2));

			if (g.getClipBounds() == null
					|| g.getClipBounds().intersects(poly.getBounds()))
			{
				g.fillPolygon(poly);
				g.drawPolygon(poly);
			}

			offset = new mxPoint(-nx * 3 / 4, -ny * 3 / 4);
		}
		else if (type.equals(mxConstants.ARROW_OPEN))
		{
			nx *= 1.2;
			ny *= 1.2;

			drawLine((int) Math.round(pe.getX() - nx - ny / 2), (int) Math
					.round(pe.getY() - ny + nx / 2), (int) Math.round(pe.getX()
					- nx / 6), (int) Math.round(pe.getY() - ny / 6));
			drawLine((int) Math.round(pe.getX() - nx / 6), (int) Math.round(pe
					.getY()
					- ny / 6), (int) Math.round(pe.getX() + ny / 2 - nx),
					(int) Math.round(pe.getY() - ny - nx / 2));

			offset = new mxPoint(-nx / 4, -ny / 4);
		}
		else if (type.equals(mxConstants.ARROW_OVAL))
		{
			nx *= 1.2;
			ny *= 1.2;
			absSize *= 1.2;

			int cx = (int) Math.round(pe.getX() - nx / 2);
			int cy = (int) Math.round(pe.getY() - ny / 2);
			int a = (int) Math.round(absSize / 2);
			int a2 = (int) Math.round(absSize);

			if (g.hitClip(cx - a, cy - a, a2, a2))
			{
				g.fillOval(cx - a, cy - a, a2, a2);
				g.drawOval(cx - a, cy - a, a2, a2);
			}

			offset = new mxPoint(-nx / 2, -ny / 2);
		}
		else if (type.equals(mxConstants.ARROW_DIAMOND))
		{
			nx *= 1.2;
			ny *= 1.2;

			Polygon poly = new Polygon();
			poly.addPoint((int) Math.round(pe.getX() + nx / 2), (int) Math
					.round(pe.getY() + ny / 2));
			poly.addPoint((int) Math.round(pe.getX() - ny / 2), (int) Math
					.round(pe.getY() + nx / 2));
			poly.addPoint((int) Math.round(pe.getX() - nx / 2), (int) Math
					.round(pe.getY() - ny / 2));
			poly.addPoint((int) Math.round(pe.getX() + ny / 2), (int) Math
					.round(pe.getY() - nx / 2));

			if (g.getClipBounds() == null
					|| g.getClipBounds().intersects(poly.getBounds()))
			{
				g.fillPolygon(poly);
				g.drawPolygon(poly);
			}
		}

		return offset;
	}

	/**
	 * Draws the specified HTML markup.
	 * 
	 * @param text HTML markup to be painted.
	 * @param x X-coordinate of the text.
	 * @param y Y-coordinate of the text.
	 * @param w Width of the text.
	 * @param h Height of the text.
	 * @param style Style to be used for painting the text.
	 */
	protected void drawHtmlText(String text, int x, int y, int w, int h,
			Map<String, Object> style)
	{
		mxLighweightLabel textRenderer = mxLighweightLabel.getSharedInstance();

		if (textRenderer != null && rendererPane != null)
		{
			boolean horizontal = mxUtils.isTrue(style,
					mxConstants.STYLE_HORIZONTAL, true);

			if (g.hitClip(x, y, w, h))
			{
				AffineTransform at = g.getTransform();

				if (!horizontal)
				{
					g.rotate(-Math.PI / 2, x + w / 2, y + h / 2);
					g.translate(w / 2 - h / 2, h / 2 - w / 2);

					int tmp = w;
					w = h;
					h = tmp;
				}

				// Renders the scaled text
				textRenderer.setText(mxUtils.createHtmlDocument(style, text));
				g.scale(scale, scale);
				rendererPane.paintComponent(g, textRenderer, rendererPane,
						(int) (x / scale) + mxConstants.LABEL_INSET,
						(int) (y / scale) + mxConstants.LABEL_INSET,
						(int) (w / scale), (int) (h / scale), true);

				// Restores the previous transformation
				g.setTransform(at);
			}
		}
	}

	/**
	 * Draws the specified string as plain text.
	 * 
	 * @param text HTML markup to be painted.
	 * @param x X-coordinate of the text.
	 * @param y Y-coordinate of the text.
	 * @param w Width of the text.
	 * @param h Height of the text.
	 * @param style Style to be used for painting the text.
	 */
	protected void drawPlainText(String text, int x, int y, int w, int h,
			Map<String, Object> style)
	{
		if (g.hitClip(x, y, w, h))
		{
			// Uses JTextPane for drawing because JTextArea has no alignment
			mxLighweightTextPane textRenderer = mxLighweightTextPane
					.getSharedInstance();

			// Text rendering with word-wrapping
			if (mxUtils.getString(style, mxConstants.STYLE_WHITE_SPACE,
					"nowrap").equals("wrap")
					&& textRenderer != null && rendererPane != null)
			{
				textRenderer.setText(text);

				// Sets the font style, alignment and color via the styled document
				SimpleAttributeSet sas = new SimpleAttributeSet();
				Object align = mxUtils.getString(style,
						mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
				int scAlign = (align.equals(mxConstants.ALIGN_LEFT)) ? StyleConstants.ALIGN_LEFT
						: (align.equals(mxConstants.ALIGN_RIGHT)) ? StyleConstants.ALIGN_RIGHT
								: StyleConstants.ALIGN_CENTER;
				StyleConstants.setAlignment(sas, scAlign);
				StyleConstants.setForeground(sas, mxUtils.getColor(style,
						mxConstants.STYLE_FONTCOLOR, Color.black));

				Font font = g.getFont();
				StyleConstants.setFontFamily(sas, font.getFamily());
				StyleConstants.setFontSize(sas, font.getSize());
				StyleConstants.setItalic(sas, font.isItalic());
				StyleConstants.setBold(sas, font.isBold());
				((StyledDocument) textRenderer.getDocument())
						.setParagraphAttributes(0, textRenderer.getDocument()
								.getLength(), sas, true);

				// FIXME: For large scales there is a vertical offset
				rendererPane.paintComponent(g, textRenderer, rendererPane, x,
						(int) (y + mxConstants.LABEL_INSET * scale), w, h,
						false);
			}
			else
			{
				// Stores the original transform
				AffineTransform at = g.getTransform();

				// Rotates the canvas if required
				boolean horizontal = mxUtils.isTrue(style,
						mxConstants.STYLE_HORIZONTAL, true);

				if (!horizontal)
				{
					g.rotate(-Math.PI / 2, x + w / 2, y + h / 2);
					g.translate(w / 2 - h / 2, h / 2 - w / 2);
				}

				// Shifts the y-coordinate down by the ascent plus a workaround
				// for the line not starting at the exact vertical location
				FontMetrics fm = g.getFontMetrics();
				y += 2 * fm.getMaxAscent() - fm.getHeight()
						+ mxConstants.LABEL_INSET * scale;

				// Gets the alignment settings
				Object align = mxUtils.getString(style,
						mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);

				if (align.equals(mxConstants.ALIGN_LEFT))
				{
					x += mxConstants.LABEL_INSET;
				}
				else if (align.equals(mxConstants.ALIGN_RIGHT))
				{
					x -= mxConstants.LABEL_INSET;
				}

				// Sets the color
				Color fontColor = mxUtils.getColor(style,
						mxConstants.STYLE_FONTCOLOR, Color.black);
				g.setColor(fontColor);

				// Draws the text line by line
				String[] lines = text.split("\n");

				for (int i = 0; i < lines.length; i++)
				{
					int dx = 0;

					if (align.equals(mxConstants.ALIGN_CENTER))
					{
						int sw = fm.stringWidth(lines[i]);

						if (horizontal)
						{
							dx = (w - sw) / 2;
						}
						else
						{
							dx = (h - sw) / 2;
						}
					}
					else if (align.equals(mxConstants.ALIGN_RIGHT))
					{
						int sw = fm.stringWidth(lines[i]);
						dx = ((horizontal) ? w : h) - sw;
					}

					g.drawString(lines[i], x + dx, y);
					y += fm.getHeight() + mxConstants.LINESPACING;
				}

				// Resets the transformation
				g.setTransform(at);
			}
		}
	}
}
