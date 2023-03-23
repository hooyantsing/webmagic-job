package xyz.hooy.wj.executor.selector;

import java.util.List;

public abstract class Selector {

    public abstract String select(String path, String rawText);

    public abstract List<String> selects(String path, String rawText);
}
