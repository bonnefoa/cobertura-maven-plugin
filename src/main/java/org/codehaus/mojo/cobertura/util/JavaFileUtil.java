package org.codehaus.mojo.cobertura.util;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility classes for listing java files and converting them to java name classes
 *
 * @author Anthonin Bonnefoy
 */
public class JavaFileUtil
{

    /**
     * List all java class name in a maven project
     *
     * @param project Maven project to analyze
     * @return List of java class name
     */
    public static List<String> listProjectClassFiles(MavenProject project)
    {
        final List<String> sourceRootsList = project.getCompileSourceRoots();
        final List<String> stringList = new ArrayList<String>();
        for (String sourceRoot : sourceRootsList)
        {
            final File sourceDir = new File(sourceRoot);
            listJavaFiles(sourceDir, sourceDir, stringList);
        }
        return stringList;
    }

    /**
     * List all java class names in the given root
     *
     * @param sourceDir   The source root used to deduce java class name
     * @param currentFile The current file or directory to analyze
     * @param result      Placeholder for java class names
     * @return List of java class names
     */
    public static List<String> listJavaFiles(File sourceDir, File currentFile, List<String> result)
    {
        final File[] currentList = currentFile.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() || pathname.getName().endsWith(".java");
            }
        }
        );
        if (currentList == null)
        {
            return result;
        }
        for (File file : currentList)
        {
            if (file.isDirectory())
            {
                listJavaFiles(sourceDir, file, result);
            }
            else
            {
                result.add(convertJavaPathToClass(sourceDir, file));
            }
        }
        return result;
    }

    /**
     * Convert a path to a java file to a java class name
     *
     * @param sourceDir The source root to package
     * @param file      Java file to convert
     * @return Java class name
     */
    public static String convertJavaPathToClass(File sourceDir, File file)
    {
        String javaPath = StringUtils.remove(file.getAbsolutePath(), sourceDir.getAbsolutePath());
        return javaPath.replaceAll(".java", "")
                .replaceAll("\\\\", ".").replaceAll("/", ".").replaceAll("^\\.", "");
    }
}
