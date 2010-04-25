package org.codehaus.mojo.cobertura;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.number.IsGreaterThan;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
public class CoberturaConsolidateMultiModuleItTest
{

    /**
     * Maven request to invoke
     */
    private DefaultInvocationRequest request;

    /**
     * Invoker of maven request
     */
    private DefaultInvoker invoker;

    @Before
    public void initInvokers() throws IOException
    {
        invoker = new DefaultInvoker();
        request = new DefaultInvocationRequest();
    }

    @Test
    public void consolidateShouldGenerateFullSer() throws Exception
    {
        final File coberturaWorkingDir = initPomAndBaseDir();
//        request.setMavenOpts("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");

        request.setGoals(Arrays.asList("cobertura:cobertura-consolidate"));
        invoker.execute(request);

        checkInstrumentedTarget(coberturaWorkingDir);

        final File globalSer = new File(coberturaWorkingDir, "target/cobertura/consolidate-cobertura.ser");
        assertThat(globalSer.exists(), Is.is(true));
        final ProjectData globalData = CoverageDataFileHandler.loadCoverageData(globalSer);
        for (Object classesObject : globalData.getClasses())
        {
            ClassData classData = (ClassData) classesObject;
            System.out.println(classData.getName());
            System.out.println(classData.getLineCoverageRate());
            assertThat(classData.getLineCoverageRate(), Is.is(1D));
        }

        final File moduleASer = new File(coberturaWorkingDir, "moduleA/target/cobertura/consolidate-cobertura.ser");
        final ProjectData moduleAdata = CoverageDataFileHandler.loadCoverageData(moduleASer);
        final Collection<ClassData> moduleAclasses = moduleAdata.getClasses();
        assertThat(moduleAclasses.size(), Is.is(1));

        final ClassData aClassData = moduleAclasses.iterator().next();
        assertThat(aClassData.getLineCoverageRate(), new IsGreaterThan<Double>(0.7D));
    }

    private File initPomAndBaseDir()
            throws URISyntaxException, IOException
    {
        File source = new File(getClass().getClassLoader().getResource("mulimodule/").toURI());
        File pomFile = new File(source, "pom.xml");

        final File coberturaWorkingDir = UtilityTestClass.copyProjectToTemp(source);

        request.setBaseDirectory(coberturaWorkingDir);
        request.setPomFile(pomFile);
        return coberturaWorkingDir;
    }

    @Test
    public void coberturaSonarShouldInvokeAllModules() throws Exception
    {
        final File coberturaWorkingDir = initPomAndBaseDir();
        request.setGoals(Arrays.asList("cobertura:cobertura-sonar"));
        invoker.execute(request);

        final File moduleASer = new File(coberturaWorkingDir, "moduleA/target/cobertura/consolidate-cobertura.ser");
        final ProjectData moduleAdata = CoverageDataFileHandler.loadCoverageData(moduleASer);
        final Collection<ClassData> moduleAclasses = moduleAdata.getClasses();
        assertThat(moduleAclasses.size(), Is.is(1));
    }

    private void checkInstrumentedTarget(File coberturaWorkingDir)
    {
        final File file = new File(coberturaWorkingDir, "moduleB/target/classes/izpack");
        assertThat(file.list(), IsArrayContaining.hasItemInArray(StringContains.containsString("AClass.class")));

        final File generatedClasses = new File(coberturaWorkingDir, "moduleB/target/generated-classes/cobertura/izpack");
        assertThat(generatedClasses.list(), IsArrayContaining.hasItemInArray(StringContains.containsString("AClass.class")));

        final File generatedClassesTarget = new File(coberturaWorkingDir, "moduleB/target/generated-classes/cobertura/");
        assertThat(generatedClassesTarget.list(), IsNot.not(IsArrayContaining.hasItemInArray(StringContains.containsString("junit"))));
    }

}
