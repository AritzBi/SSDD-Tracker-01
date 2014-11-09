package es.deusto.ssdd.tracker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;

import es.deusto.ssdd.tracker.controller.ConfigurationController;

public class ConfigurationView extends JPanel implements Observer,
		ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -291548739663122869L;
	private ConfigurationController controller;

	public ConfigurationView(ConfigurationController configurationController) {
		super(new BorderLayout());
		controller = configurationController;
		controller.addObserver(this);
		controller.addObserverRedundandcy(this);
		setUpPanel();
	}

	// Create the labels
	private Label lblIpAddress;
	private Label lblPort;
	private Label lblPortForPeers;
	private Label lblId;

	// Create the texfields
	private JTextField txtIpAddress;
	private JTextField txtPort;
	private JTextField txtPortForPeers;
	private JSpinner spinnerId;

	// Create the buttons
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnForceError;

	private void setUpPanel() {
		// Specify the labels
		lblIpAddress = new Label("IP address");
		lblIpAddress.setFont(new Font("Serif", Font.BOLD, 14));
		lblPort = new Label("Port (Trackers)");
		lblPort.setFont(new Font("Serif", Font.BOLD, 14));
		lblPortForPeers = new Label("Port (Peers)");
		lblPortForPeers.setFont(new Font("Serif", Font.BOLD, 14));
		lblId = new Label("Id");
		lblId.setFont(new Font("Serif", Font.BOLD, 14));

		// Specify the box for IP ADDRESS
		Box boxForIpAddress = Box.createVerticalBox();
		boxForIpAddress.add(Box.createVerticalGlue());
		txtIpAddress = new JTextField();
		txtIpAddress.setColumns(20);
		txtIpAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				txtIpAddress.getPreferredSize().height));
		txtIpAddress.setText("224.0.0.4");
		boxForIpAddress.add(txtIpAddress);
		boxForIpAddress.add(Box.createVerticalGlue());

		// Specify the box for PORT
		Box boxForPort = Box.createVerticalBox();
		boxForPort.add(Box.createVerticalGlue());
		txtPort = new JTextField();
		txtPort.setColumns(20);
		txtPort.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtPort
				.getPreferredSize().height));
		txtPort.setText("40");
		boxForPort.add(txtPort);
		boxForPort.add(Box.createVerticalGlue());

		// Specify the box for PORT FOR PEERS
		Box boxForPortForPeers = Box.createVerticalBox();
		boxForPortForPeers.add(Box.createVerticalGlue());
		txtPortForPeers = new JTextField();
		txtPortForPeers.setColumns(20);
		txtPortForPeers.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				txtPortForPeers.getPreferredSize().height));
		txtPortForPeers.setText("50");
		boxForPortForPeers.add(txtPortForPeers);
		boxForPortForPeers.add(Box.createVerticalGlue());

		// Specify the box for ID TRACKER
		Box boxForId = Box.createVerticalBox();
		boxForId.add(Box.createVerticalGlue());
		String[] idsString = getListIds();
		SpinnerListModel idModel = new SpinnerListModel(idsString);
		spinnerId = new JSpinner(idModel);
		spinnerId.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtIpAddress
				.getPreferredSize().height));
		boxForId.add(spinnerId);
		boxForId.add(Box.createVerticalGlue());

		// Specify a panel per label/input field
		JPanel panelIpAddress = new JPanel(new GridLayout(1, 0));
		panelIpAddress.add(lblIpAddress);
		panelIpAddress.add(boxForIpAddress);

		JPanel panelPort = new JPanel(new GridLayout(1, 0));
		panelPort.add(lblPort);
		panelPort.add(boxForPort);

		JPanel panelPortForPeers = new JPanel(new GridLayout(1, 0));
		panelPortForPeers.add(lblPortForPeers);
		panelPortForPeers.add(boxForPortForPeers);

		JPanel panelId = new JPanel(new GridLayout(1, 0));
		panelId.add(lblId);
		panelId.add(boxForId);

		JPanel panelData = new JPanel(new GridLayout(4, 0));
		panelData.add(panelIpAddress);
		panelData.add(panelPort);
		panelData.add(panelPortForPeers);
		panelData.add(panelId);

		// Specify the button panel
		btnStart = new JButton("Start");
		btnStart.addActionListener(this);
		btnStop = new JButton("Stop");
		btnStop.addActionListener(this);
		btnStop.setVisible(false);
		btnForceError = new JButton("Force Error");
		btnForceError.addActionListener(this);
		btnForceError.setEnabled(false);

		JPanel buttonPane = new JPanel(new FlowLayout());
		buttonPane.add(btnStart);
		buttonPane.add(btnStop);
		buttonPane.add(btnForceError);

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		add(panelData, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.SOUTH);

	}

	private String[] getListIds() {
		String[] ids = new String[40];

		for (int i = 0; i < 40; i++) {
			String value = String.valueOf(i);
			ids[i] = value;
		}

		return ids;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(btnStart)) {
			String ipAddress = txtIpAddress.getText();
			String port = txtPort.getText();
			String portForPeers = txtPortForPeers.getText();
			String message = "";
			if (ipAddress != null && !ipAddress.equals("") && port != null
					&& !port.equals("") && portForPeers != null
					&& !portForPeers.equals("")) {
				if (controller.checkIpAddress(txtIpAddress.getText())) {
					// Si la IP es correcta, se conecta el tracker
					controller.connect(ipAddress, Integer.parseInt(port),
							Integer.parseInt(portForPeers),
							(String) spinnerId.getValue());
					btnForceError.setEnabled(true);
					btnStart.setVisible(false);
					btnStop.setVisible(true);
				} else {
					message = "The specified IP ( " + txtIpAddress.getText()
							+ " ) is not a correct IP address";
				}
			} else {
				message = "You have to specify a value for all fields";
			}

			if (message != "") {
				JOptionPane.showMessageDialog(null, message);
			}

		} else if (e.getSource().equals(btnStop)) {
			controller.disconnect();
			btnForceError.setEnabled(false);
			btnStart.setVisible(true);
			btnStop.setVisible(false);
		}
		
		else if ( e.getSource().equals(btnForceError)) {
			controller.disconnect();
			btnForceError.setEnabled(false);
			btnStart.setVisible(true);
			btnStop.setVisible(false);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if ( arg.equals("NewIdTracker") )
		{
			//Update the value of the id of the tracker
			spinnerId.setValue(controller.getTracker().getId());
		}
	}

}
