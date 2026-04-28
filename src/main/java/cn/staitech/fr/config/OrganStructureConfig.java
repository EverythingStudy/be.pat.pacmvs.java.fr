package cn.staitech.fr.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "organ-structures")
@Slf4j
public class OrganStructureConfig {
    /**
     * key: 器官ID（liver, kidney, heart）
     * value: 对应结构列表
     */
    private Map<String, List<OrganStructure>> structures;
    private Map<String, List<OrganStructure>> outline;

    public Map<String, List<OrganStructure>> getStructures() {
        return structures;
    }

    public void setStructures(Map<String, List<OrganStructure>> structures) {
        this.structures = structures;
    }

    public Map<String, List<OrganStructure>> getOutline() {
        return outline;
    }

    public void setOutline(Map<String, List<OrganStructure>> outline) {
        this.outline = outline;
    }

    @Data
    public static class OrganStructure {
        private String structureId;
        private String name;
        private Boolean enabled = false;
    }
}
