package io.fabric8.maven.probes;

import io.fabric8.maven.AbstractProfileMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

@Mojo(name = "attach-zip", defaultPhase = PACKAGE)
public class AttachZipMojo extends AbstractProfileMojo {

    @Component
    private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputDir = new File(project.getBuild().getOutputDirectory());
        if(!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir,project.getArtifactId() + "-" + project.getVersion() + ".zip");
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry("dummy"));
            zos.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("unexpected error opening dummy zip file, perhaps it is a directory",e);
        } catch (IOException e) {
            throw new MojoExecutionException("unexpected io exception while creating dummy zip file");
        }


        this.projectHelper.attachArtifact(this.project, "zip", outputFile);
    }
}
