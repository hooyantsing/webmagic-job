package xyz.hooy.wj.executor.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import xyz.hooy.wj.executor.business.listdetail.model.ListDetailTask;
import xyz.hooy.wj.executor.business.listdetail.processor.ListDetailPatternProcessor;
import xyz.hooy.wj.executor.downloader.ExtendedHttpClientDownloader;
import xyz.hooy.wj.executor.pipeline.FilePipeline;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class ListDetailJob {

    @Resource(name = "crawlerThreadPool")
    private ExecutorService crawlerThreadPool;

    @Resource
    private ObjectMapper objectMapper;

    @XxlJob("ListDetailJobHandler")
    public void ListDetailJobHandler() throws Exception {
        // step1: param
        String taskText = XxlJobHelper.getJobParam();
        ListDetailTask taskDO;
        // 标准 json 格式配置文件
        taskDO = objectMapper.readValue(taskText, ListDetailTask.class);
        String dir = StringUtils.appendIfMissing(taskDO.getCrawler().getOutputFilePath(), "/") + LocalDate.now();
        // step2: processor
        CompositePageProcessor compositePageProcessor = new CompositePageProcessor(Site.me());
        Map<String, String> constant = taskDO.getResolve().getConstant();
        taskDO.getResolve().getTemplates().stream()
                .map(templateDO -> new ListDetailPatternProcessor(constant, templateDO))
                .forEach(cpp -> compositePageProcessor.addSubPageProcessor(cpp));
        // step3: request
        Set<Request> requests = taskDO.getStartUrl().stream()
                .map(startUrl -> new Request(startUrl.getNextPageUrl()).putExtra("startUrl", startUrl))
                .collect(Collectors.toSet());
        // step4: spider
        Spider.create(compositePageProcessor)
                .addRequest(requests.toArray(new Request[0]))
                .setDownloader(new ExtendedHttpClientDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(new FilePipeline(dir))
                .thread(crawlerThreadPool, taskDO.getCrawler().getThread())
                .setUUID(taskDO.getTaskUUID())
                .run();
        // step5: storage
        // TODO: 基于命令行 Runtime.getRuntime().exec(); 实现自动入库
    }
}
