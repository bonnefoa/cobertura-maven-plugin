package org.codehaus.mojo.cobertura;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.mojo.cobertura.stubs.ArtifactStub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base TestCase for all Cobertura Tests.
 *
 * @author Joakim Erdfelt
 */
public abstract class AbstractCoberturaTestCase
        extends AbstractMojoTestCase
{
    protected void assertArtifactExists(Artifact artifact)
    {
        if (artifact == null)
        {
            fail("Artifact is null.");
        }

        if (artifact.getFile() == null)
        {
            fail("Artifact.file is not defined for " + artifact);
        }

        if (!artifact.getFile().exists())
        {
            fail("Artifact " + artifact.getFile().getAbsolutePath() + " file does not exist.");
        }
    }

    protected List<Artifact> getPluginClasspath()
            throws Exception
    {
        String localRepository = System.getProperty("localRepository");
        if (localRepository == null)
        {
            localRepository = System.getenv("MVN_REPO");
        }

        assertNotNull("System.property(localRepository) should not be null.", localRepository);

        List<Artifact> pluginClasspath = new ArrayList<Artifact>();

        Artifact artifact;

        artifact =
                createArtifact("net.sourceforge.cobertura", "cobertura", "1.9.4.1", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("log4j", "log4j", "1.2.9", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("org.apache.ant", "ant", "1.7.0", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("org.apache.ant", "ant-launcher", "1.7.0", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("oro", "oro", "2.0.8", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("asm", "asm", "3.0", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("asm", "asm-tree", "3.0", localRepository, "jar");
        pluginClasspath.add(artifact);

        artifact =
                createArtifact("net.sourceforge.cobertura", "cobertura-runtime", "1.9.4.1", localRepository, "pom");
        pluginClasspath.add(artifact);

        return pluginClasspath;
    }

    private Artifact createArtifact(String groupId, String artifactId, String version, String localRepository, String packaging)
    {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setGroupId(groupId);
        artifact.setArtifactId(artifactId);
        artifact.setVersion(version);
        String fullPath = localRepository +
                String.format("/%s/%s/%s/%s-%s.%s", groupId.replaceAll("\\.", "/"), artifactId, version, artifactId, version, packaging);
        artifact.setFile(new File(fullPath));
        assertArtifactExists(artifact);
        return artifact;
    }

}
