package com.mxgraph.examples.swing.editor.fileimportexport;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

public class IMGImportExport implements IImportExport {

	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return false;
	}

	@Override
	public void read(String from, mxGraphComponent graphComponent) throws IOException {
	}

	@Override
	public void write(mxGraphComponent graphComponent, String into) throws IOException {
		Color bg = null;
		mxGraph graph = graphComponent.getGraph();
		String ext = into.substring(into.lastIndexOf('.') + 1);
		
		if ((!ext.equalsIgnoreCase("gif") && !ext
				.equalsIgnoreCase("png"))
				|| JOptionPane.showConfirmDialog(
						graphComponent, mxResources
								.get("transparentBackground")) != JOptionPane.YES_OPTION)
		{
			bg = graphComponent.getBackground();
		}

		BufferedImage image = mxCellRenderer
				.createBufferedImage(graph, null, 1, bg,
						graphComponent.isAntiAlias(), null,
						graphComponent.getCanvas());

		if (image != null)
		{
			ImageIO.write(image, ext, new File(into));
		}
		else
		{
			JOptionPane.showMessageDialog(graphComponent,
					mxResources.get("noImageData"));
		}
	}

	@Override
	public Object buildNodeValue() {
		return null;
	}

	@Override
	public Object buildEdgeValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object cloneValue(Object value) {
		return null;
	}



}
