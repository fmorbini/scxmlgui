package com.mxgraph.examples.swing.editor.scxml;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mxgraph.util.StringUtils;
import com.mxgraph.util.mxResources;

public class TextDialog extends JDialog implements ActionListener, WindowListener {
	public TextDialog(Window parent, String title,String content,ModalityType modal) {
		super(parent,modal);
		setTitle(title);
		JPanel contentPane = new JPanel(new BorderLayout());
		populateGUI(contentPane,content);
		contentPane.setOpaque(true);
		
		addWindowListener(this);
		
		//Create and set up the window.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(contentPane);

		//Display the window.
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private void populateGUI(JPanel contentPane, String content) {
		JButton okButton = new JButton(mxResources.get("ok"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		//Create the list and put it in a scroll pane.
		JTextArea textArea = new JTextArea(Math.max(StringUtils.getRows(content), 10),Math.max(StringUtils.getColumns(content),10));
		textArea.setText(content);
		textArea.setEditable(false);
		JScrollPane textPane = new JScrollPane(textArea);

		contentPane.setLayout(new GridBagLayout());

		//Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        contentPane.add(textPane, c);

        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        contentPane.add(okButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd=e.getActionCommand();
		if (cmd.equals("OK")) {
			dispose();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
