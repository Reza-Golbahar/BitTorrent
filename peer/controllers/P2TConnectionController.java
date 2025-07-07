package peer.controllers;

import common.models.Message;
import common.utils.MD5Hash;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2TConnectionController {
    public static Message handleCommand(Message message) {
        // TODO: Handle incoming tracker-to-peer commands
        // 1. Parse command from message
        String commandType = message.getFromBody("command");

        // 2. Call appropriate handler (status, get_files_list, get_sends, get_receives)
        // 3. Return response message
        switch (commandType) {
            case "status":
                return status();
            case "get_files_list":
                return getFilesList();
            case "get_sends":
                return getSends();
            case "get_receives":
                return getReceives();
        }

        throw new UnsupportedOperationException("handleCommand not implemented yet");
    }

    private static Message getReceives() {
        // TODO: Return information about received files
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("response", "ok");
            put("command", "get_receives");
        }};

        HashMap<String, List<String>> receivedFiles = new HashMap<>();

        for (String peerAddress : PeerApp.getReceivedFiles().keySet()) {
            List<String> fileNames = new ArrayList<>();
            for (String fileName : PeerApp.getReceivedFiles().get(peerAddress)) {
                fileNames.add("%s %s".formatted(fileName, MD5Hash.HashFile(fileName)));
            }
            receivedFiles.put(peerAddress, fileNames);
        }

        messageBody.put("received_files",receivedFiles);
        return new Message(messageBody, Message.Type.response);
    }

    private static Message getSends() {
        // TODO: Return information about sent files
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("command", "get_sends");
            put("response", "ok");
        }};

        HashMap<String, List<String>> sentFiles = new HashMap<>();

        for (String peerAddress : PeerApp.getSentFiles().keySet()) {
            List<String> fileNames = new ArrayList<>();
            for (String fileName : PeerApp.getSentFiles().get(peerAddress)) {
                fileNames.add("%s %s".formatted(fileName, MD5Hash.HashFile(fileName)));
            }
            sentFiles.put(peerAddress, fileNames);
        }

        messageBody.put("sent_files",sentFiles);
        return new Message(messageBody, Message.Type.response);
        //throw new UnsupportedOperationException("getSends not implemented yet");
    }

    public static Message getFilesList() {
        // TODO: Return list of files in shared folder
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("command", "get_files_list");
            put("response", "ok");
        }};

        HashMap<String, String> files = new HashMap<>();

        File folder = new File(PeerApp.getSharedFolderPath());
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if(file.isFile())
                    files.put(file.getName(), MD5Hash.HashFile(file.getPath()));
            }
        }
        messageBody.put("files", files);

        return new Message(messageBody, Message.Type.response);
    }

    public static Message status() {
        // TODO: Return peer status information
        HashMap<String, Object> messageBody = new HashMap<>() {{
            put("command", "status");
            put("response", "ok");
            put("peer", "%s".formatted(PeerApp.getPeerIP()));
            put("listen_port", "%s".formatted(PeerApp.getPeerPort()));
        }};

        return new Message(messageBody, Message.Type.response);
        //throw new UnsupportedOperationException("status not implemented yet");
    }

    public static Message sendFileRequest(P2TConnectionThread tracker, String fileName) throws Exception {
        // TODO: Send file request to tracker and handle response
        // 1. Build request message
        HashMap<String, Object> messageBody = new HashMap<>(){{
           put("name", fileName);
        }};
        // 2. Send message and wait for response
        Message answer = tracker.sendAndWaitForResponse(new Message(messageBody, Message.Type.file_request), TIMEOUT_MILLIS);
        // 3. raise exception if error or return response
        return answer;
    }
}
