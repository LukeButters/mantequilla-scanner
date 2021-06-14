package com.lbutters.mantequilla.scanner;

import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lbutters.mantequilla.scanner.io.ScannerIO;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import static lombok.AccessLevel.PACKAGE;

@Log4j2
public class ScanZipsForVulnrabilities {

    @Setter(PACKAGE) private CsvVulnerabilityScanner csvVulnerabilityScanner = new CsvVulnerabilityScanner();
    private ApacheParquetSuspiciousLineRecorder parquetLineRecorder = new ApacheParquetSuspiciousLineRecorder();
    
    
    public void process(ScannerIO scannerIOProvider, Path wrkDir) {
        Path parquetWrkFile = Paths.get(wrkDir.toString(), "work.parquet"); // We re-use the same file, which will be a problem if we use a parallel stream.
        scannerIOProvider.allFileNames()
            .filter(fileName -> fileName.endsWith(".zip"))
            .forEach(zipFile -> {
                try(InputStream zipFileToScan = scannerIOProvider.getFileAsInputStream(zipFile)) {
                    new ProcessZip().forEachFile(zipFileToScan, (zipEntryName, zipEntryContents) -> {
                        handleZipContents(scannerIOProvider, parquetWrkFile, zipFile, zipEntryContents, zipEntryName);
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }


    private void handleZipContents(ScannerIO scannerIOProvider, 
            Path parquetWrkFile, 
            String zipFile,
            InputStream zipEntryContents, 
            String zipEntryName) {
        try {
            if(!csvVulnerabilityScanner.isCsvFile(zipEntryName)) return;
            // Aws requires that we send them the Content size, so we have to stop the streaming 
            // low memory usage here. Ideally I would have liked to stream the data to the s3 bucket.
            // Also our implementation of creating a parquet file 
            List<String> badLines = csvVulnerabilityScanner.findBadLines(zipEntryContents).collect(Collectors.toList());
            if(!badLines.isEmpty()) {
                parquetLineRecorder.recordSuspiciousLines(parquetWrkFile, badLines);
                scannerIOProvider.putFile(remoteFileNameToStoreSusLines(zipEntryName), parquetWrkFile);
            }
        } catch (Exception e) {
            log.warn("Could not create or send parquet file for zip entry '{}' within zip '{}'", zipEntryName, zipFile, e);
        }
    }
    
    private String remoteFileNameToStoreSusLines(String zipEntryName) {
        return csvVulnerabilityScanner.stripCsvExtensionFromPath(zipEntryName) + parquetLineRecorder.getExtension();
    }
}
