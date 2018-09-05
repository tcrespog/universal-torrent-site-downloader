package torrent

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

@Slf4j
class TransmissionClientManager extends AbstractTorrentClientManager {

    final String RPC_URL

    String transmissionSessionId

    OkHttpClient httpClient

    TransmissionClientManager(String host, Integer port, String username, String password) {
        super(host, port, username, password)

        RPC_URL = "http://${host}:${port}/transmission/rpc"
        httpClient = new OkHttpClient()
    }

    Map isTorrentClientSupported() {
        Map requestContent = [method: 'torrent-get', arguments: [ids: [], fields: ['addedDate']]]

        Response response
        String errorText
        try {
            response = performRequest(requestContent)

            errorText = response.isSuccessful() ? null : "Response error code: ${response.code()}"
        } catch (e) {
            errorText = e.message
        } finally {
            response?.close()
        }


        if (errorText) {
            [isSupported: false, error: "The Transmission server is not supported. Make sure the connection data is correct. Trace info: ${errorText}"]
        } else {
            [isSupported: true]
        }
    }

    boolean addTorrentToClient(String torrentUri) {
        Map requestContent = (torrentUri.startsWith('magnet:?')) ? [method: 'torrent-add', arguments: [filename: torrentUri]] : [method: 'torrent-add', arguments: [metainfo: Base64.getEncoder().encodeToString(new File(torrentUri).bytes)]]

        log.info("Add torrent using the request ${requestContent}")

        Response response
        try {
            response = performRequest(requestContent)

            if (response.isSuccessful()) {
                Map responseContent = new JsonSlurper().parseText(response.body().string()) as Map
                downloadingTorrentId = responseContent.arguments?.'torrent-added'?.id

                log.info("Response: '${responseContent}'")

                return (responseContent.result == 'success')
            } else {
                log.error("An error occurred. Response code: ${response.code()}")

                return false
            }
        } catch (e) {
            log.error("An error occurred. Exception: ${e.message}")
            return false
        } finally {
            response?.close()
        }
    }

    boolean verifyTransmissionSessionIdStatus(Response response, Request.Builder requestBuilder) {
        if (response.code() == 409) {
            transmissionSessionId = response.header('X-Transmission-Session-Id')
            requestBuilder.header('X-Transmission-Session-Id', transmissionSessionId)

            return false
        }

        true
    }

    Response performRequest(Map requestContent) {
        RequestBody body = RequestBody.create(MediaType.parse('application/json; charset=utf-8'), JsonOutput.toJson(requestContent))
        Request.Builder requestBuilder = new Request.Builder()
                .url(RPC_URL)
                .header('X-Transmission-Session-Id', transmissionSessionId ?: '')
                .post(body)

        Response response
        boolean requestPerformed = false
        while (!requestPerformed) {
            response = httpClient.newCall(requestBuilder.build()).execute()
            requestPerformed = verifyTransmissionSessionIdStatus(response, requestBuilder)
        }

        response
    }

    int getTorrentCompletionPercentage() {
        Map requestContent = [method: 'torrent-get', arguments: [ids: [downloadingTorrentId.toInteger()], fields: ['percentDone']]]

        Response response = performRequest(requestContent)
        Map responseContent = new JsonSlurper().parseText(response.body().string()) as Map

        double percentage = responseContent.arguments.torrents[0].percentDone

        (percentage * 100).toInteger()
    }

    boolean removeTorrentFromClient() {
        Map requestContent = [method: 'torrent-remove', arguments: [ids: [downloadingTorrentId.toInteger()], 'delete-local-data': true]]

        Response response = performRequest(requestContent)

        (response.code() == 200)
    }

}
