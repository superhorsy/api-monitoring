package com.superhorsy.apimonitoring;

import com.google.gson.Gson;
import com.superhorsy.apimonitoring.entities.RequestData;
import com.superhorsy.apimonitoring.services.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import rawhttp.core.EagerHttpResponse;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.client.TcpRawHttpClient;
import rawhttp.core.client.TcpRawHttpClient.TcpRawHttpClientOptions;
import rawhttp.core.errors.InvalidHttpRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConsoleRunner implements CommandLineRunner {
    private final FileService fileService;
    private final Gson gson;
    private final RawHttp rawHttp;

    @Value("${requests.path: config/requests.config}")
    private String requestsConfigPath;
    @Value("${requests.interval: 5}")
    private Integer interval;

    @Autowired
    public ConsoleRunner(FileService fileService, Gson gson, RawHttp rawHttp) {
        this.fileService = fileService;
        this.gson = gson;
        this.rawHttp = rawHttp;
    }

    @Override
    public void run(String... args) throws Exception {
        do {
            List<RawHttpRequest> requests = getRequestsFromConfigFile();

            FileWriter writer = fileService.prepareWriter();

            TcpRawHttpClient client = prepareClient();

            sendRequsts(requests, writer, client);

            writer.close();
            client.close();

            TimeUnit.MINUTES.sleep(interval);
        } while (true);
    }

    private void sendRequsts(List<RawHttpRequest> requests, FileWriter writer, TcpRawHttpClient client)
            throws IOException {
        for (RawHttpRequest request : requests) {
            RequestData requestData = sendRequest(request, client);
            // Add to log
            String json = gson.toJson(requestData);
            if (Objects.nonNull(requestData)) {
                log.info("Method: " + requestData.getMethod() + ", code: " + requestData.getStatusCode() + ", time: "
                        + requestData.getRequestTime());
            }
            writer.append(json).append("\n").flush();
        }
    }

    @Nullable
    private RequestData sendRequest(RawHttpRequest request, TcpRawHttpClient client) throws IOException {
        try {
            long timestamp = Instant.now().toEpochMilli();
            long startTime = System.nanoTime();
            EagerHttpResponse<?> rawResponse = client.send(request).eagerly();
            String rawHttpContentType = rawResponse.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE).orElse("");
            // 1 second = 1_000_000_000 nano seconds
            double elapsedTimeInSecond = (double) (System.nanoTime() - startTime) / 1_000_000_000;
            return new RequestData(
                timestamp, 
                request.getUri().getHost(),
                 request.getUri().getPath(),
                 request.getUri().toString(),
                 rawResponse.getStatusCode(),
                 rawHttpContentType,
                 elapsedTimeInSecond
            );
        } catch (IOException e) {
            log.warn("Can't perform request: " + request.toString());
            request.writeTo(System.out);
            e.printStackTrace();
        }
        return null;
    }

    private List<RawHttpRequest> getRequestsFromConfigFile() {
        return fileService.getRequestsFromFile(requestsConfigPath, "#").stream().map(this::getRequestFromString)
                .filter(x -> x != null).collect(Collectors.toList());
    }

    @Nullable
    private RawHttpRequest getRequestFromString(String string) {
        try {
            return rawHttp.parseRequest(string);
        } catch (InvalidHttpRequest e) {
            log.warn("Bad request found: " + string);
            e.printStackTrace();
            return null;
        }
    }

    private TcpRawHttpClient prepareClient() {
        // Таймаут по сокету в ноль
        TcpRawHttpClientOptions options = new TcpRawHttpClient.DefaultOptions() {
            public Map<String, Socket> socketByHost = new HashMap<>(4);

            @Override
            public Socket getSocket(URI uri) {
                String host = Optional.ofNullable(uri.getHost())
                        .orElseThrow(() -> new RuntimeException("Host is not available in the URI"));

                Socket socket = socketByHost.get(host);

                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    boolean useHttps = "https".equalsIgnoreCase(uri.getScheme());
                    int port = uri.getPort();
                    if (port < 1) {
                        port = useHttps ? 443 : 80;
                    }
                    try {
                        socket = createSocket(useHttps, host, port);
                        // здесь - Таймаут по сокету в ноль
                        socket.setSoTimeout(0);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    socketByHost.put(host, socket);
                }

                return socket;
            }
        };
        return new TcpRawHttpClient(options);
    }
}
