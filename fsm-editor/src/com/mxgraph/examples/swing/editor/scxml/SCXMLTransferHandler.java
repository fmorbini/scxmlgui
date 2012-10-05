package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.OpenAction;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.util.mxResources;

public class SCXMLTransferHandler extends mxGraphTransferHandler {

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavors)
	{
		if (super.canImport(comp, flavors))
			return true;
		else {
			for (int i = 0; i < flavors.length; i++)
			{
				if (flavors[i] != null
						&& flavors[i].equals(DataFlavor.javaFileListFlavor))
				{
					return true;
				}
			}

			return false;
		}
	}
	
	@Override
    public boolean importData(TransferHandler.TransferSupport support) {
		try {
        Transferable t = support.getTransferable();
        SCXMLGraphEditor editor = getEditorFromComponent(support.getComponent());

            List<File> l = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);

            int num=l.size();
            if (num>0) {
                File f=l.get(0);
            	if (num>1) {
            		JOptionPane.showMessageDialog(editor,
            				"Importing only first file: "+f,
            				mxResources.get("warning"),
            				JOptionPane.WARNING_MESSAGE);
            	}
            	OpenAction action = new OpenAction(f);
            	action.actionPerformed(new ActionEvent(editor, 0, "", 0));
            }
        } catch (UnsupportedFlavorException e) {
        	return super.importData(support);
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return true;
    }

	public static SCXMLGraphEditor getEditorFromComponent(Component c) {
		Container p=null;
		do {
			if (c instanceof SCXMLGraphEditor) return (SCXMLGraphEditor) c;
		} while ((c=c.getParent())!=null);
		return null;
	}
	
}
