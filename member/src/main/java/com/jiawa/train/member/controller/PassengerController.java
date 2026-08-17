package com.jiawa.train.member.controller;

import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.member.req.PassengerQueryReq;
import com.jiawa.train.member.req.PassengerSaveReq;
import com.jiawa.train.member.resp.PassengerQueryResp;
import com.jiawa.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 乘车人业务 REST 控制器。 */
@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;

    /**
     * 新增或修改乘车人。id 为空时新增并绑定当前登录会员；id 非空时按主键更新。
     *
     * @param req 乘车人保存请求
     * @return 空业务体
     */
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq req) {
        passengerService.save(req);
        return new CommonResp<>();
    }

    /**
     * 分页查询乘车人列表，自动限定为当前登录会员的数据。
     *
     * @param req 分页查询条件（memberId 由本方法从上下文注入）
     * @return 分页结果
     */
    @GetMapping("/query-list")
    public CommonResp<PageResp<PassengerQueryResp>> queryList(@Valid PassengerQueryReq req) {
        // 从拦截器解析的上下文中取当前会员 ID，实现数据隔离
        req.setMemberId(LoginMemberContext.getId());
        PageResp<PassengerQueryResp> list = passengerService.queryList(req);
        return new CommonResp<>(list);
    }

    /**
     * 按主键删除乘车人。
     *
     * @param id 乘车人主键
     * @return 空业务体
     */
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return new CommonResp<>();
    }

    /**
     * 查询当前登录会员的全部乘车人（不分页，按姓名升序），常用于购票页下拉选择。
     *
     * @return 乘车人列表
     */
    @GetMapping("/query-mine")
    public CommonResp<List<PassengerQueryResp>> queryMine() {
        List<PassengerQueryResp> list = passengerService.queryMine();
        return new CommonResp<>(list);
    }

}
