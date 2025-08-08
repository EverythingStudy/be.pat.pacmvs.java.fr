package cn.staitech.fr.enmu;

public enum SpeciesTypeEnum {
    RAT(1, "大鼠"),
    MOUSE(2, "小鼠"),
    DOG(3, "犬"),
    MONKEY(4, "猴");

    private final int code;
    private final String name;

    SpeciesTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
