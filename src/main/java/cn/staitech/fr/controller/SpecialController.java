package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialLockLog;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialsQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.service.AccessProjectRecordsService;
import cn.staitech.fr.service.SpecialLockLogService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.system.api.domain.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @Author wudi
 * @Date 2024/3/29 14:17
 * @desc
 */
@Api(value = "专题", tags = "专题")
@RestController
@RequestMapping("/special")
public class SpecialController  extends BaseController {

    @Autowired
    private SpecialService specialService;
    
    @Autowired
    private AccessProjectRecordsService accessProjectRecordsService;
    
    @Autowired
    private SpecialLockLogService specialLockLogService;
    
    @Resource
	private DiagnosisMapper diagnosisMapper;

    @ApiOperation(value = "专题列表分页查询")
    @PostMapping("/list")
    public R<PageResponse<SpecialListQueryOut>> list(@RequestBody @Validated SpecialListQueryIn req) {
        PageResponse<SpecialListQueryOut> resp = specialService.getSpecialList(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "专题新增")
    @PostMapping("/add")
    public R add(@RequestBody @Validated SpecialAddIn req) {
        return specialService.addSpecial(req);

    }

    @ApiOperation(value = "专题详情")
    @GetMapping("/info")
    public R<Special> info(@RequestParam("specialId") @ApiParam(name = "specialId", value ="专题id" ) Long specialId){
    	 //add访问记录
        AccessProjectRecords record=AccessProjectRecords.builder().projectId(specialId).userId(SecurityUtils.getUserId()).accessTime(new Date()).build();
        accessProjectRecordsService.save(record);
        return specialService.getInfoById(specialId);

    }

    @ApiOperation(value = "专题修改")
    @PostMapping("/edit")
    public R edit(@RequestBody @Validated SpecialEditIn req) {
        return specialService.editSpecial(req);
    }

    @ApiOperation(value = "专题删除")
    @GetMapping("/remove")
    public R remove(@RequestParam("specialId") @ApiParam(name = "specialId", value ="专题id" ) Long specialId) {
        return specialService.removeSpecial(specialId);
    }

    @ApiOperation(value = "编辑专题状态")
    @PostMapping("/editStatus")
    public R editStatus(@Validated @RequestBody EditSpecialStatusIn req) {
        return specialService.editSpecialStatus(req);

    }

    @ApiOperation(value = "染色类型列表", notes = "染色类型列表")
    @GetMapping("/colorType")
    public R<Map<Integer, String>> colorType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.COLOR_TYPE_EN;
        } else {
            map = Container.COLOR_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "试验类型列表", notes = "试验类型列表")
    @GetMapping("/trialType")
    public R<Map<Integer, String>> trialType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.TRIAL_TYPE_EN;
        } else {
            map = Container.TRIAL_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "专题状态列表", notes = "专题状态列表")
    @GetMapping("/specialStatus")
    public R<Map<Integer, String>> specialStatus() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.SPECIAL_STATUS_EN;
        } else {
            map = Container.SPECIAL_STATUS;
        }
        return R.ok(map);
    }


    @ApiOperation(value = "智能阅片-专题阅片列表")
    @PostMapping("/specialList")
    public R<PageResponse<SpecialListQueryOut>> specialList(@RequestBody @Validated SpecialsQueryIn req) {
        PageResponse<SpecialListQueryOut> resp = specialService.getSpecials(req);
        return R.ok(resp);
    }
    
    @ApiOperation(value = "专题锁定日志")
    @GetMapping("/getLockLog")
    public R<List<SpecialLockLog>> getLockLog(@RequestParam("specialId") @ApiParam(name = "specialId", value ="专题id" ) Long specialId){
    	//查询锁定记录
    	QueryWrapper<SpecialLockLog> queryWrapper = new QueryWrapper<>();
    	queryWrapper.eq("special_id", specialId);
    	queryWrapper.orderByDesc("create_time");
    	List<SpecialLockLog> list = specialLockLogService.list(queryWrapper);
    	if(CollectionUtils.isNotEmpty(list)){
    		for(SpecialLockLog log:list){
    			Map<String,Object> parm = new HashMap<>();
				parm.put("userId", log.getCreateBy());
				List<SysUser> loginUserList = diagnosisMapper.selectUserById(parm);
				log.setNickName(loginUserList.get(0).getNickName());
    		}
    	}
    	return R.ok(list);
    }
    
    @ApiOperation(value = "根据帐号查询昵称")
    @GetMapping("/getLockLog")
    public R<String> getNickName(@RequestParam("userName") @ApiParam(name = "userName", value ="帐号名称" ) String userName){
    	//查询锁定记录
    	Map<String,Object> parm = new HashMap<>();
		parm.put("userName", userName);
		List<SysUser> userList = diagnosisMapper.selectUserById(parm);
		String nickName = "";
		if(CollectionUtils.isNotEmpty(userList)){
			 nickName = userList.get(0).getNickName();
		}
    	return R.ok(nickName);
    }

}
