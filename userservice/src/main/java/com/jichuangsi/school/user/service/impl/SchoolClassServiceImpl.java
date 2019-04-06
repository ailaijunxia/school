package com.jichuangsi.school.user.service.impl;

import com.jichuangsi.microservice.common.constant.ResultCode;
import com.jichuangsi.school.user.constant.MyResultCode;
import com.jichuangsi.school.user.entity.org.ClassInfo;
import com.jichuangsi.school.user.entity.org.GradeInfo;
import com.jichuangsi.school.user.entity.org.SchoolInfo;
import com.jichuangsi.school.user.exception.ClassServiceException;
import com.jichuangsi.school.user.exception.SchoolServiceException;
import com.jichuangsi.school.user.feign.model.ClassDetailModel;
import com.jichuangsi.school.user.model.org.ClassModel;
import com.jichuangsi.school.user.model.school.SchoolModel;
import com.jichuangsi.school.user.model.transfer.TransferStudent;
import com.jichuangsi.school.user.repository.IClassInfoRepository;
import com.jichuangsi.school.user.repository.IGradeInfoRepository;
import com.jichuangsi.school.user.repository.ISchoolInfoRepository;
import com.jichuangsi.school.user.service.ISchoolClassService;
import com.jichuangsi.school.user.service.UserInfoService;
import com.jichuangsi.school.user.util.MappingEntity2ModelConverter;
import com.jichuangsi.school.user.util.MappingModel2EntityConverter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchoolClassServiceImpl implements ISchoolClassService {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private IGradeInfoRepository gradeInfoRepository;
    @Resource
    private ISchoolInfoRepository schoolInfoRepository;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private IClassInfoRepository classInfoRepository;

    @Override
    public void saveOrUpClass(String schoolId, String gradeId, ClassModel classModel) throws ClassServiceException {
        if(StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(gradeId)) throw new ClassServiceException(MyResultCode.PARAM_MISS_MSG);
        SchoolInfo schoolInfo = mongoTemplate.findOne(new Query(Criteria.where("id").is(schoolId).andOperator(Criteria.where("gradeIds").is(gradeId))), SchoolInfo.class);
        if(schoolInfo == null) throw new ClassServiceException(MyResultCode.SCHOOL_GRADE_NOT_MATCH);
        ClassInfo classInfo = MappingModel2EntityConverter.ConvertClass(classModel);
        mongoTemplate.save(classInfo);
        GradeInfo gradeInfo = mongoTemplate.findOne(new Query(Criteria.where("id").is(gradeId).andOperator(Criteria.where("classIds").is(classInfo.getId()))), GradeInfo.class);
        if(gradeInfo == null){
            gradeInfo = mongoTemplate.findAndModify(new Query(Criteria.where("id").in(gradeId)), new Update().push("classIds", classInfo.getId()), GradeInfo.class);
            if(gradeInfo == null) throw  new ClassServiceException(MyResultCode.GRADE_CLASS_NOT_SYNC);
        }
    }

    @Override
    public void deleteClass(String schoolId, String gradeId, String classId) throws ClassServiceException{
        SchoolInfo schoolInfo = mongoTemplate.findOne(new Query(Criteria.where("id").is(schoolId).andOperator(Criteria.where("gradeIds").is(gradeId))), SchoolInfo.class);
        if(schoolInfo == null) new ClassServiceException(MyResultCode.SCHOOL_GRADE_NOT_MATCH);
        GradeInfo gradeInfo = mongoTemplate.findAndModify(new Query(Criteria.where("id").in(gradeId)), new Update().pull("classIds", classId), GradeInfo.class);
        if(gradeInfo == null) new ClassServiceException(MyResultCode.GRADE_CLASS_NOT_SYNC);
        ClassInfo classInfo = mongoTemplate.findAndRemove(new Query(Criteria.where("id").in(classId)), ClassInfo.class);
        if(classInfo == null) new ClassServiceException(MyResultCode.CLASS_FAIL2REMOVE);
    }

    @Override
    public ClassModel getClassInfo(String schoolId, String gradeId, String classId) throws ClassServiceException{
        SchoolInfo schoolInfo = mongoTemplate.findOne(new Query(Criteria.where("id").is(schoolId).andOperator(Criteria.where("gradeIds").is(gradeId))), SchoolInfo.class);
        if(schoolInfo == null) new ClassServiceException(MyResultCode.SCHOOL_GRADE_NOT_MATCH);
        GradeInfo gradeInfo = mongoTemplate.findOne(new Query(Criteria.where("id").is(gradeId).andOperator(Criteria.where("classIds").is(classId))), GradeInfo.class);
        if(gradeInfo == null) new ClassServiceException(MyResultCode.GRADE_CLASS_NOT_MATCH);
        return MappingEntity2ModelConverter.TransferClass(mongoTemplate.findById(classId,ClassInfo.class));
    }

    @Override
    public ClassDetailModel getClassDetail(String classId) throws ClassServiceException {
        if (StringUtils.isEmpty(classId)) throw new ClassServiceException(ResultCode.PARAM_MISS_MSG);
        GradeInfo gradeInfo = gradeInfoRepository.findByClassIdsContaining(classId);
        if (null == gradeInfo) throw new ClassServiceException(ResultCode.SELECT_NULL_MSG);
        SchoolInfo schoolInfo = schoolInfoRepository.findByGradeIdsContaining(gradeInfo.getId());
        if (null == schoolInfo) throw new ClassServiceException(ResultCode.SELECT_NULL_MSG);
        ClassModel classModel = getClassInfo(schoolInfo.getId(),gradeInfo.getId(),classId);
        ClassDetailModel model = new ClassDetailModel();
        model.setClassId(classId);
        model.setClassName(classModel.getClassName());
        model.setGradeId(gradeInfo.getId());
        model.setGradeName(gradeInfo.getName());
        model.setSchoolId(schoolInfo.getId());
        model.setSchoolName(schoolInfo.getName());
        List<TransferStudent> transferStudents = userInfoService.getStudentsByClassId(classId);
        if (null == transferStudents) throw new ClassServiceException(ResultCode.SELECT_NULL_MSG);
        model.setStudentNum(transferStudents.size());
        return model;
    }

    @Override
    public List<ClassModel> getClassesByGradeId(String gradeId) throws SchoolServiceException {
        GradeInfo gradeInfo = gradeInfoRepository.findFirstById(gradeId);
        if (null == gradeInfo){
            throw new SchoolServiceException(ResultCode.SELECT_NULL_MSG);
        }
        List<String> classIds = gradeInfo.getClassIds();
        List<ClassInfo> classInfos = classInfoRepository.findByIdInAndDeleteFlag(classIds,"0");
        List<ClassModel> classModels = new ArrayList<ClassModel>();
        classInfos.forEach(classInfo -> {
            classModels.add(MappingEntity2ModelConverter.TransferClass(classInfo));
        });
        return classModels;
    }

    @Override
    public SchoolModel getSchoolBySchoolId(String schoolId) throws SchoolServiceException {
        if (StringUtils.isEmpty(schoolId)){
            throw new SchoolServiceException(ResultCode.PARAM_MISS_MSG);
        }
        SchoolInfo schoolInfo = schoolInfoRepository.findFirstByDeleteFlagAndId("0",schoolId);
        if (null == schoolInfo){
            throw new SchoolServiceException(ResultCode.SELECT_NULL_MSG);
        }
        return MappingEntity2ModelConverter.CONVERTEFROMSCHOOLINFO(schoolInfo);
    }

    @Override
    public List<SchoolModel> getBackSchools() throws SchoolServiceException {
        List<SchoolInfo> schoolInfos = schoolInfoRepository.findByDeleteFlag("0");
        List<SchoolModel> schoolModels = new ArrayList<SchoolModel>();
        schoolInfos.forEach(schoolInfo -> {
            schoolModels.add(MappingEntity2ModelConverter.CONVERTEFROMSCHOOLINFO(schoolInfo));
        });
        return schoolModels;
    }
}
