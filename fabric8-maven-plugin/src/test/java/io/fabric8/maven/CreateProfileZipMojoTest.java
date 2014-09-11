package io.fabric8.maven;


import io.fabric8.maven.stubs.CreateProfileZipProjectStub;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
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

    private CreateProfileZipProjectStub projectStub;

    protected void setUp() throws Exception {
        super.setUp();
        projectStub = new CreateProfileZipProjectStub();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOverrideType() throws Exception {

        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", getPom());

        assertNotNull( profileZipMojo );

        setVariableValueToObject(profileZipMojo,"buildDir", getGeneratedProfilesDir());

        File profileZip = getProfileZip();

        setVariableValueToObject(profileZipMojo,"outputFile", profileZip);

        setVariableValueToObject(profileZipMojo, "artifactBundleType", "zip");

        profileZipMojo.execute();

        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        String value = props.getProperty(getArtifactBundleKey("zip"));

        Assert.assertEquals(getExpectedArtifactBundleValue("zip"),value);

    }

    private File getProfileZip() {
        return new File(getBasedir() + "/target/profile.zip");
    }

    private File getGeneratedProfilesDir() {
        return new File(getBasedir() + "/target/generated-profiles");
    }

    private String getExpectedArtifactBundleValue(String type) {
        return "fab:mvn:" + getBundleSpec(type);
    }

    private String getArtifactBundleKey(String type) {
        return "bundle.fab:mvn:" + getBundleSpec(type);
    }

    private String getBundleSpec(String type) {
        return getGroupId() + "/" + getArtifactId() + "/" + getVersion() + "/" +type;
    }

    private String getProfilePathComponent() {
        // profilePathComponent: looks like: io.fabric8.maven.test/zip/test.profile
        return getGroupId() + "/" +
                getArtifactId().replace('-', '/') + ".profile";
    }

    private String getVersion() {
        return projectStub.getVersion();
    }

    private String getGroupId() {
        return projectStub.getGroupId();
    }

    public void testDefaultType() throws Exception {

        String bundleSpec = getBundleSpec("jar");
        String artifactBundleKey = getArtifactBundleKey("jar");
        String expectedArtifactBundleValue = getExpectedArtifactBundleValue("jar");
        File generatedProfiles = getGeneratedProfilesDir();
        File fabricAgentPropertiesFile = getFabricAgentPropertiesFile(generatedProfiles);

        Assert.assertEquals("jar",projectStub.getPackaging());

        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", getPom());

        assertNotNull( profileZipMojo );

        setVariableValueToObject(profileZipMojo,"buildDir", generatedProfiles);

        setVariableValueToObject(profileZipMojo,"outputFile", getProfileZip());

        profileZipMojo.execute();

        Properties props = loadProperties(fabricAgentPropertiesFile);

        String value = props.getProperty(artifactBundleKey);

        Assert.assertEquals(expectedArtifactBundleValue,value);

    }

    private File getFabricAgentPropertiesFile(File generatedProfiles) {
        return new File(generatedProfiles, getProfilePathComponent() +
                "/io.fabric8.agent.properties");
    }

    private String getPom() {
        return projectStub.getFile().toString();
    }

    private String getArtifactId() {
        return projectStub.getArtifactId();
    }

    private Properties loadProperties(File fabricAgentPropertiesFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(fabricAgentPropertiesFile));
        return props;
    }

}