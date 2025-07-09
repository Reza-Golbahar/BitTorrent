package peer.app;

import common.utils.MD5Hash;

import java.io.*;
import java.net.Socket;

public class TorrentP2PThread extends Thread {
    private final Socket socket;
    private final File file;
    private final String receiver;
    private final BufferedOutputStream dataOutputStream;

    public TorrentP2PThread(Socket socket, File file, String receiver) throws IOException {
        this.socket = socket;
        this.file = file;
        this.receiver = receiver;
        this.dataOutputStream = new BufferedOutputStream(socket.getOutputStream());
        PeerApp.addTorrentP2PThread(this);
    }

    @Override
    public void run() {
        // 1. Open file input stream
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                //OutputStream out = socket.getOutputStream()
        ) {

            // 2. Read file in chunks and send to peer
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            // 3. Flush and close output stream
            dataOutputStream.flush();
            // 4. Update sent files list with file name and MD5 hash
            PeerApp.addSentFile(receiver, file.getName() + " " + MD5Hash.HashFile(file.getPath()));

        } catch (Exception e) {
            e.printStackTrace();
            this.end();
        }

        try {
            socket.close();
        } catch (Exception e) {
        }

        PeerApp.removeTorrentP2PThread(this);
    }

    public void end() {
        try {
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {
        }
    }
}
