package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.domain.Species;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.WaxBlockNumber;
import cn.staitech.fr.domain.in.UploadWaxBlockIn;
import cn.staitech.fr.domain.in.WaxBlockNumberEditIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.mapper.WaxBlockNumberMapper;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.service.SpeciesService;
import cn.staitech.fr.service.TopicService;
import cn.staitech.fr.service.WaxBlockInfoService;
import cn.staitech.fr.service.WaxBlockNumberService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * <p>
 * 蜡块编号表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-28
 */
@Slf4j
@Service
public class WaxBlockNumberServiceImpl extends ServiceImpl<WaxBlockNumberMapper, WaxBlockNumber> implements WaxBlockNumberService {
    @Autowired
    private TopicService topicService;
    @Autowired
    private SpeciesService speciesService;
    @Autowired
    private OrganService organService;
    @Autowired
    private WaxBlockInfoService waxBlockInfoService;

    @Override
    public PageResponse<WaxBlockNumberListOut> getWaxList(WaxBlockNumberListIn req) {
        log.info("蜡块编号分页查询接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<WaxBlockNumberListOut> waxList = this.baseMapper.getWaxList(req);
        resp.setTotal(page.getTotal());
        resp.setList(waxList);
        resp.setPages(page.getPages());
        return resp;
    }

    @Override
    public R edit(WaxBlockNumberEditIn req) {
        log.info("蜡块编号编辑接口开始：");
        WaxBlockNumber waxBlockNumber = new WaxBlockNumber();
        BeanUtils.copyProperties(req, waxBlockNumber);
        updateById(waxBlockNumber);
        return R.ok();

    }

    @Override
    public R delete(Long id) {
        log.info("蜡块编号删除接口开始：");
        WaxBlockNumber waxBlockNumber = new WaxBlockNumber();
        waxBlockNumber.setDelFlag(CommonConstant.NUMBER_1);
        waxBlockNumber.setNumberId(id);
        this.baseMapper.updateById(waxBlockNumber);
        //删除详情信息
        WaxBlockInfo waxBlockInfo = new WaxBlockInfo();
        waxBlockInfo.setDelFlag(CommonConstant.NUMBER_1);
        LambdaQueryWrapper<WaxBlockInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaxBlockInfo::getNumberId, id);
        wrapper.eq(WaxBlockInfo::getDelFlag, CommonConstant.NUMBER_0);
        waxBlockInfoService.update(waxBlockInfo, wrapper);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R upload(UploadWaxBlockIn req) throws IOException {
        log.info("导入文件接口开始：");
        //校验专题是否已经存在
        LambdaQueryWrapper<WaxBlockNumber> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WaxBlockNumber::getTopicId,req.getTopicId());
        queryWrapper.eq(WaxBlockNumber::getDelFlag,CommonConstant.NUMBER_0);
        queryWrapper.eq(WaxBlockNumber::getOrganizationId,SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        List<WaxBlockNumber> waxList = list(queryWrapper);
        if (waxList.size()>0){
            return R.fail("该专题下已经存在蜡块编号信息，请勿重复导入");
        }
        File file1 = new File("D:/words");
        FileUtils.copyInputStreamToFile(req.getFile().getInputStream(), file1);
        FileInputStream fis = new FileInputStream(file1);
        XWPFDocument document = new XWPFDocument(fis);
        //解析专题号
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        String text = paragraphs.get(1).getText().trim();
        String s = StringUtils.substringAfter(text, "：");
        //专题号
        String topicName = StringUtils.substringBefore(s, "动物种属").trim();
     /*   if(StringUtils.isEmpty(topicName)){
            return R.fail("上传文件专题号不存在！");
        }*/
        log.info("专题号：{}", topicName);
        Topic topic = getTopic(topicName);
        if (ObjectUtils.isEmpty(topic)) {
            return R.fail("上传文件专题不存在！");
        }

        //种属
        String speciesName = StringUtils.substringAfterLast(text, "：").trim();
        speciesName = StringUtils.substringBeforeLast(speciesName, "(");
        log.info("种属：{}", speciesName);
        Species species = getSpecies(speciesName);
        if (ObjectUtils.isEmpty(species)) {
            return R.fail("上传文件种属不存在！");
        }
        WaxBlockNumber waxBlockNumber = getWaxBlockNumber(req, topic, species);
        this.baseMapper.insert(waxBlockNumber);

        // 解析详情信息
        List<XWPFTable> tables = document.getTables();
        XWPFTable xwpfTable = tables.get(0);
        List<XWPFTableRow> rows = xwpfTable.getRows();
        //用来存放雌雄顺序
        List<String> sexList = new ArrayList<>();

        if (CollectionUtils.isEmpty(rows)) {
            return R.fail("文件内容为空！");
        }
        //查询所有脏器
        Map<String, String> organList = extracted(species.getSpeciesId());
        //蜡块详情信息
        List<WaxBlockInfo> insertList = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {

            String s1 = rows.get(i).getTableCells().stream()
                    .map(cell -> cell.getText().trim())
                    .reduce((cell1, cell2) -> cell1 + " | " + cell2)
                    .orElse("");

            log.info("详情信息：{}", s1);
            if (s1.contains(CommonConstant.END_FLAG)) {
                continue;
            }
            String[] split = s1.split("\\|");
            if (split.length == 0) {
                return R.fail("文件内容格式错误！");
            } else if (split.length == 2) {
                if (s1.contains(CommonConstant.MALE_FLAG)) {
                    if (CommonConstant.MALE.equals(split[0].trim())) {
                        sexList.add(CommonConstant.MALE);
                        sexList.add(CommonConstant.FEMALE);
                        continue;
                    } else {
                        sexList.add(CommonConstant.FEMALE);
                        sexList.add(CommonConstant.MALE);
                        continue;
                    }

                }else{

                    String[] split1 = split[1].trim().split(CommonConstant.SEMICOLON_FLAG);
                    for (String s2 : split1) {
                        //设置详情信息
                        if("NA".equals(s2)){
                            continue;
                        }
                        extracted(getWaxBlockInfo(topic, species, waxBlockNumber, split[0].trim()), organList, insertList, s2,null);
                    }
                }
            }else {
                if (split.length !=4){
                    return R.fail("文件内容格式错误！");
                }
                String[] split1 = split[1].trim().split(CommonConstant.SEMICOLON_FLAG);
                for (String s2 : split1) {
                    if("NA".equals(s2)){
                        continue;
                    }
                    //设置详情信息
                    extracted(getWaxBlockInfo(topic, species, waxBlockNumber, split[0]), organList, insertList, s2,sexList.get(0));
                }
                String[] split2 = split[3].trim().split(CommonConstant.SEMICOLON_FLAG);
                for (String s2 : split2) {
                    if("NA".equals(s2)){
                        continue;
                    }
                    //设置详情信息
                    extracted(getWaxBlockInfo(topic, species, waxBlockNumber, split[0]), organList, insertList, s2,sexList.get(1));
                }

            }
        }
        fis.close();
        waxBlockInfoService.saveBatch(insertList);
        return R.ok();
    }

    private void extracted(WaxBlockInfo waxBlockInfo1, Map<String, String> organList, List<WaxBlockInfo> insertList, String s2,String genderFlag) {
        Pattern pattern = Pattern.compile(CommonConstant.EN_FLAG);
        log.info("错误"+s2);
        String[] parts = pattern.split(s2);
        String part = parts[0];
        log.info("脏器中文+数量:{}", part);
        String s3 = StringUtils.substringBeforeLast(part, CommonConstant.CODE_START);
        log.info("脏器中文:{}", s3);
        String s4 = StringUtils.substringAfterLast(part, CommonConstant.CODE_START);
        log.info("脏器数量+):{}", s4);
        String s5 = StringUtils.substringBeforeLast(s4, CommonConstant.CODE_END);
        log.info("脏器数量:{}", s5);
        String s6 = StringUtils.substringAfterLast(s2, part);
        log.info("脏器英文+数量:{}", s6);
        String s7 = StringUtils.substringBeforeLast(s6, CommonConstant.CODE_START);
        log.info("脏器英文:{}", s7);
        WaxBlockInfo waxBlockInfo = waxBlockInfo1;
        waxBlockInfo.setOrganId(organList.get(s3));
        waxBlockInfo.setOrganName(s3);
        waxBlockInfo.setOrganNumber(Integer.valueOf(s5));
        waxBlockInfo.setOrganNameEn(s7);
        waxBlockInfo.setGenderFlag(genderFlag);
        waxBlockInfo.setCreateBy(SecurityUtils.getUserId());
        waxBlockInfo.setCreateTime(new Date());
        insertList.add(waxBlockInfo);
    }

    private WaxBlockInfo getWaxBlockInfo(Topic topic, Species species, WaxBlockNumber waxBlockNumber, String s2) {
        WaxBlockInfo waxBlockInfo = new WaxBlockInfo();
        waxBlockInfo.setNumberId(
                waxBlockNumber.getNumberId());
        waxBlockInfo.setWaxCode(s2.trim());
        waxBlockInfo.setTopicId(topic.getTopicId());
        waxBlockInfo.setTopicName(topic.getTopicName());
        waxBlockInfo.setSpeciesId(species.getSpeciesId());
        waxBlockInfo.setSpeciesName(species.getName());
        waxBlockInfo.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        return waxBlockInfo;
    }

    /**
     * 脏器字典
     *
     * @return
     */
    private Map<String, String> extracted(String speciesCode) {
        LambdaQueryWrapper<Organ> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organ::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        wrapper.eq(Organ::getSpeciesCode, speciesCode);
        List<Organ> organs = organService.list(wrapper);
        if (CollectionUtils.isEmpty(organs)) {
            throw new RuntimeException("脏器信息为空！");
        }
        Map<String, String> collect = organs.stream().collect(Collectors.toMap(Organ::getName, Organ::getOrganId));
        return collect;
    }

    private Species getSpecies(String speciesName) {
        LambdaQueryWrapper<Species> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Species::getName, speciesName);
        wrapper.eq(Species::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        Species species = speciesService.getOne(wrapper);
        return species;
    }

    private Topic getTopic(String topicName) {
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Topic::getTopicName, topicName);
        queryWrapper.eq(Topic::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        Topic topic = topicService.getOne(queryWrapper);
        return topic;
    }

    private WaxBlockNumber getWaxBlockNumber(UploadWaxBlockIn req, Topic topic, Species species) {
        WaxBlockNumber waxBlockNumber = new WaxBlockNumber();
        waxBlockNumber.setTopicId(topic.getTopicId());
        waxBlockNumber.setCreateBy(SecurityUtils.getUserId());
        waxBlockNumber.setCreateTime(new Date());
        waxBlockNumber.setTopicName(topic.getTopicName());
        waxBlockNumber.setFileName(req.getFile().getOriginalFilename());
        waxBlockNumber.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        waxBlockNumber.setSpeciesId(species.getSpeciesId());
        waxBlockNumber.setSpeciesName(species.getName());
        return waxBlockNumber;
    }

    public static String[] splitByLetters(String str) {
        // 使用正则表达式分割字符串，"|"表示"或"，"\\B"表示非单词边界，即任意英文字母位置
        return str.split("\\B");
    }
}
