package com.lbutters.mantequilla.scanner;

import java.util.concurrent.Callable;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.amazonaws.regions.Regions;
import com.google.common.io.Files;
import com.lbutters.mantequilla.scanner.io.AWSBucketAccessScannerIO;
import com.lbutters.mantequilla.scanner.io.ScannerIO;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Scanner", mixinStandardHelpOptions = true, version = "checksum 0.1",
         description = "Scans all CSVs in all zips in a s3 bucket for issues, reporting as .parquet")
public class Main implements Callable<Integer> {

    @Option(names = {"--access-key"}, description = "AWS access key")
    private String awsAccessKey;
    
    @Option(names = {"--secret-key"}, description = "AWS secret key")
    private String awsSecretKey;
    
    @Option(names = {"--bucket"}, description = "AWS s3 bucket name")
    private String bucket;
    
    @Option(names = {"--aws-region"}, description = "AWS region")
    private Regions region;
    
    @Override
    public Integer call() throws Exception {
        File tmpDir = Files.createTempDir();
        try {
            ScannerIO scannerIo = new AWSBucketAccessScannerIO(awsAccessKey, awsSecretKey, bucket, region);
            new ScanZipsForVulnrabilities()
                .process(scannerIo, tmpDir.toPath());
            return 0;
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }
    
    public static void main(String... args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
    
}
