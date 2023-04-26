package com.mindolph.core.search;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BaseSearchMatcher implements SearchMatcher{

    protected boolean returnContextEnabled;

    protected String matchContext;

    public BaseSearchMatcher(boolean returnContextEnabled) {
        this.returnContextEnabled = returnContextEnabled;
    }

    @Override
    public String getMatchContext() {
        return matchContext;
    }
}
