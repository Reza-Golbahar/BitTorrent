package common.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {

	public static Map<String, String> listFilesInFolder(String folderPath) {
		// 1. Create folder object
		File folder = new File(folderPath);

		// 2. Get list of files
		ArrayList<File> files = new ArrayList<>();
		if (folder.exists() && folder.isDirectory()) {
			for (File file : folder.listFiles()) {
				if (file.isFile())
					files.add(file);
			}
		}

		// 3. Calculate MD5 hash for each file
		Map<String, String> filenamesAndHashes = new HashMap<>();
		for (File file : files) {
			filenamesAndHashes.put(file.getName(), MD5Hash.HashFile(file.getPath()));
		}

		// 4. Return map of filename to hash
		return filenamesAndHashes;
	}

	public static String getSortedFileList(Map<String, String> files) {
		// 1. Check if files map is empty
		if (files.isEmpty())
			return "";

		// 2. Sort file names
		ArrayList<String> sortedFilenames = new ArrayList<>(files.keySet());
		Collections.sort(sortedFilenames);

		// 3. Create formatted string with names and hashes
		StringBuilder result = new StringBuilder();
		for (String filename : sortedFilenames) {
			result.append("%s %s\n".formatted(
					filename,
					files.get(filename)
			));
		}
		return result.toString().trim();
	}
}
