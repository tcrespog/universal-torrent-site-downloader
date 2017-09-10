package torrent

//TODO Add support for multiple torrent clients, similar to: https://github.com/SickRage/SickRage/tree/72f937b3667166801ff595ea674a7877dd8b67cd/sickbeard/clients
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