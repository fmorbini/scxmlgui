package com.mxgraph.examples.swing.editor.scxml;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.ImportExportPicker;
import com.mxgraph.util.mxResources;

public class SCXMLFileChoser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	private static FileChoserCustomControls ac=null;
	
	public SCXMLFileChoser(SCXMLGraphEditor editor,String lastDir,File file) {
		ac=new FileChoserCustomControls(editor);
		String dirOfLastOpenedFile=editor.menuBar.getLastOpenedDir();
		String wd=(lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():((dirOfLastOpenedFile!=null)?dirOfLastOpenedFile:System.getProperty("user.dir")));
		setCurrentDirectory(new File(wd));

		ImportExportPicker fileIO=editor.getIOPicker();
		fileIO.addImportFiltersToFileChooser(this);

		if (file!=null) setSelectedFile(file);
		else {
			setAccessory(ac);
			int rc = showDialog(null, mxResources.get("openFile"));
			if (rc != JFileChooser.APPROVE_OPTION) setSelectedFile(null);
		}
	}

	public boolean ignoreStoredLayout() {
		return ac.ignoreStoredLayout();
	}
	
	public static class FileChoserCustomControls extends JPanel implements ItemListener {

		private static final long serialVersionUID = 1L;
		public static final String PREFERENCE_IGNORE_STORED_LAYOUT = "IGNORE_STORED_LAYOUT";
		private JCheckBox ignoreSizes;
		private SCXMLGraphEditor editor=null;

		public FileChoserCustomControls(SCXMLGraphEditor editor) {
			ignoreSizes = new JCheckBox(mxResources.get("ignoreStoredLayout"));
			this.editor=editor;
			boolean ignoreStoredLayout=editor.preferences.getBoolean(PREFERENCE_IGNORE_STORED_LAYOUT, true);
			ignoreSizes.setSelected(ignoreStoredLayout);
			ignoreSizes.addItemListener(this);
			add(ignoreSizes);
			setVisible(true);
		}
		
		public boolean ignoreStoredLayout() {
			return ignoreSizes.isSelected();
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			editor.preferences.putBoolean(PREFERENCE_IGNORE_STORED_LAYOUT, e.getStateChange()==ItemEvent.SELECTED);
		}
	}
}
