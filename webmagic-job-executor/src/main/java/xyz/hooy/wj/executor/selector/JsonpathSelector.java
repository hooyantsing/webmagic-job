package xyz.hooy.wj.executor.selector;

import us.codecraft.webmagic.selector.JsonPathSelector;

import java.util.List;

public class JsonpathSelector extends Selector {
    @Override
    public String select(String path, String rawText) {
        return new JsonPathSelector(path).select(rawText);
    }

    @Override
    public List<String> selects(String path, String rawText) {
        return new JsonPathSelector(path).selectList(rawText);
    }
}
