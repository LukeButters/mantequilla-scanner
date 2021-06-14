package com.lbutters.mantequilla.scanner.io;

import java.util.stream.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import lombok.AllArgsConstructor;

/**
 * A very simple file system based scanner that will work only with files that are 
 * direct children of the given dir.
 *
 */
@AllArgsConstructor
public class SimpleFileSystemScannerIO implements ScannerIO {

    public final Path dir;

    @Override
    public Stream<String> allFileNames() {
        try {
            return Files.list(dir)
                .filter(Files::isRegularFile)
                .map(f -> f.getFileName())
                .map(Path::toString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getFileAsInputStream(String fileName) throws RuntimeException {
        try {
            return Files.newInputStream(Paths.get(dir.toString(), fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putFile(String fileName, Path content) {
        try {
            Files.copy(content, Paths.get(dir.toString(), fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    
}
