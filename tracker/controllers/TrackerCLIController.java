package tracker.controllers;

import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.util.*;
import java.util.regex.Matcher;

public class TrackerCLIController {
    public static String processCommand(String command) {
        // 1. Check command type (END_PROGRAM, REFRESH_FILES, RESET_CONNECTIONS, LIST_PEERS, LIST_FILES, GET_RECEIVES, GET_SENDS)
        TrackerCommands commandType = null;
        for (TrackerCommands value : TrackerCommands.values()) {
            if (value.getMatcher(command).matches()) {
                commandType = value;
                break;
            }
        }

        // 2. Call appropriate handler
        // 3. Return result or error message
        if (commandType == null)
            return TrackerCommands.invalidCommand;

        switch (commandType) {
            case TrackerCommands.REFRESH_FILES:
                return refreshFiles();
            case TrackerCommands.RESET_CONNECTIONS:
                return resetConnections();
            case TrackerCommands.LIST_PEERS:
                return listPeers();
            case TrackerCommands.LIST_FILES:
                return listFiles(command);
            case TrackerCommands.GET_SENDS:
                return getSends(command);
            case TrackerCommands.GET_RECEIVES:
                return getReceives(command);
        }
        return endProgram();
        //throw new UnsupportedOperationException("processCommand not implemented yet");
    }

    private static String getReceives(String command) {
        // TODO: Get list of files received by a peer
        Matcher matcher = TrackerCommands.GET_RECEIVES.getMatcher(command);
        matcher.matches();
        String IP = matcher.group("IP");
        int port = Integer.parseInt(matcher.group("PORT"));

        PeerConnectionThread peerConnectionThread = TrackerApp.getConnectionByIpPort(IP, port);
        if (peerConnectionThread == null)
            return "Peer not found.";

        Map<String, List<String>> receivesMap = TrackerConnectionController.getReceives(peerConnectionThread);

        if (receivesMap == null)
            return "Request timed out.";
        if (receivesMap.isEmpty())
            return "No files received by " + IP + ":" + port;

        List<String> formattedLines = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : receivesMap.entrySet()) {
            String filename = entry.getKey();
            List<String> hashesAndReceivers = entry.getValue();

            for (String hashAndReceiver : hashesAndReceivers) {
                formattedLines.add(filename + " " + hashAndReceiver);  // format: <filename> <hash> - <receiver>
            }
        }

        // Sort first by filename, then by receiver address
        formattedLines.sort(
                Comparator.comparing((String s) -> s.split(" ")[0])  // filename
                        .thenComparing(s -> s.split(" - ")[1])     // receiver
        );

        return String.join("\n", formattedLines);
    }

    private static String getSends(String command) {
        // TODO: Get list of files sent by a peer
        Matcher matcher = TrackerCommands.GET_SENDS.getMatcher(command);
        matcher.matches();
        String IP = matcher.group("IP");
        int port = Integer.parseInt(matcher.group("PORT"));

        PeerConnectionThread peerConnectionThread = TrackerApp.getConnectionByIpPort(IP, port);
        if (peerConnectionThread == null)
            return "Peer not found.";

        Map<String, List<String>> sendsMap = TrackerConnectionController.getSends(peerConnectionThread);

        if (sendsMap == null)
            return "Request timed out.";
        if (sendsMap.isEmpty())
            return "No files sent by " + IP + ":" + port;

        List<String> formattedLines = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : sendsMap.entrySet()) {
            String filename = entry.getKey();
            List<String> hashesAndReceivers = entry.getValue();

            for (String hashAndReceiver : hashesAndReceivers) {
                formattedLines.add(filename + " " + hashAndReceiver);  // format: <filename> <hash> - <receiver>
            }
        }

        // Sort first by filename, then by receiver address
        formattedLines.sort(
                Comparator.comparing((String s) -> s.split(" ")[0])  // filename
                        .thenComparing(s -> s.split(" - ")[1])     // receiver
        );

        return String.join("\n", formattedLines);
    }

    private static String listFiles(String command) {
        // TODO: List files of a peer (do not send command, use the cached list)
        Matcher matcher = TrackerCommands.LIST_FILES.getMatcher(command);

        matcher.matches(); //needed for matcher.group() to work
        String IP = matcher.group("IP");
        int port = Integer.parseInt(matcher.group("PORT"));

        PeerConnectionThread peerConnectionThread = TrackerApp.getConnectionByIpPort(IP, port);
        if (peerConnectionThread == null)
            return "Peer not found.";

        StringBuilder result = new StringBuilder();

        Map<String, String> fileAndHashes = peerConnectionThread.getFileAndHashes();
        List<String> sortedFilenames = new ArrayList<>(fileAndHashes.keySet());
        Collections.sort(sortedFilenames);

        for (String filename : sortedFilenames) {
            result.append("%s %s\n".formatted(
                    filename,
                    peerConnectionThread.getFileAndHashes().get(filename)));
        }
        return result.toString().trim();
    }

    private static String listPeers() {
        StringBuilder result = new StringBuilder();
        for (PeerConnectionThread connection : TrackerApp.getConnections()) {
            result.append("%s:%s\n".formatted(connection.getOtherSideIP(), connection.getOtherSidePort()));
        }
        if (result.isEmpty())
            return "No peers connected.";
        return result.toString().trim();
    }

    private static String resetConnections() {
        // Refresh status and file list for each peer
        for (PeerConnectionThread connection : TrackerApp.getConnections()) {
            connection.refreshStatus();
        }
        return "";
    }

    private static String refreshFiles() {
        for (PeerConnectionThread connection : TrackerApp.getConnections()) {
            connection.refreshFileList();
        }
        return "";
    }

    private static String endProgram() {
        TrackerApp.endAll();
        return "";
    }
}
