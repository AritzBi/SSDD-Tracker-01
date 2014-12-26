package es.deusto.ssdd.tracker.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import es.deusto.ssdd.tracker.controller.PeerListController;

public class PeerListView extends JPanel implements Observer, ActionListener {

	private static final long serialVersionUID = -3290055577494365102L;
	private PeerListController controller;
	private JTable table;
	private MyButtonModel model;
	private Object[][] rows;
	public static final int numberRowsExample = 20;

	public PeerListView(PeerListController peerListController) {
		controller = peerListController;
		//controller.addObserver(this);
		createTable();
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public void createTable() {
		String[] columnNames = { "Peer ID", "IP", "Port", "Downloaded",
				"Uploaded", "Torrents" };
		generateTestData();
		model = new MyButtonModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(55);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(5).setCellRenderer(new ButtonRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.addMouseListener(new JTableButtonMouseListener());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500, 250));
		this.add(scrollPane);
	}

	public void generateTestData() {
		rows = new Object[numberRowsExample][];
		Object[] rowData;
		for (int i = 0; i < numberRowsExample; i++) {
			rowData = new Object[6];
			rowData[0] = "Peer #" + (i + 1);
			rowData[1] = "129.123.123.123";
			rowData[2] = "7808";
			rowData[3] = "10GB";
			rowData[4] = "5GB";
			rows[i] = rowData;
		}
	}

	public Object[][] generateTorrentTestData() {
		Object[][] data = new Object[numberRowsExample][];
		Object[] rowData;
		for (int i = 0; i < numberRowsExample; i++) {
			rowData = new Object[2];
			rowData[0] = "AQWJDANSM213ASDDANM31231S";
			rowData[1] = "SEEDER";
			data[i] = rowData;
		}
		return data;
	}

	class JTableButtonMouseListener implements MouseListener {

		private void forwardEventToButton(MouseEvent e) {
			TableColumnModel columnModel = table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			// TODO Use the rows later
			// int row = e.getY() / table.getRowHeight();
			if (column == 5) {
				new TorrentsListView(generateTorrentTestData());
			}

		}

		public JTableButtonMouseListener() {

		}

		public void mouseClicked(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {

		}

		public void mousePressed(MouseEvent e) {

		}

		public void mouseReleased(MouseEvent e) {

		}
	}

}

class MyButtonModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public boolean isCellEditable(int row, int column) {
		return false;
	}

}

class ButtonRenderer implements TableCellRenderer {
	JButton button = new JButton();

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		button.setText("View");
		return button;
	}
}
