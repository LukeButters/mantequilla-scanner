package com.lbutters.mantequilla.scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.lbutters.mantequilla.scanner.testutil.ZipCreatorHelper;
import com.lbutters.mantequilla.scanner.testutil.ZipCreatorHelper.ZipEntryInput;

public class ProcessZipTest {

    private final ZipCreatorHelper zipCreatorHelper = new ZipCreatorHelper();
    
    @Test
    public void testProcessZip() throws Exception {
        byte[] zip = zipCreatorHelper.createInMemoryZip(new ZipEntryInput("one", "1"));
        ProcessZip processZip = new ProcessZip();
        processZip.forEachFile(new ByteArrayInputStream(zip), (name, contents) -> {
            assertEquals(name, "one");
            try {
                assertEquals(IOUtils.toString(contents, UTF_8), "1");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    
    

}
