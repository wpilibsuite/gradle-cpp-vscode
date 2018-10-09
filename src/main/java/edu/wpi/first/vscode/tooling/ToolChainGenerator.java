package edu.wpi.first.vscode.tooling;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.GsonBuilder;

import org.gradle.api.file.FileCollection;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.c.CSourceSet;
import org.gradle.language.cpp.CppSourceSet;
import org.gradle.language.nativeplatform.HeaderExportingSourceSet;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.VisualCppPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioInstall;
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolConfigurationInternal;
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult;
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath;
import org.gradle.platform.base.internal.toolchain.SearchResult;

import edu.wpi.first.vscode.VsCodeConfigurationExtension;
import edu.wpi.first.vscode.tooling.models.BinaryObject;
import edu.wpi.first.vscode.tooling.models.SourceBinaryPair;
import edu.wpi.first.vscode.tooling.models.SourceSet;
import edu.wpi.first.vscode.tooling.models.ToolChains;

public class ToolChainGenerator {
  private static String normalizeDriveLetter(String path) {
    if (OperatingSystem.current().isWindows() && path.charAt(1) == ':') {
      return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
    return path;
  }

  public static String generateToolChains(VsCodeConfigurationExtension ext) {
    Set<ToolChains> toolChains = new LinkedHashSet<>();

    Map<Class<? extends NativeDependencySet>, Method> depClasses = new HashMap<>();

    for (NativeBinarySpec bin : ext._binaries) {
      if (!bin.isBuildable()) {
        continue;
      }

      BinaryObject bo = new BinaryObject();

      Set<String> libSources = new LinkedHashSet<>();

      for (LanguageSourceSet sSet : bin.getInputs()) {
        if (sSet instanceof HeaderExportingSourceSet) {
          if (!(sSet instanceof CppSourceSet) && !(sSet instanceof CSourceSet)) {
            continue;
          }
          HeaderExportingSourceSet hSet = (HeaderExportingSourceSet) sSet;
          SourceSet s = new SourceSet();

          for (File f : hSet.getSource().getSrcDirs()) {
            s.source.srcDirs.add(normalizeDriveLetter(f.toString()));
          }

          s.source.includes.addAll(hSet.getSource().getIncludes());
          s.source.excludes.addAll(hSet.getSource().getExcludes());

          for (File f : hSet.getExportedHeaders().getSrcDirs()) {
            s.exportedHeaders.srcDirs.add(normalizeDriveLetter(f.toString()));
          }

          s.exportedHeaders.includes.addAll(hSet.getExportedHeaders().getIncludes());
          s.exportedHeaders.excludes.addAll(hSet.getExportedHeaders().getExcludes());

          for (Map.Entry<String, String> macro : bin.getCppCompiler().getMacros().entrySet()) {
            s.macros.add(macro.getKey() + "=" + macro.getValue());
          }

          bo.sourceSets.add(s);

          if (sSet instanceof CppSourceSet) {
            s.args.addAll(bin.getCppCompiler().getArgs());
            for (Map.Entry<String, String> macro : bin.getCppCompiler().getMacros().entrySet()) {
              s.macros.add(macro.getKey() + "=" + macro.getValue());
            }
            s.cpp = true;
          } else if (sSet instanceof CSourceSet) {
            s.args.addAll(bin.getcCompiler().getArgs());
            for (Map.Entry<String, String> macro : bin.getcCompiler().getMacros().entrySet()) {
              s.macros.add(macro.getKey() + "=" + macro.getValue());
            }
            s.cpp = false;
          }

        }
      }

      for (NativeDependencySet dep : bin.getLibs()) {
        for (File f : dep.getIncludeRoots()) {
          bo.libHeaders.add(f.toString());
        }
        Class<? extends NativeDependencySet> cls = dep.getClass();
        Method sourceMethod = null;
        if (depClasses.containsKey(cls)) {
          sourceMethod = depClasses.get(cls);
        } else {
          try {
            sourceMethod = cls.getDeclaredMethod("getSourceFiles");
          } catch (NoSuchMethodException | SecurityException e) {
            sourceMethod = null;
          }
          depClasses.put(cls, sourceMethod);
        }
        if (sourceMethod != null) {
          try {
            Object rootsObject = sourceMethod.invoke(dep);
            if (rootsObject instanceof FileCollection) {
              for (File f : (FileCollection)rootsObject) {
                libSources.add(f.toString());
              }
            }
          } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }

      bo.componentName = bin.getComponent().getName();

      ToolChains tc = new ToolChains();

      tc.flavor = bin.getFlavor().getName();
      tc.buildType = bin.getBuildType().getName();

      tc.architecture = bin.getTargetPlatform().getArchitecture().getName();
      tc.operatingSystem = bin.getTargetPlatform().getOperatingSystem().getName();

      boolean added = toolChains.add(tc);
      if (!added) {
        for (ToolChains tc2 : toolChains) {
          if (tc.equals(tc2)) {
            tc = tc2;
          }
        }
      } else {

        tc.name = bin.getTargetPlatform().getName();

        CommandLineToolConfigurationInternal cppInternal = null;
        CommandLineToolConfigurationInternal cInternal = null;

        NativeToolChain toolChain = bin.getToolChain();

        for (VisualCppPlatformToolChain msvcPlat : ext._visualCppPlatforms) {
          if (msvcPlat.getPlatform().equals(bin.getTargetPlatform())) {
            tc.msvc = true;
            cppInternal = (CommandLineToolConfigurationInternal) msvcPlat.getCppCompiler();
            cInternal = (CommandLineToolConfigurationInternal) msvcPlat.getcCompiler();

            if (toolChain instanceof org.gradle.nativeplatform.toolchain.VisualCpp) {
              org.gradle.nativeplatform.toolchain.VisualCpp vcpp = (org.gradle.nativeplatform.toolchain.VisualCpp) toolChain;
              SearchResult<VisualStudioInstall> vsiSearch = ext._vsLocator.locateComponent(vcpp.getInstallDir());
              if (vsiSearch.isAvailable()) {
                VisualStudioInstall vsi = vsiSearch.getComponent();
                VisualCpp vscpp = vsi.getVisualCpp().forPlatform((NativePlatformInternal) bin.getTargetPlatform());
                tc.cppPath = vscpp.getCompilerExecutable().toString();
                tc.cPath = vscpp.getCompilerExecutable().toString();
                break;
              }
            }
          }
        }

        for (GccPlatformToolChain gccPlat : ext._gccLikePlatforms) {
          if (gccPlat.getPlatform().equals(bin.getTargetPlatform()) && toolChain instanceof GccCompatibleToolChain) {
            tc.msvc = false;
            GccCompatibleToolChain gccToolC = (GccCompatibleToolChain)toolChain;
            cppInternal = (CommandLineToolConfigurationInternal) gccPlat.getCppCompiler();
            cInternal = (CommandLineToolConfigurationInternal) gccPlat.getcCompiler();
            tc.cppPath = gccPlat.getCppCompiler().getExecutable();
            tc.cPath = gccPlat.getcCompiler().getExecutable();

            ToolSearchPath tsp = new ToolSearchPath(OperatingSystem.current());
            tsp.setPath(gccToolC.getPath());
            CommandLineToolSearchResult cppSearch = tsp.locate(ToolType.CPP_COMPILER,
                gccPlat.getCppCompiler().getExecutable());
            if (cppSearch.isAvailable()) {
              tc.cppPath = cppSearch.getTool().toString();
            }
            CommandLineToolSearchResult cSearch = tsp.locate(ToolType.C_COMPILER,
                gccPlat.getcCompiler().getExecutable());
            if (cSearch.isAvailable()) {
              tc.cPath = cSearch.getTool().toString();
            }
            if (cppSearch.isAvailable() && cSearch.isAvailable()) {
              break;
            }
          }
        }

        List<String> list = new ArrayList<>();
        cppInternal.getArgAction().execute(list);

        for (int i = 0; i < list.size(); i++) {
          String trim = list.get(i).trim();
          if (trim.startsWith("-D") || trim.startsWith("/D")) {
            list.remove(i);
          } else {
            continue;
          }
          tc.systemCppMacros.add(trim.substring(2));
        }

        tc.systemCppArgs.addAll(list);

        list.clear();

        cInternal.getArgAction().execute(list);

        for (int i = 0; i < list.size(); i++) {
          String trim = list.get(i).trim();
          if (trim.startsWith("-D") || trim.startsWith("/D")) {
            list.remove(i);
          } else {
            continue;
          }
          tc.systemCMacros.add(trim.substring(2));
        }

        tc.systemCArgs.addAll(list);

      }

      tc.allLibFiles.addAll(bo.libHeaders);
      tc.allLibFiles.addAll(libSources);
      tc.binaries.add(bo);

    }

    for(ToolChains tc : toolChains) {
      for(int i = 0; i < tc.binaries.size(); i++) {

        BinaryObject bin = tc.binaries.get(i);
        tc.nameBinaryMap.put(bin.componentName, i);
        for(SourceSet ss : bin.sourceSets) {
          tc.sourceBinaries.add(new SourceBinaryPair(ss, ss.source, bin.componentName));
          tc.sourceBinaries.add(new SourceBinaryPair(ss, ss.exportedHeaders, bin.componentName));
        }
      }
    }

    GsonBuilder builder = new GsonBuilder();

    if (ext.getPrettyPrinting()) {
      builder.setPrettyPrinting();
    }

    String json = builder.create().toJson(toolChains);
    return json;
  }
}
