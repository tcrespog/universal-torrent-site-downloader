import spock.lang.Specification
import spock.lang.Unroll
import torrent.AbstractTorrentClientManager
import torrent.NonImplementedClientManager
import torrent.QBittorrentClientManager
import torrent.TorrentClientManagerFactory
import torrent.TransmissionClientManager

class TorrentClientManagerFactoryUnitSpec extends Specification {

    @Unroll
    void "create an specific torrent client manager depending on the received params"() {
        when: 'create a new torrent client manager instance'
        AbstractTorrentClientManager torrentClientManager = TorrentClientManagerFactory.create(clientName, serverHost, serverPort, serverUsername, serverPassword)

        then: 'the torrent client manager was constructed as expected'
        if (clientName == 'transmission') assert torrentClientManager instanceof TransmissionClientManager
        else if (clientName == 'qbittorrent')     assert torrentClientManager instanceof QBittorrentClientManager
        else                                      assert torrentClientManager instanceof NonImplementedClientManager
        torrentClientManager.host == expectedServerHost
        torrentClientManager.port == expectedServerPort
        torrentClientManager.username == expectedServerUsername
        torrentClientManager.password == expectedServerPassword

        where: 'the data to create the torrent client manager is'
        clientName     | serverHost     | serverPort | serverUsername | serverPassword | expectedServerHost | expectedServerPort | expectedServerUsername | expectedServerPassword
        'transmission' | null           | null       | null           | null           | 'localhost'        | 9091               | ''                     | ''
        'transmission' | '192.168.1.57' | 9090       | 'username'     | 'password'     | '192.168.1.57'     | 9090               | 'username'             | 'password'
        'qbittorrent'  | null           | null       | null           | null           | 'localhost'        | 8080               | 'admin'                | 'adminadmin'
        'qbittorrent'  | '192.168.1.57' | 8081       | 'username'     | 'password'     | '192.168.1.57'     | 8081               | 'username'             | 'password'
        'unsupported'  | '192.168.1.57' | 8081       | 'username'     | 'password'     | '192.168.1.57'     | 8081               | 'username'             | 'password'
    }

}
