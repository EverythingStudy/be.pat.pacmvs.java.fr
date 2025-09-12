package cn.staitech.fr.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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

    @PostConstruct
    public void init() {
        log.info("OrganStructureConfig initialized with organs: {}", outline);
        if (structures != null) {
            List<OrganStructureConfig.OrganStructure> structuress = structures.get("08");
            List<OrganStructureConfig.OrganStructure> structuress1 = structures.get("12");
            if (structuress == null || structuress.isEmpty()) {
                //return false;
            }

            // 获取该脏器所有启用的结构
            List<String> enabledStructures = Arrays.asList(structuress.get(0).getStructureId().split(","));
            structures.forEach((organ, structures) -> log.info("Organ: {}, Structures count: {}", organ, structures != null ? structures.size() : 0));
        }
    }
}
