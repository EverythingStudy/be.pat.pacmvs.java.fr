package cn.staitech.fr;

import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = StaiTechFrApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class FileTest {

    @Resource
    JsonTaskParserService jsonTaskParserService;


    @Test
    public void process() {
        String content = "{\n" +
                "  \"code\": \"Fine_contour\",\n" +
                "  \"imageId\":10906,\n" +
                "  \"slideId\":1,\n" +
                "  \"singleId\":144,\n" +
                "  \"organizationId\":1,\n" +
                "  \"algorithmName\":\"全脏器精细轮廓\",\n" +
                "  \"fileUrlList\":[\"D:/2.0-20240510/1/16906A.json\"]\n" +
                "}";





    jsonTaskParserService.input(content);


    }


}
