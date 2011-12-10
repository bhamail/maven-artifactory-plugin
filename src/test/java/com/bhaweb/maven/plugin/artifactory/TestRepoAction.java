package com.bhaweb.maven.plugin.artifactory;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author Dan Rollo
 * Date: 11/10/11
 * Time: 8:32 PM
 */
public final class TestRepoAction {

    // URL

    // curl -i -u username:password "http://repository.myco.com/mycompany/api/storage/libs-releases-local/?list&deep=1&listFolders=0&mdTimestamps=0"

    private RepoAction mojo;

    @Before
    public void setUp() throws IOException {
        mojo = new RepoAction();
    }

    @Test
    public void testOpParamsToURL() {
        assertEquals("", mojo.opParamsToURL());

        final Map<String, String> opParams = new HashMap<String, String>();
        opParams.put("k", "v");
        mojo.setOperationParams(opParams);
        assertEquals("&k=v", mojo.opParamsToURL());
    }

    @Test
    public void testBuildFullURL() {
        assertEquals("", mojo.buildFullURL());

        mojo.setRepoURL("a");
        mojo.setApiURL("b");
        mojo.setOperation("c");
        assertEquals("a/b/?c", mojo.buildFullURL());

        mojo.setRepoName("d");
        assertEquals("a/b/d/?c", mojo.buildFullURL());

        final Map<String, String> opParams = new HashMap<String, String>();
        opParams.put("k", "v");
        mojo.setOperationParams(opParams);
        assertEquals("a/b/d/?c&k=v", mojo.buildFullURL());
    }

    @Test(expected=NullPointerException.class)
    public void testWriteToFileNullFile() throws IOException {
        //noinspection NullableProblems
        mojo.setOutputFile(null);
        mojo.writeToFile("");
    }

    @Test(expected=NullPointerException.class)
    public void testWriteToFileNullContent() throws IOException {
        final File tmp = createTestFile();
        mojo.setOutputFile(tmp);
        //noinspection NullableProblems
        mojo.writeToFile(null);
    }

    @Test
    public void testWriteToFile() throws IOException {
        final File tmp = createTestFile();
        mojo.setOutputFile(tmp);
        //noinspection NullableProblems
        mojo.writeToFile("123");
        assertEquals(3, tmp.length());
    }

    private static File createTestFile() throws IOException {
        final File tmp = File.createTempFile("testFile", ".tmp");
        tmp.deleteOnExit();
        return tmp;
    }
}
