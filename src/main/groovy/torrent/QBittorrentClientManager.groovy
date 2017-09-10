package torrent

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

@Slf4j
class QBittorrentClientManager extends AbstractTorrentClientManager {

    final String WEB_URL

    String authorizationCookie

    OkHttpClient httpClient

    QBittorrentClientManager(String host, Integer port, String username, String password) {
        super(host, port, username, password)

        WEB_URL = "http://${host}:${port}"
        httpClient = new OkHttpClient()
    }

    Map isTorrentClientSupported() {
        Response response
        String errorText
        try {
            response = login()
            errorText = (response.isSuccessful() && authorizationCookie) ? null : 'Bad credentials'
        } catch (e) {
            errorText = e.message
        } finally {
            response?.close()
        }

        if (errorText) {
            [isSupported: false, error: "The qBittorrent client is not supported. Make sure the software is running, WebUI is active, the connection data is correct and the credentials are valid. Trace info: ${errorText}"]
        } else {
            [isSupported: true]
        }
    }

    boolean addTorrentToClient(String torrentUri) {
        if (torrentUri.startsWith('magnet:')) {
            addMagnet(torrentUri)
        } else {
            uploadTorrentFile(torrentUri)
        }

        //Wait a prudential time to set the id, sometimes the immediate request doesn't work properly
        sleep(500)
        setAddedTorrentId()

        (downloadingTorrentId != null)
    }

    void addMagnet(String torrentUri) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart('urls', torrentUri)
                .build()

        String endpointUrl = "${WEB_URL}/command/download"
        log.info("Add magnet ${torrentUri} using ${endpointUrl}")

        Request.Builder requestBuilder = new Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
        performRequest(requestBuilder)
    }

    void uploadTorrentFile(String torrentFilePath) {
        File torrentFile = new File(torrentFilePath)
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart('torrents', torrentFile.name, RequestBody.create(MediaType.parse('application/x-bittorrent'), torrentFile))
                .build()

        String endpointUrl = "${WEB_URL}/command/upload"
        log.info("Add torrent ${torrentFilePath} using ${endpointUrl}")

        Request.Builder requestBuilder = new Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
        performRequest(requestBuilder)
    }

    Response performRequest(Request.Builder requestBuilder) {
        requestBuilder.header('Cookie', authorizationCookie ?: '')

        Response response
        boolean requestPerformed = false
        while (!requestPerformed) {
            response = httpClient.newCall(requestBuilder.build()).execute()
            requestPerformed = verifyIfLoggedIn(response, requestBuilder)
        }

        response
    }

    boolean verifyIfLoggedIn(Response response, Request.Builder requestBuilder) {
        if (response.code() == 403) {
            login()
            requestBuilder.header('Cookie', authorizationCookie)

            return false
        }

        true
    }

    Response login() {
        RequestBody body = RequestBody.create(MediaType.parse('application/x-www-form-urlencoded'), "username=${username}&password=${password}")
        Request request = new Request.Builder()
                .url("${WEB_URL}/login")
                .post(body)
                .build()

        Response response
        response = httpClient.newCall(request).execute()

        authorizationCookie = response.header('Set-Cookie')?.replaceAll(/;.*/, '')
        response
    }

    void setAddedTorrentId() {
        Response response = makeListTorrentsRequest()
        List torrentsInfo = new JsonSlurper().parseText(response.body().string()) as List

        downloadingTorrentId = torrentsInfo[0]?.hash
    }

    Response makeListTorrentsRequest() {
        Request.Builder requestBuilder = new Request.Builder()
                .url("${WEB_URL}/query/torrents?sort=added_on&reverse=true")
                .get()

        performRequest(requestBuilder)
    }

    int getTorrentCompletionPercentage() {
        Response response = makeListTorrentsRequest()
        List torrentsInfo = new JsonSlurper().parseText(response.body().string()) as List

        double percentage = torrentsInfo.find { it.hash == downloadingTorrentId }.progress

        (percentage * 100).toInteger()
    }

    boolean removeTorrentFromClient() {
        RequestBody body = RequestBody.create(MediaType.parse('application/x-www-form-urlencoded'), "hashes=${downloadingTorrentId}")
        Request.Builder requestBuilder = new Request.Builder()
                .url("${WEB_URL}/command/deletePerm")
                .post(body)

        Response response = performRequest(requestBuilder)

        (response.code() == 200)
    }
}
