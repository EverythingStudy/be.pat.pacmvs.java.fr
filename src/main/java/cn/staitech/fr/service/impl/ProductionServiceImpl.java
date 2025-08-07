package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Production;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SpeciesWaxCodeTemplate;
import cn.staitech.fr.mapper.ProductionMapper;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpeciesWaxCodeTemplateMapper;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.vo.project.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
    @Resource
    private SlideService slideService;

    /**
     * 制片信息列表
     *
     * @param req 制片信息参数
     * @return 制片信息结果
     */
    @Override
    public List<ProductionVO> list(ProductionReq req) {
        List<ProductionVO> list = new ArrayList<>();
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        if (project != null && StringUtils.isNotBlank(project.getSpeciesId())) {
            // 先查询制片信息表，为空，再查询模板表+项目蜡块号
            LambdaQueryWrapper<Production> pWrapper = new LambdaQueryWrapper<>();
            pWrapper.eq(Production::getSpecialId, req.getProjectId());
            pWrapper.eq(Production::getSpeciesId, project.getSpeciesId());
            List<Production> productions = this.baseMapper.selectList(pWrapper);
            if (!CollectionUtils.isEmpty(productions)) {
                for (Production p : productions) {
                    ProductionVO vo = new ProductionVO();
                    BeanUtils.copyProperties(p, vo);
                    list.add(vo);
                }
            } else {
                // 查询模板表
                LambdaQueryWrapper<SpeciesWaxCodeTemplate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SpeciesWaxCodeTemplate::getSpeciesId, project.getSpeciesId());
                List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(templates)) {
                    for (SpeciesWaxCodeTemplate template : templates) {
                        ProductionVO vo = new ProductionVO();
                        BeanUtils.copyProperties(template, vo);
                        vo.setTemplateId(template.getId());
                        vo.setId(null);
                        list.add(vo);
                    }
                }
                // 项目切片蜡块号
                List<String> projectWaxCodes = this.slideMapper.selectWaxCodes(req.getProjectId());
                if (!CollectionUtils.isEmpty(projectWaxCodes)) {
                    for (String code : projectWaxCodes) {
                        ProductionVO vo = new ProductionVO();
                        vo.setSpeciesId(project.getSpeciesId());
                        vo.setWaxCode(code);
                        list.add(vo);
                    }
                }
            }
        }
        return list;
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
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        Set<Long> templateIds = new HashSet<>(16);
        // 校验
        if (!CollectionUtils.isEmpty(req.getProductions())) {
            List<String> list = new ArrayList<>();
            for (ProductionInfoReq r : req.getProductions()) {
                String key = r.getWaxCode() + "-" + r.getTemplateId() + "-" + r.getSexFlag();
                if (list.contains(key)) {
                    return R.fail("表中有重复信息，请删除重复信息后再保存");
                }
                list.add(key);
                templateIds.add(r.getTemplateId());
            }
        }
        // 先删除后插入
        LambdaQueryWrapper<Production> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Production::getSpecialId, req.getProjectId());
        this.baseMapper.delete(wrapper);

        if (!CollectionUtils.isEmpty(req.getProductions())) {
            // 查询种属蜡块模板信息
            LambdaQueryWrapper<SpeciesWaxCodeTemplate> tWrapper = new LambdaQueryWrapper<>();
            tWrapper.in(SpeciesWaxCodeTemplate::getId, templateIds);
            List<SpeciesWaxCodeTemplate> templateList = this.speciesWaxCodeTemplateMapper.selectList(tWrapper);
            Map<Long, SpeciesWaxCodeTemplate> map = templateList.stream().collect(Collectors.toMap(SpeciesWaxCodeTemplate::getId, item -> item));

            List<Production> productions = new ArrayList<>();
            for (ProductionInfoReq r : req.getProductions()) {
                SpeciesWaxCodeTemplate template = map.get(r.getTemplateId());
                Production production = new Production();
                // 专题ID
                production.setSpecialId(req.getProjectId());
                // 种属蜡块模板表ID
                production.setTemplateId(r.getTemplateId());
                if (template != null) {
                    // 种属ID
                    production.setSpeciesId(template.getSpeciesId());
                    // 脏器名称
                    production.setOrganName(template.getOrganName());
                    // 英文名称
                    production.setOrganEn(template.getOrganEn());
                    // 脏器编码
                    production.setOrganCode(template.getOrganCode());
                    // 脏器缩写
                    production.setAbbreviation(template.getAbbreviation());
                }
                // 蜡块编号
                production.setWaxCode(r.getWaxCode());
                // 取材块数
                production.setBlockCount(r.getBlockCount());
                // 性别
                production.setSexFlag(r.getSexFlag());
                // 机构ID
                production.setOrganizationId(project.getOrganizationId());
                Long userid = SecurityUtils.getUserId();
                Date now = new Date();
                // 创建人id
                production.setCreateBy(userid);
                // 创建时间
                production.setCreateTime(now);
                // 更新人id
                production.setUpdateBy(userid);
                // 更新时间
                production.setUpdateTime(now);
                productions.add(production);
            }
            this.saveBatch(productions);
        }
        return R.ok("保存成功");
    }

    /**
     * 种属脏器下拉列表（取自种属蜡块模板数据）
     *
     * @param req 制片信息参数
     * @return 种属脏器下拉列表（取自种属蜡块模板数据）
     */
    @Override
    public List<OrganVO> organList(ProductionReq req) {
        List<OrganVO> list = new ArrayList<>();
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        if (project != null && StringUtils.isNotBlank(project.getSpeciesId())) {
            LambdaQueryWrapper<SpeciesWaxCodeTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpeciesWaxCodeTemplate::getSpeciesId, project.getSpeciesId());
            List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(templates)) {
                for (SpeciesWaxCodeTemplate template : templates) {
                    OrganVO vo = new OrganVO();
                    vo.setTemplateId(template.getId());
                    vo.setOrganName(template.getOrganName());
                    vo.setOrganEn(template.getOrganEn());
                    list.add(vo);
                }
            }
        }
        return list;
    }
}




