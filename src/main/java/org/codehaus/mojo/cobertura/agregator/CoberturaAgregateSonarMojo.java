package org.codehaus.mojo.cobertura.agregator;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.mojo.cobertura.AbstractCoberturaMojo;

import java.util.Arrays;
import java.util.List;

/**
 * Launch complete cobertura analysis (clean, Instrument, test, consolidation and report) on all modules independently.<br />
 * It invokes the clean and cobertura:cobertura-consolidate goals on the project when maven pass on the first module.
 *
 * @goal cobertura-sonar
 */
public class CoberturaAgregateSonarMojo
        extends AbstractCoberturaMojo
{
    /**
     * List of maven project of the current build
     *
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    protected List<MavenProject> reactorProjects;

    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private Invoker invoker;

    public void execute()
            throws MojoExecutionException
    {
        if (isFirst(project, reactorProjects))
        {
            DefaultInvocationRequest request = new DefaultInvocationRequest();
            request.setBaseDirectory(project.getBasedir());
            request.setPomFile(project.getFile());
            request.setProfiles(project.getActiveProfiles());
            request.setProperties(project.getProperties());
            request.setGoals(Arrays.asList("clean", "org.codehaus.mojo:cobertura-maven-plugin:2.5:cobertura-consolidate"));
            try
            {
                invoker.execute(request);
            }
            catch (MavenInvocationException e)
            {
                getLog().error("Error when running cobertura-consolidate", e);
            }
        }
    }

    private boolean isFirst(MavenProject project, List<MavenProject> reactorProjects)
    {
        return reactorProjects.get(0).equals(project);
    }

}