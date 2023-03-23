package xyz.hooy.wj.executor.selector;

import xyz.hooy.wj.executor.selector.saxon.Xpath2Selector;

import java.util.List;

public class XpathSelector extends Selector {
    @Override
    public String select(String path, String rawText) {
        return Xpath2Selector.newInstance(path).select(rawText);
    }

    @Override
    public List<String> selects(String path, String rawText) {
        return Xpath2Selector.newInstance(path).selectList(rawText);
    }
}
