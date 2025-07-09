package tracker.controllers;

import common.models.Message;
import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.util.*;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;

public class TrackerConnectionController {
	public static Message handleCommand(Message message) {
		// 1. Validate message type and content
		String filename = message.getFromBody("name");
		List<PeerConnectionThread> peers = new ArrayList<>();
		HashSet<String> hashes = new HashSet<>();

		// 2. Find peers having the requested file
		for (PeerConnectionThread connection : TrackerApp.getConnections()) {
			if (connection.getFileAndHashes().containsKey(filename)) {
				peers.add(connection);
				hashes.add(connection.getFileAndHashes().get(filename));
			}
		}
		// 3. Check for hash consistency
		if (hashes.size() > 1) {
			HashMap<String, Object> messageBody  = new HashMap<>(){{
				put("response", "error");
				put("error", "multiple_hash");
			}};
			return new Message(messageBody, Message.Type.response);
		}

		// 4. Return peer information or error
		if (hashes.isEmpty()) {
			HashMap<String, Object> messageBody  = new HashMap<>(){{
				put("response", "error");
				put("error", "not_found");
			}};
			return new Message(messageBody, Message.Type.response);
		}

		Random random = new Random();
		int randInt = random.nextInt(peers.size());
		PeerConnectionThread peer_have = peers.get(randInt);

		HashMap<String, Object> messageBody  = new HashMap<>(){{
			put("response", "peer_found");
			put("md5", hashes.toArray()[0]);
			put("peer_have", peer_have.getOtherSideIP());
			put("peer_port", peer_have.getOtherSidePort());
		}};
		return new Message(messageBody, Message.Type.response);
	}

	public static Map<String, List<String>> getSends(PeerConnectionThread connection) {
		// 1. Build command message
		HashMap<String, Object> commandBody = new HashMap<>();
		commandBody.put("command", "get_sends");

		Message request = new Message(commandBody, Message.Type.command);

		// 2. Send message and wait for response
		Message response = connection.sendAndWaitForResponse(request, TIMEOUT_MILLIS);
		if (response == null) //request timed out
			return null;

		// 3. Parse and return sent files map
		Map<String, List<String>> sentFilesRaw = response.getFromBody("sent_files");
		if (sentFilesRaw == null || sentFilesRaw.isEmpty())
			return Collections.emptyMap();

		Map<String, List<String>> result = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : sentFilesRaw.entrySet()) {
			String receiver = entry.getKey();
			for (String fileAndHash : entry.getValue()) {
				String[] parts = fileAndHash.split(" ", 2); // [filename, hash]
				if (parts.length != 2) continue;

				String filename = parts[0];
				String hash = parts[1];
				String combined = hash + " - " + receiver;

				result.computeIfAbsent(filename, k -> new ArrayList<>()).add(combined);
			}
		}

		for (List<String> list : result.values()) {
			list.sort(Comparator.comparing(s -> s.split(" - ")[1]));
		}

		return result;
	}

	public static Map<String, List<String>> getReceives(PeerConnectionThread connection) {
		// 1. Build command message
		HashMap<String, Object> commandBody = new HashMap<>();
		commandBody.put("command", "get_receives");

		Message request = new Message(commandBody, Message.Type.command);

		// 2. Send message and wait for response
		Message response = connection.sendAndWaitForResponse(request, TIMEOUT_MILLIS);
		if (response == null) //request timed out
			return null;

		// 3. Parse and return received files map
		Map<String, List<String>> receivedFilesRaw = response.getFromBody("received_files");
		if (receivedFilesRaw == null || receivedFilesRaw.isEmpty())
			return Collections.emptyMap();

		Map<String, List<String>> result = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : receivedFilesRaw.entrySet()) {
			String receiver = entry.getKey();
			for (String fileAndHash : entry.getValue()) {
				String[] parts = fileAndHash.split(" ", 2); // [filename, hash]
				if (parts.length != 2) continue;

				String filename = parts[0];
				String hash = parts[1];
				String combined = hash + " - " + receiver;

				result.computeIfAbsent(filename, k -> new ArrayList<>()).add(combined);
			}
		}

		for (List<String> list : result.values()) {
			list.sort(Comparator.comparing(s -> s.split(" - ")[1]));
		}

		return result;
	}
}
