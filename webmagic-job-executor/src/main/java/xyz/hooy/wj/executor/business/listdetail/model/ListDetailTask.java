package xyz.hooy.wj.executor.business.listdetail.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import xyz.hooy.wj.executor.model.TaskDO;

import java.util.List;
import java.util.Map;

@Data
public class ListDetailTask extends TaskDO {

    private List<StartUrlDO> startUrl;

    private ResolveDO resolve;

    @Data
    public static class StartUrlDO {

        @Getter(AccessLevel.NONE)
        private String url;

        private String title;

        private Integer limit;

        private Integer index = 0;

        private Integer pageNum = 1;


        public String getNextPageUrl() {
            if (StringUtils.contains(url, "${pageNum}")) {
                return StringUtils.replace(url, "${pageNum}", String.valueOf(pageNum++));
            }
            return url;
        }

        public void completed() {
            index++;
        }

        public boolean isAllCompleted() {
            if (limit == null) return false;
            return index >= limit;
        }
    }

    @Data
    public static class ResolveDO {

        private Map<String, String> constant;

        private List<TemplateDO> templates;

        @Data
        public static class TemplateDO {

            private String urlRex;

            private ConfigDO config;

            private Map<String, Object> process;

            @Data
            public static class ConfigDO {

                private String outputFileName;

                private String innerUrlAppend;
            }

        }
    }
}
