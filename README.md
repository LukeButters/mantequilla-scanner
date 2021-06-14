# CSV Vulnerability scanner

Scans CSV files within zip files within an AWS s3 bucket, reporting bad lines as parquet files within the bucket.


## Getting Started

### Building & Running:

You will need to both build it and fetch the dependencies:
```
mvn clean package
mvn dependency:copy-dependencies
```

### To run

```
java -cp "target/mantequilla-scanner-0.0.1-SNAPSHOT.jar:target/dependency/*"  com.lbutters.mantequilla.scanner.Main --access-key=your_key --aws-region=AP_SOUTHEAST_2 --bucket=your_bucket --secret-key=your_secret
```

## Help

To view the help
```
java -cp "target/mantequilla-scanner-0.0.1-SNAPSHOT.jar:target/dependency/*"  com.lbutters.mantequilla.scanner.Main --help
```
Which should print:
```
Usage: Scanner [-hV] [--access-key=<awsAccessKey>] [--aws-region=<region>]
               [--bucket=<bucket>] [--secret-key=<awsSecretKey>]
Scans all CSVs in all zips in a s3 bucket for issues, reporting as .parquet
      --access-key=<awsAccessKey>
                          AWS access key
      --aws-region=<region>
                          AWS region
      --bucket=<bucket>   AWS s3 bucket name
  -h, --help              Show this help message and exit.
      --secret-key=<awsSecretKey>
                          AWS secret key
  -V, --version           Print version information and exit.
```

## Notes.

1. Interactions with AWS buckets are behind an interface which is also implemented by a simpler local file system class, making testing easier.
2. I wanted to have low memory usage, this is why you see that the unziping is all done in memory using streams. I was hoping I could stream the results back to the s3 bucket and so never really hold much in memory or write anything to disk. However that turned out to not be possible since s3 buckets want to know the size of the file they are receiving and the parquet library I used doesn't seem to support writing to a stream.
3. However it would still be possible to have low memory usage if we didn't hold all of the suspicious lines in memory before writing them.
