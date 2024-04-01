package cn.staitech.fr.constant;

/**
 * .
 *
 * @author admin
 */
public class CommonConstant {


    /**
     * cache key
     */
    public static final String NUMBER_0 = "0";

    public static final String NUMBER_1 = "1";
    public static final String NUMBER_4 = "4";
    public static final String GLIDE_LINE = "_";
    public static final String FILE_SUFFIX = ".";
    public static final String FILE_SUFFIX_DOCX = ".docx";
    public static final String FILE_SUFFIX_JSON = ".json";
    public static final String FILE_SUFFIX_TXT = ".txt";
    public static final String FILE_SUFFIX_XLSX = ".xlsx";
    public static final String PATH = "path";
    public static final String FILE_PATH = "annotation";
    public static final String FILENAME = "filename";
    public static final String IMAGE_URL = "imageUrl";
    public static final String CHARACTER_SET_UTF8 = "UTF-8";
    public static final String CONTENT_TYPE = "application/json;charset=utf-8";
    public static final String HEADER = "Content-Disposition";
    public static final Integer NOT_START_REVIEW = 0;
    public static final Integer SUBMIT_REVIEW = 3;
    /**
     * Project
     */
    public static final String NONE = "none";
    public static final String REGIONS = "regions";
    public static final String REGION_ATTRIBUTES = "region_attributes";
    public static final String BONE_MARROW = "bone_marrow";
    public static final String SHAPE_ATTRIBUTES = "shape_attributes";
    public static final String ALL_POINTS_X = "all_points_x";
    public static final String ALL_POINTS_Y = "all_points_y";
    public static final String NAME = "name";
    public static final String POLYGON_WITH_HOLES = "polygon_with_holes";
    public static final String CHILDREN_CNTS = "children_cnts";
    public static final String VIA_IMG_METADATA = "_via_img_metadata";
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";
    public static final String ANNO_TYPE_MEASURE = "Measure";
    /**
     * Annotation
     */
    public static final String MULTIPOLYGON = "MultiPolygon";
    public static final String GEOMETRYCOLLECTION = "GeometryCollection";
    public static final String ADD_STATUS = "add";
    public static final String UPDATE_STATUS = "update";
    public static final String DELETE_STATUS = "delete";
    public static final String CLEAN = "clean";
    public static final String UNION = "UNION";
    public static final String DIFFERENCE = "DIFFERENCE";
    public static final String ANNO_SLIDE = "ANNO_SLIDE_";
    public static final Long SLIDE_CACHE_HOURS = 24L;
    public static final String ANNO_IMAGE = "ANNO_IMAGE_";
    public static final Long IMAGE_CACHE_HOURS = 24L;
    public static final String ANNO_MARKING = "ANNO_MARKING_";
    public static final Long MARKING_CACHE_HOURS = 24L;
    public static final String PROJECT_ANNOTATION_REL = "PROJECT_ANNOTATION_REL_";
    public static final Long PROJECT_ANNOTATION_REL_CACHE_DAYS = 365L;
	public static final String MATCHING_FAILED = "匹配失败";

    /**
     * 上传下载限制
     */
    public static final double UPLOAD_FILE_LIMIT = 300;
    public static final double DOWN_FILE_LIMIT = 300;

    /**
     * Viewer
     */
    public static final Double MICRON = 0.26;

    /**
     * structure
     */
    public static final String STRUCTURE_RO = "RO";
    public static final String STRUCTURE_ROA = "ROA";
    public static final String STRUCTURE_ROE = "ROE";

    /**
     * Statistic
     */
    public static final int THIRTEEN_DAY = 13;
    public static final int THIRTY_ONE_DAY = 31;
    public static final int ONE_YEAR = 366;
    public static final int THREE_YEAR = 1096;
    /**
     * Special Role
     */
    public static final String RESP = "resp";
    public static final String ANNO = "anno";
    public static final String READ = "read";
    public static final Long[] RESPONSIBLE_MENU = {1001L, 1002L, 1003L, 1004L, 1005L, 1006L, 1007L, 1008L, 1009L, 1010L, 1011L, 1012L, 1013L, 1014L, 1015L, 1016L, 1017L, 1018L, 1019L, 1020L, 1021L, 1022L, 1023L, 1024L, 1025L, 1026L, 1027L, 1028L, 1029L};
    public static final Long[] ANNOTATOR_MENU = {1001L, 1003L, 1004L, 1008L, 1009L, 1010L, 1011L, 1012L, 1013L, 1014L, 1015L};
    public static final Long[] READER_MENU = {1002L, 1005L, 1006L, 1007L, 1016L, 1017L, 1018L, 1019L, 1020L, 1021L, 1022L, 1023L, 1024L, 1025L, 1026L, 1027L, 1028L, 1029L};
    public static final String[] SPECIAL_ROLE_TYPE = new String[]{"专题负责人", "标注员", "普通阅片员"};
    /**
     * ProjectRole:构造3个默认项目角色类型：1、项目代表；2、项目管理者；3、项目贡献者
     */
    public static final String[] ROLE_TYPE = new String[]{"项目代表", "项目管理者", "项目贡献者"};
    /**
     * Excel表头 - ExamineScore
     */
    public static final String[] EXAMINESCORE_COLHEAD_KEY = {"项目名称", "切片编号", "答题者", "开始时间", "交卷时间", "应标个数(下限)", "实标个数", "miou拟合区间", "fiou拟合区间", "biou拟合区间", "tiou拟合区间", "个人拟合度", "考试结果"};
    public static final String[] EXAMINESCORE_COLHEAD_VALUE = {"projectName", "imageName", "nickName", "startTime", "endTime", "shouldNumber", "realityNumber", "miou", "fiou", "biou", "tiou", "personalFit", "examResults"};
    public static final String[] ALGORITHMASSESSMENT_COLHEAD_KEY = {"项目名称", "切片编号", "标注json", "对比json", "考核人员", "考核标签", "轮廓个数", "漏检率", "误检率", "miou", "fiou", "biou", "tiou", "结果时间"};
    public static final String[] ALGORITHMASSESSMENT_COLHEAD_VALUE = {"projectName", "imageName", "annotationJsonName", "jsonName", "examinePeople", "examineCategoryName", "outlineNumber", "missedDetectionRate", "falseDetectionRate", "miou", "fiou", "biou", "tiou", "createTime"};
    /**
     * Excel表头 - Measure - 构造表头的每个列头：名称 周长/长度 面积 内角 外角 平均间距 最小间距 最大间距 总数 测量人 创建时间
     */
    public static final String[] MEASURE_COLHEAD_KEY = {"名称", "周长/长度", "面积", "内角", "外角", "平均间距", "最小间距", "最大间距", "总数", "测量人", "创建时间"};
    public static final String[] MEASURE_COLHEAD_VALUE = {"measure_full_name", "perimeter", "area", "inner_angle", "exterior_angle", "mean_distance", "min_distance", "max_distance", "point_count", "annotation_owner", "create_time"};
    /**
     * Excel表头 - Export
     */
    public static final String[] EXPORT_COLHEAD_KEY = {"文件路径", "图像路径"};
    public static final String[] EXPORT_COLHEAD_VALUE = {"path", "imageUrl"};
    /**
     * TODO:统计模块switch case语句中用到，多语言版本暂未处理，后续建议优化
     */
    public static final String ANNOTATION_DATE = "标注日期";
    public static final String PROJECT = "项目";
    public static final String PATHOLOGY_INDICATOR = "病理指标";
    public static final String ANNOTATION_CATEGORY = "标注类别";
    public static final String USER = "成员";
    public static final String SLIDE = "图像";

    /**
     * 分表限制
     */
    public static final Integer PROJECT_NUMBER_LIMIT = 10;
    public static final Integer TABLE_RECORD_LIMIT = 10000000;

    private CommonConstant() {
        throw new IllegalStateException("CommonConstant class");
    }

    public static final String MALE_FLAG = "雄性Male";

    public static final String MALE = "M";

    public static final String FEMALE = "F";
    public static final String END_FLAG = "大体损伤";
    public static final String SEMICOLON_FLAG =";";
    public static final String EN_FLAG  = "[a-zA-Z]";
    public static final String CODE_START  = "(";
    public static final String CODE_END  = ")";
    
    /**
     * 识别算法
     */
    
    public static final String RECOGNITION_MODEL_NAME  = "图像识别算法";
}
