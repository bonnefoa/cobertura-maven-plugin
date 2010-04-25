package org.codehaus.mojo.cobertura;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.cobertura.util.JavaFileUtil;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
public class CoberturaConsolidateMojoTest
{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private CoberturaConsolidateMojo coberturaConsolidateMojo;

    @Before
    public void setUp()
    {
        coberturaConsolidateMojo = new CoberturaConsolidateMojo();
    }

    @Test
    public void testListProjectClassFiles() throws Exception
    {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        final File targetClasses = new File("");
        final File srcJavaSource = new File(targetClasses.getAbsolutePath(), "src/main/java");
        Mockito.when(mavenProject.getCompileSourceRoots()).thenReturn(Arrays.asList(srcJavaSource.getAbsolutePath()));
        final List<String> stringList = JavaFileUtil.listProjectClassFiles(mavenProject);
        assertThat(stringList, IsCollectionContaining.hasItem(CoberturaConsolidateMojo.class.getName()));
    }


    @Test
    public void testConsolidateData() throws Exception
    {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        Mockito.when(mavenProject.getCompileSourceRoots()).thenReturn(
                Arrays.asList(ClassLoader.getSystemResource("mulimodule/moduleA/src/main/java").getFile())
        );
        Mockito.when(mavenProject.getBasedir()).thenReturn(new File(
                ClassLoader.getSystemResource("mulimodule/").getFile()
        ));
        File sourceFile = new File(getClass().getClassLoader().getResource("mulimodule/moduleAAndB.ser").toURI());
        coberturaConsolidateMojo.setConsolidateDataFileName("target/multimodule/destination.ser");
        File createdConsolidateData = coberturaConsolidateMojo.getConsolidatedFile(mavenProject);
        coberturaConsolidateMojo.consolidateCobertura(Collections.singletonList(mavenProject)
                , sourceFile);
        final ProjectData moduleAData = CoverageDataFileHandler.loadCoverageData(createdConsolidateData);
        assertThat(moduleAData.getClasses().size(), Is.is(1));
    }
}
