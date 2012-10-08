package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.DefaultFileFilter;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.StringUtils;
import com.mxgraph.util.mxResources;

// defines the file filters used in the open file dialog box.
// associates to each file filter decoding and encoding actions
public class ImportExportPicker {

	public class SortableFileFilter extends DefaultFileFilter implements Comparable<SortableFileFilter> {
		
		public SortableFileFilter(String ext, String desc) {
			super(ext,desc);
		}

		@Override
		public int compareTo(SortableFileFilter ff) {
			if (!StringUtils.isEmptyString(ff.getExtension()) && !StringUtils.isEmptyString(getExtension()))
				return ff.getExtension().compareTo(getExtension());
			else
				return 0;
		}
	}
	
	private final HashMap<SortableFileFilter,IImportExport> fileIO=new HashMap<SortableFileFilter, IImportExport>();
	private final HashMap<String,SortableFileFilter> fileFilterFromDescription=new HashMap<String, SortableFileFilter>();
	private final FileFilter defaultSaveFilter,defaultOpenFilter; 
	
	public void add(SortableFileFilter ff, IImportExport fie) {
		fileIO.put(ff, fie);
		fileFilterFromDescription.put(ff.getDescription(),ff);
	}
	
	public ImportExportPicker() {
		SortableFileFilter ff;
		ff = new SortableFileFilter(".mxe", "mxGraph Editor "+ mxResources.get("file") + " (.mxe)");
		add(ff,new MXEImportExport());
		ff = new SortableFileFilter(".scxml", "SCXML "+ mxResources.get("file") + " (.scxml)");
		defaultSaveFilter=ff;
		defaultOpenFilter=ff;
		add(ff, new SCXMLImportExport());
		ff=new SortableFileFilter(".html","VML " + mxResources.get("file") + " (.html)");
		add(ff, new VMLImportExport());
		ff=new SortableFileFilter(".html","HTML " + mxResources.get("file") + " (.html)");
		add(ff, new HTMLImportExport());
		ff=new SortableFileFilter(".svg","SVG " + mxResources.get("file") + " (.svg)");
		add(ff, new SVGImportExport());
		ff=new SortableFileFilter(".dot","DOT " + mxResources.get("file") + " (.dot)");
		add(ff, new DOTImportExport());
		IImportExport images=new IMGImportExport();
		// Adds a filter for each supported image format
		Object[] imageFormats = ImageIO.getReaderFormatNames();
		// Finds all distinct extensions
		HashSet<String> formats = new HashSet<String>();
		for (int i = 0; i < imageFormats.length; i++)
		{
			String ext = imageFormats[i].toString().toLowerCase();
			formats.add(ext);
		}
		imageFormats = formats.toArray();
		for (int i = 0; i < imageFormats.length; i++)
		{
			String ext = imageFormats[i].toString();
			ff=new SortableFileFilter("."+ext,ext.toUpperCase()+" "+mxResources.get("file")+" (."+ext+")");
			add(ff,images);
		}
	}
	
	public void addImportFiltersToFileChooser(JFileChooser fc) {
		ArrayList<SortableFileFilter> sortedFileFilters=new ArrayList<SortableFileFilter>(fileIO.keySet());
		Collections.sort(sortedFileFilters);
		for(FileFilter ff:sortedFileFilters) {
			if (fileIO.get(ff).canImport())
				fc.addChoosableFileFilter(ff);
		}
		fc.setFileFilter(defaultOpenFilter);
	}
	public void addExportFiltersToFileChooser(JFileChooser fc) {
		ArrayList<SortableFileFilter> sortedFileFilters=new ArrayList<SortableFileFilter>(fileIO.keySet());
		Collections.sort(sortedFileFilters);
		for(FileFilter ff:sortedFileFilters) {
			if (fileIO.get(ff).canExport())
				fc.addChoosableFileFilter(ff);
		}
		fc.setFileFilter(defaultSaveFilter);
	}

	public IImportExport read(JFileChooser fc, SCXMLGraphEditor editor) throws Exception {
		FileFilter ff = fc.getFileFilter();
		String fileName=fc.getSelectedFile().getAbsolutePath();
		mxGraphComponent graphComponent = editor.getGraphComponent();
		IImportExport io = fileIO.get(ff);
		if (io==null) {
			// try all available importers
			IImportExport foundImporter=null;
			for(FileFilter ff1 : fileIO.keySet()) {
				if ((io=fileIO.get(ff1)).canImport())
					try {
						io.read(fileName, graphComponent,fc, editor.getRestrictedStatesConfig());
						foundImporter=io;
						break;
					} catch (Exception e) {}
				}
			if (foundImporter==null) throw new IOException("No importer available to read the selected file: "+fileName);
			else return foundImporter;
		} else {
			// run the selected importer
			io.read(fileName,graphComponent,fc,editor.getRestrictedStatesConfig());
			return io;
		}
	}

	public void write(JFileChooser fc, SCXMLGraphEditor editor) throws Exception {
		String filename;
		FileFilter selectedFilter;
		mxGraphComponent graphComponent = editor.getGraphComponent();
		IImportExport fie;
		if (fc!=null) {
			filename = fc.getSelectedFile().getAbsolutePath();
			selectedFilter = fc.getFileFilter();
			if (selectedFilter instanceof DefaultFileFilter)
			{
				String ext = ((DefaultFileFilter) selectedFilter).getExtension();
				if (!filename.toLowerCase().endsWith(ext))
					filename += ext;
			}
			File file=new File(filename);
			if (file.exists()
					&& JOptionPane.showConfirmDialog(graphComponent,
							mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION)
			{
				return;
			}
			fie=fileIO.get(selectedFilter);
			fie.write(graphComponent, filename);
			if (selectedFilter==defaultSaveFilter) {
				editor.setModified(false);
				editor.setCurrentFile(file,fie);
			}
		} else {
			filename = editor.getCurrentFile().getAbsolutePath();
			fie=editor.getCurrentFileIO();
			fie.write(graphComponent, filename);
			editor.setModified(false);
		}
	}
	
	public void clearFileIO(){
		FileFilter ff = null;
		Set<SortableFileFilter> fileFilters = fileIO.keySet();
		for(SortableFileFilter fileFilter: fileFilters){
			if (fileFilter.getDescription().contains("scxml")) {
				ff = fileFilter;
			}
		}
		IImportExport importExport = fileIO.get(ff);
		importExport.clearInternalID2NodesAndSCXMLID2Nodes();
	}
}
