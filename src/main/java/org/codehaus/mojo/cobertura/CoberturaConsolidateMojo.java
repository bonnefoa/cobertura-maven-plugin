package org.codehaus.mojo.cobertura;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.cobertura.util.JavaFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gather cobertura coverage information through modules to create a global ser file.
 * This global ser file is written in {agregator-module}/target/cobertura/consolidate-cobertura.ser<br />
 * Moreover, each cobertura files in each modules will be updated with coverage information from other modules.
 * Thus, tests from other modules can participate in the coverage of others modules.
 *
 * @author Anthonin Bonnefoy
 * @goal consolidate
 */
public class CoberturaConsolidateMojo
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
     * <p>
     * The consolidated datafile destination.
     * </p>
     *
     * @parameter expression="${cobertura.consolidateDataFileName}" default-value="target/cobertura/consolidate-cobertura.ser"
     * @required
     * @readonly
     */
    protected String consolidateDataFileName;


    /**
     * The global cobertura datafiles. <br />
     * In case of a multi-module project, this file is written in {agregator-module}/target/cobertura/consolidate-cobertura.ser.
     */
    private File globalDataFile;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (reactorProjects.size() <= 1)
        {
            getLog().info("The project is not a multi-module project, skipping cobertura consolidation...");
            return;
        }
        File baseDir = reactorProjects.get(0).getBasedir();
        globalDataFile = new File(baseDir, "target/cobertura/consolidate-cobertura.ser");
        List<MavenProject> mavenProjects = removeAgregator(reactorProjects);
        if (isLastProject(project, mavenProjects))
        {
            mergeModuleDataFiles(globalDataFile, mavenProjects);
            consolidateCobertura(mavenProjects, globalDataFile);
        }
    }

    /**
     * Filter the reactorProject list to remove agregator and parent from the list
     *
     * @param reactorProjects List of all maven project
     * @return Filtered list
     */
    private List<MavenProject> removeAgregator(List<MavenProject> reactorProjects)
    {
        ArrayList<MavenProject> result = new ArrayList<MavenProject>();
        for (MavenProject reactorProject : reactorProjects)
        {
            if (!isMultiModule(reactorProject))
            {
                result.add(reactorProject);
            }
        }
        return result;
    }

    /**
     * Test if the project has pom packaging
     *
     * @param mavenProject Project to test
     * @return True if it has a pom packaging
     */
    private boolean isMultiModule(MavenProject mavenProject)
    {
        return mavenProject.getModel().getPackaging().equals("pom");
    }

    /**
     * Check whether the element is the last element of the list
     *
     * @param project          element to check
     * @param mavenProjectList list of maven project
     * @return true if project is the last element of mavenProjectList  list
     */
    private boolean isLastProject(MavenProject project, List<MavenProject> mavenProjectList)
    {
        return project.equals(mavenProjectList.get(mavenProjectList.size() - 1));
    }

    /**
     * Merge all cobertura datafile in a single cobertura file
     *
     * @param globalDataFile  destination of the merge operation
     * @param reactorProjects List of cobertura data file to merge
     */
    public void mergeModuleDataFiles(File globalDataFile, List<MavenProject> reactorProjects)
    {
        ProjectData globalProjectData = new ProjectData();
        globalDataFile.getParentFile().mkdirs();

        Iterable<? extends File> serFiles = getAllSerFiles(reactorProjects);
        for (File serFile : serFiles)
        {
            final ProjectData data = CoverageDataFileHandler.loadCoverageData(serFile);
            globalProjectData.merge(data);
        }
        getLog().info("Saving global cobertura information in " + globalDataFile.getAbsolutePath());
        CoverageDataFileHandler.saveCoverageData(globalProjectData, globalDataFile);
    }

    /**
     * Gather all ser files from the project list
     *
     * @param mavenProjectList list of maven project
     * @return List of ser files situated in project's target
     */
    private Iterable<? extends File> getAllSerFiles(List<MavenProject> mavenProjectList)
    {
        ArrayList<File> listSerFiles = new ArrayList<File>();
        for (MavenProject reactorProject : mavenProjectList)
        {
            File fileForProject = getSerFileForProject(reactorProject);
            if (fileForProject.exists())
            {
                getLog().info("Adding " + fileForProject + " for cobertura consolidation");
                listSerFiles.add(fileForProject);
            }
        }
        return listSerFiles;
    }

    /**
     * Get the cobertura ser file for a specific maven project
     *
     * @param mavenProject a maven project
     * @return The cobertura ser file contained in the target of the project (target/cobertura/cobertura.ser)
     */
    public File getSerFileForProject(MavenProject mavenProject)
    {
        File baseDir = mavenProject.getBasedir();
        return new File(baseDir, "target/cobertura/cobertura.ser");
    }

    /**
     * Rewrite the ser file of each project with coverage information from the global ser file.<br />
     * A new coverage data is written from the global file by cloning it and filtering it to keep only informations
     * from classes contained by the project.
     *
     * @param projectList    List of maven project to process
     * @param globalDataFile Location of the global ser file
     */
    public void consolidateCobertura(List<MavenProject> projectList, File globalDataFile)
    {
        for (MavenProject project : projectList)
        {
            File consolidateDataFile = getConsolidatedFile(project);
            consolidateDataFile.delete();
            getLog().info("Consolidate project " + project.getName());
            final List<String> classNameList = JavaFileUtil.listProjectClassFiles(project);
            final ProjectData globalData = CoverageDataFileHandler.loadCoverageData(globalDataFile);
            final ProjectData moduleData = filterCoberturaSerWithClassName(globalData, classNameList);
            getLog().info("Project " + project.getName() + " has line coverage to " + moduleData.getLineCoverageRate() + ", saving file in " + consolidateDataFile);
            CoverageDataFileHandler.saveCoverageData(moduleData, consolidateDataFile);
        }
    }

    public File getConsolidatedFile(MavenProject project)
    {
        return new File(project.getBasedir(), this.consolidateDataFileName);
    }

    /**
     * Filter the projectData with the class name list.
     *
     * @param globalSer     The global cobertura data to clone
     * @param classListName The class name list to use to filter the cobertura data
     * @return Cobertura data with only className given
     */
    public ProjectData filterCoberturaSerWithClassName(ProjectData globalSer, List<String> classListName)
    {
        final ProjectData resultProjectData = new ProjectData();
        for (String className : classListName)
        {
            final ClassData classData = globalSer.getClassData(className);
            resultProjectData.addClassData(classData);
        }
        return resultProjectData;
    }

    public void setConsolidateDataFileName(String consolidateDataFileName)
    {
        this.consolidateDataFileName = consolidateDataFileName;
    }
}
