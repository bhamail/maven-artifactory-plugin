package com.bhaweb.maven.plugin.artifactory;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
public class RepoAction
    extends AbstractMojo
{

    /**
     * The artifactory repository login user name.
     *
     * @parameter expression="${repoUsername}"
     */
    private String repoUsername;

    /**
     * The artifactory repository login password.
     *
     * @parameter expression="${repoPassword}"
     */
    private String repoPassword;

    /**
     * The artifactory repository URL prefix, like: http://repository.myco.com/mycompany/.
     *
     * @parameter expression="${repoURLprefix}"
     */
    private String repoURL;

    /**
     * The artifactory rest api URL portion, like: api/storage/.
     *
     * @parameter expression="${apiURL}"
     */
    private String apiURL;

    /**
     * The artifactory repository name, like: libs-releases-local.
     *
     * @parameter expression="${repoName}"
     */
    private String repoName;

    /**
     * The artifactory repository rest api operation, like: list.
     *
     * @parameter expression="${repoRestOp}"
     */
    private String repoOperation;

    /**
     * Any operation parameters, like: deep=1&listFolders=0&mdTimestamps=0.
     *
     * @parameter expression="${opParams}"
     */
    private Map<String, String> operationParams;

    /**
     * Type (class) of expected return value, like: RepoAction.FolderInfo.class.
     *
     * @parameter expression="${resultType}"
     */
    private Type resultType;

    /**
     * File to which results are written.
     *
     * @parameter expression="${outputFile}" default-value="${project.build.directory}/artifactoryRepoOutput.txt"
     */
    private File outputFile;


    public void execute()
        throws MojoExecutionException
    {
        try {
            writeToFile(getRepoName() + getResult());
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing to file: " + outputFile.getAbsolutePath(), e);
        }
    }

    String opParamsToURL() {

        final StringBuilder sb = new StringBuilder();

        if (operationParams != null) {
            // example:
            // &deep=1&listFolders=0&mdTimestamps=0

            for (final String name : operationParams.keySet()) {
                sb.append("&").append(name).append("=").append(operationParams.get(name));
            }
        }

        return sb.toString();
    }


    String buildFullURL() {
        // example:
        // http://repository.myco.com/mycompany/api/storage/libs-releases-local/?list&deep=1&listFolders=0&mdTimestamps=0
        StringBuilder fullURL = new StringBuilder(
                (repoURL != null ? repoURL : ""));

        if (fullURL.length() != 0) {
            fullURL.append(repoURL.endsWith("/") ? "" : "/");
        }

        if (fullURL.length() != 0) {
            fullURL.append((apiURL != null ? apiURL + "/" : ""));
        }

        if (fullURL.length() != 0) {
            fullURL.append((repoName != null ? repoName + "/" : ""));
        }

        if (fullURL.length() != 0) {
            fullURL.append("?").append(repoOperation != null ? repoOperation : "").append(opParamsToURL());
        }

        return fullURL.toString();
    }


    Object getResult() {
        final ClientConfig config = new DefaultClientConfig();

        //config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hv, ctx));
        //config.getClasses().add(JAXBContextResolver.class);

        final Client client = Client.create(config);

        client.addFilter(new HTTPBasicAuthFilter(repoUsername, repoPassword));

        // example:
        // http://repository.myco.com/mycompany/api/storage/libs-releases-local/?list&deep=1&listFolders=0&mdTimestamps=0
        final String fullURL = buildFullURL();

        final WebResource wr = client.resource(fullURL);

        final Object objResult =  wr
                            //.path("statuses/friends_timeline.json")
                           //.header(AUTHENTICATION_HEADER, authentication)
                           //.accept(MediaType.APPLICATION_JSON_TYPE)
                           //.accept("application/vnd.org.jfrog.artifactory.storage.FolderInfo+json")
                           .accept(MediaType.APPLICATION_JSON)
                           //.get(new GenericType<List<String>>() { }
                           //.get(new GenericType<List>() { }
                           //.get(new GenericType<String>() { }
                           //.get(new GenericType<FolderInfo>() { }
                           .get(String.class
                           );


        final Gson gson = new Gson();

        return gson.fromJson((String)objResult, resultType);
    }


    void writeToFile(final Object result) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            writer.write(result.toString());
        } finally {
            writer.close();
        }
    }

    public void setRepoURL(final String repoURL) {
        this.repoURL = repoURL;
    }

    public void setApiURL(final String apiURL) {
        this.apiURL = apiURL;
    }

    public void setRepoName(final String repoName) {
        this.repoName = repoName;
    }
    String getRepoName() { return repoName; }

    public void setOperation(final String repoOperation) {
        this.repoOperation = repoOperation;
    }

    public void setOperationParams(final Map<String, String> operationParams) {
        this.operationParams = operationParams;
    }

    public void setResultType(final Type resultType) {
        this.resultType = resultType;
    }



    public void setRepoUsername(final String repoUsername) {
        this.repoUsername = repoUsername;
    }

    public void setRepoPassword(final String repoPassword) {
        this.repoPassword = repoPassword;
    }

    public void setOutputFile(final File file) {
        outputFile = file;
    }
}
