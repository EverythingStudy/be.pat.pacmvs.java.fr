package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.domain.WaxBlockNumber;
import cn.staitech.fr.domain.in.WaxBlockNumberEditIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.mapper.WaxBlockNumberMapper;
import cn.staitech.fr.service.TopicService;
import cn.staitech.fr.service.WaxBlockNumberService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;


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
        waxBlockNumber.setId(id);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R upload(MultipartFile file) throws IOException {
        log.info("导入文件接口开始：");
        File file1 = new File("D:/words");
        FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
        FileInputStream fis = new FileInputStream(file1);
        XWPFDocument document = new XWPFDocument(fis);
        //解析专题号
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        String text = paragraphs.get(1).getText().trim();
        String s = StringUtils.substringAfter(text, "：");
        //专题号
        String topicName = StringUtils.substringBefore(s, "动物种属");
        log.info("专题号：{}",topicName);
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Topic::getTopicName,topicName);
        queryWrapper.eq(Topic::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        Topic topic = topicService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(topic)){
            return R.fail("上传文件专题不存在！");
        }
        WaxBlockNumber waxBlockNumber = new WaxBlockNumber();
        waxBlockNumber.setTopicId(topic.getTopicId());
        waxBlockNumber.setCreateBy(SecurityUtils.getUserId());
        waxBlockNumber.setCreateTime(new Date());
        waxBlockNumber.setTopicName(topic.getTopicName());
        waxBlockNumber.setFileName(file.getOriginalFilename());
        waxBlockNumber.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        //种属
        String speciesName = StringUtils.substringAfterLast(text, "：");
        log.info("种属：{}",speciesName);

        System.out.println("--"+text);

        // 读取每个表格
        List<XWPFTable> tables = document.getTables();
        XWPFTable xwpfTable = tables.get(0);
        List<XWPFTableRow> rows = xwpfTable.getRows();
        for (int i=1;i<rows.size();i++){
            System.out.println(rows.get(i).getTableCells().stream()
                    .map(cell -> cell.getText().trim())
                    .reduce((cell1, cell2) -> cell1 + " | " + cell2)
                    .orElse(""));
        }

        fis.close();
        return R.ok();
    }
}
