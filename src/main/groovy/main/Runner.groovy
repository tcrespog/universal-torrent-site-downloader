package main

import download.Scraper
import groovy.util.logging.Slf4j
import torrent.AbstractTorrentClientManager
import torrent.TorrentClientManagerFactory

import java.text.DecimalFormat

@Slf4j
class Runner {


    Integer retryTime
    Boolean isWaitUntilTorrentCompletedEnabled

    String sayThanksSelector
    Scraper scraper

    AbstractTorrentClientManager torrentClientManager

    Runner(Map map) {
        map.each { k,v -> if (this.hasProperty(k)) { this."$k" = v} }

        scraper = new Scraper(loginPageAddress: map.loginPageAddress, usernameInputSelector: map.usernameInputSelector, passwordInputSelector: map.passwordInputSelector, loggedInIndicatorSelector: map.loggedInIndicatorSelector, username: map.torrentSiteUsername, password: map.torrentSitePassword, isCaptcha: map.isCaptcha,
                torrentListUrl: map.torrentListUrl, torrentLinksSelector: map.torrentLinksSelector, coincidences: map.coincidences, isCaseSensitive: map.isCaseSensitive,
                isMagnetLink: map.isMagnetLink, browserDownloadDirectoryPath: map.downloadDirPath, maxTimeToDownload: map.maxTimeToDownload, torrentProvisionSelector: map.torrentProvisionSelector,
                sayThanksSelector: map.sayThanksSelector)
        torrentClientManager = TorrentClientManagerFactory.create(map.torrentClient, map.torrentServerHost, map.torrentServerPort, map.torrentServerUsername, map.torrentServerPasword)
    }

    boolean run() {
        Map supportInfo = torrentClientManager.isTorrentClientSupported()
        if (!supportInfo.isSupported) {
            log.error(supportInfo.error)
            return false
        }

        try {
            String torrentAddress = waitForTorrentFilePublication()
            addDownloadedTorrentToClient(torrentAddress)
            sayThanksIfEnabled()
        } catch (Throwable t) {
            log.error("An error occurred: ${extractStackTraceString(t)}")

            return false
        } finally {
            scraper.finishScraping()
        }

        waitForTorrentCompletionIfEnabled()

        true
    }

    String waitForTorrentFilePublication() {
        String torrentAddress = null
        while (!torrentAddress) {
            torrentAddress = scraper.obtainTorrentIfPublished()

            if (torrentAddress) {
                log.info('Downloading torrent file')
            } else {
                log.info("Torrent not available yet, will retry after ${retryTime} milliseconds (${new DecimalFormat("##.##").format(retryTime / 60000.0)} minutes)")
                sleep(retryTime)
            }
        }

        torrentAddress
    }

    void addDownloadedTorrentToClient(String torrentAdress) {
        boolean additionResult = torrentClientManager.addTorrentToClient(torrentAdress)
        if (!additionResult) {
            throw new RuntimeException('File not added to the torrent client')
        }
    }

    void sayThanksIfEnabled() {
        if (sayThanksSelector) {
            scraper.executeThanksSaying()
            log.info('You said thanks!')
        }
    }

    void waitForTorrentCompletionIfEnabled() {
        if (isWaitUntilTorrentCompletedEnabled) {
            waitUntilTorrentCompleted()
        }
    }

    void waitUntilTorrentCompleted() {
        boolean isTorrentContentDownloaded = false
        while (!isTorrentContentDownloaded) {
            int torrentCompletionPercentage = torrentClientManager.getTorrentCompletionPercentage()
            isTorrentContentDownloaded = (torrentCompletionPercentage == 100)

            if (!isTorrentContentDownloaded) {
                log.info("Torrent not completed yet (${torrentCompletionPercentage}%), will recheck after ${retryTime} milliseconds (${new DecimalFormat("##.##").format(retryTime / 60000.0)} minutes)")
                sleep(retryTime)
            }
        }
        log.info("Torrent successfully completed")
    }

    private static String extractStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter()
        throwable.printStackTrace(new PrintWriter(sw))

        sw.toString()
    }

}
