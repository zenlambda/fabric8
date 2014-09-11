package io.fabric8.maven;


import io.fabric8.maven.stubs.CreateProfileZipProjectStub;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * This test uses JUnit3 API because the Junit4 API for
 * maven plugin test framework depends on eclipse aether, but we use
 * sonatype aether.
 */
public class CreateProfileZipMojoTest extends AbstractMojoTestCase {

    /** {@inheritDoc} */
    protected void setUp()
            throws Exception
    {
        // required
        super.setUp();

    }

    /** {@inheritDoc} */
    protected void tearDown()
            throws Exception
    {
        // required
        super.tearDown();

    }


    public void testSomething()
            throws Exception
    {
        System.out.println("***************HELLO MAVEN WORLD!******************");
        System.out.println(getBasedir());

        CreateProfileZipProjectStub projectStub = new CreateProfileZipProjectStub();

        String pom = projectStub.getFile().toString();

        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", pom );

        assertNotNull( profileZipMojo );

        File generatedProfiles = new File(getBasedir() + "/target/generated-profiles");

        setVariableValueToObject(profileZipMojo,"outputFile",new File(getBasedir() + "/target/profile.zip"));

        setVariableValueToObject(profileZipMojo, "artifactBundleType", "zip");

        profileZipMojo.execute();

        Properties props = new Properties();

        props.load(new FileInputStream(getBasedir()+"/target/generated-profiles/io.fabric8.maven.test/zip/test.profile/io.fabric8.agent.properties"));

        String value = props.getProperty("bundle.fab:mvn:io.fabric8.maven.test/zip-test/0.0.1-SNAPSHOT/zip");

        System.out.println(props.keySet());

        Assert.assertEquals("fab:mvn:io.fabric8.maven.test/zip-test/0.0.1-SNAPSHOT/zip",value);


        System.out.println("***************GOODBYE MAVEN WORLD!****************");
    }

}