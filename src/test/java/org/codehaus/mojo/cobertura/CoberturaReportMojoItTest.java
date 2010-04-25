package org.codehaus.mojo.cobertura;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
public class CoberturaReportMojoItTest
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
    public void testExecution() throws Exception
    {
        File source = new File(getClass().getClassLoader().getResource("report/").toURI());
        File pomFile = new File(source, "pom.xml");

        final File coberturaWorkingDir = UtilityTestClass.copyProjectToTemp(source);

        request.setBaseDirectory(coberturaWorkingDir);
        request.setPomFile(pomFile);
//        request.setMavenOpts("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
        request.setGoals(Arrays.asList("cobertura:cobertura", "site"));
        invoker.execute(request);

        final File globalSer = new File(coberturaWorkingDir, "target/cobertura/cobertura.ser");
        assertThat(globalSer.exists(), Is.is(true));
        final File coberturaSite = new File(coberturaWorkingDir, "target/site/cobertura/index.html");
        assertThat(coberturaSite.exists(), Is.is(true));
    }

}