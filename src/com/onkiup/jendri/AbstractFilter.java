package com.onkiup.jendri;

import javax.servlet.Filter;

public abstract class AbstractFilter implements Filter {
    public abstract Integer getPriority();
    public abstract String getUrl();
}
