package peer.controllers;

import common.models.Message;
import common.utils.MD5Hash;
import peer.app.P2PListenerThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;

public class PeerCLIController {
	public static String processCommand(String command) {
		// TODO: Process Peer CLI commands
		// 1. Check command type (END_PROGRAM, DOWNLOAD, LIST)
		// 2. Call appropriate handler
		// 3. Return result or error message
		if (PeerCommands.DOWNLOAD.matches(command)) {
			return handleDownload(command);
		} else if (PeerCommands.LIST.matches(command)) {
			return handleListFiles();
		} else if (PeerCommands.END.matches(command))
			return endProgram();
		throw new UnsupportedOperationException("processCommand not implemented yet");
	}

	private static String handleListFiles() {
		// TODO: Handle list files command
		StringBuilder result = new StringBuilder();
		File folder = new File(PeerApp.getSharedFolderPath());
		if (folder.exists() && folder.isDirectory()) {
			for (File file : folder.listFiles()) {
				if (file.isFile())
					result.append("%s %s\n".formatted(file.getName(), MD5Hash.HashFile(file.getPath())));
			}
		}
		if (result.isEmpty())
			return "Repository is empty.";

		return result.toString().trim();
	}

	private static String handleDownload(String command) {
		// TODO: Handle download command
		// Send file request to tracker
		Matcher matcher = PeerCommands.DOWNLOAD.getMatcher(command);
		Message message;
		try {
			message = P2TConnectionController.sendFileRequest(PeerApp.getP2TConnection(), matcher.group("filename"));
		} catch (Exception e) {
			return "";
		}
		String response = message.getFromBody("response");
		if (response.equals("error")) {
			String error = message.getFromBody("error");
			if (error.equals("not_found"))
				return "No peer has the file!";
			else if (error.equals("multiple_hash"))
				return "Multiple hashes found!";
		}
		// Get peer info and file hash
		String peerIP = message.getFromBody("peer_have");
		int peerPort = Integer.parseInt(message.getFromBody("peer_port"));
		String md5Hash = message.getFromBody("md5");

		// Request file from peer

		//TODO
//		HashMap<String, Object> messageBody = new HashMap<>();
//		Message response2 = ;

		// Return success or error message

		//TODo
		//return "File downloaded successfully: %s".formatted();
		return "";
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "";
	}
}
