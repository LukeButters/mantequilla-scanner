package com.lbutters.mantequilla.scanner;

import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ProcessZip {

    
    /**
     * Runs the given BiConsumer on each file within the zip file supplied via incomingZip.
     * 
     * Note that the InputStream must not be closed, since that closes the zip file, if java 
     * didn't use abstract classes that would be easily fixed with delegates.
     * 
     * @param incomingZip the zip file.
     * @param onFile Called for each file within the zip file passing the name of the file within
     * the zip file and an InputStream for reading that file.
     */
    public void forEachFile(InputStream incomingZip, BiConsumer<String, InputStream> onFile) {
            ZipInputStream zipInputStream = new ZipInputStream(incomingZip);
            
            ZipEntry localFileHeader;
            try {
                while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                    String fileName = localFileHeader.getName();
                    onFile.accept(fileName, zipInputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
}
