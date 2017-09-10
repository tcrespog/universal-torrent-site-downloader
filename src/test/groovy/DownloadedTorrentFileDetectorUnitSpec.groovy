import download.DownloadedTorrentFileDetector
import spock.lang.Specification
import utils.Util

import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DownloadedTorrentFileDetectorUnitSpec extends Specification {

    void "wait for a file to be downloaded"() {
        given: 'downloads detector associated with a specific download directory and with a specific max waiting time to download'
        DownloadedTorrentFileDetector downloadedTorrentFileDetector = new DownloadedTorrentFileDetector(Util.DOWNLOAD_DIRECTORY.absolutePath, 1000)

        and: 'copy a file to the download directory simulating a download in another thread after a prudential time'
        File destinationCopyFile = new File(Util.DOWNLOAD_DIRECTORY, Util.TORRENT_FILE.name)
        Executors.newSingleThreadScheduledExecutor().schedule({
            Files.copy(Util.TORRENT_FILE.toPath(), destinationCopyFile.toPath())
        }, downloadedTorrentFileDetector.maxWaitingTimeToDownload - 500, TimeUnit.MILLISECONDS)

        when: 'obtain the path of the recently download file'
        String obtainedPath = downloadedTorrentFileDetector.waitForTorrentFileDownload()

        then: 'the obtained path is as expected'
        obtainedPath == destinationCopyFile.absolutePath

        cleanup: 'delete the copied file'
        destinationCopyFile.delete()
    }

    void "wait for a file to be downloaded, but max waiting time is exceeded"() {
        given: 'downloads detector associated with a specific download directory and with a specific max waiting time to download'
        DownloadedTorrentFileDetector downloadedTorrentFileDetector = new DownloadedTorrentFileDetector(Util.DOWNLOAD_DIRECTORY.absolutePath, 1000)

        when: 'wait for the download of a file which will never occur'
        downloadedTorrentFileDetector.waitForTorrentFileDownload()

        then: 'no path was obtained'
        thrown(RuntimeException)
    }

    void "search for the most recently downloaded torrent file in the file system"() {
        given: 'downloads detector associated with a specific download directory and with a specific max waiting time to download'
        DownloadedTorrentFileDetector downloadedTorrentFileDetector = new DownloadedTorrentFileDetector(Util.DOWNLOAD_DIRECTORY.absolutePath, 1000)

        and: 'copy a file to the download directory'
        File destinationCopyFile = new File(Util.DOWNLOAD_DIRECTORY, Util.TORRENT_FILE.name)
        Files.copy(Util.TORRENT_FILE.toPath(), destinationCopyFile.toPath())

        when: 'obtain the path of the recently downloaded file'
        String obtainedPath = downloadedTorrentFileDetector.searchForRecentlyDownloadedFile()

        then: 'the obtained path is as expected'
        obtainedPath == destinationCopyFile.absolutePath

        cleanup: 'delete the copied file'
        destinationCopyFile.delete()
    }

    void "search for the most recently downloaded but there is nothing"() {
        given: 'downloads detector associated with a specific download directory and with a specific max waiting time to download'
        DownloadedTorrentFileDetector downloadedTorrentFileDetector = new DownloadedTorrentFileDetector(Util.DOWNLOAD_DIRECTORY.absolutePath, 1000)

        when: 'try to obtain the path of the recently downloaded file'
        String obtainedPath = downloadedTorrentFileDetector.searchForRecentlyDownloadedFile()

        then: 'there is no obtained path'
        !obtainedPath
    }
    
}
