package edu.wpi.first.vscode.compilecommands;

import java.util.ArrayList;
import java.util.List;

public class EscapeUserArgs {
    public static String escapeUserArg(String original) {
        return new EscapeUserArgs().transform(original);
    }

    public static List<String> escapeUserArgs(List<String> original) {
        return new EscapeUserArgs().transform(original);
    }

    public String transform(String original) {
        return original.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public List<String> transform(List<String> args) {
        List<String> transformed = new ArrayList<>(args.size());
        for (String arg : args) {
            transformed.add(transform(arg));
        }
        return transformed;
    }
}
