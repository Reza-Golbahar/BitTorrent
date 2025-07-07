package common.utils;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5Hash {
	public static String HashFile(String filePath) {
		// TODO: Calculate MD5 hash of file
		// 1. Open file input stream
		try (FileInputStream fis = new FileInputStream(filePath)) {
			// 2. Create message digest
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[1024];
			int bytesRead;

			// 3. Read file in chunks and update digest
			while ((bytesRead = fis.read(buffer)) != -1) {
				md.update(buffer, 0, bytesRead);
			}

			// 4. Convert digest to hex string
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			// 5. Handle errors
			e.printStackTrace();
			return "ERROR";
		}
	}
}
