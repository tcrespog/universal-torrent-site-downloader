import main.Runner
import spock.lang.Specification
import utils.Util

import java.util.concurrent.Executors
import java.util.concurrent.Future

class RunnerUnitSpec extends Specification {


    void "execute the complete set of steps in order to obtain a torrent and download it in the torrent client"() {
        given: 'a runner pointing to web page to download a torrent'
        Runner runner = new Runner(
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html",
                usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]',
                loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html", torrentLinksSelector: '.torrent-list a',
                torrentProvisionSelector: '#download', sayThanksSelector: '#thanks',
                torrentSiteUsername: 'username', torrentSitePassword: 'password',
                coincidences: ['torrent', 'you', 'are', 'looking'],

                downloadDirPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 2000,
                retryTime: 5,

                torrentClient: 'transmission'
        )

        when: 'execute the complete process'
        runner.run()

        then: 'a torrent file exists in the browser download directory'
        Util.downloadedTorrentFile.exists()

        and: 'the torrent was added to the torrent client'
        runner.torrentClientManager.downloadingTorrentId

        cleanup: 'delete the downloaded torrent file from the filesystem and from the torrent client'
        Util.downloadedTorrentFile.delete()
        runner.torrentClientManager.removeTorrentFromClient()
    }

    void "execute the complete set of steps in order to obtain a torrent magnet and download it in the torrent client"() {
        given: 'a runner pointing to web page with torrent magent'
        Runner runner = new Runner(
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html",
                usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]',
                loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html", torrentLinksSelector: '.torrent-list a',
                isMagnetLink: true, torrentProvisionSelector: '#magnet', sayThanksSelector: '#thanks',
                torrentSiteUsername: 'username', torrentSitePassword: 'password',
                coincidences: ['torrent', 'you', 'are', 'looking'],
                retryTime: 5,
                torrentClient: 'transmission'
        )

        when: 'execute the complete process'
        runner.run()

        then: 'the torrent was added to the torrent client'
        runner.torrentClientManager.downloadingTorrentId

        cleanup: 'delete the torrent from the torrent client'
        runner.torrentClientManager.removeTorrentFromClient()
    }

    void "execute the complete set of steps in order to obtain a torrent and download, but give a wrong selector for the scraper"() {
        given: 'a runner pointing to a web page with a wrong selector'
        Runner runner = new Runner(
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html",
                usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]',
                loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html", torrentLinksSelector: '.torrent-list a',
                torrentProvisionSelector: '#badSelector', sayThanksSelector: '#thanks',
                torrentSiteUsername: 'username', torrentSitePassword: 'password',
                coincidences: ['torrent', 'you', 'are', 'looking'],

                downloadDirPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath,
                retryTime: 5,
                maxTimeToDownload: 2000,
                isWaitUntilTorrentCompletedEnabled: false,

                torrentClient: 'transmission'
        )

        when: 'execute the complete process'
        boolean result = runner.run()

        then: 'the process could not finish successfully'
        !result
    }

    void "try to execute the runner with an unsupported torrent client"() {
        given: 'a runner pointing to web page to download a torrent'
        Runner runner = new Runner(
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html",
                usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]',
                loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html", torrentLinksSelector: '.torrent-list a',
                torrentProvisionSelector: '#download', sayThanksSelector: '#thanks',
                torrentSiteUsername: 'username', torrentSitePassword: 'password',
                coincidences: ['torrent', 'you', 'are', 'looking'],

                downloadDirPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath, maxTimeToDownload: 2000,
                retryTime: 5,

                torrentClient: 'unssuported'
        )

        expect: 'the process is canceled'
        !runner.run()
    }

    void "try to scrap the offline web with JavaScript disabled (which will cause an infinite loop in this case)"() {
        given: 'create a runner'
        Runner runner = new Runner(
                loginPageAddress: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/login.html",
                usernameInputSelector: 'input[name="username"]', passwordInputSelector: 'input[name="password"]',
                loggedInIndicatorSelector: '.logged-indicator',
                torrentListUrl: "file://${Util.OFFLINE_WEB_DIRECTORY.absolutePath}/list.html", torrentLinksSelector: '.torrent-list a',
                torrentProvisionSelector: '#download', sayThanksSelector: '#thanks',
                torrentSiteUsername: 'username', torrentSitePassword: 'password',
                coincidences: ['torrent', 'you', 'are', 'looking'],

                downloadDirPath: Util.BROWSER_DOWNLOAD_DIRECTORY.absolutePath,
                retryTime: 5,
                maxTimeToDownload: 2000,

                torrentClient: 'transmission'
        )

        and: 'set the system property which dictates that JavaScript is disabled'
        System.setProperty('disableJavaScript', 'true')

        and: 'wait for the torrent publication in other thread to check its running status'
        Future task = Executors.newSingleThreadExecutor().submit {
            runner.waitForTorrentFilePublication()
        }

        expect: 'after a while the process is still running'
        sleep(15000)
        !task.done

        cleanup: 'finish the task, close the browser and restore the system property'
        task.cancel(true)
        runner.scraper.finishScraping()
        System.clearProperty('disableJavaScript')
    }

    void "wait for a torrent added to the client to be completed (simulates the percentage getter)"() {
        given: 'a runner instance with the retry time between checks set'
        Runner runner = new Runner(downloadDirPath: Util.DOWNLOAD_DIRECTORY.absolutePath, retryTime: 5, torrentClient: 'transmission')

        and: 'mock the torrent manager completion method'
        int completionPercentage = 0
        runner.torrentClientManager.metaClass.getTorrentCompletionPercentage = { completionPercentage }

        and: 'increase completion percentage gradually in another thread'
        Executors.newSingleThreadExecutor().submit {
            while (completionPercentage < 100) {
                sleep(5)
                completionPercentage += 10
            }
        }

        when: 'wait for the torrent to complete'
        runner.waitUntilTorrentCompleted()

        then: 'the method finished'
        true
    }

}
