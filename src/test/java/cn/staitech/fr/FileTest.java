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

//    @Resource
//    FineContourParserStrategyImpl parserStrategy;


    /**
     * Harderian_gland 哈氏腺
     */
    @Test
    public void processHarderian_gland() {
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


        // Harderian_gland   Lacrimal_gland

        String content = "{\n" +
                "  \"code\": \"200\",\n" +
                "  \"msg\": \"\",\n" +
                "  \"algorithmCode\": \"Harderian_gland\",\n" +
                "  \"imageId\":10906,\n" +
                "  \"slideId\":115,\n" +
                "  \"singleId\":130,\n" +
                "  \"organizationId\":1,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"structureName\": \"cell\",\n" +
                "      \"fileUrl\": \"D:/2.0-20240510/hsx/ST16Rf-EY-HG-OE-282-1-000017-1F_10206D.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell\",\n" +
                "      \"fileUrl\": \"D:/2.0-20240510/hsx/ST16Rf-EY-HG-OE-282-1-000017-1F_10206E.json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        jsonTaskParserService.input(content);
    }


    /**
     * Thyroid_gland 大鼠甲状腺
     */
    @Test
    public void processThyroid_gland() {
        String content = "{\n" +
                "  \"code\": \"200\",\n" +
                "  \"msg\": \"\",\n" +
                "  \"algorithmCode\": \"Kidney\",\n" +
                "  \"imageId\":11010,\n" +
                "  \"slideId\":186,\n" +
                "  \"singleId\":328,\n" +
                "  \"organizationId\":1,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"structureName\": \"cell1\",\n" +
                "      \"fileUrl\": \"C:/Users/Administrator/Desktop/11B-v1.0/11B02D.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell2\",\n" +
                "      \"fileUrl\": \"C:/Users/Administrator/Desktop/11B-v1.0/11B02E.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell3\",\n" +
                "      \"fileUrl\": \"C:/Users/Administrator/Desktop/11B-v1.0/11B02F.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell4\",\n" +
                "      \"fileUrl\": \"C:/Users/Administrator/Desktop/11B-v1.0/11B03D.json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"structureName\": \"cell5\",\n" +
                "      \"fileUrl\": \"C:/Users/Administrator/Desktop/11B-v1.0/11B031.json\"\n" +
                "    }\n" +
//                "    {\n" +
//                "      \"structureName\": \"cell6\",\n" +
//                "      \"fileUrl\": \"D:/2.0-20240510/FeatureCollection/ST20Rf-PD-TG-320-1-000016/107089.json\"\n" +
//                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        jsonTaskParserService.input(content);
    }


    /**
     * Cerebellum 大鼠小脑
     */
    @Test
    public void processCerebellum() {
        String content = "{\n" +
                "  \"code\": \"200\",\n" +
                "  \"msg\": \"\",\n" +
                "  \"algorithmCode\": \"Cerebellum\",\n" +
                "  \"imageId\":10924,\n" +
//                 "  \"slideId\":85,\n" +
                "  \"slideId\":115,\n" +
                "  \"singleId\":154,\n" +
                "  \"organizationId\":1,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"structureName\": \"cell7\",\n" +
                "      \"fileUrl\": \"D:/2.0-20240510/ST20Rf-BR-SC-277-1-000016_CM_0/13E0A5.json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        jsonTaskParserService.input(content);
    }

    /**
     * Harderian_gland 哈氏腺
     */
    @Test
    public void processHarderian_gland1() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-d098-4e6a-8c41-03af9373add1\",\"categoryId\":2,\"singleId\":154,\"algorithmCode\":\"Harderian_gland\",\"AlgorithmName\":\"大鼠哈氏腺\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\",\"data\":[{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/102-v1.0/10206D.json\"},{\"structureName\":\"time1\",\"fileUrl\":\"哈氏腺腺泡推理耗时:718.70 s\"},{\"structureName\":\"fileurl2\",\"fileUrl\":\"D:/2.0-20240510/102-v1.0/10206E.json\"},{\"structureName\":\"time2\",\"fileUrl\":\"哈氏腺腺泡细胞核推理耗时:635.85 s\"}]}";
//        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-d098-4e6a-8c41-03af9373add1\",\"categoryId\":2,\"singleId\":154,\"algorithmCode\":\"Cerebellum\",\"AlgorithmName\":\"大鼠哈氏腺\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\",\"data\":[{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/102-v1.0/10206D.json\"},{\"structureName\":\"time1\",\"fileUrl\":\"哈氏腺腺泡推理耗时:718.70 s\"},{\"structureName\":\"fileurl2\",\"fileUrl\":\"D:/2.0-20240510/102-v1.0/10206E.json\"},{\"structureName\":\"time2\",\"fileUrl\":\"哈氏腺腺泡细胞核推理耗时:635.85 s\"}]}";
        jsonTaskParserService.input(content);
    }

    /**
     * 大鼠大脑 BR1_BR2 Brain
     */
    @Test
    public void process_Brain() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-1\",\"categoryId\":2,\"singleId\":154," +
                "\"algorithmCode\":\"Brain\",\"AlgorithmName\":\"大鼠大脑 \",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\",\"" +
                "data\":[{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/132-v1.0/13209C.json\"}," +
                "{\"structureName\":\"fileurl2\",\"fileUrl\":\"D:/2.0-20240510/132-v1.0/132004.json\"}," +
                "{\"structureName\":\"time2\",\"fileUrl\":\"大鼠大脑 :635.85 s\"}]}";
        jsonTaskParserService.input(content);
    }

    /**
     * 大鼠脾脏	SP Spleen
     */
    @Test
    public void process_Spleen() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-2\",\"categoryId\":2,\"singleId\":154," +
                "\"algorithmCode\":\"Spleen\",\"AlgorithmName\":\"大鼠脾脏\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\"," +
                "\"data\":[" +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/14504A.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/145004.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/145045.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/145046.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/145047.json\"}," +
                "{\"structureName\":\"fileurl2\",\"fileUrl\":\"D:/2.0-20240510/145-v1.0/145048.json\"}," +
                "{\"structureName\":\"time2\",\"fileUrl\":\"大鼠脾脏:635.85 s\"}" +
                "]}";
        jsonTaskParserService.input(content);
    }

    /**
     * 大鼠肝脏	LI Liver
     */
    @Test
    public void process_Liver() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-3\",\"categoryId\":2,\"singleId\":154," +
                "\"algorithmCode\":\"Liver\",\"AlgorithmName\":\"大鼠肝脏\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\"," +
                "\"data\":[" +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/11214A.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/11214D.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/112145.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/112146.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/112147.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/112-v1.0/112149.json\"}," +
                "{\"structureName\":\"time2\",\"fileUrl\":\"大鼠肝脏635.85 s\"}" +
                "]}";
        jsonTaskParserService.input(content);
    }

    /**
     * 大鼠腹股沟淋巴结	Inguinal lymph node
     */
    @Test
    public void process_InguinalLymphNode() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-4\",\"categoryId\":2,\"singleId\":154," +
                "\"algorithmCode\":\"Inguinal_lymph_node\",\"AlgorithmName\":\"大鼠腹股沟淋巴结\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\",\"data" +
                "\":[" +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/147-v1.0/14703E.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/147-v1.0/147030.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/147-v1.0/147031.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/147-v1.0/147032.json\"}," +
                "{\"structureName\":\"time2\",\"fileUrl\":\"大鼠腹股沟淋巴结:635.85 s\"}" +
                "]}";
        jsonTaskParserService.input(content);
    }

    /**
     * 大鼠颌下淋巴结 ML Mandibular_lymph_node
     */
    @Test
    public void process_MandibularLymphNode() {
        String content = "{\"redis_status_key\":\"172.31.2.101_0\",\"imageId\":10924,\"slideId\":115,\"organizationId\":1,\"task_id\":\"ff9d875a-5\",\"categoryId\":2,\"singleId\":154," +
                "\"algorithmCode\":\"Mandibular_lymph_node\",\"AlgorithmName\":\"大鼠颌下淋巴结\",\"occupy_time\":360,\"elapsed_time\":560,\"code\":200,\"msg\":\"success\",\"data" +
                "\":[" +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/148-v1.0/14803E.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/148-v1.0/148030.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/148-v1.0/148031.json\"}," +
                "{\"structureName\":\"fileurl1\",\"fileUrl\":\"D:/2.0-20240510/148-v1.0/148032.json\"}," +
                "{\"structureName\":\"time2\",\"fileUrl\":\"大鼠颌下淋巴结:635.85 s\"}" +
                "]}";
        jsonTaskParserService.input(content);
    }


//
//    @Test
//    public void process1231() {
//
//        parserStrategy.parseJson("C:\\Users\\Administrator\\Desktop\\ST16Rf-EY-HG-OE-282-1-000017-1F_10206D.json");
//
//    }
}
