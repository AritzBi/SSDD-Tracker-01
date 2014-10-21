package es.deusto.ssdd.tracker.view;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TorrentsListView extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4164593704177471081L;
	private JTable table;
	private MyBooleanModel model;
	private Object [][] rows;
	
	public TorrentsListView ( Object [][] rows)
	{
		this.rows=rows;
		createTable();
	}
	
	public void createTable(){
		setLocationRelativeTo(null);
		setTitle("Torrent List");
		String[] columnNames = {"Info hash", "Role"};
		model=new MyBooleanModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table=new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(65);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane=new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(425,250));
		this.add(scrollPane);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				dispose();
			}
		});
		this.setVisible(true);
		setSize(400, 300);
	}
}
