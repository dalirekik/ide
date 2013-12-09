/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.sdk.tools;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * Collection of utility methods.
 *
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: Utils.java Jul 31, 2013 11:30:14 AM azatsarynnyy $
 */
class Utils {
    /** Maven POM reader. */
    private static MavenXpp3Reader pomReader = new MavenXpp3Reader();
    /** Maven POM writer. */
    private static MavenXpp3Writer pomWriter = new MavenXpp3Writer();

    private Utils() {
    }

    /**
     * Read pom.xml.
     *
     * @param path
     *         pom.xml path
     * @return a project object model
     * @throws java.io.IOException
     *         error occurred while reading content of file
     */
    static Model readPom(Path path) throws IOException {
        return readPom(Files.newInputStream(path));
    }

    /**
     * Read pom.xml.
     *
     * @param stream
     *         input stream that represents a pom.xml
     * @return a project object model
     * @throws java.io.IOException
     *         error occurred while reading content of file
     */
    static Model readPom(InputStream stream) throws IOException {
        try {
            return pomReader.read(stream, true);
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Error occurred while parsing pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * Write provided project object model to the specified path.
     *
     * @param pom
     *         a project object model
     * @param path
     *         path to pom.xml
     * @throws java.io.IOException
     *         error occurred while writing content of file
     */
    static void writePom(Model pom, Path path) throws IOException {
        pomWriter.write(Files.newOutputStream(path), pom);
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param path
     *         pom.xml path
     * @param groupId
     *         groupId
     * @param artifactId
     *         artifactId
     * @param version
     *         artifact version
     * @throws java.io.IOException
     *         error occurred while reading or writing content of file
     */
    static void addDependencyToPom(Path path, String groupId, String artifactId, String version) throws IOException {
        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);

        Model pom = readPom(path);
        pom.getDependencies().add(dep);

        writePom(pom, path);
    }

    /**
     * Add the specified module name as a dependency to the provided GWT module descriptor.
     *
     * @param path
     *         GWT module descriptor
     * @param inheritableModuleLogicalName
     *         logical name of the GWT module to inherit
     * @throws java.io.IOException
     *         error occurred while reading or writing content of file
     */
    static void inheritGwtModule(Path path, String inheritableModuleLogicalName) throws IOException {
        final String inheritsString = "    <inherits name='" + inheritableModuleLogicalName + "'/>";
        List<String> content = Files.readAllLines(path, UTF_8);
        // insert custom module as last 'inherits' entry
        int i = 0, lastInheritsLine = 0;
        for (String str : content) {
            i++;
            if (str.contains("<inherits")) {
                lastInheritsLine = i;
            }
        }
        content.add(lastInheritsLine, inheritsString);
        Files.write(path, content, UTF_8);
    }

    /**
     * Detects and returns {@code Path} to file by name pattern.
     *
     * @param pattern
     *         file name pattern
     * @param folder
     *         path to folder that contains project sources
     * @return pom.xml path
     * @throws java.io.IOException
     *         if an I/O error is thrown while finding pom.xml
     * @throws IllegalArgumentException
     *         if pom.xml not found
     */
    static Path findFile(String pattern, Path folder) throws IOException {
        Finder finder = new Finder(pattern);
        Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, finder);
        if (finder.getFirstMatchedFile() == null) {
            throw new IllegalArgumentException("File not found.");
        }
        return finder.getFirstMatchedFile();
    }

    /** A {@code FileVisitor} that finds first file that match the specified pattern. */
    private static class Finder extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;
        private       Path        firstMatchedFile;

        Finder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        /** {@inheritDoc} */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path fileName = file.getFileName();
            if (fileName != null && matcher.matches(fileName)) {
                firstMatchedFile = file;
                return TERMINATE;
            }
            return CONTINUE;
        }

        /** Returns the first matched {@link java.nio.file.Path}. */
        Path getFirstMatchedFile() {
            return firstMatchedFile;
        }
    }
}