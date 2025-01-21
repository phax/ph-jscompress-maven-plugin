/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.maven.jscompress;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.google.javascript.jscomp.ClosureRunner;

/**
 * @author Philip Helger
 * @goal jscompress
 * @phase generate-resources
 * @description Compress existing JS file using the Google Closure compressor.
 */
public final class JSCompressMojo extends AbstractMojo
{
  private static final String EXTENSION_JS = ".js";
  private static final String [] EXTENSIONS_JS_COMPRESSED = new String [] { ".min.js", "-min.js", ".minified.js", "-minified.js" };

  /**
   * The Maven Project.
   *
   * @parameter property="project"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The directory where the JS files reside. It must be an existing directory.
   *
   * @required
   * @parameter property="sourceDirectory"
   *            default-value="${basedir}/src/main/resources"
   */
  private File sourceDirectory;

  /**
   * Should the system properties be emitted as well?
   *
   * @parameter property="recursive" default-value="true"
   */
  private boolean recursive = true;

  /**
   * The encoding of the source JS files.
   *
   * @parameter property="sourceEncoding" default-value="UTF-8"
   */
  private String sourceEncoding = "UTF-8";

  /**
   * The extension that should be supplied to the minified/compressed JS file.
   *
   * @parameter property="targetFileExtension" default-value=".min.js"
   */
  private String targetFileExtension = ".min.js";

  /**
   * Should the system properties be emitted as well?
   *
   * @parameter property="forceCreation" default-value="false"
   */
  private boolean forceCreation = false;

  public void setProject (final MavenProject aProject)
  {
    project = aProject;
  }

  public void setSourceDirectory (final File aDir)
  {
    sourceDirectory = aDir;
    if (!sourceDirectory.isAbsolute ())
      sourceDirectory = new File (project.getBasedir (), aDir.getPath ());
    if (!sourceDirectory.exists ())
      getLog ().error ("JS source directory '" + sourceDirectory + "' does not exist!");
  }

  public void setRecursive (final boolean bRecursive)
  {
    recursive = bRecursive;
  }

  public void setSourceEncoding (final String sSourceEncoding)
  {
    sourceEncoding = sSourceEncoding;
  }

  public void setTargetFileExtension (final String sTargetFileExtension)
  {
    targetFileExtension = sTargetFileExtension;
  }

  public void setForceCreation (final boolean bForceCreation)
  {
    forceCreation = bForceCreation;
  }

  /**
   * Check if the passed file is already compressed. The check is only done
   * using the file extension of the file name.
   *
   * @param sFilename
   *        The filename to be checked.
   * @return <code>true</code> if the file is already compressed.
   */
  private static boolean _isAlreadyCompressed (final String sFilename)
  {
    for (final String sExt : EXTENSIONS_JS_COMPRESSED)
      if (sFilename.endsWith (sExt))
        return true;
    return false;
  }

  @Nullable
  public static String getWithoutExtension (@Nullable final String sFilename)
  {
    if (sFilename == null)
      return null;

    final int nExtensionIndex = sFilename.lastIndexOf ('.');
    final int nLastSepIndex = Math.max (sFilename.lastIndexOf ('/'), sFilename.lastIndexOf ('\\'));
    final int nIndex = nLastSepIndex > nExtensionIndex ? -1 : nExtensionIndex;
    return nIndex == -1 ? sFilename : sFilename.substring (0, nIndex);
  }

  private void _compressJSFile (@Nonnull final File aChild, @Nonnull final ClosureRunner aRunner)
  {
    // Compress the file only if the compressed file is older than the original
    // file. Note: lastModified on a non-existing file returns 0L
    final File aCompressed = new File (getWithoutExtension (aChild.getAbsolutePath ()) + targetFileExtension);
    if (forceCreation || aCompressed.lastModified () < aChild.lastModified ())
    {
      getLog ().debug ("Start " + (forceCreation ? "forced " : "") + "compressing JS file " + aChild.toString ());
      aRunner.compressJSFile (aChild, aCompressed, new File [0]);
    }
    else
      getLog ().debug ("Ignoring already compressed JS file " + aChild.toString ());
  }

  private void _scanDirectory (@Nonnull final File aDir, @Nonnull final ClosureRunner aRunner)
  {
    final File [] aChildren = aDir.listFiles ();
    if (aChildren != null)
      for (final File aChild : aChildren)
      {
        if (aChild.isDirectory ())
        {
          // Shall we recurse into sub-directories?
          if (recursive)
            _scanDirectory (aChild, aRunner);
        }
        else
          if (aChild.isFile () && aChild.getName ().endsWith (EXTENSION_JS) && !_isAlreadyCompressed (aChild.getName ()))
          {
            // We're ready to rumble!
            _compressJSFile (aChild, aRunner);
          }
      }
  }

  public void execute () throws MojoExecutionException
  {
    final ClosureRunner aRunner = new ClosureRunner (getLog (), sourceEncoding);
    _scanDirectory (sourceDirectory, aRunner);
  }
}
