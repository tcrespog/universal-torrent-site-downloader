package torrent

class TorrentClientManagerFactory {

    static AbstractTorrentClientManager create(String torrentClient, String torrentServerHost, Integer torrentServerPort, String torrentServerUsername, String torrentServerPassword) {
        torrentServerHost = torrentServerHost ?: 'localhost'

        if (torrentClient == 'transmission') {
            torrentServerPort = torrentServerPort ?: 9091
            torrentServerUsername = torrentServerUsername ?: ''
            torrentServerPassword = torrentServerPassword ?: ''

            new TransmissionClientManager(torrentServerHost, torrentServerPort, torrentServerUsername, torrentServerPassword)
        } else if (torrentClient == 'qbittorrent') {
            torrentServerPort = torrentServerPort ?: 8080
            torrentServerUsername = torrentServerUsername ?: 'admin'
            torrentServerPassword = torrentServerPassword ?: 'adminadmin'

            new QBittorrentClientManager(torrentServerHost, torrentServerPort, torrentServerUsername, torrentServerPassword)
        } else {
            new NonImplementedClientManager(torrentServerHost, torrentServerPort, torrentServerUsername, torrentServerPassword)
        }
    }

}
