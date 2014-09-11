package io.fabric8.maven.stubs;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;

public class CreateProfileArtifactStub
        extends ArtifactStub
{
    private String groupId;

    private String artifactId;

    private String version;

    private VersionRange versionRange;

    private ArtifactHandler handler;

    public CreateProfileArtifactStub( String groupId, String artifactId,
                                          String version, String type )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        setType(type);
        versionRange = VersionRange.createFromVersion( version );
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public VersionRange getVersionRange()
    {
        return versionRange;
    }

    public void setVersionRange( VersionRange versionRange )
    {
        this.versionRange = versionRange;
    }

    public ArtifactHandler getArtifactHandler()
    {
        return handler;
    }

    public void setArtifactHandler( ArtifactHandler handler )
    {
        this.handler = handler;
    }
}
