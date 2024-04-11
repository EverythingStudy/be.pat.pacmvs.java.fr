package cn.staitech.fr.enums;


public enum SysDictTypeEnum {
	organization(1, "sys_viscera"),
	position(2, "sys_position"),
	lesion(3, "sys_lesion"),
	ddefinition(4, "sys_ddefinition"),
	grade(5, "sys_grade"),
	sysvisceraorganization(6, "sys_viscera_organization");

    private int value;
    private String label;

    SysDictTypeEnum(int value, String label) {
        this.label = label;
        this.value = value;
    }


    public int value() {
        return value;
    }

    public String label() {
        return label;
    }
}
