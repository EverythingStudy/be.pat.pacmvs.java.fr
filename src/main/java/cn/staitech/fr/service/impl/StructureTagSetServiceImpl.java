package cn.staitech.fr.service.impl;

import cn.staitech.common.core.constant.Constants;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.vo.structure.StructureTagSetVo;
import cn.staitech.fr.mapper.OrganMapper;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.service.StructureTagService;
import cn.staitech.fr.utils.MessageSource;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.StructureTagSet;
import cn.staitech.fr.service.StructureTagSetService;
import cn.staitech.fr.mapper.StructureTagSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Date;
import static cn.staitech.fr.constant.Constants.TAG_TYPE_CUSTOM;
import static cn.staitech.fr.constant.Constants.TAG_TYPE_DROPDOWN;


/**
 * @author mugw
 * @version 1.0
 * @description 结构标签集管理
 * @date 2025/5/14 13:44:14
 */
@Slf4j
@Service
public class StructureTagSetServiceImpl extends ServiceImpl<StructureTagSetMapper, StructureTagSet>
        implements StructureTagSetService {

    @Resource
    private OrganMapper organMapper;
    @Resource
    private OrganService organService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private StructureTagService structureTagService;


    /**
     * 校验 OrganCode是否存在
     */
    private boolean checkOrganCodeExist(StructureTagSetVo req) {
        Long count = organMapper.selectCount(Wrappers.<Organ>lambdaQuery()
                .eq(Organ::getOrganCode, req.getOrganCode())
                .eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Organ::getSpeciesId, req.getSpeciesId()));
        return count > 0;
    }

    /**
     * 校验 OrganName是否存在
     */
    private boolean checkOrganNameExist(StructureTagSetVo req) {
        Long count = organMapper.selectCount(Wrappers.<Organ>lambdaQuery()
                .eq(Organ::getName, req.getOrganName())
                .eq(Organ::getSpeciesId, req.getSpeciesId())
                .eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 新增 organ
     */
    private void addOrgan(StructureTagSetVo req) {
        Organ organPo = new Organ();
        organPo.setName(req.getOrganName());
        organPo.setNameEn(req.getOrganNameEn());
        organPo.setOrganCode(req.getOrganCode());
        organPo.setSpeciesId(req.getSpeciesId());
        organPo.setOrganizationId(SecurityUtils.getOrganizationId());
        organMapper.insert(organPo);
    }

    /**
     * 提取设置标签集名称逻辑
     * @param tagSet
     * @param req
     */
    private void setTagSetName(StructureTagSet tagSet, StructureTagSetVo req) {
        if (req.getType() == TAG_TYPE_DROPDOWN) {
            Organ organ = organMapper.selectOne(Wrappers.<Organ>lambdaQuery()
                    .eq(Organ::getOrganCode, req.getOrganCode())
                    .eq(Organ::getSpeciesId, req.getSpeciesId())
                    .eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId()));
            if (organ != null) {
                tagSet.setStructureTagSetName(organ.getName());
                tagSet.setStructureTagSetNameEn(organ.getNameEn());
            }
        } else {
            tagSet.setStructureTagSetName(req.getOrganName());
            tagSet.setStructureTagSetNameEn(req.getOrganName());
        }
    }

    /**
     * @param req
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<StructureTagSet> addTagSet(StructureTagSetVo req) throws Exception {
        Long organizationId = SecurityUtils.getOrganizationId();
        //标签类型 0:下拉筛选标签；1:自定义标签
        Integer type = req.getType();
        Long tagSetCount = count(Wrappers.<StructureTagSet>lambdaQuery()
                .eq(StructureTagSet::getSpeciesId, req.getSpeciesId())
                .eq(StructureTagSet::getOrganCode, req.getOrganCode())
                .eq(StructureTagSet::getDelFlag, false)
                .eq(StructureTagSet::getOrganizationId, organizationId));
        if (tagSetCount > 0) {
            return R.fail(MessageSource.M("INDICATOR_EXIST"));
        }
        boolean isOrganCodeExist = checkOrganCodeExist(req);
        if (type == TAG_TYPE_CUSTOM) {
            if (isOrganCodeExist) {
                return R.fail(MessageSource.M("InsertOrganVO.ORGANID.EXIST"));
            } else if (checkOrganNameExist(req)) {
                return R.fail(MessageSource.M("InsertOrganVO.NAME.EXIST"));
            }
            addOrgan(req);
        } else if (type == TAG_TYPE_DROPDOWN && !isOrganCodeExist) {
            return R.fail(MessageSource.M("ORGAN_NOT_EXIST")); // 统一使用国际化消息
        }
        StructureTagSet tagSet = new StructureTagSet();
        tagSet.setSpeciesId(req.getSpeciesId());
        tagSet.setOrganCode(req.getOrganCode());
        tagSet.setOrganizationId(organizationId);
        tagSet.setType(type);
        tagSet.setCreateBy(SecurityUtils.getUserId());
        tagSet.setUpdateBy(SecurityUtils.getUserId());
        setTagSetName(tagSet, req);
        baseMapper.insert(tagSet);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<StructureTagSet> updateTagSet(StructureTagSetVo req) throws Exception {

        if (isBoundProject(req.getSpeciesId())) {
            return R.fail(MessageSource.M("ALREADY_BOUND"));
        }
        Long organizationId = SecurityUtils.getOrganizationId();
        if (req.getType() == TAG_TYPE_CUSTOM) {
            if (checkOrganNameExist(req)) {
                return R.fail(MessageSource.M("InsertOrganVO.NAME.EXIST"));
            }else if (checkOrganCodeExist(req)) {
                organService.update(Wrappers.<Organ>lambdaUpdate()
                        .eq(Organ::getOrganCode, req.getOrganCode())
                        .eq(Organ::getSpeciesId, req.getSpeciesId())
                        .eq(Organ::getOrganizationId, organizationId)
                        .set(Organ::getNameEn, req.getOrganNameEn())
                        .set(Organ::getName, req.getOrganName()));
            }else{
                addOrgan(req);
            }
        }
        StructureTagSet tagSet = new StructureTagSet();
        tagSet.setStructureTagSetId(req.getStructureTagSetId());
        tagSet.setUpdateTime(new Date());
        tagSet.setUpdateBy(SecurityUtils.getUserId());
        setTagSetName(tagSet, req);
        updateById(tagSet);
        return R.ok(tagSet, MessageSource.M("OPERATE_SUCCEED"));
    }

    /**
     * 判断是否已经绑定项目
     *
     * @param speciesId
     * @return
     */
    @Override
    public boolean isBoundProject(String speciesId) {
        long projectCount = projectMapper.selectCount(Wrappers.<Project>lambdaQuery()
                .eq(Project::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Project::getSpeciesId, speciesId)
                .eq(Project::getDelFlag, Constants.DEL_FLAG_NORMAL));
        if (projectCount > 0) {
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<String> delTagSet(Long id) throws Exception {
        StructureTagSet structureTagSet = getById(id);
        if (structureTagSet == null) {
            return R.fail("无此结构标签集");
        }
        if (isBoundProject(structureTagSet.getSpeciesId())) {
            return R.fail(MessageSource.M("ALREADY_BOUND_NO_DEL"));
        }
        boolean structureTagUpdateFlag = structureTagService.update(Wrappers.<StructureTag>lambdaUpdate()
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(StructureTag::getStructureTagSetId, structureTagSet.getStructureTagSetId())
                .set(StructureTag::getDelFlag, true));
        log.info("结构标签集:[{}],删除结构标签:[{}]", structureTagSet, structureTagUpdateFlag);
        if (structureTagSet.getType() == TAG_TYPE_CUSTOM) {
            int organNum = organMapper.delete(Wrappers.<Organ>lambdaQuery()
                    .eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId())
                    .eq(Organ::getOrganCode, structureTagSet.getOrganCode())
                    .eq(Organ::getSpeciesId, structureTagSet.getSpeciesId()));
            log.info("结构标签集:[{}],删除脏器:[{}]", structureTagSet, organNum);
        }
        structureTagSet.setDelFlag(true);
        boolean structureTagSetUpdateFlag = updateById(structureTagSet);
        log.info("结构标签集:[{}],删除结构标签集:[{}]", structureTagSet, structureTagSetUpdateFlag);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }
}




