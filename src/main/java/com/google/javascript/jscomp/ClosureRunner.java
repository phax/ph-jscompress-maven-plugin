/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CompilerOptions.JsonStreamMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.deps.ClosureBundler;
import com.google.javascript.jscomp.deps.ModuleLoader;
import com.google.javascript.jscomp.parsing.parser.FeatureSet.Feature;
import com.google.javascript.jscomp.transpile.BaseTranspiler;
import com.google.javascript.jscomp.transpile.BaseTranspiler.CompilerSupplier;
import com.google.javascript.jscomp.transpile.Transpiler;

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
  protected List <SourceFile> createExterns (final CompilerOptions options) throws FlagUsageException, IOException
  {
    final List <SourceFile> externs = super.createExterns (options);
    // Use the default externs provided by the Closure CommandLineRunner
    final List <SourceFile> defaultExterns = AbstractCommandLineRunner.getBuiltinExterns (options.getEnvironment ());
    defaultExterns.addAll (externs);
    return defaultExterns;
  }

  @Override
  protected CompilerOptions createOptions ()
  {
    // TODO make language configurable
    final LanguageMode eJSLanguage = LanguageMode.ECMASCRIPT5;

    final CompilerOptions options = new CompilerOptions ();
    options.setCodingConvention (new ClosureCodingConvention ());
    options.setLanguageIn (eJSLanguage);
    options.setLanguageOut (eJSLanguage);

    // Optimizations:
    final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel (options);
    if (DEBUG)
      level.setDebugOptionsForCompilationLevel (options);

    if (GENERATE_EXPORTS)
      options.setGenerateExports (GENERATE_EXPORTS);

    WarningLevel.QUIET.setOptionsForWarningLevel (options);
    options.setPrettyPrint (false);
    options.setPrintInputDelimiter (false);
    options.setClosurePass (false);
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
                           .setJsOutputFile ("")
                           .setJsonStreamMode (JsonStreamMode.NONE)
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
                           .setOutputManifest (Lists.<String> newArrayList ());
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

      final List <String> aExternList = new ArrayList <> ();
      for (final File f : aExterns)
        aExternList.add (f.getAbsolutePath ());

      _setDefaultConfig ();

      // Since v20160315 setJs has no effect
      final List <FlagEntry <JsSourceType>> aSources = new ArrayList <> ();
      aSources.add (new FlagEntry <> (JsSourceType.JS, aSourceFile.getAbsolutePath ()));
      getCommandLineConfig ().setExterns (aExternList)
                             .setMixedJsSources (aSources)
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

  private ClosureBundler m_aBundler;

  private ClosureBundler _getBundler ()
  {
    if (m_aBundler == null)
    {
      final ImmutableList <String> moduleRoots = ImmutableList.of (ModuleLoader.DEFAULT_FILENAME_PREFIX);
      final CompilerOptions options = createOptions ();
      m_aBundler = new ClosureBundler (Transpiler.NULL,
                                       new BaseTranspiler (new CompilerSupplier (LanguageMode.ECMASCRIPT_NEXT.toFeatureSet ()
                                                                                                             .without (Feature.MODULES),
                                                                                 options.getModuleResolutionMode (),
                                                                                 moduleRoots,
                                                                                 options.getBrowserResolverPrefixReplacements ()),
                                                           /*
                                                            * runtimeLibraryName=
                                                            */ ""));
    }
    return m_aBundler;
  }

  @Override
  protected void prepForBundleAndAppendTo (final Appendable aOut,
                                           final CompilerInput aInput,
                                           final String aContent) throws IOException
  {
    _getBundler ().withPath (aInput.getName ()).appendTo (aOut, aInput, aContent);
  }

  @Override
  protected void appendRuntimeTo (final Appendable out) throws IOException
  {
    _getBundler ().appendRuntimeTo (out);
  }
}
