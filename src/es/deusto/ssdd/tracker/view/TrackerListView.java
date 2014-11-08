package es.deusto.ssdd.tracker.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import es.deusto.ssdd.tracker.controller.TrackerListController;
import es.deusto.ssdd.tracker.vo.ActiveTracker;

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
	private String[] columnNames = {"Tracker ID", "Active", "Last Keep Alive","Master"};

	public TrackerListView ( TrackerListController trackerListController )
	{
		controller = trackerListController;
		controller.addObserver(this);
		createTable();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if ( arg.equals("NewActiveTracker") || arg.equals("DeleteActiveTracker") || arg.equals("EditActiveTracker"))
		{
			generateTrackersData();
			model.setDataVector(rows, columnNames);
			model.fireTableDataChanged();
			configureSizesOfTable(table);
			table.repaint();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	public void createTable(){
		
		generateTrackersData();
		model=new MyBooleanModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table=new JTable(model);
		configureSizesOfTable(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane=new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500,225));
		this.add(scrollPane);
	}
	
	public void configureSizesOfTable( JTable table ){
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
	}
	
	public void generateTrackersData() {
		List<ActiveTracker> listActiveTrackers = controller.getActiveTrackers();
		System.out.println("Lista Active Trackers actual " + listActiveTrackers.toString());
		rows=new Object[listActiveTrackers.size()][];
		Object []rowData;
		ActiveTracker tracker;
		for ( int i=0; i < listActiveTrackers.size(); i++) {
			tracker = listActiveTrackers.get(i);
			if ( tracker != null )
			{
				rowData = new Object[ActiveTracker.numColumns];
				rowData[0] = tracker.getId();
				rowData[1] = tracker.isActive();
				rowData[2] = tracker.getLastKeepAlive();
				rowData[3] = tracker.isMaster();
				rows[i] = rowData;
			}
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
