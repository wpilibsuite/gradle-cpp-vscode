package edu.wpi.first.vscode.compilecommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.api.Transformer;

public class MacroArgsConverter implements Transformer<List<String>, Map<String, String>> {
    @Override
    public List<String> transform(Map<String, String> original) {
        List<String> macroList = new ArrayList<String>(original.size());
        for (String macroName : original.keySet()) {
            String macroDef = original.get(macroName);
            String arg = macroDef == null ? macroName : (macroName + "=" + macroDef);
            macroList.add(arg);
        }
        return macroList;
    }
}
