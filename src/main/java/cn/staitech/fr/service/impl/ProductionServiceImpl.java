package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Production;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SpeciesWaxCodeTemplate;
import cn.staitech.fr.mapper.ProductionMapper;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpeciesWaxCodeTemplateMapper;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.vo.project.OrganVO;
import cn.staitech.fr.vo.project.ProductionReq;
import cn.staitech.fr.vo.project.ProductionSaveReq;
import cn.staitech.fr.vo.project.ProductionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 专题制片信息
 *
 * @author yxy
 */
@Service
public class ProductionServiceImpl extends ServiceImpl<ProductionMapper, Production> implements ProductionService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SlideMapper slideMapper;
    @Resource
    private SpeciesWaxCodeTemplateMapper speciesWaxCodeTemplateMapper;

    /**
     * 制片信息列表
     *
     * @param req 制片信息参数
     * @return 制片信息结果
     */
    @Override
    public List<ProductionVO> list(ProductionReq req) {
        return Collections.emptyList();
    }

    /**
     * 蜡块编号下拉列表
     *
     * @param req 制片信息参数
     * @return 蜡块编号下拉列表
     */
    @Override
    public List<String> waxCodeList(ProductionReq req) {
        Set<String> set = new HashSet<>(16);
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        if (project != null) {
            // 项目切片蜡块号
            List<String> projectWaxCodes = this.slideMapper.selectWaxCodes(req.getProjectId());
            if (!CollectionUtils.isEmpty(projectWaxCodes)) {
                set.addAll(projectWaxCodes);
            }
            // 模板蜡块号
            if (StringUtils.isNotBlank(project.getSpeciesId())) {
                LambdaQueryWrapper<SpeciesWaxCodeTemplate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SpeciesWaxCodeTemplate::getSpeciesId, project.getSpeciesId());
                List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(templates)) {
                    Set<String> templateWaxCodes = templates.stream().map(SpeciesWaxCodeTemplate::getWaxCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                    set.addAll(templateWaxCodes);
                }

            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 保存制片信息
     *
     * @param req 制片信息
     * @return 结果
     */
    @Override
    public R<String> save(ProductionSaveReq req) {
        return null;
    }

    /**
     * 种属脏器下拉列表（取自种属蜡块模板数据）
     *
     * @param req 制片信息参数
     * @return 种属脏器下拉列表（取自种属蜡块模板数据）
     */
    @Override
    public List<OrganVO> organList(ProductionReq req) {
        return Collections.emptyList();
    }
}




