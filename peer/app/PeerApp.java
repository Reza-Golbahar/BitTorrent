package peer.app;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class PeerApp {
	public static final int TIMEOUT_MILLIS = 500;

	private static int peerPort;
	private static String peerIP;

	private static String sharedFolderPath;
	//sentFiles -> {PeerAddress(ip:port), List of {filename hash}}
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
		// 1. End tracker connection
		trackerConnectionThread.end();
		// 2. End all torrent threads
		for (TorrentP2PThread torrentP2PThread : torrentP2PThreads) {
			removeTorrentP2PThread(torrentP2PThread);
		}
		torrentP2PThreads.clear();

		exitFlag = true;
		// 3. Clear file lists
		System.exit(0);
	}

	public static void connectTracker() {
		// Check if thread exists and not running, then Start thread
		if (trackerConnectionThread != null && !trackerConnectionThread.isAlive()) {
			trackerConnectionThread.start();
		}
	}

	public static void startListening() {
		// Check if thread exists and not running, then Start thread
		if (p2PListenerThread != null && !p2PListenerThread.isAlive()) {
			p2PListenerThread.start();
		}
	}

	public static void removeTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		torrentP2PThreads.remove(torrentP2PThread);
		torrentP2PThread.end();
	}

	public static void addTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		// 1. Check if thread is valid
		if (torrentP2PThread == null) return;
		// 2. Check if already exists
		if (torrentP2PThreads.contains(torrentP2PThread)) return;
		// 3. Add to list
		torrentP2PThreads.add(torrentP2PThread);
	}

	public static String getSharedFolderPath() {
		return sharedFolderPath;
	}

	public static void addSentFile(String receiver, String fileNameAndHash) {
		sentFiles.computeIfAbsent(receiver, k -> new ArrayList<>()).add(fileNameAndHash);
	}

	public static void addReceivedFile(String sender, String fileNameAndHash) {
		receivedFiles.computeIfAbsent(sender, k -> new ArrayList<>()).add(fileNameAndHash);
	}

	public static String getPeerIP() {
		return peerIP;
	}

	public static int getPeerPort() {
		return peerPort;
	}

	public static Map<String, List<String>> getSentFiles() {
		Map<String, List<String>> copy = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : sentFiles.entrySet()) {
			copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return copy;
	}

	public static Map<String, List<String>> getReceivedFiles() {
		Map<String, List<String>> copy = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : receivedFiles.entrySet()) {
			copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return copy;
	}

	public static P2TConnectionThread getP2TConnection() {
		return trackerConnectionThread;
	}

	public static void requestDownload(String ip, int port, String filename, String md5) throws Exception {
		// 1. Check if file already exists
		File file = new File(sharedFolderPath, filename);
		if (file.exists()) {
			throw new RuntimeException("You already have the file!");
		}

		// 2. Create download request message
		HashMap<String, Object> messageBody = new HashMap<>() {{
			put("name", filename);
			put("md5", md5);
			put("receiver_ip", PeerApp.getPeerIP());
			put("receiver_port", PeerApp.getPeerPort());
		}};
		Message request = new Message(messageBody, Message.Type.download_request);

		// 3. Connect to peer
		Socket socket = new Socket(ip, port);

		// 4. Send request
		String JSONString = JSONUtils.toJson(request);

		try {
			DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataOutputStream.writeUTF(JSONString);
			dataOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 5. Receive file data
		// 6. Save file
		FileOutputStream fileOut = new FileOutputStream(file);
		InputStream dataInputStream = socket.getInputStream();

		byte[] buffer = new byte[4096];
		int bytesRead;

		while ((bytesRead = dataInputStream.read(buffer)) != -1) {
			fileOut.write(buffer, 0, bytesRead);
		}
		fileOut.close();
		socket.close();

		// 7. Verify file integrity
		String downloadedMd5 = MD5Hash.HashFile(file.getAbsolutePath());
		if (!downloadedMd5.equals(md5)) {
			file.delete();
			throw new RuntimeException("The file has been downloaded from peer but is corrupted!");
		}

		// 8. Update received files list
		addReceivedFile(ip + ":" + port, filename + " " + md5);
	}

}
