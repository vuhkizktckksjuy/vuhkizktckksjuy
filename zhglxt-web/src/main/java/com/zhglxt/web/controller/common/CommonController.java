package com.zhglxt.web.controller.common;

import com.zhglxt.common.config.GlobalConfig;
import com.zhglxt.common.config.ServerConfig;
import com.zhglxt.common.constant.Constants;
import com.zhglxt.common.core.entity.AjaxResult;
import com.zhglxt.common.core.entity.FileInfo;
import com.zhglxt.common.util.StringUtils;
import com.zhglxt.common.util.file.FileUploadUtils;
import com.zhglxt.common.util.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@Controller
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = GlobalConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);

            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/common/upload")
    @ResponseBody
    public AjaxResult uploadFile(MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = GlobalConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            if(StringUtils.isNotEmpty(url)){
                if(url.contains(contextPath)){
                    url=url.substring(url.indexOf(contextPath));
                }
            }

            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/common/uploads")
    @ResponseBody
    public AjaxResult uploadFiles(List<MultipartFile> files) {
        try
        {
            // 上传文件路径
            String filePath = GlobalConfig.getUploadPath();
            List<FileInfo> fileInfos = new LinkedList<FileInfo>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                if(StringUtils.isNotEmpty(url)){
                    if(url.contains(contextPath)){
                        url=url.substring(url.indexOf(contextPath));
                    }
                }

                fileInfos.add(new FileInfo(fileName, url));
            }
            return AjaxResult.success(fileInfos);
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * CMS上传请求
     */
    @PostMapping("/common/cms/upload")
    @ResponseBody
    public AjaxResult cmsUploadFile(MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = GlobalConfig.getCMSUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            if(StringUtils.isNotEmpty(url)){
                if(url.contains(contextPath)){
                    url=url.substring(url.indexOf(contextPath));
                }
            }

            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * markdown上传请求
     * @param file @RequestParam(value = "editormd-image-file", required = false) 其中 value = "editormd-image-file" 固定写法不要改变
     * @return
     */
    @PostMapping("/common/markdown/upload")
    @ResponseBody
    public AjaxResult markdownUploadFile(@RequestParam(value = "editormd-image-file", required = false) MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = GlobalConfig.getMarkdownUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            if(StringUtils.isNotEmpty(url)){
                if(url.contains(contextPath)){
                    url=url.substring(url.indexOf(contextPath));
                }
            }

            AjaxResult ajax = AjaxResult.success();
            /*editormd所需的返回结构*/
            ajax.put("message", "上传成功");
            ajax.put("success", 1);//成功：1、失败：0
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = GlobalConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }
}