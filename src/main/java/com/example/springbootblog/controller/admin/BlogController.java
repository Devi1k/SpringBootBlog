package com.example.springbootblog.controller.admin;

import com.example.springbootblog.entity.Blog;
import com.example.springbootblog.service.BlogService;
import com.example.springbootblog.service.CategoryService;
import com.example.springbootblog.utils.MyBlogUtils;
import com.example.springbootblog.utils.PageQueryUtil;
import com.example.springbootblog.utils.Result;
import com.example.springbootblog.utils.ResultGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Slf4j
@Controller
@RequestMapping("/admin")
public class BlogController {

    @Value("${upload.file.path}")
    public String uploadPath;
    @Resource
    private BlogService blogService;

    @Resource
    private CategoryService categoryService;

    @GetMapping("/blogs")
    public String list(HttpServletRequest request) {
        request.setAttribute("path", "blogs");
        return "admin/blog";
    }

    @GetMapping("/blogs/list")
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> param) {
        if (StringUtils.isEmpty(param.get("page")) || StringUtils.isEmpty(param.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常");
        }
        PageQueryUtil pageQueryUtil = new PageQueryUtil(param);
        return ResultGenerator.genSuccessResult(blogService.getBlogsPage(pageQueryUtil));
    }

    @GetMapping("/blogs/edit")
    public String edit(HttpServletRequest request) {
        request.setAttribute("path", "edit");
        request.setAttribute("categories", categoryService.getAllCategories());
        return "admin/edit";
    }

    @GetMapping("/blogs/edit/{blogId}")
    public String edit(HttpServletRequest request, @PathVariable("blogId") Long blogId) {
        request.setAttribute("path", "edit");
        log.info(String.valueOf(blogId));
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null) {
            return "error/error_400";
        }
        request.setAttribute("blog", blog);
        request.setAttribute("categories", categoryService.getAllCategories());
        return "admin/edit";
    }

    @PostMapping("/blogs/md/uploadfile")
    public void uploadFileByEditorMd(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(name = "editormd-image-file", required = true) MultipartFile multipartFile) throws IOException, URISyntaxException {
        String FILE_UPLOAD_DIC = uploadPath;
        String fileName = multipartFile.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        StringBuilder tempName = new StringBuilder();
        tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(suffixName);
        String newFileName = tempName.toString();
        // create file
        File destFile = new File(FILE_UPLOAD_DIC + newFileName);
        String fileUrl = MyBlogUtils.getHost(new URI(request.getRequestURI() + "")) + "/upload/" + newFileName;
        File fileDirectory = new File(FILE_UPLOAD_DIC);
        try {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new IOException("文件夹创建失败，路径为" + fileDirectory);
                }
            }
            multipartFile.transferTo(destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.setCharacterEncoding("utf-8");
            response.setHeader("Content-Type", "text/html");
            response.getWriter().write("{\"success\": 1, \"message\":\"success\",\"url\":\"" + fileUrl + "\"}");
        } catch (UnsupportedEncodingException e) {
            response.getWriter().write("{\"success\":0}");
        } catch (IOException e) {
            response.getWriter().write("{\"success\":0}");
        }
    }


    @PostMapping("/blogs/save")
    @ResponseBody
    public Result save(@RequestParam("blogTitle") String blogTitle,
                       @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                       @RequestParam("blogCategoryId") Integer blogCategoryId,
                       @RequestParam("blogTags") String blogTags,
                       @RequestParam("blogContent") String blogContent,
                       @RequestParam("blogCoverImage") String blogCoverImage,
                       @RequestParam("blogStatus") Byte blogStatus,
                       @RequestParam("enableComment") Byte enableComment) {
        if (StringUtils.isEmpty(blogTitle)) {
            return ResultGenerator.genFailResult("请输入文章标题");
        }
        if (blogTitle.trim().length() > 150) {
            return ResultGenerator.genFailResult("标题过长");
        }
        if (StringUtils.isEmpty(blogTags)) {
            return ResultGenerator.genFailResult("请输入文章标签");
        }
        if (blogTags.trim().length() > 150) {
            return ResultGenerator.genFailResult("标签过长");
        }
        if (blogSubUrl.trim().length() > 150) {
            return ResultGenerator.genFailResult("路径过长");
        }
        if (StringUtils.isEmpty(blogContent)) {
            return ResultGenerator.genFailResult("内容不能为空");
        }
        if (blogContent.trim().length() > 10000) {
            return ResultGenerator.genFailResult("文章内容过长");
        }
        if (StringUtils.isEmpty(blogCoverImage)) {
            return ResultGenerator.genFailResult("封面不能为空");
        }
        Blog blog = new Blog();
        blog.setBlogTitle(blogTitle);
        blog.setBlogCategoryId(blogCategoryId);
        blog.setBlogContent(blogContent);
        blog.setBlogSubUrl(blogSubUrl);
        blog.setBlogTags(blogTags);
        blog.setBlogCoverImage(blogCoverImage);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        String saveResult = blogService.saveBlog(blog);
        if ("success".equals(saveResult)) {
            return ResultGenerator.genSuccessResult("添加成功");
        } else {
            return ResultGenerator.genFailResult(saveResult);
        }
    }

    @PostMapping("/blogs/update")
    @ResponseBody
    public Result update(@RequestParam("blogId") Long blogId,
                         @RequestParam("blogTitle") String blogTitle,
                         @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                         @RequestParam("blogCategoryId") Integer blogCategoryId,
                         @RequestParam("blogTags") String blogTags,
                         @RequestParam("blogContent") String blogContent,
                         @RequestParam("blogCoverImage") String blogCoverImage,
                         @RequestParam("blogStatus") Byte blogStatus,
                         @RequestParam("enableComment") Byte enableComment) {
        if (StringUtils.isEmpty(blogTitle)) {
            return ResultGenerator.genFailResult("请输入文章标题");
        }
        if (blogTitle.trim().length() > 150) {
            return ResultGenerator.genFailResult("标题过长");
        }
        if (StringUtils.isEmpty(blogTags)) {
            return ResultGenerator.genFailResult("请输入文章标签");
        }
        if (blogTags.trim().length() > 150) {
            return ResultGenerator.genFailResult("标签过长");
        }
        if (blogSubUrl.trim().length() > 150) {
            return ResultGenerator.genFailResult("路径过长");
        }
        if (StringUtils.isEmpty(blogContent)) {
            return ResultGenerator.genFailResult("内容不能为空");
        }
        if (blogContent.trim().length() > 10000) {
            return ResultGenerator.genFailResult("文章内容过长");
        }
        if (StringUtils.isEmpty(blogCoverImage)) {
            return ResultGenerator.genFailResult("封面不能为空");
        }
        Blog blog = blogService.getBlogById(blogId);
        blog.setBlogTitle(blogTitle);
        blog.setBlogCategoryId(blogCategoryId);
        blog.setBlogContent(blogContent);
        blog.setBlogSubUrl(blogSubUrl);
        blog.setBlogTags(blogTags);
        blog.setBlogCoverImage(blogCoverImage);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        String saveResult = blogService.updateBlog(blog);
        if ("success".equals(saveResult)) {
            return ResultGenerator.genSuccessResult("修改成功");
        } else {
            return ResultGenerator.genFailResult(saveResult);
        }
    }

    @PostMapping("/blogs/delete")
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数错误");
        }
        if (blogService.deleteBatch(ids)) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("删除失败");
    }

}
