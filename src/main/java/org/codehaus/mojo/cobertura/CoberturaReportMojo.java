package org.codehaus.mojo.cobertura;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.doxia.siterenderer.DefaultSiteRenderer;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.cobertura.tasks.ReportTask;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Generates a Cobertura Report.
 *
 * @author <a href="will.gwaltney@sas.com">Will Gwaltney</a>
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @goal report
 */
public class CoberturaReportMojo
        extends AbstractMavenReport
{

    private static String COBERTURA_CONSOLIDATED_FILENAME = "target/cobertura/consolidate-cobertura.ser";

    /**
     * The format of the report. (supports 'html' or 'xml'. defaults to 'html')
     *
     * @parameter expression="${cobertura.report.format}"
     * @deprecated
     */
    private String format;

    /**
     * The format of the report. (can be 'html' and/or 'xml'. defaults to 'html')
     *
     * @parameter
     */
    private String[] formats = new String[]{"html"};

    /**
     * The encoding for the java source code files.
     *
     * @parameter expression="${project.build.sourceEncoding}" default-value="UTF-8".
     * @since 2.4
     */
    private String encoding;

    /**
     * Maximum memory to pass to JVM of Cobertura processes.
     *
     * @parameter expression="${cobertura.maxmem}"
     */
    private String maxmem = "64m";

    /**
     * List of maven project of the current build
     *
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    protected List<MavenProject> reactorProjects;

    /**
     * <p>
     * The Datafile Location.
     * </p>
     */
    protected File dataFile;
    /**
     * <i>Maven Internal</i>: List of artifacts for the plugin.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List pluginClasspathList;

    /**
     * The output filename for the report relative to the module.
     *
     * @parameter default-value="${project.reporting.outputDirectory}/cobertura"
     * @required
     */
    private String outputDirectory;

    /**
     * Only output cobertura errors, avoid info messages.
     *
     * @parameter expression="${quiet}" default-value="false"
     * @since 2.1
     */
    private boolean quiet;

    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * <p>
     * Use the consolidated cobertura coverage for report generation.
     * </p>
     *
     * @parameter expression="${cobertura.useConsolidated}" default-value="false"
     * @required
     */
    private boolean useConsolidated;

    /**
     * <p>
     * The relative name of the cobertura ser file
     * </p>
     *
     * @parameter expression="${cobertura.dataFileName}" default-value="target/cobertura/cobertura.ser"
     * @required
     */
    private String dataFileName;

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale)
    {
        return getBundle(locale).getString("report.cobertura.name");
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale)
    {
        return getBundle(locale).getString("report.cobertura.description");
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return new File(outputDirectory).getAbsolutePath();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject()
    {
        return project;
    }

    @Override
    protected Renderer getSiteRenderer()
    {
        return new DefaultSiteRenderer();
    }

    @Override
    public void generate(Sink sink, Locale locale) throws MavenReportException
    {
        executeReport(locale);
    }

    private void executeReportTask(ReportTask task, String format)
            throws MavenReportException
    {
        task.setOutputFormat(format);

        // execute task
        try
        {
            task.execute();
        }
        catch (MojoExecutionException e)
        {
            // throw new MavenReportException( "Error in Cobertura Report generation: " + e.getMessage(), e );
            // better don't break the build if report is not generated, also due to the sporadic MCOBERTURA-56
            getLog().error("Error in Cobertura Report generation: " + e.getMessage(), e);
        }
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    protected void executeReport(Locale locale)
            throws MavenReportException
    {
        if (!project.equals(reactorProjects.get(reactorProjects.size() - 1)))
        {
            return;
        }
        for (MavenProject reactorProject : reactorProjects)
        {
            if (useConsolidated)
            {
                dataFileName = COBERTURA_CONSOLIDATED_FILENAME;
            }
            dataFile = new File(reactorProject.getBasedir(), dataFileName);
            getLog().info("Using coverage data of file " + dataFile.getAbsolutePath());

            if (!canGenerateReport())
            {
                continue;
            }
            ReportTask task = new ReportTask();

            // task defaults
            task.setLog(getLog());
            task.setPluginClasspathList(pluginClasspathList);
            task.setQuiet(quiet);

            // task specifics
            task.setMaxmem(maxmem);
            task.setDataFile(dataFile);

            File moduleReportOutputDirectory = new File(reactorProject.getReporting().getOutputDirectory(), "cobertura");
            task.setOutputDirectory(moduleReportOutputDirectory);
            task.setCompileSourceRoots(reactorProject.getCompileSourceRoots());
            task.setSourceEncoding(encoding);

            if (format != null)
            {
                formats = new String[]{format};
            }
            getLog().info("Generating report in " + moduleReportOutputDirectory.getAbsolutePath());
            for (String format1 : formats)
            {
                executeReportTask(task, format1);
            }
        }

    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "index";
    }

    public boolean isExternalReport()
    {
        return true;
    }

    public boolean canGenerateReport()
    {
        /*
         * Don't have to check for source directories or java code or the like for report generation. Checks for source
         * directories or java project classpath existence should only occur in the Instrument Mojo.
         */
        if (dataFile == null || !dataFile.exists())
        {
            getLog().info(
                    "Not executing cobertura:report as the cobertura data file (" + dataFile
                            + ") could not be found");
            return false;
        }
        else
        {
            return true;
        }
    }

    private List getCompileSourceRoots()
    {
        MavenProject executionProject = project.getExecutionProject();
        if (executionProject == null)
        {
            return Collections.emptyList();
        }
        return executionProject.getCompileSourceRoots();
    }

    /**
     * Gets the resource bundle for the report text.
     *
     * @param locale The locale for the report, must not be <code>null</code>.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle(Locale locale)
    {
        return ResourceBundle.getBundle("cobertura-report", locale, getClass().getClassLoader());
    }

}
