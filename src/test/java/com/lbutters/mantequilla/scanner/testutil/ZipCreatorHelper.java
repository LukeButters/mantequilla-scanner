package com.lbutters.mantequilla.scanner.testutil;

import java.util.stream.Stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

public class ZipCreatorHelper {

    /**
     * Can be used to create a zip file for example:
     * 
     * <pre>{@code
     * try(FileOutputStream fos = new FileOutputStream(new File("my.zip"))) {
     *     createZip(fos, Stream.of(new ZipEntryInput("hello.txt", "hello world")));
     * }
     * }</pre>
     * 
     * @param zipOutput the output stream to write the zip file.
     * @param zipEntries The entries for the zip file.
     * @throws IOException
     */
    public void createZip(OutputStream zipOutput, Stream<ZipEntryInput> zipEntries)
        throws IOException {

        ZipParameters zipParameters = buildZipParameters();
        
        try(ZipOutputStream zos = new ZipOutputStream(zipOutput)) {
            zipEntries.forEach(zipEntryInput -> {
                // Entry size has to be set if you want to add entries of STORE compression method (no compression)
                // This is not required for deflate compression
                if (zipParameters.getCompressionMethod() == CompressionMethod.STORE) {
                    zipParameters.setEntrySize(zipEntryInput.getEntrySize());
                }

                zipParameters.setFileNameInZip(zipEntryInput.getZipEntryName());
                try {
                    zos.putNextEntry(zipParameters);    
                    IOUtils.copy(zipEntryInput.getContent(), zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    
    public byte[] createInMemoryZip(Stream<ZipEntryInput> zipEntries) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            createZip(bos, zipEntries);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public byte[] createInMemoryZip(ZipEntryInput ... zipEntries) {
        return createInMemoryZip(varargsToStream(zipEntries));
    }
    
    public void createZip(Path path, ZipEntryInput ... zipEntries) {
        try(OutputStream os = Files.newOutputStream(path)) {
            createZip(os, varargsToStream(zipEntries));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Stream<ZipEntryInput> varargsToStream(ZipEntryInput ... zipEntries) {
        if(zipEntries == null) return Stream.of();
        return Stream.of(zipEntries);
    }
    
    @AllArgsConstructor
    @Getter
    public static class ZipEntryInput {
        public final String zipEntryName;
        public final long entrySize;
        public final InputStream content;
        
        public ZipEntryInput(String zipEntryName, String content) {
            this.zipEntryName = zipEntryName;
            byte[] b = content.getBytes(StandardCharsets.UTF_8);
            entrySize = b.length;
            this.content = new ByteArrayInputStream(b);
        }
    }

    private ZipParameters buildZipParameters() {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        return zipParameters;
    }
}
