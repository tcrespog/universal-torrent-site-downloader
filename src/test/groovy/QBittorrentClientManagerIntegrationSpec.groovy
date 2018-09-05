import com.sun.security.auth.module.UnixSystem
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import torrent.QBittorrentClientManager
import utils.Util

@Testcontainers
@Stepwise
class QBittorrentClientManagerIntegrationSpec extends Specification {

    static final String VERSION_TAG = '93' //qBittorrent 4.1.2

    //The host port and container port must be the same. See "WEBUI_PORT variable" section at https://hub.docker.com/r/linuxserver/qbittorrent
    @Shared
    FixedHostPortGenericContainer qBittorrentContainer = new FixedHostPortGenericContainer("linuxserver/qbittorrent:${VERSION_TAG}")
            .withFixedExposedPort(8080, 8080)
            .withClasspathResourceMapping(Util.DOWNLOAD_DIRECTORY.name, '/downloads', BindMode.READ_WRITE)
            .withEnv([PUID: Util.getSystemUserUid().toString(), PGID: Util.getSystemUserGid().toString()])
            .waitingFor(Wait.forHttp('/version/api'))

    @Shared
    QBittorrentClientManager torrentManager

    void setupSpec() {
        torrentManager = new QBittorrentClientManager('localhost', qBittorrentContainer.getMappedPort(8080), 'admin', 'adminadmin')
    }


    void "check that the torrent client is supported in the system"() {
        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        info.isSupported
    }

    void "add a torrent to the client via a file"() {
        when: 'add a torrent to the client'
        boolean result = torrentManager.addTorrentToClient(Util.TORRENT_FILE.absolutePath)

        then: 'the torrent was successfully added'
        result

        and: 'the torrent id has been properly set'
        torrentManager.downloadingTorrentId

        cleanup: 'remove the torrent from the client'
        torrentManager.removeTorrentFromClient()
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
        sleep(500) //Wait a prudential time, sometimes the endpoint doesn't respond correctly after adding the torrent immediately
        int completionPercentage = torrentManager.getTorrentCompletionPercentage()

        then: 'a number is received'
        (completionPercentage >= 0) && (completionPercentage <= 100)

        cleanup: 'remove the torrent from the client'
        torrentManager.removeTorrentFromClient()
    }

    void "check that the torrent client is not supported in the system given a wrong configuration"() {
        given: 'a manager with a wrong configuration'
        QBittorrentClientManager torrentManager = new QBittorrentClientManager('localhost', qBittorrentContainer.getMappedPort(8080) - 1, '', '')

        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        !info.isSupported
        info.error.startsWith('The qBittorrent client is not supported')
    }

    void "check that the torrent client is not supported in the system given wrong credentials"() {
        given: 'a manager with a wrong configuration'
        QBittorrentClientManager torrentManager = new QBittorrentClientManager('localhost', qBittorrentContainer.getMappedPort(8080), 'admin', 'badPassword')

        when: 'extract the support info'
        Map info = torrentManager.isTorrentClientSupported()

        then: 'the torrent client is supported'
        !info.isSupported
        info.error.startsWith('The qBittorrent client is not supported')
    }

}
