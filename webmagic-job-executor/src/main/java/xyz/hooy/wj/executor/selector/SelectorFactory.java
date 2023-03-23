package xyz.hooy.wj.executor.selector;

import java.util.HashMap;
import java.util.Map;

public class SelectorFactory {

    private Map<String, Selector> selectorEnumMap = new HashMap<>();

    public SelectorFactory() {
        addSelector("text/html", new XpathSelector());
        addSelector("application/json", new JsonpathSelector());
    }

    public Selector get(String responseContentType) {
        return selectorEnumMap.get(responseContentType);
    }

    public void addSelector(String responseContentType, Selector selector) {
        selectorEnumMap.put(responseContentType, selector);
    }
}
