package org.codehaus.mojo.cobertura;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/**
 * @author Anthonin Bonnefoy (vfkc3065)
 */
public class UtilityTestClass
{
    public static File copyProjectToTemp(File sourceDirectory) throws IOException
    {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        final File destination = temporaryFolder.newFolder("coberturaWorkingDirectory");
        FileUtils.copyDirectory(sourceDirectory, destination);
        return destination;
    }
}
