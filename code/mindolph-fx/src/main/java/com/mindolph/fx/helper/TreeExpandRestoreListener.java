package com.mindolph.fx.helper;

import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
@FunctionalInterface
public interface TreeExpandRestoreListener {

    void onTreeExpandRestore(List<String> expandedNodes);
}
