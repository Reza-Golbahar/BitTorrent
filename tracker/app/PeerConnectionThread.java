package tracker.app;

import common.models.ConnectionThread;
import common.models.Message;
import tracker.controllers.TrackerConnectionController;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;

public class PeerConnectionThread extends ConnectionThread {
    private HashMap<String, String> fileAndHashes = new HashMap<>();

    public PeerConnectionThread(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public boolean initialHandshake() {
        try {
            // TODO: Implement initial handshake
            // Refresh peer status (IP and port), Get peer's file list, Add connection to tracker's connection list
            refreshStatus();
            refreshFileList();
            TrackerApp.addPeerConnection(this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void refreshStatus() {
        // TODO: Implement status refresh
        // Send status command and update peer's IP and port and wait for response
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("command", "status");
        }};
        Message response = sendAndWaitForResponse(new Message(messageBody, Message.Type.command), TIMEOUT_MILLIS);
        // then update peer's IP and port
        otherSideIP = response.getFromBody("peer");
        otherSidePort = response.getIntFromBody("listen_port");
    }

    public void refreshFileList() {
        // TODO: Implement file list refresh
        // Request and update peer's file list
        fileAndHashes.clear();
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("command", "get_files_list");
        }};
        Message message = sendAndWaitForResponse(new Message(messageBody, Message.Type.command), TIMEOUT_MILLIS);
        Map<String, Object> files = message.getFromBody("files");

        for (String file : files.keySet()) {
            fileAndHashes.put(file, (String)files.get(file));
        }
    }

    @Override
    protected boolean handleMessage(Message message) {
        if (message.getType() == Message.Type.file_request) {
            sendMessage(TrackerConnectionController.handleCommand(message));
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        TrackerApp.removePeerConnection(this);
    }

    public Map<String, String> getFileAndHashes() {
        return Map.copyOf(fileAndHashes);
    }
}
