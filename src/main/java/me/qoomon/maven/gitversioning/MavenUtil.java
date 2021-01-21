package me.qoomon.maven.gitversioning;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;

/**
 * Created by qoomon on 18/11/2016.
 */
final class MavenUtil {

    /**
     * Read model from pom file
     *
     * @param pomFile pomFile
     * @return Model
     * @throws IOException IOException
     */
    static Model readModel(File pomFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(pomFile)) {
            Model model = new MavenXpp3Reader().read(inputStream);
            model.setPomFile(pomFile);
            return model;
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes model to pom file
     *
     * @param pomFile pomFile
     * @param model   model
     * @throws IOException IOException
     */
    static void writeModel(File pomFile, Model model) throws IOException {
        try (FileWriter fileWriter = new FileWriter(pomFile)) {
            new MavenXpp3Writer().write(fileWriter, model);
        }
    }

    /**
     * Builds pom file location from relative path
     *
     * @param workingDirectory current working directory
     * @param relativePath     pom file path or directory of pom.xml
     * @return pom.xml file
     */
    public static File pomFile(File workingDirectory, String relativePath) {
        File modulePomFile = new File(workingDirectory, relativePath);
        if (modulePomFile.isDirectory()) {
            modulePomFile = new File(modulePomFile, "pom.xml");
        }
        return modulePomFile;
    }

}
