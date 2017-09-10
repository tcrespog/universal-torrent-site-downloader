package download

import groovy.util.logging.Slf4j

@Slf4j
class DownloadedTorrentFileDetector {

    String downloadDirectoryPath
    List<File> fileListingSnapshot
    Integer maxWaitingTimeToDownload

    DownloadedTorrentFileDetector(String downloadDirectoryPath, Integer maxWaitingTimeToDownload) {
        this.downloadDirectoryPath = downloadDirectoryPath
        this.fileListingSnapshot = new File(downloadDirectoryPath).listFiles().toList()

        this.maxWaitingTimeToDownload = maxWaitingTimeToDownload
    }

    String waitForTorrentFileDownload() {
        String downloadedFilePath = null

        log.info('Searching for the recently downloaded file in the file system')
        long searchingStartTime = System.currentTimeMillis()
        while (!downloadedFilePath) {
            downloadedFilePath = searchForRecentlyDownloadedFile()

            if (!downloadedFilePath && (System.currentTimeMillis() - searchingStartTime > maxWaitingTimeToDownload)) {
                throw new RuntimeException('File not found. Searching max time exceeded')
            }
        }

        downloadedFilePath
    }

    String searchForRecentlyDownloadedFile() {
        File downloadDirectory = new File(downloadDirectoryPath)

        List<File> currentFiles = downloadDirectory.listFiles().toList()
        List<File> newFiles = (currentFiles - fileListingSnapshot)

        File downloadedFile = newFiles.findAll { File file -> file.name.endsWith('.torrent') }.max { File file -> file.lastModified() }

        downloadedFile?.absolutePath
    }

}
