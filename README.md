# BitTorrent
A simplified **BitTorrent-style Peer-to-Peer (P2P)** file sharing system implemented in **Java**...


## 📦 Features

- Peer-to-peer file transfer using sockets
- Central tracker server for coordination
- Multiple simultaneous peer downloads
- File integrity checking via MD5
- Threaded server for handling multiple peers
- Command-line interface with dynamic configuration
- Custom protocol over TCP sockets (with JSON messages)


## 🛠️ Tech Stack

- Java 17+ or 21+ (for compiling and running)
- Bash (for build scripts)
- JSON-based messaging system
- Linux/macOS/WSL compatible



## 🧾 Project Structure

- .
- ├── peer/ # Peer-side code
- ├── tracker/ # Tracker-side code
- ├── common/ # Shared models and utilities
- ├── lib/ # Third-party libraries (e.g., gson)
- ├── build-tracker.sh # Script to compile and build tracker.jar
- ├── build_peer.sh # Script to compile and build peer.jar
- └── README.md


## 🚀 Getting Started

### Prerequisites

- Java JDK 21 or higher
- Bash shell (for running `build_peer.sh`)
- Internet access (for GitHub packages, if needed)

