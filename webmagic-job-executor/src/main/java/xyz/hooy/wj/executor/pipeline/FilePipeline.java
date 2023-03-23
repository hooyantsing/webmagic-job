package xyz.hooy.wj.executor.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.MultiPageModel;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

@Slf4j
public class FilePipeline extends FilePersistentBase implements Pipeline {

    public FilePipeline(String path) {
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        if (resultItems.getAll().size() <= 0) return;
        Path path = Paths.get(getPath() + resultItems.getRequest().getExtra("outputFileName"));
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            for (Map.Entry<String, Object> result : resultItems.getAll().entrySet()) {
                Object obj = result.getValue();
                if (obj instanceof MultiPageModel) {
                    byte[] bytes = StringUtils.appendIfMissing(obj.toString(), "\n").getBytes(StandardCharsets.UTF_8);
                    Files.write(path, bytes, StandardOpenOption.APPEND);
                } else if (obj instanceof List) {
                    for (Map item : (List<Map>) obj) {
                        Object convert = convert(item);
                        byte[] bytes = StringUtils.appendIfMissing(convert.toString(), "\n").getBytes(StandardCharsets.UTF_8);
                        Files.write(path, bytes, StandardOpenOption.APPEND);
                    }
                } else {
                    Object convert = convert((Map<String, String>) obj);
                    byte[] bytes = StringUtils.appendIfMissing(convert.toString(), "\n").getBytes(StandardCharsets.UTF_8);
                    Files.write(path, bytes, StandardOpenOption.APPEND);
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    protected Object convert(Map<String, String> item) {
        return item;
    }
}
