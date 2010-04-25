package org.codehaus.mojo.cobertura.module;

import net.sourceforge.cobertura.util.RegexUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.cobertura.configuration.ConfigInstrumentation;
import org.codehaus.mojo.cobertura.util.ZipUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allow to unpack dependencies of a maven project in the outputDir.
 * Dependencies to include/exclude are specified in the instrumentation configuration
 *
 * @author Anthonin Bonnefoy
 */
public class DependenciesModuleManager
{

    public void unpackModuleDependencies(MavenProject project, File outputDirectory, ConfigInstrumentation configInstrumentation)
    {
        final Set artifacts = project.getArtifacts();
        final List includesArtifactId = configInstrumentation.getIncludesArtifactId();
        final List includesGroupId = configInstrumentation.getIncludesGroupId();
        final List excludesGroupId = configInstrumentation.getExcludesGroupId();
        final List excludesArtifactId = configInstrumentation.getExcludesArtifactId();

        for (Object object : artifacts)
        {
            final Artifact artifact = (Artifact) object;
            final File file = artifact.getFile();
            if (includesGroupId.size() == 0)
            {
                continue;
            }
            if (!matchListRegex(includesGroupId, artifact.getGroupId()))
            {
                continue;
            }
            if (includesArtifactId.size() > 0)
            {
                if (!matchListRegex(includesArtifactId, artifact.getArtifactId()))
                {
                    continue;
                }
            }
            if (excludesGroupId.size() > 0)
            {
                if (matchListRegex(excludesGroupId, artifact.getGroupId()))
                {
                    continue;
                }
            }
            if (excludesArtifactId.size() > 0)
            {
                if (matchListRegex(excludesArtifactId, artifact.getArtifactId()))
                {
                    continue;
                }
            }
            ZipUtil.unzipArchive(file, outputDirectory);
        }
    }

    public boolean matchListRegex(List listStringPattern, String toMatch)
    {
        final HashSet setPattern = new HashSet();
        for (Object regexp : listStringPattern)
        {
            RegexUtil.addRegex(setPattern, (String) regexp);
        }
        return RegexUtil.matches(setPattern, toMatch);
    }


}

