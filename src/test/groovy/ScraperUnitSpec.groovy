import download.Scraper
import spock.lang.Specification
import utils.Util

class ScraperUnitSpec extends Specification {

    Scraper scraper

    void cleanup() {
        scraper.finishScraping()
    }

    void "execute a torrent download on an offline web"() {
        given: 'a torrent scraper pointing to a torrent download'

        scraper = new Scraper(
                username: 'wathever', password: 'wathever',
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html", usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]', loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html?showResult=true", //Show the result at first load
                torrentLinksSelector: '.torrent-list a', torrentProvisionSelector: '#download',
                browserDownloadDirectoryPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 1000,
                coincidences: ['Torrent', 'You', 'Are', 'Looking']
        )
        when: 'check for the torrent and download it'
        String pathToTorrentFile = scraper.obtainTorrentIfPublished()

        then: 'the path to the downloaded torrent is returned'
        File downloadedTorrentFile = new File(Util.BROWSER_DOWNLOAD_DIRECTORY, Util.TORRENT_FILE.name)
        pathToTorrentFile == downloadedTorrentFile.absolutePath

        and: 'the torrent effectively appears at the browser download directory after a prudential waiting time'
        downloadedTorrentFile.exists()

        cleanup: 'delete the downloaded torrent file'
        downloadedTorrentFile.delete()
    }

    void "extract the magnet torrent link on an offline web"() {
        given: 'a torrent scraper pointing to a magnet link'
        scraper = new Scraper(
                username: 'wathever', password: 'wathever',
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html", usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]', loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html?showResult=true", //Show the result at first load
                torrentLinksSelector: '.torrent-list a', isMagnetLink: true, torrentProvisionSelector: '#magnet',
                coincidences: ['torrent', 'you', 'are', 'looking']
        )

        when: 'check for the torrent and get the magnet'
        String magnetLink = scraper.obtainTorrentIfPublished()

        then: 'the magent link is returned'
        magnetLink.startsWith('magnet')
    }

    void "try to execute a torrent download on an offline web, but case-sensitive comparison is active"() {
        given: 'a torrent scraper pointing to a torrent download'

        scraper = new Scraper(
                username: 'wathever', password: 'wathever',
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html", usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]', loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html?showResult=true", //Show the result at first load
                torrentLinksSelector: '.torrent-list a', torrentProvisionSelector: '#download',
                browserDownloadDirectoryPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 1000,
                coincidences: ['Torrent', 'You', 'Are', 'Looking'], isCaseSensitive: true
        )

        when: 'check for the torrent and download it'
        String pathToTorrentFile = scraper.obtainTorrentIfPublished()

        then: 'the path marks that the torrent has not been downloaded'
        !pathToTorrentFile
    }

    void "try to execute a torrent download on an offline web, but the torrent is not present yet"() {
        given: 'a scraper poiting to a web where the torrent is not present yet'
        scraper = new Scraper(
                username: 'wathever', password: 'wathever',
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html", usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]', loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html",
                torrentLinksSelector: '.torrent-list a', torrentProvisionSelector: '#download',
                browserDownloadDirectoryPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 1000,
                coincidences: ['torrent', 'you', 'are', 'looking']
        )

        when: 'check for the torrent and download it'
        String pathToTorrentFile = scraper.obtainTorrentIfPublished()

        then: 'the path marks that the torrent has not been downloaded'
        !pathToTorrentFile
    }

    void "say thanks to a contribution on the offline web"() {
        given: 'a torrent scraper pointing to a torrent download'
        scraper = new Scraper(
                username: 'wathever', password: 'wathever',
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html", usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]', loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html?showResult=true", //Show the result at first load
                torrentLinksSelector: '.torrent-list a', torrentProvisionSelector: '#download',
                sayThanksSelector: '#thanks',
                browserDownloadDirectoryPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 1000,
                coincidences: ['torrent', 'you', 'are', 'looking']
        )

        when: 'say thanks on the contribution page'
        scraper.executeThanksSaying()

        then: 'the thanks saying link no longer appears'
        scraper.browser.$(scraper.sayThanksSelector).isEmpty()
    }

}
