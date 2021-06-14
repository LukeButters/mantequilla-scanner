package com.lbutters.mantequilla.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

public class ApacheParquetSuspiciousLineRecorderTest {

    @Test
    public void testWriteLines(@TempDir java.nio.file.Path temp) throws Exception {
        
        java.nio.file.Path path = Paths.get(temp.toString(), "billy.parquet");
        
        new ApacheParquetSuspiciousLineRecorder()
            .recordSuspiciousLines(path, List.of("that's a paddlin", "you better believe"));
        
        List<String> recorded = readFromParquet(path);
        
        assertThat(recorded, contains("that's a paddlin", "you better believe"));
    }
    
    @Test
    public void testWriteNoLines(@TempDir java.nio.file.Path temp) throws Exception {
        
        java.nio.file.Path path = Paths.get(temp.toString(), "billy.parquet");
        
        new ApacheParquetSuspiciousLineRecorder()
            .recordSuspiciousLines(path, List.of());
        
        List<String> recorded = readFromParquet(path);
        assertEquals(0, recorded.size());
    }
    
    public static List<String> readFromParquet(java.nio.file.Path filePathToRead) throws IOException {
        List<String> susLines = new ArrayList<>();
        try (@SuppressWarnings("unchecked")
        ParquetReader<GenericData.Record> reader = AvroParquetReader
                .<GenericData.Record>builder(new org.apache.hadoop.fs.Path(filePathToRead.toString()))
                .withConf(new Configuration())
                .build()) {

            GenericData.Record record;
            while ((record = reader.read()) != null) {
                susLines.add(record.get("suspicious_line").toString());
            }
        }
        return susLines;
    }
}
