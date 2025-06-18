package cn.staitech.fr.utils;

import java.util.ArrayList;
import java.util.List;
import cn.staitech.fr.constant.Constants;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2025/5/20 13:32:10
 */
public class ProjectButtonGenerator {

    public static final String DETAIL = "DETAIL";
    public static final String DEL = "DEL";
    public static final String CONFIG = "CONFIG";
    public static final String PAUSE = "PAUSE";
    public static final String START = "START";
    public static final String COMPLETE = "COMPLETE";
    public static final String LOG = "LOG";
    public static final String ARCHIVE = "ARCHIVE";
    public static final String CANCEL_COMPLETE = "CANCEL_COMPLETE";


    /**
     * 根据项目状态和成员角色生成操作按钮组合
     *
     * @param projectStatus 项目状态
     * @param memberRole    成员角色（使用整型表示）
     * @return 操作按钮集合
     */
    public static List<String> generateButtons(int projectStatus, int memberRole) {
        List<String> buttons = new ArrayList<>();

        switch (projectStatus) {
            case Constants.STATUS_PENDING:
                addPendingButtons(buttons, memberRole);
                break;
            case Constants.STATUS_RUNNING:
                addInProgressButtons(buttons, memberRole);
                break;
            case Constants.STATUS_PAUSED:
                addPausedButtons(buttons, memberRole);
                break;
            case Constants.STATUS_COMPLETED:
                addCompletedOrArchivedButtons(buttons, memberRole);
                break;
            case Constants.STATUS_ARCHIVED:
                addCompletedOrArchivedButtons(buttons, memberRole);
                break;
            default:
                // 未知状态不添加按钮
                break;
        }
        buttons.add(LOG);
        return buttons;
    }

    // 待启动状态下的按钮
    private static void addPendingButtons(List<String> buttons, int memberRole) {
        //配置、启动、删除、日志
        buttons.add(CONFIG);
        buttons.add(START);
        buttons.add(DEL);
    }

    // 进行中状态下的按钮
    private static void addInProgressButtons(List<String> buttons, int memberRole) {
        if (isProjectOwnerOrAdmin(memberRole)) {
            //详情、配置、暂停、完成、日志
            buttons.add(DETAIL);
            buttons.add(CONFIG);
            buttons.add(PAUSE);
            buttons.add(COMPLETE);
        } else if (isProjectMember(memberRole)) {
            //详情、日志
            buttons.add(DETAIL);
        }
    }

    // 暂停状态下的按钮
    private static void addPausedButtons(List<String> buttons, int memberRole) {
        if (isProjectOwnerOrAdmin(memberRole)) {
            //配置、启动、完成、日志
            buttons.add(CONFIG);
            buttons.add(START);
            buttons.add(COMPLETE);
        }
    }

    // 完成或归档状态下的按钮
    private static void addCompletedOrArchivedButtons(List<String> buttons, int memberRole) {
        if (isProjectOwnerOrAdmin(memberRole)) {
            //取消完成、归档、日志
            buttons.add(CANCEL_COMPLETE);
            buttons.add(ARCHIVE);
        }
    }

    // 判断是否为项目负责人或管理员
    private static boolean isProjectOwnerOrAdmin(int memberRole) {
        return memberRole == Constants.ROLE_OWNER || memberRole == Constants.ROLE_ADMIN;
    }

    private static boolean isProjectMember(int memberRole) {
        return memberRole == Constants.ROLE_MEMBER;
    }


}


