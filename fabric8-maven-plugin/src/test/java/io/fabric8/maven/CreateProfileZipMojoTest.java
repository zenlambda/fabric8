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

        // GIVEN

        pomWithJarPackaging();

        CreateProfileZipMojo mojo = createProfileZipMojoWithBasicConfig();

        // WHEN

        artifactBundleTypeIsOverridden(mojo);

        mojo.execute();

        // THEN

        bundleReferencesHaveZipExtension();

    }

    public void testDefaultType() throws Exception {

        // GIVEN

        pomWithJarPackaging();

        // WHEN

        createProfileZipMojoWithBasicConfig().execute();

        // THEN

        bundleReferencesHaveJarExtension();

    }

    private void bundleReferencesHaveJarExtension() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        String value = props.getProperty(getArtifactBundleKey("jar"));

        Assert.assertEquals(getExpectedArtifactBundleValue("jar"), value);
    }

    private void bundleReferencesHaveZipExtension() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        String value = props.getProperty(getArtifactBundleKey("zip"));

        Assert.assertEquals(getExpectedArtifactBundleValue("zip"), value);
    }

    private CreateProfileZipMojo createProfileZipMojoWithBasicConfig() throws Exception {
        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", getPom());

        assertNotNull(profileZipMojo);

        setVariableValueToObject(profileZipMojo,"buildDir", getGeneratedProfilesDir());

        setVariableValueToObject(profileZipMojo,"outputFile", getProfileZip());

        return profileZipMojo;
    }

    private void artifactBundleTypeIsOverridden(CreateProfileZipMojo mojo) throws IllegalAccessException {
        setVariableValueToObject(mojo, "artifactBundleType", "zip");
    }

    private void pomWithJarPackaging() {
        Assert.assertEquals("jar", getPackaging());

        Assert.assertEquals("jar", getArtifactType());
    }

    // helpers...

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

    private String getArtifactType() {
        return projectStub.getArtifact().getType();
    }

    private String getPackaging() {
        return projectStub.getPackaging();
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