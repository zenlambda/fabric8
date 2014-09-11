package io.fabric8.maven;


import io.fabric8.maven.stubs.CreateProfileZipProjectStub;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This test uses JUnit3 API because the Junit4 API for
 * maven plugin test framework depends on eclipse aether, but we use
 * sonatype aether.
 */
public class CreateProfileZipMojoTest extends AbstractMojoTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOverrideType() throws Exception {

        CreateProfileZipProjectStub projectStub = new CreateProfileZipProjectStub();
        String groupId = projectStub.getGroupId();
        String artifactId = projectStub.getArtifactId();
        String version = projectStub.getVersion();
        String pom = projectStub.getFile().toString();
        // profilePathComponent: looks like: io.fabric8.maven.test/zip/test.profile
        String profilePathComponent = groupId + "/" +
                artifactId.replace('-', '/') + ".profile";
        String bundleSpec = groupId + "/" + artifactId + "/" + version + "/zip";
        String artifactBundleKey = "bundle.fab:mvn:" + bundleSpec;
        String expectedArtifactBundleValue = "fab:mvn:" + bundleSpec;
        File generatedProfiles = new File(getBasedir() + "/target/generated-profiles");
        File fabricAgentPropertiesFile = new File(generatedProfiles, profilePathComponent +
                "/io.fabric8.agent.properties");

        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", pom );

        assertNotNull( profileZipMojo );

        setVariableValueToObject(profileZipMojo,"buildDir", generatedProfiles);

        File profileZip = new File(getBasedir() + "/target/profile.zip");

        setVariableValueToObject(profileZipMojo,"outputFile", profileZip);

        setVariableValueToObject(profileZipMojo, "artifactBundleType", "zip");

        profileZipMojo.execute();

        Properties props = loadProperties(fabricAgentPropertiesFile);

        String value = props.getProperty(artifactBundleKey);

        Assert.assertEquals(expectedArtifactBundleValue,value);

    }

    public void testDefaultType() throws Exception {

        CreateProfileZipProjectStub projectStub = new CreateProfileZipProjectStub();
        String groupId = projectStub.getGroupId();
        String artifactId = projectStub.getArtifactId();
        String version = projectStub.getVersion();
        String pom = projectStub.getFile().toString();
        // profilePathComponent: looks like: io.fabric8.maven.test/zip/test.profile
        String profilePathComponent = groupId + "/" +
                artifactId.replace('-', '/') + ".profile";
        String bundleSpec = groupId + "/" + artifactId + "/" + version + "/jar";
        String artifactBundleKey = "bundle.fab:mvn:" + bundleSpec;
        String expectedArtifactBundleValue = "fab:mvn:" + bundleSpec;
        File generatedProfiles = new File(getBasedir() + "/target/generated-profiles");
        File fabricAgentPropertiesFile = new File(generatedProfiles, profilePathComponent +
                "/io.fabric8.agent.properties");

        Assert.assertEquals("jar",projectStub.getPackaging());

        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", pom );

        assertNotNull( profileZipMojo );

        setVariableValueToObject(profileZipMojo,"buildDir", generatedProfiles);

        File profileZip = new File(getBasedir() + "/target/profile.zip");

        setVariableValueToObject(profileZipMojo,"outputFile", profileZip);

//        setVariableValueToObject(profileZipMojo, "artifactBundleType", "zip");

        profileZipMojo.execute();

        Properties props = loadProperties(fabricAgentPropertiesFile);

        String value = props.getProperty(artifactBundleKey);

        Assert.assertEquals(expectedArtifactBundleValue,value);

    }

    private Properties loadProperties(File fabricAgentPropertiesFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(fabricAgentPropertiesFile));
        return props;
    }

}