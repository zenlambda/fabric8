package io.fabric8.maven.probes;

import io.fabric8.maven.AbstractProfileMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL;

@Mojo(name = "nofork-attachments", defaultPhase = INSTALL)
public class ProjectAttachmentsProbe extends AbstractProfileMojo {

    @Component
    private MavenProject project;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
