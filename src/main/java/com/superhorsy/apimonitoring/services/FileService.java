package com.superhorsy.apimonitoring.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileService {

    @Value("${log.path: logs/api-monitoring.log}")
    private String logPath;

    public ArrayList<String> getRequestsFromFile(String inputFileName, String delimiterPrefix) {
        ArrayList<String> requests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            StringBuilder request = new StringBuilder(0);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(delimiterPrefix) && !request.toString().isBlank()) {
                    requests.add(request.toString());
                    request.setLength(0);
                    continue;
                }
                request.append(line).append("\n");
            }
            if (request.length() > 0) {
                requests.add(request.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public FileWriter prepareWriter() throws IOException {
        File file = getLogFile();
        if (file.length() / 1024 > 10) {
            log.info("Logstash filesize " + file.length() / 1024 + "kB, wiping out");
        }
        return new FileWriter(file, true);
    }

    protected File getLogFile() throws IOException {
        File file = new File(logPath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }
}
