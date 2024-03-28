package cn.staitech.fr.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author: wangfeng
 * @create: 2023-11-08 13:53:47
 * @Description: 缩略图
 */
public class ImgPicCompression {
    /**
     * 压缩图片方法
     *
     * @param oldFile    原图片路径
     * @param width      压缩宽
     * @param height     压缩高
     * @param outFile    压缩图片后,图片名称路径
     * @param percentage 是否等比压缩 若true宽高比率将将自动调整
     */
    public static String doCompress(String oldFile, int width, int height, String outFile, boolean percentage) {
        if (oldFile != null && width > 0 && height > 0) {
            Image srcFile = null;
            try {
                File file = new File(oldFile);
                // 文件不存在
                if (!file.exists()) {
                    return null;
                }
                /*读取图片信息*/
                srcFile = ImageIO.read(file);
                int new_w = width;
                int new_h = height;
                if (percentage) {
                    // 为等比缩放计算输出的图片宽度及高度
                    double rate1 = ((double) srcFile.getWidth(null)) / (double) width + 0.1;
                    double rate2 = ((double) srcFile.getHeight(null)) / (double) height + 0.1;
                    double rate = rate1 > rate2 ? rate1 : rate2;
                    new_w = (int) (((double) srcFile.getWidth(null)) / rate);
                    new_h = (int) (((double) srcFile.getHeight(null)) / rate);
                }
                /* 宽高设定*/
                BufferedImage tag = new BufferedImage(new_w, new_h, BufferedImage.TYPE_INT_RGB);
                tag.getGraphics().drawImage(srcFile, 0, 0, new_w, new_h, null);

                /* Windows操作系统特殊处理 */
                String osName = System.getProperty("os.name");
                if (osName.toLowerCase().contains("windows")) {
                    outFile = "D:\\" + outFile;
                }

                /* 目录是否存在 */
                if (!new File(outFile).getParentFile().isDirectory()) {
                    new File(outFile).getParentFile().mkdirs();
                }

                /*压缩之后临时存放位置*/
                FileOutputStream out = new FileOutputStream(outFile);

                ImageIO.write(tag, "JPG", out);
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                srcFile.flush();
            }
            return outFile;
        } else {
            return null;
        }
    }
}
