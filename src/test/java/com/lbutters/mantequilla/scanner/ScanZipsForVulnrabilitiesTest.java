package com.lbutters.mantequilla.scanner;

import static com.lbutters.mantequilla.scanner.ApacheParquetSuspiciousLineRecorderTest.readFromParquet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lbutters.mantequilla.scanner.io.SimpleFileSystemScannerIO;
import com.lbutters.mantequilla.scanner.testutil.ZipCreatorHelper;
import com.lbutters.mantequilla.scanner.testutil.ZipCreatorHelper.ZipEntryInput;

public class ScanZipsForVulnrabilitiesTest {

    private ZipCreatorHelper zipCreatorHelper = new ZipCreatorHelper();
    
    @Test
    public void testSingleZipWithOneCSVWithOneBadLine(@TempDir java.nio.file.Path temp,
            @TempDir java.nio.file.Path wrkDir) throws Exception {
        Path zipFile = Paths.get(temp.toString(), "my.zip");
        
        zipCreatorHelper.createZip(zipFile, new ZipEntryInput("foo.csv", "good\r\nmalo"));
        
        new ScanZipsForVulnrabilities().process(new SimpleFileSystemScannerIO(temp), wrkDir);
        
        checkParquetFile(temp, "foo", "malo");
    }
    
    @Test
    public void testDoesntProcessParquetFiles(@TempDir java.nio.file.Path temp,
            @TempDir java.nio.file.Path wrkDir) throws Exception {
        Path zipFile = Paths.get(temp.toString(), "my.parquet");
        
        zipCreatorHelper.createZip(zipFile, new ZipEntryInput("foo.csv", "good\r\nmalo"));
        
        new ScanZipsForVulnrabilities().process(new SimpleFileSystemScannerIO(temp), wrkDir);
        
        checkParquetFileDoesNotExist(temp, "foo");
    }
    
    @Test
    public void testSkipsBadCSVFiles(@TempDir java.nio.file.Path temp,
            @TempDir java.nio.file.Path wrkDir) throws Exception {
        Path zipFile = Paths.get(temp.toString(), "my.zip");
        
        zipCreatorHelper.createZip(zipFile, new ZipEntryInput("foo.csv", "good\r\nFirdt malo"),
                                            new ZipEntryInput("bar.csv", "good\r\n2nd malo"));
        
        CsvVulnerabilityScanner csvVulnerabilityScanner = spy(new CsvVulnerabilityScanner());
        // Mock an error being thrown by the csv scanner on the first csv it gets.
        doThrow(new RuntimeException("Expected error on first csv"))
            .doCallRealMethod()
            .when(csvVulnerabilityScanner)
            .findBadLines(any());
        
        ScanZipsForVulnrabilities scanner = new ScanZipsForVulnrabilities();
        scanner.setCsvVulnerabilityScanner(csvVulnerabilityScanner);
        scanner.process(new SimpleFileSystemScannerIO(temp), wrkDir);
        
        checkParquetFileDoesNotExist(temp, "foo");
        checkParquetFile(temp, "bar", "2nd malo");
    }

    private void checkParquetFile(java.nio.file.Path temp, String baseName, String ... expectedValues) throws IOException {
        
        Path expectedReportFile = Paths.get(temp.toString(), baseName + ".parquet");
        assertTrue(Files.exists(expectedReportFile));
        
        List<String> badLines = readFromParquet(expectedReportFile);
        assertThat(badLines, contains(expectedValues));
    }
    
    private void checkParquetFileDoesNotExist(java.nio.file.Path temp, String baseName) throws IOException {
        Path expectedReportFile = Paths.get(temp.toString(), baseName + ".parquet");
        assertFalse(Files.exists(expectedReportFile));
    }
    
    
}
