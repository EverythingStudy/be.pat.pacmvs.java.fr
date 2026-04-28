package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Production;
import cn.staitech.fr.vo.project.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 专题制片信息
 *
 * @author yxy
 */
public interface ProductionService extends IService<Production> {
    /**
     * 制片信息列表
     *
     * @param req 制片信息参数
     * @return 制片信息结果
     */
    List<ProductionVO> list(ProductionReq req);

    /**
     * 蜡块编号下拉列表
     *
     * @param req 制片信息参数
     * @return 蜡块编号下拉列表
     */
    List<String> waxCodeList(ProductionReq req);

    /**
     * 保存制片信息
     *
     * @param req 制片信息参数
     * @return 结果
     */
    R<String> save(ProductionSaveReq req);

    /**
     * 种属脏器下拉列表（取自种属蜡块模板数据）
     *
     * @param req 制片信息参数
     * @return 种属脏器下拉列表（取自种属蜡块模板数据）
     */
    List<OrganVO> organList(ProductionReq req);

    /**
     * 制片信息是否保存过
     *
     * @param req 制片信息是否保存过
     * @return 制片信息是否保存过
     */
    R<ProductionHasSaveVO> productionHasSave(ProductionHasSaveReq req);
}
