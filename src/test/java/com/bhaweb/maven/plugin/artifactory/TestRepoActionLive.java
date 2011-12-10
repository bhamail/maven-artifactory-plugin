package com.bhaweb.maven.plugin.artifactory;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * These tests require a valid user/pwd and a real artifactory repository, so maybe they are really Integration Tests?
 *
 * You must provide valid properties for 'repoURL' in test/resource/ , 'serverID', 'username'
 *
 * @author Dan Rollo
 * Date: 12/9/11
 * Time: 6:38 PM
 */
public final class TestRepoActionLive extends TestCase {


    /** URL to live artifactory server, read from test.properties file. */
    private static String repoURL;

    /** Server <id> tag in settings.xml file, read from test.properties file. */
    private static String serverID;

    private String username;
    private String password;

    private RepoAction mojo;

    protected void setUp() throws IOException {
        final Properties testProps = loadTestProperties();
        repoURL = testProps.getProperty("repoURL");
        serverID = testProps.getProperty("serverID");

        mojo = new RepoAction();

        mojo.setRepoURL(repoURL);

        //noinspection NullableProblems
        if (!isSkipLive(null)) {
            readServerUsernamePwdFromSettings();
        }
    }

    private boolean isSkipLive(final String testName) {
        if ("http://<your live artifactory server>/".equals(repoURL)) {
            if (null != testName) {
                System.err.println("***   Skipping Live Server TEST: " + testName
                        + " - provide 'repoURL' and 'serverID' in test.properties file.");
            }
            return true;
        }
        return false;
    }

    private Properties loadTestProperties() throws IOException {
        final Properties testProps = new Properties();

        final InputStream is = this.getClass().getClassLoader().getResource("test.properties").openStream();
        if (null == is) {
            throw new RuntimeException("Missing test.properties file.");
        }
        try {

            final Reader reader = new InputStreamReader(is);
            try {
                testProps.load(reader);
            } finally {
                reader.close();
            }
        } finally {
            is.close();
        }
        return testProps;
    }

    private void readServerUsernamePwdFromSettings() throws IOException {
        // @todo User maven user home settings file (might not be 'user.home'...
        final File settings = new File(System.getProperty("user.home"), ".m2/settings.xml");
        assertTrue("Couldn't find settings file: " + settings.getAbsolutePath(), settings.exists());

        // @todo Read settings.xml with a dom library or using maven api.
        final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(settings)));
        try {
            String line = br.readLine();
            while (line != null && !line.contains(serverID)) {
                line = br.readLine();
            }
            if (null == line) {
                throw new IllegalStateException("Could not find Server <id> tag: " + serverID
                        + " in settings.xml file: " + settings.getAbsolutePath());
            }

            // look for user name
            final String tagUser = "<username>";
            line = br.readLine();
            while (line != null && !line.contains(tagUser)) {
                line = br.readLine();
            }
            if (null == line) {
                throw new IllegalStateException("Could not find <username> tag in settings.xml file: " + settings.getAbsolutePath());
            }
            username = line.substring(line.indexOf(tagUser) + tagUser.length(), line.indexOf("</username>"));

            // look for pwd
            final String tagPwd = "<password>";
            line = br.readLine();
            while (line != null && !line.contains(tagPwd)) {
                line = br.readLine();
            }
            if (null == line) {
                throw new IllegalStateException("Could not find <password> tag in settings.xml file: " + settings.getAbsolutePath());
            }
            password = line.substring(line.indexOf(tagPwd) + tagPwd.length(), line.indexOf("</password>"));

        } finally {
            br.close();
        }
    }


    public void testGetResultList() throws Exception {
        if (isSkipLive(getName())) {
            return;
        }

        final RestAPI.OPERATION op = RestAPI.OPERATION.LIST;
        mojo.setApiURL(op.getApiURL());

        mojo.setRepoName("libs-releases-local");

        mojo.setOperation(op.getOpName());

        final Map<String, String> opParams = new HashMap<String, String>();
        opParams.put("deep", "1");
        opParams.put("listFolders", "0");
        opParams.put("mdTimestamps", "0");
        mojo.setOperationParams(opParams);
        mojo.setResultType(op.getReturnType());

        mojo.setRepoUsername(username);
        mojo.setRepoPassword(password);

        System.out.println(mojo.getRepoName() + mojo.getResult());

        mojo.setRepoName("libs-snapshots-local");
        System.out.println(mojo.getRepoName() + mojo.getResult());
    }

    public void testGetResultRepositories() throws Exception {
        if (isSkipLive(getName())) {
            return;
        }

        final RestAPI.OPERATION opRepos = RestAPI.OPERATION.GETREPOSITORIES;
        mojo.setApiURL(opRepos.getApiURL());
        mojo.setOperation(opRepos.getOpName());

        mojo.setResultType(opRepos.getReturnType());

        mojo.setRepoUsername(username);
        mojo.setRepoPassword(password);

        final Object[] results = (Object[]) mojo.getResult();
        for (final Object item : results) {
            System.out.println(item);
        }
    }

    public void testGetResultDumpAll() throws Exception {
        if (isSkipLive(getName())) {
            return;
        }

        final RestAPI.OPERATION opRepos = RestAPI.OPERATION.GETREPOSITORIES;
        mojo.setApiURL(opRepos.getApiURL());
        mojo.setOperation(opRepos.getOpName());

        mojo.setResultType(opRepos.getReturnType());

        mojo.setRepoUsername(username);
        mojo.setRepoPassword(password);

        final RestAPI.RepoInfo[] results = (RestAPI.RepoInfo[]) mojo.getResult();


        final RestAPI.OPERATION op = RestAPI.OPERATION.LIST;
        mojo.setApiURL(op.getApiURL());
        mojo.setOperation(op.getOpName());

        final Map<String, String> opParams = new HashMap<String, String>();
        opParams.put("deep", "1");
        opParams.put("listFolders", "0");
        opParams.put("mdTimestamps", "0");
        mojo.setOperationParams(opParams);
        mojo.setResultType(op.getReturnType());

        long totalSizeMB = 0;

        for (final RestAPI.RepoInfo item : results) {
            System.out.println(Arrays.asList(item));

            if (!"VIRTUAL".equals(item.getType())) {
                mojo.setRepoName(item.getKey());

                final RestAPI.FolderInfo result = (RestAPI.FolderInfo) mojo.getResult();
                totalSizeMB += result.getTotalSizeMB();
                System.out.println("\t" + result);
            } else {
                System.out.println("\tSkipping dump of " + item.getType() + " repo: " + item.getKey());
            }
        }

        System.out.println("\n*** Total Size of All Repos: " + totalSizeMB + "mb");

    }

}
