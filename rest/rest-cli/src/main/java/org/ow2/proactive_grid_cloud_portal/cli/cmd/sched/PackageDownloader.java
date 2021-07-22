/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc2.SvnExport;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;


/**
 * @author ActiveEon Team
 * @since 15/11/2017
 */
public class PackageDownloader {

    private static final String METADATA_FILENAME = "METADATA.json";

    private static final String RESOURCE_DIRECTORYNAME = "resources";

    private static final String REGX_GITHUB_REPO_CONTENT = "(\\S*?.(www\\.)?github\\.com/[^\\s/]+/[^\\s/]+)/?((tree/[^\\s/]+)(\\S*))?";

    private static final String REGX_GITHUB_URL = "((http|https)://)?(www\\.)?github\\.com.*";

    private static final String REGX_FILE_URL = "^.*\\..{1,}$";

    private static final String REGX_ARCHIVE_URL = "^\\S*\\.(zip|tar\\.gz|ZIP|TAR\\.GZ)$";

    private static Logger logger = Logger.getLogger(PackageDownloader.class);

    /**
     * This method downloads a package from a remote location. <br>
     *     It supports the following types of urls:
     *     <ul>
     *         <li>Direct-download zip urls</li>
     *         <li>Exposed web directories (supports only tomcat directory listings for the moment)</li>
     *         <li>Entire Github repositories given by their http links or downloadable archive format</li>
     *         <li>Sub-directories within a github repository</li>
     *         <li>Supports forwarded URLS as in shortened bitly urls, etc </li>
     *     </ul>
     * @param packageUrl url of the package to download
     * @return path of the temporary folder where the package has been downloaded
     * @throws CLIException
     */
    public String downloadPackage(String packageUrl) {
        // Get the real URL in case it is a shortened one (via bit.ly, etc)
        URL url;
        try {
            url = getRealURLIfForwarded(new URL(createNormalizeURL(packageUrl)));
            checkReachableURL(url);
            if (isGithubURL(url.toString())) {
                return downloadPackageFromGithub(url.toString());
            } else {
                return downloadPackageFromWeb(url);
            }
        } catch (IOException e) {
            logger.warn(packageUrl + " is not a reachable package URL.");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' is not a reachable package URL.", packageUrl));
        } catch (URISyntaxException e) {
            logger.warn(packageUrl + " is not a reachable package URL.");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' is not a valid valid package URL.", packageUrl));
        } catch (SVNException e) {
            // This exception can only be raised if transformGithubURLtoSvnURL doesn't work properly anymore.
            // In that case, the method needs to be revised.
            // If a non-existing github url (repo or else) is given, an IOException
            logger.warn(packageUrl + " cannot be downloaded from github.com");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' cannot be downloaded from github.com", packageUrl));
        }

    }

    /**
     * This method downloads an entire github repository or only a sub-directory within a repository.<br>
     * It can also download and extract an archive version of a github repository.
     * It relies on svn export command.<br>
     *
     * @param githubURL
     * @return
     */
    private String downloadPackageFromGithub(String githubURL) throws SVNException {
        // Convert the githubRL to an svn format
        String svnURL = transformGithubURLToSvnURL(githubURL);

        // Save files in a system temporary directory
        String tDir = System.getProperty("java.io.tmpdir");
        String packagePath = tDir + "pkg_tmp" + System.nanoTime();
        File outputDir = new File(packagePath);

        // Perform an "svn export" command to download an entire repository or a subdirectory
        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
        try {
            final SvnExport export = svnOperationFactory.createExport();
            export.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(svnURL)));
            export.setSingleTarget(SvnTarget.fromFile(outputDir));
            //overwrite an existing file
            export.setForce(true);
            export.run();
        } finally {
            //close connection pool associted with this object
            svnOperationFactory.dispose();
        }
        return packagePath;
    }

    /**
     * This methods downloads a package from a direct-download location.
     * It can be either a web directory or an archive file
     * @param packageURL
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private String downloadPackageFromWeb(URL packageURL) throws IOException, URISyntaxException {
        // Save files in a system temporary directory
        String tDir = System.getProperty("java.io.tmpdir");
        String packagePath = tDir + "pkg_tmp" + System.nanoTime();
        File outputDir = new File(packagePath, FilenameUtils.getName(packageURL.toString()));

        if (isCompressedPackage(packageURL.toString())) {
            FileUtils.copyURLToFile(packageURL, outputDir);

        } else {
            validateWebDirectoryContent(packageURL);
            Set<String> result = listWebDirectoryContent(packageURL, "");
            downloadWebDirectory(packageURL, outputDir, result);
        }
        return outputDir.getAbsolutePath();
    }

    /**
     * This method downloads all the contents of a web directory and its subdirectories.
     *
     * @param dirUrl
     * @param outputDir
     * @param result
     * @throws IOException
     */
    private void downloadWebDirectory(URL dirUrl, File outputDir, Set<String> result) throws IOException {
        for (String relativeURL : result) {
            if (isFileURL(relativeURL)) {
                URL absoluteUrl = new URL(dirUrl, relativeURL);
                FileUtils.copyURLToFile(absoluteUrl, new File(outputDir, relativeURL));
            }
        }
    }

    /**
     * This method performs an "ls" on a web directory to list the names or urls of its content.
     *
     * @param dirUrl A valid web directory URL
     * @return
     * @throws IOException
     */
    private Set<String> listWebDirectoryContent(URL dirUrl) throws IOException {
        Set<String> result = new HashSet<String>();

        //Load the directory listing page and extract all the links it contains.
        Document doc = Jsoup.connect(dirUrl.toString()).get();
        for (Element file : doc.select("a[href]")) {
            String relativeURL = file.attr("href");
            result.add(relativeURL);
        }
        return result;
    }

    /**
     * This method browses a web directory and all its subdirectories and returns a set containing all the urls of their contents.
     *
     * @param dirUrl
     * @param cummulativeRelativeUrl a string used for keeping track of a directory structure in the recursive context. MUST BE empty ("") in the first call.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private Set<String> listWebDirectoryContent(URL dirUrl, String cummulativeRelativeUrl)
            throws IOException, URISyntaxException {
        Set<String> result = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        //Load the directory listing page and extract all the links it contains.
        Document doc = Jsoup.connect(dirUrl.toString()).get();
        logger.info("Listing directories from: " + doc.location());
        for (Element file : doc.select("a[href]")) {
            String relativeURL = file.attr("href");
            // skip sort urls in Apache Tomcat
            if (relativeURL.startsWith("?")) {
                continue;
            }
            // skip parent directory url
            if (isRelativeParentDirectoryUrl(relativeURL)) {
                continue;
            }
            result.add(cummulativeRelativeUrl + relativeURL);
        }

        for (String relativeURL : result) {
            URL absoluteUrl = new URL(dirUrl, relativeURL);
            if (!isFileURL(relativeURL)) {
                result.addAll(listWebDirectoryContent(absoluteUrl, relativeURL));
            }
        }
        return result;
    }

    /**
     * This method throws an exception if the given web directory doesn't contain a valid package.<br>
     * Package validation relies on the existence of a METADATA.json and a resources/ directory.
     *
     * @param packageUrl a valid web directory URL
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void validateWebDirectoryContent(URL packageUrl) throws IllegalArgumentException, IOException {
        Set<String> resourceContents = listWebDirectoryContent(packageUrl);
        if (!(resourceContents.contains(METADATA_FILENAME) && resourceContents.contains(RESOURCE_DIRECTORYNAME))) {
            logger.warn(packageUrl +
                        " is not a valid URL of a proactive package as it does not contain the METADATA.json and the resources folder required for installation.");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' is not a valid URL of a proactive package as it does not contain the METADATA.json and the resources folder required for installation.",
                                                 packageUrl));
        }
    }

    /**
     * This method transforms a github url to an svn-compatible url in order to allow svn operations on github repositories.
     * @param githubUrl
     * @return
     */
    private String transformGithubURLToSvnURL(String githubUrl) {
        String svnUrl;

        //convert a github repository archive Url to a github repositor Url
        githubUrl = githubUrl.replaceAll("/archive/[^\\s/]+\\.zip$", "");

        Pattern pattern = Pattern.compile(REGX_GITHUB_REPO_CONTENT);
        Matcher matcher = pattern.matcher(githubUrl);
        if (githubUrl.matches(REGX_GITHUB_REPO_CONTENT) && matcher.find()) {
            if (matcher.group(4) != null && matcher.group(4).matches("(tree/[^\\s/]+)")) {
                svnUrl = githubUrl.replace(matcher.group(4), "trunk/");

            } else {
                svnUrl = matcher.group(1).concat("/trunk");
            }
            //remove wwww. from svn URL otherwise it doesn't work
            if (matcher.group(2) != null) {
                svnUrl = svnUrl.replace(matcher.group(2), "");
            }
            return svnUrl;
        } else {
            logger.warn(githubUrl + " is not a valid github URL.");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' is not a valid github URL.", githubUrl));
        }
    }

    /**
     * Returns true if the given URL refers to a github.com URL (repository or else inc pulls, issues, etc...)
     *
     * @param url
     * @return
     */
    private boolean isGithubURL(String url) {
        return url.matches(REGX_GITHUB_URL);
    }

    /**
     * Returns true if URL is reachable, false if the server returns a non-OK code (!=200),
     * throws an IOException if the server is unreachable<br>
     *
     * @param url
     * @return
     * @throws IOException
     */
    private void checkReachableURL(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // This exception will be managed in the calling method to avoid management redundancy.
            throw new IOException();
        }
    }

    /**
     * This method returns the real URL if the given URL forwards to another one. Otherwise it returns the given URL.
     *
     * @param url
     * @return
     * @throws IOException
     */
    private URL getRealURLIfForwarded(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        return con.getHeaderField("Location") != null ? new URL(con.getHeaderField("Location")) : url;
    }

    /**
     *  This method returns a normalized given url by adding the http protocol if it doesn't exist
     * @param packageUrl
     * @return
     * @throws MalformedURLException
     */

    private String createNormalizeURL(String packageUrl) throws MalformedURLException {
        if (!packageUrl.matches("^((http|https|ftp)://).*")) {
            packageUrl = "http://" + packageUrl;
        }
        return (packageUrl);
    }

    /**
     * Returns true if a relative URL in some context refers to a parent folder.<br>
     * Example: /parent becomes http://example.com/parent <br>
     * child instead becomes http://example.com/parent/child
     *
     * @param relativeURL
     * @return
     */
    private boolean isRelativeParentDirectoryUrl(String relativeURL) {
        return relativeURL.startsWith("/");
    }

    /**
     * Returns true if the given URL refers to a file (any extension) or false if it refers to a directory
     *
     * @param url
     * @return
     */
    private boolean isFileURL(String url) {
        return url.matches(REGX_FILE_URL);
    }

    /**
     * Returns true if the given URL refers to a compressed file (zip or tar.gz)
     *
     * @param url
     * @return
     */
    private boolean isCompressedPackage(String url) {
        return url.matches(REGX_ARCHIVE_URL);
    }
}
