package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Production;
import cn.staitech.fr.mapper.ProductionMapper;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.vo.project.ProductionReq;
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
}




