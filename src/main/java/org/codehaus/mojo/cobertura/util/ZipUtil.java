package org.codehaus.mojo.cobertura.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class to unzip a file
 *
 * @author Anthonin Bonnefoy
 */
public class ZipUtil
{

    /**
     * Unzip a file
     *
     * @param archive   zip source to uncompress
     * @param outputDir destination directory
     */
    public static void unzipArchive(File archive, File outputDir)
    {
        try
        {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements();)
            {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException
    {

        if (entry.isDirectory())
        {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists())
        {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try
        {
            IOUtils.copy(inputStream, outputStream);
        }
        finally
        {
            outputStream.close();
            inputStream.close();
        }
    }

    private static void createDir(File dir)
    {
        if (!dir.exists())
        {
            if (!dir.mkdirs())
            {
                throw new RuntimeException("Can not create dir " + dir);
            }
        }
    }


}
