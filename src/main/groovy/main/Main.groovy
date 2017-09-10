package main

import com.beust.jcommander.JCommander
import groovy.util.logging.Slf4j
import com.beust.jcommander.Parameter

@Slf4j
class Main {

    @Parameter(names = ['--torrentListUrl'], required = true, description = 'URL address to the torrent link list page of the web', order = 0)
    String torrentListUrl
    @Parameter(names = ['--torrentLinksSelector'], required = true, description = 'CSS selector for all the torrent links in the list page', order = 1)
    String torrentLinksSelector
    @Parameter(names = ['--coincidence', '-c'], variableArity = true, required = true, description = 'The text coincidences to search for in the torrent title', order = 2)
    List<String> coincidences
    @Parameter(names = ['--caseSensitive', '-s'], description = 'If present, compare the coincidences in a case-sensitive way', order = 3)
    Boolean isCaseSensitive = false

    @Parameter(names = ['--loginPageUrl'], description = 'URL address to the login page of the web', order = 4)
    String loginPageUrl
    @Parameter(names = ['--usernameInputSelector'], description = 'CSS selector for the username input in the login page', order = 5)
    String usernameInputSelector
    @Parameter(names = ['--passwordInputSelector'], description = 'CSS selector for the password input in the login page', order = 6)
    String passwordInputSelector
    @Parameter(names = ['--loggedInIndicatorSelector'], description = 'CSS selector for an element on the page which indicates that the user is logged in', order = 7)
    String loggedInIndicatorSelector
    @Parameter(names = ['--torrentSiteUsername', '-u'], description = 'Username to log in the torrent site', order = 8)
    String torrentSiteUsername
    @Parameter(names = ['--torrentSitePassword', '-p'], description = 'Password to log in the torrent site', order = 9)
    String torrentSitePassword
    @Parameter(names = ['--captcha'], description = 'If present, the program will wait a prudential time to let the user fill the captcha in the login form', order = 10)
    Boolean isCaptcha = false

    @Parameter(names = ['--torrentProvisionSelector'], required = true, description = 'CSS selector to the link in charge of providing a torrent file (download or magnet)', order = 11)
    String torrentProvisionSelector
    @Parameter(names = ['--sayThanksSelector'], description = 'CSS selector for thanks-saying link', order = 12)
    String sayThanksSelector

    @Parameter(names = ['--magnetLink', '-m'], description = 'If present, the provision link is considered a magnet link', order = 13)
    Boolean isMagnetLink = false
    @Parameter(names = ['--downloadDir', '-d'], description = 'The directory path where the torrent file will be downloaded', order = 14)
    String downloadDirPath
    @Parameter(names = ['--waitingTimeDownload', '-w'], description = 'The max amount of milliseconds to wait for a torrent file to download (10000 [10 seconds] by default)', order = 15)
    Integer maxTimeToDownload = 10000

    @Parameter(names = ['--waitUntilTorrentCompleted'], description = 'If present, the program will wait until the torrent has been completed monitoring its completion status', order = 16)
    Boolean isWaitUntilTorrentCompletedEnabled = false
    @Parameter(names = ['--retryTime', '-r'], description = 'The retry time in milliseconds to watch for changes in the web and changes in the torrent progress (300000 [5 minutes] by default)', order = 17)
    Integer retryTime = 300000

    @Parameter(names = ['--torrentClient', '-t'], required = true, description = 'Name of the torrent client to add the torrent to, one of: transmission, qbittorrent', order = 18)
    String torrentClient
    @Parameter(names = ['--torrentServerHost'], description = 'Host where the torrent software is located (defaults to localhost)', order = 19)
    String torrentServerHost
    @Parameter(names = ['--torrentServerPort'], description = 'Port where the torrent software is listening (defaults: Transmission - 9091, qBittorrent - 8080)', order = 20)
    Integer torrentServerPort
    @Parameter(names = ['--torrentServerUsername'], description = 'User of the torrent software (defaults: Transmission - <empty>, qBittorrent - admin)', order = 21)
    String torrentServerUsername
    @Parameter(names = ['--torrentServerPassword'], description = 'Password of the torrent softwares\'s user (defaults: Transmission - <empty>, qBittorrent - adminadmin)', order = 22)
    String torrentServerPassword

    @Parameter(names = ['--help', '-h'], description = 'Show usage information', help = true, order = 23)
    boolean help

    static void main(String... args) {
        Main main = new Main()
        JCommander jCommander = JCommander.newBuilder().addObject(main).build()
        jCommander.parse(args)

        if (main.help) {
            jCommander.usage()
            System.exit(1)
        }

        Runner runner = new Runner(loginPageAddress: main.loginPageUrl,
                                   usernameInputSelector: main.usernameInputSelector, passwordInputSelector: main.passwordInputSelector,
                                   loggedInIndicatorSelector: main.loggedInIndicatorSelector,
                                   torrentSiteUsername: main.torrentSiteUsername, torrentSitePassword: main.torrentSitePassword, isCaptcha: main.isCaptcha,

                                   torrentListUrl: main.torrentListUrl, torrentLinksSelector: main.torrentLinksSelector,
                                   coincidences: main.coincidences, caseSensitive: main.isCaseSensitive,

                                   torrentProvisionSelector: main.torrentProvisionSelector, sayThanksSelector: main.sayThanksSelector,

                                   retryTime: main.retryTime,
                                   isMagnetLink: main.isMagnetLink, downloadDirPath: main.downloadDirPath, maxTimeToDownload: main.maxTimeToDownload,
                                   isWaitUntilTorrentCompletedEnabled: main.isWaitUntilTorrentCompletedEnabled,

                                   torrentClient: main.torrentClient, torrentServerHost: main.torrentServerHost, torrentServerPort: main.torrentServerPort,
                                   torrentServerUsername: main.torrentServerUsername, torrentServerPassword: main.torrentServerPassword)

        Boolean result = runner.run()

        result ? System.exit(0) : System.exit(1)
    }

}
