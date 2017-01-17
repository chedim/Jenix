package com.onkiup.ai;

@FunctionalInterface
public interface ParamTest<X> {
    /**
     * Returns null if object doesn't match
     * @param $
     * @return
     */
    boolean $(X $);
}
