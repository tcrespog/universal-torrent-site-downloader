package torrent

class NonImplementedClientManager extends AbstractTorrentClientManager {

    NonImplementedClientManager(String host, Integer port, String username, String password) {
        super(host, port, username, password)
    }

    Map isTorrentClientSupported() {
        [isSupported: false, error: 'Client manager not implemented']
    }

    boolean addTorrentToClient(String torrentUri) {
        throw new UnsupportedOperationException()
    }

    int getTorrentCompletionPercentage() {
        throw new UnsupportedOperationException()
    }

    boolean removeTorrentFromClient() {
        throw new UnsupportedOperationException()
    }

}
