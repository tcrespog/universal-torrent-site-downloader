package download

import geb.Browser
import geb.ConfigurationLoader
import geb.navigator.Navigator
import groovy.util.logging.Slf4j
import org.openqa.selenium.Keys

@Slf4j
class Scraper {

    Browser browser

    String username
    String password
    String loginPageAddress
    String usernameInputSelector
    String passwordInputSelector
    String loggedInIndicatorSelector
    Boolean isCaptcha

    String torrentListUrl
    String torrentLinksSelector
    List<String> coincidences
    Boolean isCaseSensitive

    Boolean isMagnetLink
    String torrentProvisionSelector
    String sayThanksSelector

    String browserDownloadDirectoryPath
    Integer maxTimeToDownload
    DownloadedTorrentFileDetector downloadedTorrentFileDetector

    Scraper(Map map) {
        map.each { k,v -> if (this.hasProperty(k)) { this."$k" = v} }

        String gebEnv = System.getProperty('geb.env') ?: 'chrome'
        ConfigurationLoader loader = new ConfigurationLoader(gebEnv)
        browser = new Browser(loader.conf)

        if (!isCaseSensitive) {
            coincidences = coincidences*.toLowerCase()
        }

        if (!isMagnetLink) {
            downloadedTorrentFileDetector = new DownloadedTorrentFileDetector(browserDownloadDirectoryPath, maxTimeToDownload)
        }

    }

    String obtainTorrentIfPublished() {
        goToTorrentList()

        if (isLoginRequired() && !isLoggedIn()) {
            loginOnWeb()
            goToTorrentList()
        }

        Navigator torrentDetailLink = searchTorrent()

        if (!torrentDetailLink) {
            return null
        }

        torrentDetailLink.click()

        obtainTorrentFile()
    }

    void goToTorrentList() {
        browser.go(torrentListUrl)
    }

    boolean isLoginRequired() {
        username && password && loginPageAddress && usernameInputSelector && passwordInputSelector
    }

    boolean isLoggedIn() {
        !browser.$(loggedInIndicatorSelector).isEmpty()
    }

    Navigator getElementMakingSureIsVisible(String selector, boolean justFirst = false) {
        Navigator element = justFirst ? browser.$(selector, 0) : browser.$(selector)
        browser.waitFor { !element.isEmpty() && element.every { it.displayed } }

        element
    }

    void loginOnWeb() {
        log.info("Logging in ${new URL(loginPageAddress).host}")

        browser.go(loginPageAddress)

        Navigator usernameInput = getElementMakingSureIsVisible(usernameInputSelector, true)
        Navigator passwordInput = getElementMakingSureIsVisible(passwordInputSelector, true)

        usernameInput.value(username)
        passwordInput.value(password)

        if (isCaptcha) {
            waitForCaptchaFill(10 * 1000)
        }
        passwordInput << Keys.ENTER
    }


    Navigator searchTorrent() {
        Navigator torrentList = getElementMakingSureIsVisible(torrentLinksSelector)

        Navigator torrentDetailLink = torrentList.find { Navigator torrentDetail ->
            String title = (isCaseSensitive) ? torrentDetail.text() : torrentDetail.text().toLowerCase()

            coincidences.every { String coincidence -> title.contains(coincidence) }
        }

        torrentDetailLink
    }

    String obtainTorrentFile() {
        Navigator torrentProvisionLink = getElementMakingSureIsVisible(torrentProvisionSelector, true)

        if (isMagnetLink) {
            torrentProvisionLink.attr('href')
        } else {
            torrentProvisionLink.click()
            downloadedTorrentFileDetector.waitForTorrentFileDownload()
        }
    }

    void executeThanksSaying() {
        goToTorrentList()

        if (isLoginRequired() && !isLoggedIn()) {
            loginOnWeb()
            goToTorrentList()
        }

        Navigator torrentDetailLink = searchTorrent()
        torrentDetailLink.click()

        Navigator thanksButton = getElementMakingSureIsVisible(sayThanksSelector, true)
        thanksButton.click()
    }

    void finishScraping() {
        browser.close()
    }

    void waitForCaptchaFill(Integer millisToFill) {
        Date start = new Date()

        Date end = new Date(start.time + millisToFill)
        Date current = start

        Long remainingTime = end.time - current.time
        while (remainingTime > 0) {
            String message = "Fill the captcha in ${remainingTime.intdiv(1000)} seconds!"
            log.info(message)
            createCaptchaAlert(message)

            remainingTime = end.time - new Date().time
            sleep(1000)
        }
    }

    void createCaptchaAlert(String message) {
        browser.js.exec("""
            var captchaAlertDiv = document.getElementById('captchaAlert');
            if (!captchaAlertDiv) {
                captchaAlertDiv = document.createElement('div');
                captchaAlertDiv.setAttribute('id', 'captchaAlert');
                captchaAlertDiv.style.cssText = 'position: fixed; bottom: 0; right: 0; background-color: #a94442; font-size: large; font-weight: bold; padding: 5px;';
                document.body.appendChild(captchaAlertDiv);
            }
            captchaAlertDiv.innerHTML = '${message}';
        """)
    }



}
