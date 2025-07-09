package peer.controllers;

import common.models.Message;
import common.utils.FileUtils;
import common.utils.MD5Hash;
import peer.app.PeerApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class PeerCLIController {
	public static String processCommand(String command) {
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
		Map<String, String> filenamesAndHashes = FileUtils.listFilesInFolder(PeerApp.getSharedFolderPath());
		String result = FileUtils.getSortedFileList(filenamesAndHashes);

		if (result.isEmpty())
			return "Repository is empty.";
		return result;
	}

	private static String handleDownload(String command) {
		// Send file request to tracker
		Matcher matcher = PeerCommands.DOWNLOAD.getMatcher(command);
		matcher.matches();
		String filename = matcher.group("filename");

		Message message;
		try {
			message = P2TConnectionController.sendFileRequest(PeerApp.getP2TConnection(), matcher.group("filename"));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		String response = message.getFromBody("response");
		if (response.equals("error")) {
			String error = message.getFromBody("error");
			if (error.equals("not_found"))
				return "No peer has the file!";
			else if (error.equals("multiple_hash"))
				return "Multiple hashes found!";
			else
				return "Unknown tracker error.";
		}

		// Get peer info and file hash
		String peerIP = message.getFromBody("peer_have");
		int peerPort = message.getIntFromBody("peer_port");
		String md5Hash = message.getFromBody("md5");

		// Request file from peer
		// Return success or error message
		try {
			PeerApp.requestDownload(peerIP, peerPort, filename, md5Hash);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "File downloaded successfully: " + filename;
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "";
	}
}
