package cn.staitech.fr.service;

import cn.staitech.fr.domain.Production;
import cn.staitech.fr.vo.project.ProductionReq;
import cn.staitech.fr.vo.project.ProductionVO;
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
}
