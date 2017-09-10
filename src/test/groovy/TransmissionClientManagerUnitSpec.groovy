import spock.lang.Specification
import spock.lang.Stepwise
import torrent.TransmissionClientManager
import utils.Util

@Stepwise
class TransmissionClientManagerUnitSpec extends Specification {

    TransmissionClientManager torrentManager = new TransmissionClientManager('localhost', 9091, '', '')


    void "check that the torrent client is supported in the system"() {
        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        info.isSupported
    }

    void "check that the torrent client is not supported in the system given a wrong configuration"() {
        given: 'a manager with a worng configuration'
        torrentManager = new TransmissionClientManager('localhost', 9092, '', '')

        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        !info.isSupported
        info.error.startsWith('The Transmission server is not supported. Make sure the connection data is correct. Trace info:')
    }

    void "add a torrent to the client via a magnet"() {
        when: 'add a torrent to the client'
        boolean result = torrentManager.addTorrentToClient(Util.MAGNET_LINK)

        then: 'the torrent was successfully added'
        result

        and: 'the torrent id has been properly set'
        torrentManager.downloadingTorrentId

        cleanup: 'remove the torrent from the client'
        torrentManager.removeTorrentFromClient()
    }

    void "get torrent completion percentage of a torrent added via file"() {
        given: 'add a torrent to the client'
        torrentManager.addTorrentToClient(Util.TORRENT_FILE.absolutePath)

        when: 'get the completion percentage'
        int completionPercentage = torrentManager.getTorrentCompletionPercentage()

        then: 'a number is received'
        completionPercentage >= 0

        cleanup: 'remove the torrent from the client'
        torrentManager.removeTorrentFromClient()
    }

    void "get torrent completion percentage of a torrent added via magnet"() {
        given: 'add a torrent to the client'
        torrentManager.addTorrentToClient(Util.MAGNET_LINK)

        when: 'get the completion percentage'
        int completionPercentage = torrentManager.getTorrentCompletionPercentage()

        then: 'a number is received'
        (completionPercentage >= 0) && (completionPercentage <= 100)

        cleanup: 'remove the torrent from the client'
        torrentManager.removeTorrentFromClient()
    }

}
