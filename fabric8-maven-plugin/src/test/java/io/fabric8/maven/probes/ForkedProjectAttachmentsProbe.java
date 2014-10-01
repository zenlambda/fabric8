package io.fabric8.maven.probes;

import io.fabric8.maven.AbstractProfileMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL;

@Mojo(name = "forked-attachments", defaultPhase = INSTALL)
@Execute(phase = LifecyclePhase.INSTALL)
public class ForkedProjectAttachmentsProbe extends AbstractProfileMojo {

    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${executedProject}")
    private MavenProject executedProject;

    private List<Artifact> projectAttachedArtifacts;

    private List<Artifact> executedProjectAttachedArtifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        projectAttachedArtifacts = project.getAttachedArtifacts();

        executedProjectAttachedArtifacts = executedProject.getAttachedArtifacts();
    }

    public List<Artifact> getProjectAttachedArtifacts() {
        return projectAttachedArtifacts;
    }

    public List<Artifact> getExecutedProjectAttachedArtifacts() {
        return executedProjectAttachedArtifacts;
    }
}
