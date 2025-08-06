package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Production;
import cn.staitech.fr.mapper.ProductionMapper;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.vo.project.OrganVO;
import cn.staitech.fr.vo.project.ProductionReq;
import cn.staitech.fr.vo.project.ProductionSaveReq;
import cn.staitech.fr.vo.project.ProductionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 专题制片信息
 *
 * @author yxy
 */
@Service
public class ProductionServiceImpl extends ServiceImpl<ProductionMapper, Production> implements ProductionService {
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
        return Collections.emptyList();
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




