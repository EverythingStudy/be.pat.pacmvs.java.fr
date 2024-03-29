package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.WaxBlockNumber;
import cn.staitech.fr.domain.in.UploadWaxBlockIn;
import cn.staitech.fr.domain.in.WaxBlockNumberEditIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 蜡块编号表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-28
 */
public interface WaxBlockNumberService extends IService<WaxBlockNumber> {

    PageResponse<WaxBlockNumberListOut> getWaxList(WaxBlockNumberListIn req);

    R edit(WaxBlockNumberEditIn req);

    R delete(Long id);

    R upload( UploadWaxBlockIn req) throws IOException;
}
