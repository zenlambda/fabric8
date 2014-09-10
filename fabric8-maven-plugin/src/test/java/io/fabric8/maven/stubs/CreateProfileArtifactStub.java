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

    private String packaging;

    private VersionRange versionRange;

    private ArtifactHandler handler;

    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @param packaging
     */
    public CreateProfileArtifactStub( String groupId, String artifactId,
                                          String version, String packaging )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        versionRange = VersionRange.createFromVersion( version );
    }

    /** {@inheritDoc} */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /** {@inheritDoc} */
    public String getGroupId()
    {
        return groupId;
    }

    /** {@inheritDoc} */
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    /** {@inheritDoc} */
    public String getArtifactId()
    {
        return artifactId;
    }

    /** {@inheritDoc} */
    public void setVersion( String version )
    {
        this.version = version;
    }

    /** {@inheritDoc} */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param packaging
     */
    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }

    /**
     * @return the packaging
     */
    public String getPackaging()
    {
        return packaging;
    }

    /** {@inheritDoc} */
    public VersionRange getVersionRange()
    {
        return versionRange;
    }

    /** {@inheritDoc} */
    public void setVersionRange( VersionRange versionRange )
    {
        this.versionRange = versionRange;
    }

    /** {@inheritDoc} */
    public ArtifactHandler getArtifactHandler()
    {
        return handler;
    }

    /** {@inheritDoc} */
    public void setArtifactHandler( ArtifactHandler handler )
    {
        this.handler = handler;
    }
}
