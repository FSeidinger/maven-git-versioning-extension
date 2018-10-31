package me.qoomon.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class BuildProperties {

    private static final String FILE_PATH = "mavenBuild.properties";

    private static final Properties PROPERTIES = loadProperties();

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = BuildProperties.class.getClassLoader().getResource(FILE_PATH).openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public static String value(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static String projectGroupId() {
        return PROPERTIES.getProperty("project.groupId");
    }

    public static String projectArtifactId() {
        return PROPERTIES.getProperty("project.artifactId");
    }

    public static String projectVersion() {
        return PROPERTIES.getProperty("project.version");
    }
}