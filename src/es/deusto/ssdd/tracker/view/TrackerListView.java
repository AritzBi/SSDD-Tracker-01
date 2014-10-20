package es.deusto.ssdd.tracker.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import es.deusto.ssdd.tracker.controller.TrackerListController;

public class TrackerListView extends JPanel implements Observer,ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3494245647851905237L;
	private TrackerListController controller;
	private JTable table;
	private DefaultTableModel model;
	private Object [][] rows;
	public static final int numberRowsExample=10;
	

	public TrackerListView ( TrackerListController trackerListController )
	{
		controller = trackerListController;
		controller.addObserver(this);
		createTable();
	}
	
	@Override
	public void update(Observable o, Object arg) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void createTable(){
		Container cTable=new JPanel();
		//Array con el nombre de las colummnas
		String[] columnNames = {"Tracker ID", "Active", "Last Keep Alive","Master"};
		generateTestData();
		model=new DefaultTableModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table=new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane=new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500,500));
		this.add(scrollPane);
	}
	int[] myIntArray = new int[3];
	public void generateTestData(){
		rows=new Object[numberRowsExample][];
		Object []rowData;
		for(int i=0;i<numberRowsExample;i++){
			rowData=new String[4];
			rowData[0]="Tracker #"+(i+1);
			rowData[1]="YES";
			rowData[2]="123132437148126312";
			rowData[3]="YES";
			rows[i]=rowData;
		}
	}

}
