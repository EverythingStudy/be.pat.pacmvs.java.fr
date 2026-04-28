package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Species;
import cn.staitech.fr.domain.Structure;
import cn.staitech.fr.domain.StructureTagSet;
import cn.staitech.fr.vo.structure.StructureTagPageReq;
import cn.staitech.fr.vo.structure.StructureTagPageVo;
import cn.staitech.fr.vo.structure.StructureTagVo;
import cn.staitech.fr.mapper.SpeciesMapper;
import cn.staitech.fr.mapper.StructureMapper;
import cn.staitech.fr.mapper.StructureTagSetMapper;
import cn.staitech.fr.service.StructureService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.system.api.RemoteAnnotationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.service.StructureTagService;
import cn.staitech.fr.mapper.StructureTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static cn.staitech.common.security.utils.SecurityUtils.isAdmin;
import static cn.staitech.fr.constant.Constants.*;

/**
 * @author mugw
 * @version 1.0
 * @description 结构标签管理
 * @date 2025/5/14 13:44:14
 */
@Service
public class StructureTagServiceImpl extends ServiceImpl<StructureTagMapper, StructureTag>
        implements StructureTagService {

    @Resource
    private StructureTagSetMapper structureTagSetMapper;
    @Resource
    private StructureMapper structureMapper;
    @Resource
    private StructureService structureService;
    @Resource
    private SpeciesMapper speciesMapper;
    @Resource
    private RemoteAnnotationService remoteAnnotationService;

    /**
     * 校验标签集是否存在
     */
    private boolean checkStructureTagSetExist(StructureTagVo req) {
        long count = structureTagSetMapper.selectCount(Wrappers.<StructureTagSet>lambdaQuery()
                .eq(StructureTagSet::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTagSet::getDelFlag, false));
        return count > 0;
    }

    /**
     * 校验标签是否存在
     */
    private boolean checkStructureTagExist(StructureTagVo req) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTag::getStructureId, req.getStructureId())
                .eq(StructureTag::getDelFlag, false)
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验标签颜色是否存在
     */
    private boolean checkStructureTagColorExist(StructureTagVo req) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTag::getHex, req.getHex())
                .eq(StructureTag::getDelFlag, false)
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验标签名称
     */
    private boolean checkStructureTagNameExist(StructureTagVo req, StructureTagSet structureTagSet) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTag::getStructureTagName, structureTagSet.getStructureTagSetName() + req.getStructureName())
                .eq(StructureTag::getDelFlag, false)
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验结构structureId是否存在
     */
    private boolean checkStructureIdExist(StructureTagVo req, StructureTagSet structureTagSet) {
        long count = structureMapper.selectCount(Wrappers.<Structure>lambdaQuery()
                .eq(Structure::getStructureId, req.getStructureId())
                .eq(Structure::getSpeciesId, structureTagSet.getSpeciesId())
                .eq(Structure::getOrganCode, structureTagSet.getOrganCode())
                .eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验结构名称是否存在
     */
    private boolean checkStructureNameExist(StructureTagVo req, StructureTagSet structureTagSet) {
        long count = structureMapper.selectCount(Wrappers.<Structure>lambdaQuery()
                .eq(Structure::getName, req.getStructureName())
                .eq(Structure::getSpeciesId, structureTagSet.getSpeciesId())
                .eq(Structure::getOrganCode, structureTagSet.getOrganCode())
                .eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 提取新增结构逻辑
     */
    private void addStructure(StructureTagVo req, StructureTagSet structureTagSet) throws Exception {
        Structure structure = new Structure();
        structure.setStructureId(req.getStructureId());
        structure.setName(req.getStructureName());
        structure.setNameEn(req.getStructureNameEn());
        structure.setSpeciesId(structureTagSet.getSpeciesId());
        structure.setOrganCode(structureTagSet.getOrganCode());
        structure.setType(STRUCTURE_RO);
        structure.setOrganizationId(SecurityUtils.getOrganizationId());
        structureMapper.insert(structure);
    }

    /**
     * 校验结构标签中结构id是否存在
     */
    private boolean checkStructureIdExist(StructureTagVo req) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureId, req.getStructureId())
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .ne(StructureTag::getStructureTagId, req.getStructureTagId())
                .eq(StructureTag::getDelFlag, false)
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验结构标签中颜色是否重复
     */
    private boolean checkStructureTagHexExist(StructureTagVo req) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTag::getHex, req.getHex())
                .eq(StructureTag::getDelFlag, false)
                .ne(StructureTag::getStructureTagId, req.getStructureTagId())
                .eq(StructureTag::getOrganizationId, SecurityUtils.getOrganizationId()));
        return count > 0;
    }

    /**
     * 校验结构标签名称是否重复
     */
    private boolean checkStructureTagNameExistForUpdate(StructureTagVo req, StructureTagSet structureTagSet, Species species) {
        long count = baseMapper.selectCount(Wrappers.<StructureTag>lambdaQuery()
                .eq(StructureTag::getStructureTagSetId, req.getStructureTagSetId())
                .eq(StructureTag::getStructureTagName, species.getName() + structureTagSet.getStructureTagSetName() + req.getStructureName())
                .eq(StructureTag::getDelFlag, false)
                .ne(StructureTag::getStructureTagId, req.getStructureTagId()));
        return count > 0;
    }

    /**
     * 添加结构标签
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<StructureTag> addTag(StructureTagVo req) throws Exception {
        Integer type = req.getType();
        String rgb = req.getRgb();
        String hex = req.getHex();
        Long structureTagSetId = req.getStructureTagSetId();
        Integer orderNumber = req.getOrderNumber();
        String structureId = req.getStructureId();
        StructureTagSet structureTagSet = structureTagSetMapper.selectById(structureTagSetId);
        if (!checkStructureTagSetExist(req)) {
            return R.fail(MessageSource.M("INDICATOR_ABSENT"));
        } else if (checkStructureTagExist(req)) {
            return R.fail(MessageSource.M("CATEGORY_CODE_CHECK_EXIST"));
        } else if (checkStructureTagColorExist(req)) {
            return R.fail(MessageSource.M("COLOR_NAME_CHECK_EXIST"));
        } else if (checkStructureTagNameExist(req, structureTagSet)) {
            return R.fail(MessageSource.M("CATEGORY_NAME_CHECK_EXIST"));
        }
        if (type == TAG_TYPE_CUSTOM) {

            if (checkStructureIdExist(req, structureTagSet)) {
                return R.fail(MessageSource.M("CATEGORY_CODE_CHECK_EXIST"));
            } else if (checkStructureNameExist(req, structureTagSet)) {
                return R.fail(MessageSource.M("CATEGORY_NAME_CHECK_EXIST"));
            } else {
                addStructure(req, structureTagSet);
            }
        }
        Species species = speciesMapper.selectOne(Wrappers.<Species>lambdaQuery()
                .eq(Species::getSpeciesId, structureTagSet.getSpeciesId())
                .eq(Species::getOrganizationId, SecurityUtils.getOrganizationId()));
        StructureTag structureTag = new StructureTag();
        structureTag.setStructureId(structureId);
        structureTag.setRgb(rgb);
        structureTag.setHex(hex);
        structureTag.setStructureTagSetId(structureTagSetId);
        structureTag.setOrderNumber(orderNumber);
        structureTag.setStructureTagName(species.getName() + structureTagSet.getStructureTagSetName() + req.getStructureName());
        structureTag.setCreateBy(SecurityUtils.getUserId());
        structureTag.setOrganizationId(SecurityUtils.getOrganizationId());
        structureTag.setGroupInnerOrder(STRUCTURE_RO_GROUP_NUMBER);
        structureTag.setType(type);
        baseMapper.insert(structureTag);
        return R.ok(structureTag, MessageSource.M("OPERATE_SUCCEED"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<StructureTag> editTag(StructureTagVo req) throws Exception {

        Integer type = req.getType();
        StructureTagSet structureTagSet = structureTagSetMapper.selectById(req.getStructureTagSetId());
        Species species = speciesMapper.selectOne(Wrappers.<Species>lambdaQuery()
                .eq(Species::getSpeciesId, structureTagSet.getSpeciesId())
                .eq(Species::getOrganizationId, SecurityUtils.getOrganizationId()));
        boolean isUsage = remoteAnnotationService.checkTagUsageStatus(req.getStructureTagId()).getData();
        if (isUsage) {
            return R.fail(MessageSource.M("CATEGORY_NO_EDIT"));
        } else if (!checkStructureTagSetExist(req)) {
            return R.fail(MessageSource.M("INDICATOR_ABSENT"));
        } else if (checkStructureIdExist(req)) {
            return R.fail(MessageSource.M("CATEGORY_CODE_CHECK_EXIST"));
        } else if (checkStructureTagHexExist(req)) {
            return R.fail(MessageSource.M("COLOR_NAME_CHECK_EXIST"));
        } else if (checkStructureTagNameExistForUpdate(req, structureTagSet,species)) {
            return R.fail(MessageSource.M("CATEGORY_NAME_CHECK_EXIST"));
        }

        if (type == TAG_TYPE_CUSTOM) {
            if (checkStructureIdExist(req, structureTagSet)) {
                structureService.update(Wrappers.<Structure>lambdaUpdate()
                        .eq(Structure::getStructureId, req.getStructureId())
                        .eq(Structure::getSpeciesId, structureTagSet.getSpeciesId())
                        .eq(Structure::getOrganCode, structureTagSet.getOrganCode())
                        .eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId())
                        .set(Structure::getName, req.getStructureName())
                        .set(Structure::getNameEn, req.getStructureNameEn()));
            } else if (checkStructureNameExist(req, structureTagSet)) {
                return R.fail(MessageSource.M("CATEGORY_NAME_CHECK_EXIST"));
            } else {
                addStructure(req, structureTagSet);
            }
        }
        StructureTag structureTag = new StructureTag();
        structureTag.setStructureTagId(req.getStructureTagId());
        structureTag.setStructureTagSetId(req.getStructureTagSetId());
        structureTag.setStructureTagName(species.getName() + structureTagSet.getStructureTagSetName() + req.getStructureName());
        structureTag.setRgb(req.getRgb());
        structureTag.setHex(req.getHex());
        structureTag.setOrderNumber(req.getOrderNumber());
        structureTag.setType(type);
        structureTag.setStructureId(req.getStructureId());
        structureTag.setUpdateBy(SecurityUtils.getUserId());
        structureTag.setUpdateTime(new Date());
        structureTag.setGroupInnerOrder(STRUCTURE_RO_GROUP_NUMBER);
        baseMapper.updateById(structureTag);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }

    @Override
    public R<CustomPage<StructureTagPageVo>> pageTag(StructureTagPageReq req) throws Exception {
        CustomPage<StructureTagPageVo> page = new CustomPage<>(req);
        if (!isAdmin(SecurityUtils.getOrganizationId())) {
            req.setOrganizationId(SecurityUtils.getOrganizationId());
        }
        baseMapper.pageTag(page, req);
        return R.ok(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R delTag(Long structureTagId) throws Exception {

        boolean isUsage = remoteAnnotationService.checkTagUsageStatus(structureTagId).getData();
        if (isUsage) {
            return R.fail(MessageSource.M("USED"));
        }
        StructureTag structureTag = baseMapper.selectById(structureTagId);
        if (structureTag == null) {
            return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
        } else {
            structureMapper.delete(Wrappers.<Structure>lambdaQuery()
                    .eq(Structure::getStructureId, structureTag.getStructureId())
                    .eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId()));
            baseMapper.deleteById(structureTagId);
        }
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }
}




