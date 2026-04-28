package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.domain.OrganTag;
import cn.staitech.fr.domain.Production;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SpeciesWaxCodeTemplate;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.vo.project.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private OrganTagMapper organTagMapper;

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
            // 保存过：直接查询制片信息表
            if (project.getProductionSave() != null && project.getProductionSave() == 1) {
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
                }
            }
            // 没有保存过：查询模板表+项目蜡块号
            else {
                Set<String> waxCodes = new HashSet<>();
                // 查询模板表
                LambdaQueryWrapper<SpeciesWaxCodeTemplate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SpeciesWaxCodeTemplate::getSpeciesId, project.getSpeciesId());
                List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(templates)) {
                    for (SpeciesWaxCodeTemplate template : templates) {
                        waxCodes.add(template.getWaxCode());
                        ProductionVO vo = new ProductionVO();
                        BeanUtils.copyProperties(template, vo);
                        // 种属蜡块模板表ID
                        vo.setWaxCodeId(template.getId());
                        list.add(vo);
                    }
                }
                // 项目切片蜡块号
                List<String> projectWaxCodes = this.slideMapper.selectWaxCodes(req.getProjectId());
                if (!CollectionUtils.isEmpty(projectWaxCodes)) {
                    for (String code : projectWaxCodes) {
                        // 不在模板内的新增
                        if (!waxCodes.contains(code)) {
                            ProductionVO vo = new ProductionVO();
                            vo.setSpeciesId(project.getSpeciesId());
                            vo.setWaxCode(code);
                            list.add(vo);
                        }
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
        List<String> list = new ArrayList<>();
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        if (project != null) {
            // 模板蜡块号
            if (StringUtils.isNotBlank(project.getSpeciesId())) {
                LambdaQueryWrapper<SpeciesWaxCodeTemplate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SpeciesWaxCodeTemplate::getSpeciesId, project.getSpeciesId());
                List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(templates)) {
                    Set<String> templateWaxCodes = templates.stream().map(SpeciesWaxCodeTemplate::getWaxCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                    list.addAll(templateWaxCodes);
                    list.sort(Comparator.comparing(Integer::valueOf));
                }
            }
            // 项目切片蜡块号
            List<String> projectWaxCodes = this.slideMapper.selectWaxCodes(req.getProjectId());
            if (!CollectionUtils.isEmpty(projectWaxCodes)) {
                for (String waxCode : projectWaxCodes) {
                    if (!list.contains(waxCode)) {
                        list.add(waxCode);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 保存制片信息
     *
     * @param req 制片信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public R<String> save(ProductionSaveReq req) {
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        // 校验
        if (Constants.STATUS_COMPLETED == project.getStatus()) {
            return R.fail("项目已完成，不可修改");
        }
        if (Constants.STATUS_RUNNING == project.getStatus() || Constants.STATUS_PAUSED == project.getStatus()) {
            if (!(SecurityUtils.getUserId().equals(project.getPrincipal()) || SecurityUtils.isOrgAdmin())) {
                return R.fail("您没有该项目的配置权限，请联系该项目负责人或机构管理员");
            }
        }
        Set<Long> waxCodeIds = new HashSet<>(16);
        // 校验
        if (!CollectionUtils.isEmpty(req.getProductions())) {
            List<String> list = new ArrayList<>();
            for (ProductionInfoReq r : req.getProductions()) {
                String key = r.getWaxCode() + "-" + r.getWaxCodeId();
                if (list.contains(key)) {
                    return R.fail("表中有重复信息，请删除重复信息后再保存");
                }
                list.add(key);
                waxCodeIds.add(r.getWaxCodeId());
            }
        }
        // 先删除后插入
        LambdaQueryWrapper<Production> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Production::getSpecialId, req.getProjectId());
        this.baseMapper.delete(wrapper);

        if (!CollectionUtils.isEmpty(req.getProductions())) {
            // 查询模板表
            List<SpeciesWaxCodeTemplate> templates = speciesWaxCodeTemplateMapper.selectBatchIds(waxCodeIds);
            // 脏器编码集合
            List<String> organCodes = templates.stream().map(SpeciesWaxCodeTemplate::getOrganCode).collect(Collectors.toList());
            Map<Long, SpeciesWaxCodeTemplate> speciesWaxCodeTemplateMap = templates.stream().collect(Collectors.toMap(SpeciesWaxCodeTemplate::getId, item -> item));
            // 查询脏器标签信息
            LambdaQueryWrapper<OrganTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(OrganTag::getOrganizationId, project.getOrganizationId());
            // 种属ID取模板表里面的：防止此时项目种属变更导致脏器标签id漂移
            tagWrapper.eq(OrganTag::getSpeciesId, templates.get(0).getSpeciesId());
            tagWrapper.in(OrganTag::getOrganTagCode, organCodes);
            List<OrganTag> tags = organTagMapper.selectList(tagWrapper);
            Map<String, Long> organTagMap = tags.stream().collect(Collectors.toMap(OrganTag::getOrganTagCode, OrganTag::getOrganTagId));

            List<Production> productions = new ArrayList<>();
            Date now = new Date();
            for (ProductionInfoReq r : req.getProductions()) {
                Long organTagId = null;
                SpeciesWaxCodeTemplate speciesWaxCodeTemplate = speciesWaxCodeTemplateMap.get(r.getWaxCodeId());
                if (speciesWaxCodeTemplate != null) {
                    organTagId = organTagMap.get(speciesWaxCodeTemplate.getOrganCode());
                }
                if (organTagId == null) {
                    continue;
                }
                Production production = new Production();
                // 专题ID
                production.setSpecialId(req.getProjectId());
                // 种属蜡块模板表ID
                production.setWaxCodeId(r.getWaxCodeId());
                // 脏器标签ID
                production.setOrganTagId(organTagId);
                // 种属ID
                production.setSpeciesId(speciesWaxCodeTemplate.getSpeciesId());
                // 脏器名称
                production.setOrganName(speciesWaxCodeTemplate.getOrganName());
                // 英文名称
                production.setOrganEn(speciesWaxCodeTemplate.getOrganEn());
                // 脏器编码
                production.setOrganCode(speciesWaxCodeTemplate.getOrganCode());
                // 脏器缩写
                production.setAbbreviation(speciesWaxCodeTemplate.getAbbreviation());
                // 对应算法接口脏器编码：只记录不同的
                production.setAlgorithmMethod(speciesWaxCodeTemplate.getAlgorithmMethod());

                // 蜡块编号
                production.setWaxCode(r.getWaxCode());
                // 取材块数
                production.setBlockCount(r.getBlockCount());
                // 性别
                production.setSexFlag(r.getSexFlag());
                // 机构ID
                production.setOrganizationId(project.getOrganizationId());
                Long userid = SecurityUtils.getUserId();
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
        // 制片信息是否保存过
        Project update = new Project();
        update.setProjectId(req.getProjectId());
        update.setProductionSave(1);
        this.projectMapper.updateById(update);
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
            List<SpeciesWaxCodeTemplate> templates = this.speciesWaxCodeTemplateMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(templates)) {
                for (SpeciesWaxCodeTemplate template : templates) {
                    OrganVO vo = new OrganVO();
                    vo.setWaxCodeId(template.getId());
                    vo.setOrganName(template.getOrganName());
                    vo.setOrganEn(template.getOrganEn());
                    list.add(vo);
                }
            }
        }
        return list;
    }

    /**
     * 制片信息是否保存过
     *
     * @param req 制片信息是否保存过
     * @return 制片信息是否保存过
     */
    @Override
    public R<ProductionHasSaveVO> productionHasSave(ProductionHasSaveReq req) {
        ProductionHasSaveVO vo = new ProductionHasSaveVO();
        vo.setHasSave(false);
        // 查询项目信息
        Project project = this.projectMapper.selectById(req.getProjectId());
        if (project != null && project.getProductionSave() != null && project.getProductionSave() == 1) {
            vo.setHasSave(true);
        }
        return R.ok(vo);
    }
}




