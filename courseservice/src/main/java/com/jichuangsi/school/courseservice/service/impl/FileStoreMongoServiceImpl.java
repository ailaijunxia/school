package com.jichuangsi.school.courseservice.service.impl;

import com.jichuangsi.school.courseservice.Exception.TeacherCourseServiceException;
import com.jichuangsi.school.courseservice.constant.ResultCode;
import com.jichuangsi.school.courseservice.model.CourseFile;
import com.jichuangsi.school.courseservice.service.IFileStoreService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FileStoreMongoServiceImpl implements IFileStoreService {

    @Resource
    private GridFsOperations gridFsOperations;

    @Override
    public CourseFile uploadCourseFile(CourseFile file) throws Exception{
        ObjectId objectId = gridFsOperations.store(new ByteArrayInputStream(file.getContent()), file.getStoredName(), file.getContentType());
        return file;
    }

    @Override
    public CourseFile donwloadCourseFile(String fileName) throws Exception{
        GridFSFile result = gridFsOperations.findOne(new Query(GridFsCriteria.whereFilename().is(fileName)));
        if(result != null){
            GridFsResource resource = gridFsOperations.getResource(result.getFilename());
            CourseFile downloadFile = new CourseFile();
            downloadFile.setName(resource.getFilename());
            downloadFile.setOriginalName(resource.getFilename());
            downloadFile.setContentType(resource.getContentType());
            try{
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = resource.getInputStream();
                int ch=0;
                while((ch = in.read()) != -1){                              //读到尾部返回-1
                    out.write(ch);                                           //一个字节一个字节再写入到baos中
                }
                downloadFile.setContent(out.toByteArray());
                return downloadFile;
            }catch (IOException ioExp){
                throw new Exception(ResultCode.FILE_DOWNLOAD_ERROR);
            }
        }else {
            throw new Exception(ResultCode.FILE_NOT_EXISTED);
        }
    }

    @Override
    public void deleteCourseFile(String fileName) throws Exception {
        GridFSFile result = gridFsOperations.findOne(new Query(GridFsCriteria.whereFilename().is(fileName)));
        if(result != null){
            try{
                gridFsOperations.delete(new Query(GridFsCriteria.whereFilename().is(fileName)));
            }catch (Exception exp){
                throw new Exception(ResultCode.FILE_REMOVE_ERROR);
            }
        }else {
            throw new Exception(ResultCode.FILE_NOT_EXISTED);
        }
    }
}
