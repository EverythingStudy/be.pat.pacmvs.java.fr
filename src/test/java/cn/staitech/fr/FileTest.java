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
//        String content = "{\n" +
//                "  \"code\": \"200\",\n" +
//                "  \"msg\": \"\",\n" +
//                "  \"algorithmCode\": \"Fine_contour\",\n" +
//                "  \"imageId\":10906,\n" +
//                "  \"slideId\":1,\n" +
//                "  \"singleId\":144,\n" +
//                "  \"organizationId\":1,\n" +
//                "  \"data\": [{\n" +
//                "    \"fileurl1\": \"./save/10206D.json\",\n" +
//                "    \"fileurl2\": \"./save/10206E.json\"\n" +
//                "  }]\n" +
//                "}";

//
//        String content = "{\n" +
//                "  \"code\": \"200\",\n" +
//                "  \"msg\": \"\",\n" +
//                "  \"algorithmCode\": \"Lacrimal_gland\",\n" +
//                "  \"imageId\":10906,\n" +
//                "  \"slideId\":1,\n" +
//                "  \"singleId\":144,\n" +
//                "  \"organizationId\":1,\n" +
//                "  \"data\": {\n" +
//                "    \"cell\": {\n" +
//                "      \"code\": 0,\n" +
//                "      \"data\": {\n" +
//                "        \"fileurl_list\": \"./result_new/16906E.json\"\n" +
//                "      },\n" +
//                "      \"msg\": \"运行成功\"\n" +
//                "    },\n" +
//                "    \"daoguan\": {\n" +
//                "      \"code\": 0,\n" +
//                "      \"data\": {\"fileurl_list\": \"./result_new/16906F.json\"},\n" +
//                "      \"msg\": \"运行成功\"\n" +
//                "    },\n" +
//                "    \"jianzhi\": {\n" +
//                "      \"code\": 0,\n" +
//                "      \"data\": {\"fileurl_list\": \"./result_new/169027.json\"},\n" +
//                "      \"msg\": \"运行成功\"\n" +
//                "    },\n" +
//                "    \"dingbubaozhi\": {\n" +
//                "      \"code\": 0,\n" +
//                "      \"data\": {\"fileurl_list\": \"./result_new/16906A.json\"},\n" +
//                "      \"msg\": \"运行成功\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";


        String content = "{\n" +
                "  \"code\": \"200\",\n" +
                "  \"msg\": \"\",\n" +
                "  \"algorithmCode\": \"Harderian_gland\",\n" +
                "  \"imageId\":10906,\n" +
                "  \"slideId\":1,\n" +
                "  \"singleId\":144,\n" +
                "  \"organizationId\":1,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"structureName\": \"cell\",\n" +
                "      \"fileUrl\": \"./result_new/16906E.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell\",\n" +
                "      \"fileUrl\": \"./result_new/16906E.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell\",\n" +
                "      \"fileUrl\": \"./result_new/16906E.json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";
        jsonTaskParserService.input(content);


    }


}
