package org.codehaus.mojo.cobertura.tasks;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Base Abstract Class for all of the Tasks.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public abstract class AbstractTask
{
    protected CommandLineArguments cmdLineArgs;

    private Log log;

    private String maxmem;

    private List pluginClasspathList;

    private String taskClass;

    private boolean quiet;

    /**
     * Initialize AbstractTask.
     *
     * @param taskClassname the classname for the task.
     */
    protected AbstractTask( String taskClassname )
    {
        this.taskClass = taskClassname;
        this.cmdLineArgs = new CommandLineArguments();
        this.maxmem = "64m";
    }

    /**
     * Setter for <code>quiet</code>.
     * @param quiet The quiet to set.
     */
    public void setQuiet( boolean quiet )
    {
        this.quiet = quiet;
    }

    /**
     * Using the <code>${project.compileClasspathElements}</code> and the
     * <code>${plugin.artifacts}</code>, create a classpath string that is
     * suitable to be used from a forked cobertura process.
     *
     * @return the classpath string
     * @throws MojoExecutionException if the pluginArtifacts cannot be properly
     *                                resolved to a full system path.
     */
    public String createClasspath()
        throws MojoExecutionException
    {

        StringBuffer cpBuffer = new StringBuffer();

        for ( Iterator it = pluginClasspathList.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            try
            {
                cpBuffer.append( File.pathSeparator ).append( artifact.getFile().getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "Error while creating the canonical path for '" + artifact.getFile() + "'.", e );
            }
        }

        return cpBuffer.toString();
    }

    private String getLog4jConfigFile()
    {
        String resourceName = "cobertura-plugin/log4j-info.properties";
        if ( getLog().isDebugEnabled() )
        {
            resourceName = "cobertura-plugin/log4j-debug.properties";
        }
        if ( quiet )
        {
            resourceName = "cobertura-plugin/log4j-error.properties";
        }

        String path = null;
        try
        {
            File log4jconfigFile = File.createTempFile( "log4j", "config.properties" );
            URL log4jurl = this.getClass().getClassLoader().getResource( resourceName );
            FileUtils.copyURLToFile( log4jurl, log4jconfigFile );
            log4jconfigFile.deleteOnExit();
            path = log4jconfigFile.toURL().toExternalForm();
        }
        catch ( MalformedURLException e )
        {
            // ignore
        }
        catch ( IOException e )
        {
            // ignore
        }
        return path;
    }

    public abstract void execute()
        throws MojoExecutionException;

    protected int executeJava()
        throws MojoExecutionException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        cl.createArgument().setValue( "-cp" );
        cl.createArgument().setValue( createClasspath() );

        String log4jConfig = getLog4jConfigFile();
        if ( log4jConfig != null )
        {
            cl.createArgument().setValue( "-Dlog4j.configuration=" + log4jConfig );
        }

        cl.createArgument().setValue( "-Xmx" + maxmem );

        cl.createArgument().setValue( this.taskClass );

        if ( this.cmdLineArgs.useCommandsFile() )
        {
            cl.createArgument().setValue( "--commandsfile" );
            try
            {
                String commandsFile = this.cmdLineArgs.getCommandsFile();
                cl.createArgument().setValue( commandsFile );
                FileUtils.copyFile( new File( commandsFile ), new File( commandsFile + ".bak" ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to obtain CommandsFile location.", e );
            }
        }
        else
        {
            Iterator it = this.cmdLineArgs.iterator();
            while ( it.hasNext() )
            {
                cl.createArgument().setValue( it.next().toString() );
            }
        }

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLog().debug( "Working Directory: " + cl.getWorkingDirectory() );
        getLog().debug( "Executing command line:" );
        getLog().debug( cl.toString() );

        int exitCode;
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Unable to execute Cobertura.", e );
        }

        getLog().debug( "exit code: " + exitCode );

        getLog().debug( "--------------------" );
        getLog().debug( " Standard output from the Cobertura task:" );
        getLog().debug( "--------------------" );
        getLog().info( stdout.getOutput() );
        getLog().debug( "--------------------" );

        String stream = stderr.getOutput();

        if ( stream.trim().length() > 0 )
        {
            getLog().debug( "--------------------" );
            getLog().debug( " Standard error from the Cobertura task:" );
            getLog().debug( "--------------------" );
            getLog().error( stderr.getOutput() );
            getLog().debug( "--------------------" );
        }

        return exitCode;
    }

    public CommandLineArguments getCmdLineArgs()
    {
        return cmdLineArgs;
    }

    public Log getLog()
    {
        if ( log == null )
        {
            log = new SystemStreamLog();
        }

        return log;
    }

    public String getMaxmem()
    {
        return maxmem;
    }

    /**
     * @return Returns the pluginClasspathList.
     */
    public List getPluginClasspathList()
    {
        return pluginClasspathList;
    }

    public void setCmdLineArgs( CommandLineArguments cmdLineArgs )
    {
        this.cmdLineArgs = cmdLineArgs;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

    public void setMaxmem( String maxmem )
    {
        this.maxmem = maxmem;
    }

    /**
     * @param pluginClasspathList The pluginClasspathList to set.
     */
    public void setPluginClasspathList( List pluginClasspathList )
    {
        this.pluginClasspathList = Collections.unmodifiableList( pluginClasspathList );
    }
}
