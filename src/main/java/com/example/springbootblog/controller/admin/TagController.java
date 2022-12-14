package com.example.springbootblog.controller.admin;

import com.example.springbootblog.service.TagService;
import com.example.springbootblog.utils.PageQueryUtil;
import com.example.springbootblog.utils.Result;
import com.example.springbootblog.utils.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class TagController {
    @Resource
    private TagService tagService;

    @GetMapping("/tags")
    public String tagPages(HttpServletRequest request) {
        request.setAttribute("path", "tags");
        return "admin/tag";
    }


    @GetMapping("/tags/list")
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常");
        }
        PageQueryUtil pageQueryUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(tagService.getBlogTagPage(pageQueryUtil));
    }


    @PostMapping("/tags/save")
    @ResponseBody
    public Result save(@RequestParam("tagName") String tagName) {
        if (StringUtils.isEmpty(tagName)) {
            return ResultGenerator.genFailResult("标签名称不能为空");
        }
        if (tagService.saveTag(tagName)) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("标签名称重复");
    }


    @PostMapping("/tags/delete")
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genSuccessResult("参数异常");
        }
        if (tagService.deleteBatch(ids)) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("有关联数据请勿强行删除");
    }


}
