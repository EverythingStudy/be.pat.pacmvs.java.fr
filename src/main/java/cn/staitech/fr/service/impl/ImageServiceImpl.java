package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.constant.DictData;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.vo.image.ImageStatusVo;
import cn.staitech.fr.vo.image.ImageBatchIdsVO;
import cn.staitech.fr.vo.image.ImagePageReq;
import cn.staitech.fr.vo.image.ImageUpdateVO;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL;

/**
 * @author 94024
 * @description 针对表【tb_image】的数据库操作Service实现
 * @createDate 2024-09-10 10:21:48
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
        implements ImageService {
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private SlideMapper slideMapper;
    @Value("${file.path:/home/pacmvs}")
    private String localFilePath;

    /**
     * 切片状态字典 .
     */
    @Override
    public List<ImageStatusVo> status() {
        List<ImageStatusVo> list = new ArrayList<>();
        if (LanguageUtils.isEn()) {
            for (Map.Entry<Integer, String> entry : DictData.IMAGE_STATUS_MAP_EN.entrySet()) {
                list.add(new ImageStatusVo(entry.getKey(), entry.getValue()));
            }
        } else {
            for (Map.Entry<Integer, String> entry : DictData.IMAGE_STATUS_MAP.entrySet()) {
                list.add(new ImageStatusVo(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    /**
     * 根据提供的查询条件，获取分页的图片列表
     *
     * @throws ParseException 如果在解析时间字符串时发生错误
     */
    @Override
    public CustomPage<Image> pageImage(ImagePageReq req) {
        // 获取当前登录用户的信息
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        // 非管理员用户限制查询条件
        if (!sysUser.isAdmin()) {
            req.setOrganizationId(sysUser.getOrganizationId());
        }
        CustomPage<Image> customPage = new CustomPage<>(req);
        imageMapper.pageImage(customPage, req);
//        baseMapper.selectPage(customPage, Wrappers.<Image>lambdaQuery()
//                .like(StringUtils.isNotEmpty(req.getImageName()), Image::getImageName, req.getImageName())
//                .like(StringUtils.isNotEmpty(req.getTopicName()), Image::getTopicName, req.getTopicName())
//                .eq(!Objects.isNull(req.getOrganizationId()), Image::getOrganizationId, req.getOrganizationId())
//                .eq(!Objects.isNull(req.getStatus()), Image::getStatus, req.getStatus())
//                .ge(!Objects.isNull(req.getCreateTimeParams()), Image::getCreateTime, req.getCreateTimeParams() == null ? null : req.getCreateTimeParams().getBeginTime())
//                .le(!Objects.isNull(req.getCreateTimeParams()), Image::getCreateTime, req.getCreateTimeParams() == null ? null : req.getCreateTimeParams().getEndTime())
//                .orderByDesc(Image::getImageId));
        customPage.convert(this::renderImage);
        return customPage;
    }

    /**
     * 将Image对象转换为ImageListFindOut对象
     *
     * @param image Image对象
     * @return 转换后的ImageListFindOut对象
     */
    private Image renderImage(Image image) {
        // 根据语言设置文件状态的显示名称
        String fileStatus = LanguageUtils.isEn()
                ? DictData.IMAGE_STATUS_MAP_EN.get(image.getStatus())
                : DictData.IMAGE_STATUS_MAP.get(image.getStatus());
        image.setFileStatus(fileStatus);

        if (null != image.getAnalyzeStatus()) {
            String analyzeStatus = Objects.equals(Constants.IMAGE_NAME_PARSE_FAIL, image.getAnalyzeStatus()) ? "失败" : "成功";
            image.setAnalyzeStatusName(analyzeStatus);
        }

        // 设置删除状态
        //out.setDeleState(imageMapper.selectFrSlideCountByImageId(out.getImageId()) > 0 ? DataConstants.NUMBER_1 : DataConstants.NUMBER_0);
        return image;
    }

    /***
     * 批量删除图片（物理删除）
     * 1、若符合删除条件，图像物理删除；
     * 2、与切片列表有关联的不能删除；
     * @param req 图像ids
     * @return 不可删除的图片ID列表
     */
    @Override
    public List<Long> deleteBatchIds(ImageBatchIdsVO req) {

        // 不可删除的列表
        List<Long> forbidIds = new ArrayList<>();

        List<Long> imageIdList = req.getImageIdList();
        if (CollectionUtils.isNotEmpty(imageIdList)) {
            List<Slide> slideList = slideMapper.selectList(Wrappers.<Slide>lambdaQuery()
                    .in(Slide::getImageId, imageIdList)
                    .eq(Slide::getDelFlag, DEL_FLAG_NORMAL)
                    .select(Slide::getImageId));
            forbidIds = slideList.stream().map(Slide::getImageId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(forbidIds)){
                throw new RuntimeException("已关联项目的图像不可删除，请重新选择");
            }
            imageIdList.removeAll(forbidIds);
            if (CollectionUtils.isNotEmpty(imageIdList)){
                imageMapper.deleteBatchIds(imageIdList);
                NumberFormat formatter = NumberFormat.getNumberInstance();
                formatter.setMinimumIntegerDigits(3);
                formatter.setGroupingUsed(false);
                String orgCodeStr = "C" + formatter.format(SecurityUtils.getOrganizationId());
                for (Long imageId : imageIdList) {
                    String tilesPath = localFilePath + File.separator + orgCodeStr + File.separator + imageId + "TileGroup0";
                    // 异步执行删除切片目录
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            Path path = Paths.get(tilesPath);
                            if (Files.exists(path)) {
                                Files.walk(path)
                                        .sorted(Comparator.reverseOrder())
                                        .map(Path::toFile)
                                        .forEach(File::delete);
                                return true;
                            }
                            return false;
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete slice directory: " + tilesPath, e);
                        }
                    });
                }
            }
        }
        return forbidIds;
    }

    /**
     * 关联切片与专题、组织ID，没有专题则新添加
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ImageUpdateVO vo) {
        Image image = new Image();
        BeanUtils.copyProperties(vo, image);
        image.setFileName(StringUtils.substringBeforeLast(vo.getImageName(), "."));

        // 获取当前登录用户Id
        Long loginUser = SecurityUtils.getUserId();
        image.setUpdateBy(loginUser);
        return imageMapper.updateById(image);
    }


}




