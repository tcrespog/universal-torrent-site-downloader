# Universal torrent site downloader

Monitor a torrent site automatically waiting for a desired torrent to be published and download its contents with a torrent client.

## Synopsis

This command-line Java utility allows you to scrap torrent websites (which typically follow the same structure) until the torrent you're looking for is published. After that adds the torrent to a supported torrent client.

Get the tool ready to be run in the releases tab.

## Prerequisites

In order to run the tool the following software is required:
- [JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
- [Chrome Browser](https://www.google.com/chrome/index.html).
- A supported torrent client, currently:
    - [Transmission](https://transmissionbt.com/): through transmission-remote and [RPC](https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt).
    - [qBittorrent](https://www.qbittorrent.org/): through [Web UI](https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-Documentation).

## How does it work?

The tool automatizes a common process performed when looking for torrent results on a website:

1. Using the browser, go to the torrents site list.
2. If the torrent site is private, log in the page.
3. Wait until the torrent you are looking for based on text coincidences in its title appears in the page.
4. Once it appears, click on the link to go to the torrent detail page.
5. Download the torrent file or get the magnet link.
6. Add the torrent to the client.
7. If specified, wait until the torrent completes.

The user supplies input for this process to work properly for a specific torrent website through the CLI options:

- The results list URL.
- The CSS selector for the set of result links in the page.
- The directory where the browser downloads files.
- The torrent client to use.
- ...

Note that the proper functioning relies on this generic structure that torrent websites usually follow (results links list, login page if it's a private site, torrent detail page with download or magnet links). A change in this structure would make the tool unable to scrap the page properly.

## Usage

### Example

This is an example command line constructed for a real torrent site:
```shell
java -jar universal-torrent-site-scraper-downloader.jar --torrentSiteUsername 'username' --torrentSitePassword 'password'                                                        \
     --loginPageUrl 'https://archive.org/account/login.php' --usernameInputSelector '#username' --passwordInputSelector '#password' --loggedInIndicatorSelector '.mypic.ghost80' \
     --torrentListUrl 'https://archive.org/search.php?query=a%20corny%20concerto' --torrentLinksSelector '.C234 a'                                                               \
     --torrentProvisionSelector 'a[href$=".torrent"]'                                                                                                                            \
     --downloadDir '/home/user/Downloads/'                                                                                                                                       \
     -c 'Corny' -c 'Concerto'                                                                                                                                                    \
     --torrentClient 'transmission' --torrentServerHost 'localhost' --torrentServerPort '9091'                                                                                   \
     --waitUntilTorrentCompleted 
```

### Options

In order to see all the available CLI options run:

```shell
java -jar universal-torrent-site-scraper-downloader.jar --help
```

```
  Options:
  * --torrentListUrl
      URL address to the torrent link list page of the web
  * --torrentLinksSelector
      CSS selector for all the torrent links in the list page
  * --coincidence, -c
      The text coincidences to search for in the torrent title
    --caseSensitive, -s
      If present, compare the coincidences in a case-sensitive way. Default: false
    --loginPageAddress
      URL address to the login page of the web
    --usernameInputSelector
      CSS selector for the username input in the login page
    --passwordInputSelector
      CSS selector for the password input in the login page
    --loggedInIndicatorSelector
      CSS selector for an element on the page which indicates that the user is logged in
    --torrentSiteUsername, -u
      Username to log in the torrent site
    --torrentSitePassword, -p
      Password to log in the torrent site
    --captcha
      If present, the program will wait a prudential time to let the user fill the captcha in the login form. Default: false
  * --torrentProvisionSelector
      CSS selector to the link in charge of providing a torrent file (download or magnet)
    --sayThanksSelector
      CSS selector for thanks-saying link
    --magnetLink, -m
      If present, the provision link is considered a magnet link. Default: false
    --downloadDir, -d
      The directory path where the torrent file will be downloaded
    --waitingTimeDownload, -w
      The max amount of milliseconds to wait for a torrent file to download (10000 [10 seconds] by default). Default: 10000
    --waitUntilTorrentCompleted
      If present, the program will wait until the torrent has been completed monitoring its completion status. Default: false
    --retryTime, -r
      The retry time in milliseconds to watch for changes in the web and changes in the torrent progress (300000 [5 minutes] by default)
  * --torrentClient, -t
      Name of the torrent client to add the torrent to, one of: transmission, qbittorrent 
    --torrentServerHost
      Host where the torrent software is located (defaults to localhost)
    --torrentServerPort
      Port where the torrent software is listening (defaults: Transmission - 9091, qBittorrent - 8080)
    --torrentServerUsername
      User of the torrent software (defaults: Transmission - <empty>, qBittorrent - admin)
    --torrentServerPassword
      Password of the torrent softwares's user (defaults: Transmission - <empty>, qBittorrent - adminadmin)
    --help, -h
      Show usage information
```

### Additional properties

Aside from the options, the tool accepts some JVM system properties to control browser related aspects:

- `-DdisableJavaScript=true` disables JavaScript in the browser, useful for websites which show popups that may break the process.
- [Web driver manager properties](https://github.com/bonigarcia/webdrivermanager#configuration):
    - `-Dwdm.chromeDriverVersion=2.25` specifies a specific the [Chrome Driver](https://sites.google.com/a/chromium.org/chromedriver/downloads) version to download.
    - `-Dwdm.targetPath='/my/custom/path/to/driver/binaries'` specifies the path where the driver will be downloaded
    - ...
    
Note that system properties must be defined before the `-jar` java option in the command line: `java -DdisableJavaScript=true -jar universal-torrent-site-scraper-downloader.jar`

### Tips

- In order to construct a command line for a torrent website, inspect the structure previously using a browser's developer tools looking for selectors which identify uniquely each item of interest in the page (result links, credentials inputs, download link, ...).
- Save the command line you construct in a text file or a shell script file to reuse it anytime. 


## Contributing

The tool is written using [Groovy](http://groovy-lang.org/) and uses [Gradle](https://gradle.org/) as its build tool.

### Testing

A suite of unit tests is provided. In order to run the tests, which rely on the existence of an available torrent client of each kind, Docker is a good option to provide the required environments ready for testing:

- Docker containers
    - [linuxserver/transmission](https://hub.docker.com/r/linuxserver/transmission/)
    - [linuxserver/qbittorrent](https://hub.docker.com/r/linuxserver/qbittorrent/)

Run all tests with the task:

`./gradlew :cleanTest :test --tests *`

### Building

Run this task to generate a JAR file with all required dependencies included:

`./gradlew fatJar`
