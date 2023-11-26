package edu.wpi.first.vscode.args;

import java.util.List;

public interface ArgsTransformer {
    List<String> transform(NativeCompileSpec spec);
}
