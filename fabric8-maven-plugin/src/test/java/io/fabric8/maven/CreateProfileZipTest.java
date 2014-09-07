package io.fabric8.maven;


import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * This test uses JUnit3 API because the Junit4 API for
 * maven plugin test framework depends on eclipse aether, but we use
 * sonatype aether.
 */
public class CreateProfileZipTest extends AbstractMojoTestCase {

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
        String pom = "src/test/resources/unit/zip-test/pom.xml";


        CreateProfileZipMojo myMojo = (CreateProfileZipMojo) lookupMojo( "zip", pom );
        assertNotNull( myMojo );

        myMojo.execute();
        System.out.println("***************GOODBYE MAVEN WORLD!****************");
    }

}