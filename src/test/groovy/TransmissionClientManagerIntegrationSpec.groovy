import org.testcontainers.containers.BindMode
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import torrent.TransmissionClientManager
import utils.Util

@Testcontainers
@Stepwise
class TransmissionClientManagerIntegrationSpec extends Specification {

    static final String VERSION_TAG = '140' //Transmission 2.94

    @Shared
    GenericContainer transmissionContainer = new GenericContainer("linuxserver/transmission:${VERSION_TAG}")
            .withExposedPorts(9091)
            .withClasspathResourceMapping(Util.DOWNLOAD_DIRECTORY.name, '/downloads', BindMode.READ_WRITE)
            .withEnv([PUID: Util.getSystemUserUid().toString(), PGID: Util.getSystemUserGid().toString()])
            .waitingFor(Wait.forHttp(''))


    @Shared
    TransmissionClientManager torrentManager

    void setupSpec() {
        torrentManager = new TransmissionClientManager('localhost', transmissionContainer.getMappedPort(9091), '', '')
    }


    void "check that the torrent client is supported in the system"() {
        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        info.isSupported
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

    void "check that the torrent client is not supported in the system given a wrong configuration"() {
        given: 'a manager with a worng configuration'
        TransmissionClientManager torrentManager = new TransmissionClientManager('localhost', transmissionContainer.getMappedPort(9091) - 1, '', '')

        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        !info.isSupported
        info.error.startsWith('The Transmission server is not supported. Make sure the connection data is correct. Trace info:')
    }

}
