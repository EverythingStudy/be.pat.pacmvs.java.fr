package cn.staitech.fr.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName: SpecialStructureConfig
 * @Description:特殊的组织轮廓
 * @author wanglibei
 * @date 2026年2月24日
 * @version V1.0
 */
@Configuration
@ConfigurationProperties(prefix = "special-structures")
@Data
@Slf4j
public class SpecialStructureConfig {

    private List<String> structureIds;

    private Set<String> structureIdSet = Collections.emptySet();

    public void setStructureIds(List<String> structureIds) {
        this.structureIds = structureIds;
        
        if (structureIds == null || structureIds.isEmpty()) {
            this.structureIdSet = Collections.emptySet();
        } else {
            // 构建 HashSet 并自动去除每个元素的首尾空格，防止配置格式问题
            this.structureIdSet = new HashSet<>(structureIds.size());
            for (String id : structureIds) {
                if (id != null) {
                    this.structureIdSet.add(id.trim());
                }
            }
        }
        
        log.info("SpecialStructureConfig updated. Loaded {} structure IDs.", this.structureIdSet.size());
    }

    /**
     * 判断传入的 structureId 是否在配置列表中
     * 时间复杂度：O(1)
     * @param structureId 待检查的结构ID
     * @return 如果包含返回 true，否则返回 false
     */
    public boolean containsStructureId(String structureId) {
        // 1. 基础校验：如果传入ID为空，直接返回false
        if (structureId == null) {
            return false;
        }

        // 2. 安全校验：如果集合为空，返回false
        if (this.structureIdSet.isEmpty()) {
            // 仅在首次加载或配置被清空时打印，避免高频日志
            if (this.structureIds != null && !this.structureIds.isEmpty()) {
                 // 理论上不会进入这里，除非逻辑有误
            }
            return false;
        }

        // 3. 执行包含判断 (使用 trim 确保与 Set 中存储的格式一致)
        return this.structureIdSet.contains(structureId.trim());
    }
    
}