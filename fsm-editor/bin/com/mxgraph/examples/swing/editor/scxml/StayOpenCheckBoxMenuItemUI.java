package com.mxgraph.examples.swing.editor.scxml;

// from http://forums.sun.com/thread.jspa?threadID=5366636

import javax.swing.MenuSelectionManager;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

class StayOpenCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {
	@Override
	protected void doClick(MenuSelectionManager msm) {
		menuItem.doClick(0);
	}
}
