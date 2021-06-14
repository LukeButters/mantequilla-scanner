package com.lbutters.mantequilla.scanner;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

/**
 * Thanks to:
 * https://github.com/MaxNevermind/Hadoop-snippets/blob/master/src/main/java/org/maxkons/hadoop_snippets/parquet/ParquetReaderWriterWithAvro.java
 * https://stackoverflow.com/questions/39728854/create-parquet-files-in-java
 *
 */
public class ApacheParquetSuspiciousLineRecorder {
    
    // TODO: How do people usually work with schemas? If it is in JSON
    // it is probably easier to share, if so move this to the class path.
    // Perhaps though I should be creating the schema using pure java code
    // as that might be nicer to work with. I am surprised the schema can't
    // be generated from a class like jackson can.    
    private static final String SCHEMA_JSON = "{\n" + 
            "    \"type\" : \"record\",\n" + 
            "    \"name\" : \"suspicious_lines\",\n" + 
            "    \"namespace\" : \"com.lbutters.mantequilla\",\n" + 
            "    \"fields\" : [{\"name\" : \"suspicious_line\", \"type\": \"string\"\n" +
            "      " + 
            "   } ]\n" + 
            "}";
    
    private static final Schema schema = new Schema.Parser().parse(SCHEMA_JSON);

    /**
     * Record lines to a parquet file.
     * 
     * @param toPath where to record the .parquet file to, existing files will be replaced.
     * @param susLines the suspicious lines to record.
     * 
     * @throws IOException
     */
    public void recordSuspiciousLines(java.nio.file.Path toPath, List<String> susLines) 
            throws IOException {
        
        Files.deleteIfExists(toPath);
        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
            .<GenericData.Record>builder(new Path(toPath.toString()))
            .withSchema(schema)
            .withConf(new Configuration())
            .withCompressionCodec(CompressionCodecName.SNAPPY)
            .build()) {
            GenericData.Record record = new GenericData.Record(schema);
            record.put("suspicious_line", "a");
            List<GenericData.Record> data = new ArrayList<>();
            data.add(record);
            
            for(String line : susLines) {
                writer.write(createRecordFromLine(line));
            }
        }
    }
    
    private GenericData.Record createRecordFromLine(String line) {
        GenericData.Record record = new GenericData.Record(schema);
        record.put("suspicious_line", line);
        return record;
    }
    
    public String getExtension() {
        return ".parquet";
    }
}
