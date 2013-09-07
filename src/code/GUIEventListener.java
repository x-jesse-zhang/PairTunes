package code;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.util.Map;

import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.RealizeCompleteEvent;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GUIEventListener implements ActionListener, ControllerListener, MouseListener {

	Map<String, Object> eventSources;
	Window window;
	JFileChooser fileChooser = new JFileChooser();
	JFrame ipFrame, ipRFrame;
	JButton submit, rSubmit;
	InitiatingServer is;
	InitiatingClient ic;
	JTextField ip1, ip2;
	boolean streaming = false;
	boolean playing = false;
	String firstIP, secondIP, rcvIP;

	public GUIEventListener() {
		//ic = new InitiatingClient(srcIP, port)
	}
	
	public void setWindow(Window w) {
		this.window = w;
		this.eventSources = w.getSources();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// JFileChooser first opens in default directory (My Documents or Unix home directory, usually)
		// Subsequent JFileChooser calls open in the previous directory
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == eventSources.get("importButton")) {
			int returnVal = fileChooser.showOpenDialog(fileChooser);

			if (returnVal == JFileChooser.APPROVE_OPTION) { // Actually gives us the current directory
				String folderDir = fileChooser.getSelectedFile().toString();
				((JTextField) (eventSources.get("displaySong"))).setText(folderDir);
				
				importMusic(folderDir);

				String samIP = "172.16.200.239";
				String jesseIP = "172.16.150.122";
				String stephenIP = "172.16.138.68";
				int port1 = 42050;
				int port2 = 25000;
			}
		} else if(e.getSource() == window.streamButton) {
			ipFrame = new JFrame("Select IP Addresses");
			JLabel l1, l2;
			submit = new JButton("Submit");
			JPanel j = new JPanel();
			JPanel x = new JPanel();
			l1 = new JLabel("IP 1:");
			l2 = new JLabel("IP 2:");
			ip1 = new JTextField(10);
			ip2 = new JTextField(10);
			x.setLayout(new BorderLayout());
			j.add(l1);
			j.add(ip1);
			j.add(l2);
			j.add(ip2);
			j.add(submit);
			x.add(j, BorderLayout.CENTER);
			x.add(submit, BorderLayout.SOUTH);
			ipFrame.add(x);
			ipFrame.setSize(400,200);
			ipFrame.setVisible(true);
			submit.addActionListener(this);
			ip1.addActionListener(this);
			ip2.addActionListener(this);
		} else if(e.getSource() == window.rcvButton) {
			ipRFrame = new JFrame("Select Partner IP Addresses");
			JLabel l1;
			rSubmit = new JButton("Submit");
			JPanel j = new JPanel();
			JPanel x = new JPanel();
			l1 = new JLabel("IP 1:");
			ip1 = new JTextField(10);
			x.setLayout(new BorderLayout());
			j.add(l1);
			j.add(ip1);
			j.add(submit);
			x.add(j, BorderLayout.CENTER);
			x.add(submit, BorderLayout.SOUTH);
			ipRFrame.add(x);
			ipRFrame.setSize(400,200);
			ipRFrame.setVisible(true);
			rSubmit.addActionListener(this);
			ip1.addActionListener(this);
		} else if (e.getSource() == submit) {
			ipFrame.setVisible(false);
			firstIP = ip1.getText().trim();
			secondIP = ip2.getText().trim();
			streaming = true;
		} else if (e.getSource() == rSubmit) {
			ipRFrame.setVisible(false);
			rcvIP = ip1.getText().trim();
			ic = new InitiatingClient(rcvIP, 42050);
		}
	}

	@Override
	public void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof EndOfMediaEvent) {
			if (is != null) {
				is.rtps = null;
				is = null;
			}
			
			if (ic != null) {
				ic.rtpc = null;
				ic = null;
			}
		} else {
			// System.out.println(evt.toString());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == window.songList) {
			Rectangle r = window.songList.getCellBounds(0, window.songList.getLastVisibleIndex());
			if (r != null && r.contains(e.getPoint()) && e.getClickCount() == 2) {
				int index = window.songList.locationToIndex(e.getPoint());
				// edge case: this has the same functionality as the import button
				if (window.songListModel.getElementAt(index).songName.equals("<Add some songs!>")) {
					int returnVal = fileChooser.showOpenDialog(fileChooser);

					if (returnVal == JFileChooser.APPROVE_OPTION) { // Actually gives us the current directory
						String folderDir = fileChooser.getSelectedFile().toString();
						((JTextField) (eventSources.get("displaySong"))).setText(folderDir);
						
						importMusic(folderDir);
					}
				} else {
					window.queueModel.addElement(window.songListModel.getElementAt(index));
					if(streaming) {
						is = new InitiatingServer(firstIP, 42050, true,"file:///"+window.songListModel.getElementAt(index).filePath, this);
						is.initiate();
					} else {
						is = new InitiatingServer("127.0.0.1", 42050, false,"file:///"+window.songListModel.getElementAt(index).filePath, this);
						is.initiate();
					}
					playing = true;
				}
			}
		} else {
			Rectangle r = window.queueList.getCellBounds(0, window.queueList.getLastVisibleIndex());
			if (r != null && r.contains(e.getPoint()) && e.getClickCount() == 2) {
				int index = window.queueList.locationToIndex(e.getPoint());
				// TODO pass the directory of the song to the server and start broadcasting
				System.out.println("Playing: " + window.queueModel.getElementAt(index));
				System.out.println("file:///" + window.queueModel.getElementAt(index).filePath);
			}
		}
	}
	
	public void importMusic(String folderDir) {
		File[] files = new File(folderDir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".wav") || filename.endsWith(".mp3");
			}
		});

		for (File file : files) {
			if (window.songListModel.getElementAt(0).songName.equals("<Add some songs!>"))
				window.songListModel.remove(0);
			window.songListModel.addElement(new SongTuple<String, String>(file.getName(), file.getAbsolutePath()));
		}
		
		File[] subdirs = new File(folderDir).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		
		for (File subdir : subdirs) {
			importMusic(subdir.getPath());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
}
