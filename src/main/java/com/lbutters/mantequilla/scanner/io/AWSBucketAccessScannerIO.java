package com.lbutters.mantequilla.scanner.io;

import java.util.stream.Stream;

import java.io.InputStream;
import java.nio.file.Path;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWSBucketAccessScannerIO implements ScannerIO {

    private final AmazonS3 s3client;
    private final String bucket;
    
    public AWSBucketAccessScannerIO(String accessKey, String secretKey, String bucket, Regions region) {
         s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretKey)))
            .withRegion(region)
            .build();
         this.bucket = bucket;
    }
    
    public Stream<String> allFileNames() {
        // TODO doesn't handle buckets with lots of files.
        return s3client.listObjects(bucket).getObjectSummaries().stream()
            .map(S3ObjectSummary::getKey);
    }
    
    public InputStream getFileAsInputStream(String name) {
        // TODO: Return optional when the file no longer exists, rather than throw an exception. 
        return s3client.getObject(bucket, name).getObjectContent();
    }
    
    public void putFile(String fileName, Path content) {
        s3client.putObject(bucket, fileName, content.toFile());
    }
}
