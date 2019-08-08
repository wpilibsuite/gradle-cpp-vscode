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
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.c.CSourceSet;
import org.gradle.language.cpp.CppSourceSet;
import org.gradle.language.nativeplatform.HeaderExportingSourceSet;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.Clang;
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
import edu.wpi.first.vscode.tooling.models.BinaryObjectImpl;
import edu.wpi.first.vscode.tooling.models.SourceBinaryPairImpl;
import edu.wpi.first.vscode.tooling.models.SourceSet;
import edu.wpi.first.vscode.tooling.models.SourceSetImpl;
import edu.wpi.first.vscode.tooling.models.ToolChains;
import edu.wpi.first.vscode.tooling.models.ToolChainsImpl;

public class ToolChainGenerator {
  private static String normalizeDriveLetter(String path) {
    if (OperatingSystem.current().isWindows() && path.charAt(1) == ':') {
      return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
    return path;
  }

  public static Set<ToolChains> generateToolChains(Project project) {
    VsCodeConfigurationExtension ext = project.getExtensions().getByType(VsCodeConfigurationExtension.class);
    if (ext._toolChainsStore != null) {
      return ext._toolChainsStore;
    }
    Set<ToolChains> toolChains = new LinkedHashSet<>();

    Map<Class<? extends NativeDependencySet>, Method> depClasses = new HashMap<>();

    for (NativeBinarySpec bin : ext._binaries) {
      if (!bin.isBuildable()) {
        continue;
      }

      BinaryObjectImpl bo = new BinaryObjectImpl();

      Set<String> libSources = new LinkedHashSet<>();

      for (LanguageSourceSet sSet : bin.getInputs()) {
        if (sSet instanceof HeaderExportingSourceSet) {
          if (!(sSet instanceof CppSourceSet) && !(sSet instanceof CSourceSet)) {
            continue;
          }
          HeaderExportingSourceSet hSet = (HeaderExportingSourceSet) sSet;
          SourceSetImpl s = new SourceSetImpl();

          s.getSource().setSrcDirs(hSet.getSource().getSrcDirs().stream().map(x -> normalizeDriveLetter(x.toString()) + File.separator).collect(Collectors.toSet()));

          s.getSource().getIncludes().addAll(hSet.getSource().getIncludes());
          s.getSource().getExcludes().addAll(hSet.getSource().getExcludes());

          s.getExportedHeaders().setSrcDirs(hSet.getExportedHeaders().getSrcDirs().stream().map(x -> normalizeDriveLetter(x.toString()) + File.separator).collect(Collectors.toSet()));

          s.getExportedHeaders().getIncludes().addAll(hSet.getSource().getIncludes());
          s.getExportedHeaders().getExcludes().addAll(hSet.getSource().getExcludes());

          for (Map.Entry<String, String> macro : bin.getCppCompiler().getMacros().entrySet()) {
            if (macro.getValue() == null) {
              s.getMacros().add("-D" + macro.getKey());
            } else {
              s.getMacros().add("-D" + macro.getKey() + "=" + macro.getValue());
            }
          }

          bo.getSourceSets().add(s);

          if (sSet instanceof CppSourceSet) {
            s.getArgs().addAll(bin.getCppCompiler().getArgs());
            for (Map.Entry<String, String> macro : bin.getCppCompiler().getMacros().entrySet()) {
              if (macro.getValue() == null) {
                s.getMacros().add("-D" + macro.getKey());
              } else {
                s.getMacros().add("-D" + macro.getKey() + "=" + macro.getValue());
              }
            }
            s.cpp = true;
          } else if (sSet instanceof CSourceSet) {
            s.getArgs().addAll(bin.getcCompiler().getArgs());
            for (Map.Entry<String, String> macro : bin.getcCompiler().getMacros().entrySet()) {
              if (macro.getValue() == null) {
                s.getMacros().add("-D" + macro.getKey());
              } else {
                s.getMacros().add("-D" + macro.getKey() + "=" + macro.getValue());
              }
            }
            s.cpp = false;
          }

        }
      }

      for (NativeDependencySet dep : bin.getLibs()) {
        for (File f : dep.getIncludeRoots()) {
          bo.getLibHeaders().add(f.toString() + File.separator);
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
                libSources.add(f.toString() + File.separator);
              }
            }
          } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }

      bo.componentName = bin.getComponent().getName();

      bo.sharedLibrary = (bin instanceof SharedLibraryBinarySpec);
      bo.executable = (bin instanceof NativeExecutableBinarySpec);

      ToolChainsImpl tci = new ToolChainsImpl();

      tci.flavor = bin.getFlavor().getName();
      tci.buildType = bin.getBuildType().getName();

      tci.architecture = bin.getTargetPlatform().getArchitecture().getName();
      tci.operatingSystem = bin.getTargetPlatform().getOperatingSystem().getName();

      tci.name = bin.getTargetPlatform().getName();

      boolean added = toolChains.add(tci);

      ToolChains tc = tci;

      if (!added) {
        for (ToolChains tc2 : toolChains) {
          if (tci.equals(tc2)) {
            tc = tc2;
          }
        }
      } else {
        CommandLineToolConfigurationInternal cppInternal = null;
        CommandLineToolConfigurationInternal cInternal = null;

        NativeToolChain toolChain = bin.getToolChain();

        for (VisualCppPlatformToolChain msvcPlat : ext._visualCppPlatforms) {
          if (msvcPlat.getPlatform().equals(bin.getTargetPlatform())) {
            tci.msvc = true;
            cppInternal = (CommandLineToolConfigurationInternal) msvcPlat.getCppCompiler();
            cInternal = (CommandLineToolConfigurationInternal) msvcPlat.getcCompiler();

            if (toolChain instanceof org.gradle.nativeplatform.toolchain.VisualCpp) {
              org.gradle.nativeplatform.toolchain.VisualCpp vcpp = (org.gradle.nativeplatform.toolchain.VisualCpp) toolChain;
              SearchResult<VisualStudioInstall> vsiSearch = ext._vsLocator.locateComponent(vcpp.getInstallDir());
              if (vsiSearch.isAvailable()) {
                VisualStudioInstall vsi = vsiSearch.getComponent();
                VisualCpp vscpp = vsi.getVisualCpp().forPlatform((NativePlatformInternal) bin.getTargetPlatform());
                tci.cppPath = vscpp.getCompilerExecutable().toString();
                tci.cPath = vscpp.getCompilerExecutable().toString();
                break;
              }
            }
          }
        }

        for (GccPlatformToolChain gccPlat : ext._gccLikePlatforms) {
          if (gccPlat.getPlatform().equals(bin.getTargetPlatform()) && toolChain instanceof GccCompatibleToolChain) {
            tci.msvc = false;
            tci.gcc = !(toolChain instanceof Clang);
            GccCompatibleToolChain gccToolC = (GccCompatibleToolChain)toolChain;
            cppInternal = (CommandLineToolConfigurationInternal) gccPlat.getCppCompiler();
            cInternal = (CommandLineToolConfigurationInternal) gccPlat.getcCompiler();
            tci.cppPath = gccPlat.getCppCompiler().getExecutable();
            tci.cPath = gccPlat.getcCompiler().getExecutable();

            ToolSearchPath tsp = new ToolSearchPath(OperatingSystem.current());
            tsp.setPath(gccToolC.getPath());
            CommandLineToolSearchResult cppSearch = tsp.locate(ToolType.CPP_COMPILER,
                gccPlat.getCppCompiler().getExecutable());
            if (cppSearch.isAvailable()) {
              tci.cppPath = cppSearch.getTool().toString();
            }
            CommandLineToolSearchResult cSearch = tsp.locate(ToolType.C_COMPILER,
                gccPlat.getcCompiler().getExecutable());
            if (cSearch.isAvailable()) {
              tci.cPath = cSearch.getTool().toString();
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
          tc.getSystemCppMacros().add(trim.substring(2));
        }

        tc.getSystemCppArgs().addAll(list);

        list.clear();

        cInternal.getArgAction().execute(list);

        for (int i = 0; i < list.size(); i++) {
          String trim = list.get(i).trim();
          if (trim.startsWith("-D") || trim.startsWith("/D")) {
            list.remove(i);
          } else {
            continue;
          }
          tc.getSystemCMacros().add(trim.substring(2));
        }

        tc.getSystemCArgs().addAll(list);

      }

      tc.getAllLibFiles().addAll(bo.libHeaders);
      tc.getAllLibFiles().addAll(libSources);
      tc.getBinaries().add(bo);

    }

    for(ToolChains tc : toolChains) {
      List<BinaryObject> binaries = tc.getBinaries();
      for(int i = 0; i < binaries.size(); i++) {

        BinaryObject bin = binaries.get(i);
        tc.getNameBinaryMap().put(bin.getComponentName(), i);
        for(SourceSet ss : bin.getSourceSets()) {
          tc.getSourceBinaries().add(new SourceBinaryPairImpl(ss, ss.getSource(), bin.getComponentName(), bin.isExecutable(), bin.isSharedLibrary()));
          tc.getSourceBinaries().add(new SourceBinaryPairImpl(ss, ss.getExportedHeaders(), bin.getComponentName(), bin.isExecutable(), bin.isSharedLibrary()));
        }
      }
    }

    ext._toolChainsStore = toolChains;
    return toolChains;
  }
}
