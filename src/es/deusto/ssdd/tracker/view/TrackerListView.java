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
	private MyBooleanModel model;
	private Object [][] rows;
	public static final int numberRowsExample=20;
	

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
		String[] columnNames = {"Tracker ID", "Active", "Last Keep Alive","Master"};
		generateTestData();
		model=new MyBooleanModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table=new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane=new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500,250));
		this.add(scrollPane);
	}
	public void generateTestData(){
		rows=new Object[numberRowsExample][];
		Object []rowData;
		for(int i=0;i<numberRowsExample;i++){
			rowData=new Object[4];
			rowData[0]="Tracker #"+(i+1);
			rowData[1]="YES";
			rowData[2]="123132437148126312";
			rowData[3]=true;
			rows[i]=rowData;
		}
	}

}

class MyBooleanModel extends DefaultTableModel{

	private static final long serialVersionUID = 1L;

	public boolean isCellEditable(int row,int column){
		return false;
	}
	public Class<?> getColumnClass(int column) {
    	switch (column) {
	        case 0:
	            return String.class;
	        case 1:
	            return String.class;
	        case 2:
	            return String.class;
	        case 3:
	            return Boolean.class;
	        default:
	            return String.class;
    	}
    }
		
}
