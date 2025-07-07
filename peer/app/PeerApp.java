package peer.app;

import java.net.Socket;
import java.util.*;

public class PeerApp {
	public static final int TIMEOUT_MILLIS = 500;

	// TODO: static fields for peer's ip, port, shared folder path, sent files, received files, tracker connection thread, p2p listener thread, torrent p2p threads
	private static int peerPort;
	private static String peerIP;

	private static String sharedFolderPath;
	//sentFiles -> {PeerAddress(ip:port), List of fileNames}
	private static Map<String, List<String>> sentFiles = new HashMap<>();
	private static Map<String, List<String>> receivedFiles = new HashMap<>();

	private static P2TConnectionThread trackerConnectionThread;
	private static P2PListenerThread p2PListenerThread;
	private static ArrayList<TorrentP2PThread> torrentP2PThreads = new ArrayList<>();

	private static boolean exitFlag = false;

	public static boolean isEnded() {
		return exitFlag;
	}

	public static void initFromArgs(String[] args) throws Exception {
		// TODO: Initialize peer with command line arguments
		// 1. Parse self address (ip:port)
		int portStartIndex = args[0].lastIndexOf(":");
		peerPort = Integer.parseInt(args[0].substring(portStartIndex + 1));
		peerIP = args[0].substring(0, portStartIndex);

		// 2. Parse tracker address (ip:port)
		int trackerPortStartIndex = args[1].lastIndexOf(":");
		int trackerPort = Integer.parseInt(args[1].substring(trackerPortStartIndex + 1));
		String trackerIP = args[1].substring(0, trackerPortStartIndex);

		// 3. Set shared folder path
		sharedFolderPath = args[2];

		// 4. Create tracker connection thread
		Socket socket = new Socket(trackerIP, trackerPort);
		trackerConnectionThread = new P2TConnectionThread(socket);

		// 5. Create peer listener thread
		p2PListenerThread = new P2PListenerThread(peerPort);
	}

	public static void endAll() {
		exitFlag = true;
		// TODO: Implement cleanup
		// 1. End tracker connection
		trackerConnectionThread.end();
		// 2. End all torrent threads
		for (TorrentP2PThread torrentP2PThread : torrentP2PThreads) {
			removeTorrentP2PThread(torrentP2PThread);
		}
		torrentP2PThreads.clear();

		exitFlag = true;
		// 3. Clear file lists
		//TODO
	}

	public static void connectTracker() {
		// TODO: Start tracker connection thread
		// Check if thread exists and not running, then Start thread
		if (trackerConnectionThread != null && !trackerConnectionThread.isAlive()) {
			trackerConnectionThread.start();
		}
		//throw new UnsupportedOperationException("Tracker connection not implemented yet");
	}

	public static void startListening() {
		// TODO: Start peer listener thread
		// Check if thread exists and not running, then Start thread
		if (p2PListenerThread != null && !p2PListenerThread.isAlive()) {
			p2PListenerThread.start();
		}
		//throw new UnsupportedOperationException("Peer listener thread not implemented yet");
	}

	public static void removeTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		// TODO: Remove and cleanup torrent thread

		//throw new UnsupportedOperationException("Torrent P2P thread not implemented yet");
	}

	public static void addTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		// TODO: Add new torrent thread
		// 1. Check if thread is valid
		if (torrentP2PThread == null) return;
		// 2. Check if already exists
		if (torrentP2PThreads.contains(torrentP2PThread)) return;
		// 3. Add to list
		torrentP2PThreads.add(torrentP2PThread);
	}

	public static String getSharedFolderPath() {
		// TODO: Get shared folder path
		return sharedFolderPath;
	}

	public static void addSentFile(String receiver, String fileNameAndHash) {
		// TODO: Add file to sent files list
		throw new UnsupportedOperationException("Sent files not implemented yet");
	}

	public static void addReceivedFile(String sender, String fileNameAndHash) {
		// TODO: Add file to received files list
		throw new UnsupportedOperationException("Received files not implemented yet");
	}

	public static String getPeerIP() {
		// TODO: Get peer IP address
		return peerIP;
	}

	public static int getPeerPort() {
		// TODO: Get peer port
		return peerPort;
	}

	public static Map<String, List<String>> getSentFiles() {
		// TODO: Get copy of sent files map
		throw new UnsupportedOperationException("Received files not implemented yet");
	}

	public static Map<String, List<String>> getReceivedFiles() {
		// TODO: Get copy of received files map
		throw new UnsupportedOperationException("Received files not implemented yet");
	}

	public static P2TConnectionThread getP2TConnection() {
		// TODO: Get tracker connection thread
		return trackerConnectionThread;
	}

	public static void requestDownload(String ip, int port, String filename, String md5) throws Exception {
		// TODO: Implement file download from peer
		// 1. Check if file already exists
		// 2. Create download request message
		// 3. Connect to peer
		// 4. Send request
		// 5. Receive file data
		// 6. Save file
		// 7. Verify file integrity
		// 8. Update received files list
		String peerAddress = "%s %s".formatted();
		//receivedFiles.get().add();
		throw new UnsupportedOperationException("File download not implemented yet");
	}
}
