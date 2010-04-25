package org.codehaus.mojo.cobertura;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.model.Reporting;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.codehaus.plexus.PlexusTestCase;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

/**
 * @author Edwin Punzalan
 */
public class CoberturaReportMojoTest
        extends AbstractCoberturaTestCase
{
    public void testReport()
            throws Exception
    {
        CoberturaReportMojo reportMojo =
                (CoberturaReportMojo) lookupMojo("report", PlexusTestCase.getBasedir() + "/src/test/plugin-configs/report-plugin-config.xml");

        File outputHtml = new File(reportMojo.getReportOutputDirectory(), reportMojo.getOutputName() + ".html");
        initializeMojoValue(reportMojo, outputHtml.getAbsolutePath());

        reportMojo.execute();


        assertTrue("Test for generated html file " + outputHtml, outputHtml.exists());
    }

    public void testReportEmptySourceDir()
            throws Exception
    {
        AbstractMavenReport mojo = (AbstractMavenReport) lookupMojo("report", PlexusTestCase.getBasedir() +
                "/src/test/plugin-configs/report-empty-src-plugin-config.xml");

        initializeMojoValue(mojo, null);

        assertFalse("Should not be able to generate a report", mojo.canGenerateReport());
    }

    private void initializeMojoValue(AbstractMavenReport mojo, String outputDirectory)
            throws Exception
    {
        setVariableValueToObject(mojo, "pluginClasspathList", getPluginClasspath());

        MavenProject mavenProjectStub = Mockito.mock(MavenProject.class);
        Mockito.when(mavenProjectStub.getBasedir()).thenReturn(new File(PlexusTestCase.getBasedir()));

        Reporting reporting = new Reporting();
        reporting.setOutputDirectory(outputDirectory);
        Mockito.when(mavenProjectStub.getReporting()).thenReturn(reporting);

        setVariableValueToObject(mojo, "useConsolidated", false);
        setVariableValueToObject(mojo, "reactorProjects", Arrays.asList(mavenProjectStub));
        setVariableValueToObject(mojo, "project", mavenProjectStub);
    }
}
