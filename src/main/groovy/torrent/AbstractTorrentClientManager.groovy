package torrent

abstract class AbstractTorrentClientManager {

    String host
    Integer port
    String username
    String password

    String downloadingTorrentId


    AbstractTorrentClientManager(String host, Integer port, String username, String password) {
        this.host = host
        this.port = port
        this.username = username
        this.password = password
    }

    abstract Map isTorrentClientSupported()

    abstract boolean addTorrentToClient(String torrentUri)

    abstract int getTorrentCompletionPercentage()

    abstract boolean removeTorrentFromClient()

}