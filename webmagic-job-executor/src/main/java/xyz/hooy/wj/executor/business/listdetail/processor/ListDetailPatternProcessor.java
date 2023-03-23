package xyz.hooy.wj.executor.business.listdetail.processor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.handler.PatternProcessor;
import xyz.hooy.wj.executor.business.listdetail.model.ListDetailTask;
import xyz.hooy.wj.executor.downloader.ExtendedPage;
import xyz.hooy.wj.executor.selector.Selector;
import xyz.hooy.wj.executor.selector.SelectorFactory;

import java.net.URL;
import java.util.*;

@Slf4j
public class ListDetailPatternProcessor extends PatternProcessor {

    private Map<String, String> constant;

    private ListDetailTask.ResolveDO.TemplateDO template;

    private static final SelectorFactory selectorFactory = new SelectorFactory();

    public ListDetailPatternProcessor(Map<String, String> constant, ListDetailTask.ResolveDO.TemplateDO template) {
        super(template.getUrlRex());
        this.constant = constant;
        this.template = template;
    }

    @SneakyThrows
    @Override
    public MatchOther processPage(Page p) {
        ExtendedPage page = (ExtendedPage) p;
        log.info("ListDetailPatternProcessor, url: {}, contentType: {}", page.getUrl().get(), page.getContentType());
        String rawText = page.getRawText();
        Selector selector = selectorFactory.get(page.getContentType());
        // detail page
        Map<String, String> detailResult = new LinkedHashMap<>(constant);
        page.getRequest().getExtras().put("outputFileName", template.getConfig().getOutputFileName());
        ListDetailTask.StartUrlDO startUrl = page.getRequest().getExtra("startUrl");
        detailResult.put("listTitle", startUrl.getTitle());
        detailResult.put("currentUrl", page.getUrl().get());
        Map<String, Object> detailHooks = template.getProcess();
        for (Map.Entry<String, Object> detailHook : detailHooks.entrySet()) {
            String hookKey = detailHook.getKey();
            if (StringUtils.equals(hookKey, "items")) continue;
            String hookValue = (String) detailHook.getValue();
            if (StringUtils.isBlank(hookValue)) continue;
            String value = selector.select(hookValue, rawText);
            detailResult.put(hookKey, StringUtils.trim(value));
        }
        Object isListPage = template.getProcess().get("items");
        if (Objects.nonNull(isListPage)) {
            // list page
            Map<String, String> listHooks = (Map<String, String>) isListPage;
            String itemHookValue = listHooks.get("item");
            if (StringUtils.isNotBlank(itemHookValue)) {
                ListDetailTask.StartUrlDO sudo = page.getRequest().getExtra("startUrl");
                List<String> itemNodes = selector.selects(itemHookValue, rawText);
                if (itemNodes.isEmpty()) {
                    log.info("Not find item, url: {}, path: {}", page.getUrl().get(), itemHookValue);
                    page.setSkip(true);
                    return MatchOther.NO;
                }
                List<Map<String, String>> listResults = new ArrayList<>(itemNodes.size());
                for (String itemNode : itemNodes) {
                    Map<String, String> listResult = new LinkedHashMap<>();
                    for (Map.Entry<String, String> listHook : listHooks.entrySet()) {
                        String hookKey = listHook.getKey();
                        if (StringUtils.equals(hookKey, "item")) continue;
                        String hookValue = listHook.getValue();
                        if (StringUtils.isBlank(hookValue)) continue;
                        String value = selector.select(hookValue, itemNode);
                        // inner page
                        if (StringUtils.equals(hookKey, "innerUrl")) {
                            if (StringUtils.containsAny(value, "./", "../")) {
                                value = new URL(new URL(page.getUrl().get()), value).toString();
                            } else {
                                String innerUrlAppend = template.getConfig().getInnerUrlAppend();
                                if (StringUtils.isNotBlank(innerUrlAppend)) {
                                    value = StringUtils.replace(innerUrlAppend, "${innerUrl}", value);
                                }
                            }
                            page.addTargetRequest(new Request(value).putExtra("startUrl", startUrl));
                        }
                        listResult.put(hookKey, StringUtils.trim(value));
                    }
                    sudo.completed();
                    listResult.put("listIndex", String.valueOf(startUrl.getIndex()));
                    listResult.putAll(detailResult);
                    listResults.add(listResult);
                    if (sudo.isAllCompleted()) break;
                }
                page.putField(UUID.randomUUID().toString(), listResults);
                // next page
                if (!sudo.isAllCompleted()) {
                    Request request = new Request(sudo.getNextPageUrl()).putExtra("startUrl", sudo);
                    page.addTargetRequest(request);
                }
            }
        } else {
            page.putField(UUID.randomUUID().toString(), detailResult);
        }
        return MatchOther.NO;
    }

    @Override
    public MatchOther processResult(ResultItems resultItems, Task task) {
        return MatchOther.YES;
    }
}
