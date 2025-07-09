package peer.app;

import common.models.Message;
import common.utils.JSONUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2PListenerThread extends Thread {
	private final ServerSocket serverSocket;

	public P2PListenerThread(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
	}

	private void handleConnection(Socket socket) throws Exception {
		// TODO: Implement peer connection handling
		// 1. Set socket timeout
		socket.setSoTimeout(TIMEOUT_MILLIS);

		// 2. Read incoming message
		DataInputStream in = new DataInputStream(socket.getInputStream());
		String message = in.readUTF();

		// 3. Parse message type
		Message fromJson = JSONUtils.fromJson(message);
		Message.Type messageType = fromJson.getType();

		// 4. Handle download requests by starting a new TorrentP2PThread
		if (messageType.equals(Message.Type.download_request)) {
			File file = new File(PeerApp.getSharedFolderPath(), fromJson.getFromBody("name"));
			String receiver = "%s:%s".formatted(
					fromJson.getFromBody("receiver_ip"),
					fromJson.getIntFromBody("receiver_port"));

			TorrentP2PThread torrentP2PThread = new TorrentP2PThread(socket, file, receiver);
			torrentP2PThread.start();
		}

		// 5. Close socket for other message types (EOF)
		else {
			socket.close();
		}
	}

	@Override
	public void run() {
		while (!PeerApp.isEnded()) {
			try {
				Socket socket = serverSocket.accept();
				handleConnection(socket);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		try {serverSocket.close();} catch (Exception ignored) {}
	}
}
