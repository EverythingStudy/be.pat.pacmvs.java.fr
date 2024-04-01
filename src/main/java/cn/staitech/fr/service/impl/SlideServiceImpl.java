package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Group;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.ImageListOutVO;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.mapper.SlideMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author admin
 * @description 针对表【fr_slide(专题选片表)】的数据库操作Service实现
 * @createDate 2024-03-29 13:33:37
 */
@Service
@Slf4j
public class SlideServiceImpl extends ServiceImpl<SlideMapper, Slide>
        implements SlideService {

    @Resource
    private GroupService groupService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R choiceSave(ChoiceSaveInVo req) {
        log.info("切片选择保存接口开始：");

        for (ImageListOutVO image : req.getImages()) {
            Slide slide = new Slide();
            slide.setCreateBy(SecurityUtils.getUserId());
            slide.setCreateTime(new Date());
            slide.setImageId(image.getImageId());
            slide.setSpecialId(req.getSpecialId());
            getExtInfo(image.getFileName(), slide, req.getSpecialId());
        }
        return R.ok();
    }

    @Override
    public PageResponse<SlideListQueryOut> slideListQuery(SlideListQueryIn req) {
        log.info("专题下切片列表查询接口开始：");
        PageResponse<SlideListQueryOut> pageResponse = new PageResponse<>();
        Page<SlideListQueryOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SlideListQueryOut> resp = this.baseMapper.slideListQuery(req);
        pageResponse.setTotal(page.getTotal());
        pageResponse.setList(resp);
        pageResponse.setPages(page.getPages());
        return pageResponse;
    }

    private Slide getExtInfo(String fileName, Slide slide, Long specialId) {
        String[] s = fileName.split(" ");
        if (s.length != 3) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        String s1 = this.baseMapper.selectBySpecialId(specialId);
        if (!s[0].equals(s1)) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        slide.setAnimalCode(StringUtils.substringBeforeLast(s[1], "-"));
        slide.setWaxCode(StringUtils.substringAfterLast(s[1], "-"));
        //判断性别数据
        if (!CommonConstant.MALE.equals(s[2].substring(s[2].length() - 1)) &&
                !CommonConstant.FEMALE.equals(s[2].substring(s[2].length() - 1))) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        slide.setGenderFlag(s[2].substring(s[2].length() - 1));
        //判断组别
        Group byId = groupService.getById(s[2].substring(0, s[2].length() - 1));
        if (ObjectUtils.isEmpty(byId)) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        slide.setGroupCode(s[2].substring(0, s[2].length() - 1));

        return slide;
    }


}




