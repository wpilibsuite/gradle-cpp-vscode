package org.wpilib.vscode.compilecommands;

import java.util.List;

public interface ArgsTransformer {
    List<String> transform(NativeCompileSpec spec);
}
