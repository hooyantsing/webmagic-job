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
import xyz.hooy.wj.executor.constant.Placeholder;
import xyz.hooy.wj.executor.downloader.ExtendedPage;
import xyz.hooy.wj.executor.selector.Selector;
import xyz.hooy.wj.executor.selector.SelectorFactory;

import java.net.MalformedURLException;
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
        final String currentUrl = page.getUrl().get();
        final String contentType = page.getContentType();
        final String rawText = page.getRawText();
        log.info("ListDetailPatternProcessor, url: {}, contentType: {}", currentUrl, contentType);
        Selector selector = selectorFactory.get(contentType);
        Request request = page.getRequest();
        request.putExtra("outputFileName", template.getConfig().getOutputFileName());
        // detail page
        ListDetailTask.StartUrlDO startDO = request.getExtra("startUrl");
        Map<String, String> detailResult = new LinkedHashMap<>(constant);
        detailResult.put("currentUrl", currentUrl);
        detailResult.put("listTitle", startDO.getTitle());
        Map<String, Object> detailHooks = template.getProcess();
        for (Map.Entry<String, Object> detailHook : detailHooks.entrySet()) {
            String detailHookKey = detailHook.getKey();
            if (StringUtils.equals(detailHookKey, "items")) {
                Object detailHookValue = detailHook.getValue();
                if (detailHookValue != null) {
                    // list page
                    Map<String, String> listHooks = (Map<String, String>) detailHookValue;
                    String itemHookValue = listHooks.get("item");
                    if (StringUtils.isNotBlank(itemHookValue)) {
                        List<String> itemNodes = selector.selects(itemHookValue, rawText);
                        if (itemNodes.isEmpty()) {
                            log.info("Not find item, url: {}, path: {}", currentUrl, itemHookValue);
                            page.setSkip(true);
                            return MatchOther.NO;
                        }
                        List<Map<String, String>> listResults = new ArrayList<>(itemNodes.size());
                        for (String itemNode : itemNodes) {
                            Map<String, String> listResult = new LinkedHashMap<>(listHooks.size());
                            for (Map.Entry<String, String> listHook : listHooks.entrySet()) {
                                String listHookKey = listHook.getKey();
                                String listHookValue = listHook.getValue();
                                if (!StringUtils.equals(listHookKey, "item") && StringUtils.isNotBlank(listHookValue)) {
                                    String value = selector.select(listHookValue, itemNode);
                                    // inner page
                                    if (StringUtils.equals(listHookKey, "innerUrl")) {
                                        value = detectInnerUrl(currentUrl, value);
                                        Request innerRequest = buildRequest(value, startDO);
                                        page.addTargetRequest(innerRequest);
                                    }
                                    listResult.put(listHookKey, StringUtils.trimToEmpty(value));
                                }
                            }
                            startDO.completed();
                            listResult.put("listIndex", String.valueOf(startDO.getIndex()));
                            listResult.putAll(detailResult);
                            listResults.add(listResult);
                            if (startDO.isAllCompleted()) break;
                        }
                        // next page
                        if (!startDO.isAllCompleted()) {
                            String nextUrl = startDO.getNextPageUrl();
                            Request nextRequest = buildRequest(nextUrl, startDO);
                            page.addTargetRequest(nextRequest);
                        }
                        page.putField(UUID.randomUUID().toString(), listResults);
                        return MatchOther.NO;
                    }
                }
            } else {
                String detailHookValue = (String) detailHook.getValue();
                if (StringUtils.isNotBlank(detailHookValue)) {
                    String value = selector.select(detailHookValue, rawText);
                    detailResult.put(detailHookKey, StringUtils.trimToEmpty(value));
                }
            }
        }
        page.putField(UUID.randomUUID().toString(), detailResult);
        return MatchOther.NO;
    }

    private String detectInnerUrl(String current, String inner) throws MalformedURLException {
        if (StringUtils.containsAny(inner, "./", "../")) {
            return new URL(new URL(current), inner).toString();
        } else {
            String innerUrlAppend = template.getConfig().getInnerUrlAppend();
            if (StringUtils.isNotBlank(innerUrlAppend)) {
                return StringUtils.replace(innerUrlAppend, Placeholder.INNER_URL, inner);
            }
        }
        return inner;
    }

    private Request buildRequest(String url, ListDetailTask.StartUrlDO startUrl) {
        Request request = new Request(url);
        request.putExtra("startUrl", startUrl);
        return request;
    }

    @Override
    public MatchOther processResult(ResultItems resultItems, Task task) {
        return MatchOther.YES;
    }
}
