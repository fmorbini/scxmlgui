package com.mxgraph.canvas;

import java.awt.Point;
import java.util.Map;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;

public abstract class mxBasicCanvas implements mxICanvas
{

	/**
	 * Defines the default value for the imageBasePath in all GDI canvases.
	 * Default is an empty string.
	 */
	public static String DEFAULT_IMAGEBASEPATH = "";

	/**
	 * Defines the base path for images with relative paths. Trailing slash
	 * is required. Default value is DEFAULT_IMAGEBASEPATH.
	 */
	protected String imageBasePath = DEFAULT_IMAGEBASEPATH;

	/**
	 * Specifies the current translation. Default is (0,0).
	 */
	protected Point translate = new Point();

	/**
	 * Specifies the current scale. Default is 1.
	 */
	protected double scale = 1;

	/**
	 * Specifies whether labels should be painted. Default is true.
	 */
	protected boolean drawLabels = true;

	/**
	 * 
	 */
	public void setTranslate(int dx, int dy)
	{
		translate.setLocation(dx, dy);
	}

	/**
	 * 
	 */
	public Point getTranslate()
	{
		return translate;
	}

	/**
	 * 
	 */
	public void setScale(double scale)
	{
		this.scale = scale;
	}

	/**
	 * 
	 */
	public double getScale()
	{
		return scale;
	}

	/**
	 * 
	 */
	public void setDrawLabels(boolean drawLabels)
	{
		this.drawLabels = drawLabels;
	}

	/**
	 * 
	 */
	public String getImageBasePath()
	{
		return imageBasePath;
	}

	/**
	 * 
	 */
	public void setImageBasePath(String imageBasePath)
	{
		this.imageBasePath = imageBasePath;
	}

	/**
	 * 
	 */
	public boolean isDrawLabels()
	{
		return drawLabels;
	}

	/**
	 * Gets the image path from the given style. If the path is relative (does
	 * not start with a slash) then it is appended to the imageBasePath.
	 */
	protected String getImageForStyle(Map<String, Object> style)
	{
		String filename = mxUtils.getString(style, mxConstants.STYLE_IMAGE);

		if (filename != null && !filename.startsWith("/"))
		{
			filename = imageBasePath + filename;
		}

		return filename;
	}

}
