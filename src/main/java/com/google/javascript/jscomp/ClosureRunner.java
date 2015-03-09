/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.google.javascript.jscomp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;

import com.google.common.collect.Lists;

/**
 * The main class running the Closure compiler. It must reside in this package,
 * as AbstractCommandLineRunner has only package visibility.
 *
 * @author Philip Helger
 */
public final class ClosureRunner extends AbstractCommandLineRunner <Compiler, CompilerOptions>
{
  private final Log m_aLog;
  private final String m_sCharset;

  private static final boolean DEBUG = false;
  private static final boolean GENERATE_EXPORTS = false;
  private final CompilationLevel m_eLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;

  public ClosureRunner (@Nonnull final Log aLog, @Nonnull final String sCharset)
  {
    if (aLog == null)
      throw new NullPointerException ("log");
    if (sCharset == null)
      throw new NullPointerException ("charset");
    m_aLog = aLog;
    m_sCharset = sCharset;
  }

  @Override
  protected void addWhitelistWarningsGuard (@Nonnull final CompilerOptions aOptions, @Nonnull final File aWhitelistFile)
  {
    aOptions.addWarningsGuard (WhitelistWarningsGuard.fromFile (aWhitelistFile));
  }

  @Override
  protected Compiler createCompiler ()
  {
    return new Compiler (System.err);
  }

  @Override
  protected List <SourceFile> createExterns () throws FlagUsageException, IOException
  {
    final List <SourceFile> externs = super.createExterns ();
    // Use the default externs provided by the Closure CommandLineRunner
    final List <SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns ();
    defaultExterns.addAll (externs);
    return defaultExterns;
  }

  @Override
  protected CompilerOptions createOptions ()
  {
    final CompilerOptions options = new CompilerOptions ();
    options.setCodingConvention (new ClosureCodingConvention ());
    final CompilationLevel level = m_eLevel;
    level.setOptionsForCompilationLevel (options);
    if (DEBUG)
      level.setDebugOptionsForCompilationLevel (options);

    if (GENERATE_EXPORTS)
      options.setGenerateExports (GENERATE_EXPORTS);

    WarningLevel.QUIET.setOptionsForWarningLevel (options);
    options.prettyPrint = false;
    options.printInputDelimiter = false;
    options.closurePass = false;
    return options;
  }

  private void _setDefaultConfig ()
  {
    getCommandLineConfig ().setPrintTree (false)
                           .setPrintAst (false)
                           .setPrintPassGraph (false)
                           .setJscompDevMode (CompilerOptions.DevMode.OFF)
                           .setLoggingLevel (Level.WARNING.getName ())
                           .setExterns (Lists.<String> newArrayList ())
                           .setJs (Lists.<String> newArrayList ())
                           .setJsOutputFile ("")
                           .setModule (Lists.<String> newArrayList ())
                           .setVariableMapInputFile ("")
                           .setPropertyMapInputFile ("")
                           .setVariableMapOutputFile ("")
                           .setCreateNameMapFiles (false)
                           .setPropertyMapOutputFile ("")
                           .setCodingConvention (true ? CodingConventions.getDefault ()
                                                     : new ClosureCodingConvention ())
                           .setSummaryDetailLevel (1)
                           .setOutputWrapper ("")
                           .setModuleWrapper (Lists.<String> newArrayList ())
                           .setModuleOutputPathPrefix ("./")
                           .setCreateSourceMap ("")
                           .setDefine (Lists.<String> newArrayList ())
                           .setCharset (m_sCharset)
                           .setManageClosureDependencies (false)
                           .setClosureEntryPoints (Lists.<String> newArrayList ())
                           .setOutputManifest (Lists.<String> newArrayList ())
                           .setAcceptConstKeyword (false)
                           .setLanguageIn ("ECMASCRIPT5");
  }

  public boolean compressJSFile (@Nonnull final File aSourceFile,
                                 @Nonnull final File aDestFile,
                                 @Nonnull final File [] aExterns)
  {
    if (aSourceFile == null)
      throw new NullPointerException ("sourceFile");

    try
    {
      // Build result file name
      m_aLog.info ("Compressing JS " + aSourceFile.getName () + " to " + aDestFile.getName ());

      final List <String> aExternList = new ArrayList <String> ();
      for (final File f : aExterns)
        aExternList.add (f.getAbsolutePath ());

      _setDefaultConfig ();
      getCommandLineConfig ().setExterns (aExternList)
                             .setJs (Lists.newArrayList (aSourceFile.getAbsolutePath ()))
                             .setJsOutputFile (aDestFile.getAbsolutePath ());

      final int nErrors = doRun ();
      if (nErrors == 0)
        return true;
      m_aLog.error ("Failed to compress JS with " + nErrors + " errors: " + aSourceFile.getName ());
    }
    catch (final Exception ex)
    {
      m_aLog.error ("Failed to compress JS " + aSourceFile.toString (), ex);
    }
    return false;
  }
}
