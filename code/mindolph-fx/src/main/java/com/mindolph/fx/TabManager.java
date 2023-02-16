package com.mindolph.fx;

import javafx.scene.control.Tab;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 */
public class TabManager {
    private static final TabManager ins = new TabManager();
    private final Deque<Tab> tabStack = new ArrayDeque<>();

    public static TabManager getIns() {
        return ins;
    }

    private TabManager() {
    }

    /**
     * Active tab.
     *
     * @param selectingTab
     */
    public void activeTab(Tab selectingTab) {
        tabStack.remove(selectingTab);
        tabStack.push(selectingTab);
    }

    /**
     * Remove closed tab and return the previous tab.
     *
     * @param closedTab
     * @return
     */
    public Tab previousTabFrom(Tab closedTab) {
        if (closedTab == tabStack.peek()) {
            tabStack.pop(); // pop if on the top
        }
        else {
            tabStack.remove(closedTab); // remove if not on the top
        }
        return tabStack.peek();
    }

    @Override
    public String toString() {
        return StringUtils.join(tabStack.stream().map(Tab::getText).collect(Collectors.toList()), " - ");
    }
}
