package xyz.hooy.wj.executor.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
public class TaskDO {

    @Setter(AccessLevel.NONE)
    private String taskUUID = UUID.randomUUID().toString();

    private CrawlerDO crawler;

    @Data
    public static class CrawlerDO {

        @Getter(AccessLevel.NONE)
        private Integer thread = 1;

        private String outputFilePath = "../out/";

        @Getter(AccessLevel.NONE)
        private Boolean multi;

        private Proxy proxy;

        public Integer getThread() {
            return Math.min(Runtime.getRuntime().availableProcessors(), thread);
        }

        public Boolean getMulti() {
            return multi == null || multi;
        }

        @Data
        public static class Proxy {

            private String hostPort;

            private String username;

            private String password;
        }
    }
}
