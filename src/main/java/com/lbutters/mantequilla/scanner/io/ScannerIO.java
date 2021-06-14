package com.lbutters.mantequilla.scanner.io;

import java.util.stream.Stream;

import java.io.InputStream;
import java.nio.file.Path;

public interface ScannerIO {

    /**
     * List all files this scanner provides access to.
     * 
     * @return
     */
    public Stream<String> allFileNames();
    
    /**
     * Returns an input stream for the given file.
     * 
     * 
     * @param fileName
     * @return The input stream for the file, caller must close.
     * @throws RuntimeException
     */
    public InputStream getFileAsInputStream(String fileName) throws RuntimeException;
    
    /**
     * Sets the possibly remove file denoted by fileName, to the contents of the file at content.
     * 
     * @param fileName
     * @param content
     */
    public void putFile(String fileName, Path content);
}
