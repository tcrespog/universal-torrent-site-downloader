package utils

import java.util.regex.Matcher

class Util {


    final static File TORRENT_FILE = new File('src/test/resources/Night.Of.The.Living.Dead.1968.720p.BRRip.x264-x0r.torrent')
    final static File DOWNLOAD_DIRECTORY = new File('src/test/resources/fakeDownloadDirectory')
    final static File OFFLINE_WEB_DIRECTORY = new File('src/test/resources/torrentSite')
    final static File BROWSER_DOWNLOAD_DIRECTORY = new File("${System.getProperty('user.home')}/Downloads")
    final static String MAGNET_LINK = 'magnet:?xt=urn:btih:F17FB68CE756227FCE325D0513157915F5634985&dn=Night+Of+The+Living+Dead+1968+720p+BRRip+x264-x0r&tr=http%3A%2F%2F94.228.192.98%2Fannounce&tr=http%3A%2F%2Ftracker.torrentfrancais.com%2Fannounce&tr=udp%3A%2F%2Fopen.demonii.com%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.ex.ua%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2F10.rarbg.com%2Fannounce&tr=udp%3A%2F%2F11.rarbg.me%2Fannounce&tr=udp%3A%2F%2Fpow7.com%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.prq.to%2Fannounce&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Finferno.demonoid.ph%3A3389%2Fannounce&tr=udp%3A%2F%2Ftracker.token.ro%3A80%2Fannounce&tr=udp%3A%2F%2Fipv4.tracker.harry.lu%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce'


    static File getDownloadedTorrentFile() {
        new File(BROWSER_DOWNLOAD_DIRECTORY, TORRENT_FILE.name)
    }

}
