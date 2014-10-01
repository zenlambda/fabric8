package io.fabric8.maven;

import io.fabric8.maven.probes.AttachZipMojo;
import io.fabric8.maven.probes.ForkedProjectAttachmentsProbe;
import io.fabric8.maven.stubs.CreateProfileZipMuleProjectStub;
import io.fabric8.maven.stubs.ForkedProjectAttachmentsProjectStub;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.Assert;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ForkedTestValidatorTest extends AbstractMojoTestCase {

    private ForkedProjectAttachmentsProjectStub projectStub;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNoApparentAttachmentsWhenMojoForksLifecycle() throws Exception {

        pomWithMulePackaging();

        AttachZipMojo secondaryArtifactAttachment =
                (AttachZipMojo) configureMojo(new AttachZipMojo(), "fabric8-maven-plugin", getPomFile());

        secondaryArtifactAttachment.execute();

        ForkedProjectAttachmentsProbe forkedAttachmentsProbe = (ForkedProjectAttachmentsProbe)
                configureMojo(new ForkedProjectAttachmentsProbe(), "fabric8-maven-plugin", getPomFile());

        forkedAttachmentsProbe.execute();
        assertEquals(0,forkedAttachmentsProbe.getProjectAttachedArtifacts().size());
        assertEquals(1, forkedAttachmentsProbe.getExecutedProjectAttachedArtifacts().size());
    }

    private void pomWithMulePackaging() throws Exception {
        projectStub = new ForkedProjectAttachmentsProjectStub();

    }

    private String getPom() {
        return projectStub.getFile().toString();
    }

    private File getPomFile() {
       return new File(getPom());
    }

}
