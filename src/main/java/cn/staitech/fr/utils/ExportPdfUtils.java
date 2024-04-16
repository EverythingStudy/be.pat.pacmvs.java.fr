package cn.staitech.fr.utils;

import cn.staitech.fr.domain.out.ExportListVO;
import cn.staitech.fr.domain.out.ExportVO;
import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.PdfSaveOptions;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author wudi
 * @Date 2024/4/12 10:16
 * @desc
 */

public class ExportPdfUtils {

    /**
     * @param data 导出数据
     * @throws IOException
     */
    public static void exportFile(String outFile, ExportVO data) throws IOException {
        //模板地址，存放在resources目录下
        String filePath = "templete/123.docx";
        //使用poi-tl进行模板处理
        ConfigureBuilder builder = Configure.builder();
        builder.useSpringEL(true);
        //执行循环策略
        LoopRowTableRenderPolicy strategy = new LoopRowTableRenderPolicy();
        //绑定集合对象
        builder.bind("list", strategy);
        builder.bind("table", strategy);
        //获取模板文件流
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        //ClassPathResource classPathResource = new ClassPathResource("templete/人工诊断报告.docx");
        //InputStream inputStream = classPathResource.getInputStream();
        assert inputStream != null;

        //组装数据
        //ExportVO data = createData();
        XWPFTemplate render = XWPFTemplate.compile(inputStream, builder.build()).render(data);
        try {
            //render.write(new FileOutputStream("D:/wordss/test555.docx"));
            render.write(new FileOutputStream(outFile));
        } catch (IllegalArgumentException e) {
            System.out.println("小问题思密达");
        }


        inputStream.close();


    }

    private static ExportVO createData() throws IOException {
        ExportVO data = new ExportVO();
        //普通文本
        data.setSpecialName("食品统计");
        data.setTopicName("蔬菜统计");
        //集合数据
        List<ExportListVO> list = new ArrayList<>();
        //添加循环文本数据
        data.setList(list);
        //添加表格数据
        data.setTable(list);

        //添加图片
        PictureRenderData img = new PictureRenderData(800, 200, "D:/image/555.png");
        //ByteArrayPictureRenderData img = new ByteArrayPictureRenderData(convertFileToByteArray(new File("D:/image/555.png")));
        data.setImg(img);
        return data;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("导出word开始");
        //exportFile(createData());
        System.out.println("导出word结束");
        System.out.println("转pdf开始");
        //convertDocx2Pdf("D:/wordss/test555.docx","D:/wordss/test555.pdf");
        System.out.println("转pdf结束");


    }

    /**
     * word转pdf
     * @param wordPath
     * @param pdfPath
     */
    /*public static void convertDocx2Pdf(String wordPath, String pdfPath) {
        OutputStream os = null;
        InputStream is = null;
        if (pdfPath.endsWith("/")) {
            pdfPath = pdfPath + File.separator;
        }
        try {
            is = new FileInputStream(new File(wordPath));
            WordprocessingMLPackage mlPackage = WordprocessingMLPackage.load(is);
            Mapper fontMapper = new IdentityPlusMapper();
            fontMapper.put("隶书", PhysicalFonts.get("LiSu"));
            fontMapper.put("宋体", PhysicalFonts.get("SimSun"));
            fontMapper.put("微软雅黑", PhysicalFonts.get("Microsoft Yahei"));
            fontMapper.put("黑体", PhysicalFonts.get("SimHei"));
            fontMapper.put("楷体", PhysicalFonts.get("KaiTi"));
            fontMapper.put("新宋体", PhysicalFonts.get("NSimSun"));
            fontMapper.put("华文行楷", PhysicalFonts.get("STXingkai"));
            fontMapper.put("华文仿宋", PhysicalFonts.get("STFangsong"));
            fontMapper.put("宋体扩展", PhysicalFonts.get("simsun-extB"));
            fontMapper.put("仿宋", PhysicalFonts.get("FangSong"));
            fontMapper.put("仿宋_GB2312", PhysicalFonts.get("FangSong_GB2312"));
            fontMapper.put("幼圆", PhysicalFonts.get("YouYuan"));
            fontMapper.put("华文宋体", PhysicalFonts.get("STSong"));
            fontMapper.put("华文中宋", PhysicalFonts.get("STZhongsong"));
            mlPackage.setFontMapper(fontMapper);
            os = new java.io.FileOutputStream(pdfPath);
            //docx4j  docx转pdf
            FOSettings foSettings = Docx4J.createFOSettings();
            foSettings.setWmlPackage(mlPackage);
            Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);

            is.close();//关闭输入流
            os.close();//关闭输出流

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            // 删除word文档的地址
            File file = new File(wordPath);
            if (file != null && file.isFile() && file.exists()) {
                file.delete();
            }
        }
    }*/

    /**
     * 下载压缩包
     *
     * @param response
     * @throws Exception
     */
    public static void writePdfZip(List<String> fileName, HttpServletResponse response, String zipName) throws Exception {

        response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename="
                + new String(zipName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(os);
        for (String file : fileName) {
            // 被压缩文件
            File file1 = new File(file);
            // 读取file文件
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            ZipEntry zipEntry = new ZipEntry(file1.getName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(bytes);
            zipOutputStream.flush();
            inputStream.close();
            zipOutputStream.closeEntry();
        }

        // 关闭各种流
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }

    /**
     * 下载单个
     *
     * @param response
     * @throws Exception
     */

    public static void downloadLocal(String path, HttpServletResponse response) throws IOException {
        // 读到流中
        InputStream inputStream = new FileInputStream(path);// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(path).getName();
        response.addHeader("Content-Disposition", "attachment; filename=" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[1024];
        int len;
        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
        while ((len = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, len);
        }
        inputStream.close();
    }

    public static void wordToPdf(String wordPath, String pdfPath) {
        FileOutputStream os = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(wordPath));

            //通过aspose-words.jar中的类转换文件
            Document wordDoc = new Document(is);
        /*if(SystemUtils.IS_OS_LINUX){
            //设置汉字字体，否则转换后的文档汉字会乱码。
            FontSettings settings = new FontSettings();
            settings.setFontsFolder("/mydata/fonts",false);
            wordDoc.setFontSettings(settings);
        }*/
            os = new FileOutputStream(new File(pdfPath));

            PdfSaveOptions pso = new PdfSaveOptions();
            wordDoc.save(os, pso);
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            // 删除word文档的地址
            File file = new File(wordPath);
            if (file != null && file.isFile() && file.exists()) {
                file.delete();
            }
        }
    }
}