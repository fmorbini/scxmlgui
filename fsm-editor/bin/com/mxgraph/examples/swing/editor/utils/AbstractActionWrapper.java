package com.mxgraph.examples.swing.editor.utils;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class AbstractActionWrapper extends AbstractAction {

	private Action action;
	private Object eventSource;

	public AbstractActionWrapper(Object es, String name, Action a, ImageIcon icon) {
		super(name,icon);
		action=a;
		eventSource=es;
	}

	public void setInternalAction(Action a) {
		action=a;
	}
	public Action getInternalAction() {
		return action;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		action.actionPerformed(new ActionEvent(eventSource, e.getID(), e.getActionCommand()));
	}

}
