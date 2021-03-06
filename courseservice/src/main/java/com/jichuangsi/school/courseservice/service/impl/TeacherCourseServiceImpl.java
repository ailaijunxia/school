package com.jichuangsi.school.courseservice.service.impl;

import com.jichuangsi.microservice.common.model.UserInfoForToken;
import com.jichuangsi.school.courseservice.Exception.TeacherCourseServiceException;
import com.jichuangsi.school.courseservice.constant.Result;
import com.jichuangsi.school.courseservice.constant.ResultCode;
import com.jichuangsi.school.courseservice.constant.Status;
import com.jichuangsi.school.courseservice.entity.Course;
import com.jichuangsi.school.courseservice.entity.Question;
import com.jichuangsi.school.courseservice.entity.StudentAnswer;
import com.jichuangsi.school.courseservice.entity.TeacherAnswer;
import com.jichuangsi.school.courseservice.model.*;
import com.jichuangsi.school.courseservice.repository.CourseRepository;
import com.jichuangsi.school.courseservice.repository.QuestionRepository;
import com.jichuangsi.school.courseservice.repository.StudentAnswerRepository;
import com.jichuangsi.school.courseservice.repository.TeacherAnswerRepository;
import com.jichuangsi.school.courseservice.service.IFileStoreService;
import com.jichuangsi.school.courseservice.service.IMqService;
import com.jichuangsi.school.courseservice.service.ITeacherCourseService;
import com.jichuangsi.school.courseservice.util.CollectionsTools;
import com.jichuangsi.school.courseservice.util.MappingEntity2MessageConverter;
import com.jichuangsi.school.courseservice.util.MappingEntity2ModelConverter;
import com.jichuangsi.school.courseservice.util.MappingModel2EntityConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
public class TeacherCourseServiceImpl implements ITeacherCourseService {

    @Value("${com.jichuangsi.school.result.page-size}")
    private int defaultPageSize;

    @Resource
    private IMqService mqService;

    @Resource
    private CourseRepository courseRepository;

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private StudentAnswerRepository studentAnswerRepository;

    @Resource
    private TeacherAnswerRepository teacherAnswerRepository;

    @Resource
    private IFileStoreService fileStoreService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<CourseForTeacher> getCoursesList(UserInfoForToken userInfo) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(userInfo.getUserId())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        List<Course> courses = courseRepository.findCourseByTeacherIdAndStatus(userInfo.getUserId());
        return convertCourseList(courses);
    }

    @Override
    public PageHolder<CourseForTeacher> getHistoryCoursesList(UserInfoForToken userInfo, CourseForTeacher pageInform) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(userInfo.getUserId())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        PageHolder<CourseForTeacher> pageHolder = new PageHolder<CourseForTeacher>();
        pageHolder.setTotal(courseRepository.findByTeacherIdAndStatus(userInfo.getUserId(),Status.FINISH.getName()).size());
        pageHolder.setPageNum(pageInform.getPageNum());
        pageHolder.setPageSize(StringUtils.isEmpty(pageInform.getPageSize())||pageInform.getPageSize()==0?defaultPageSize:pageInform.getPageSize());
        List<Course> courses = courseRepository.findHistoryCourseByTeacherIdAndStatus(userInfo.getUserId(), pageInform.getPageNum(),
                StringUtils.isEmpty(pageInform.getPageSize())||pageInform.getPageSize()==0?defaultPageSize:pageInform.getPageSize());
        pageHolder.setContent(convertCourseList(courses));
        return pageHolder;
    }

    @Override
    public List<CourseForTeacher> queryCoursesList(UserInfoForToken userInfo, CourseForTeacher course) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(userInfo.getUserId())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        if(StringUtils.isEmpty(course.getCourseName())) throw new TeacherCourseServiceException(ResultCode.COURSE_QUERY_IS_EMPTY);
        List<Course> courses = courseRepository.findCourseByTeacherIdAndConditions(userInfo.getUserId(), MappingModel2EntityConverter.ConvertTeacherCourse(userInfo, course));
        return convertCourseList(courses);
    }

    @Override
    public CourseForTeacher getParticularCourse(UserInfoForToken userInfo, String courseId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(userInfo.getUserId()) || StringUtils.isEmpty(courseId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Course course = courseRepository.findFirstByIdAndTeacherIdOrderByUpdateTimeDesc(courseId, userInfo.getUserId());
        List<Question> questions = questionRepository.findQuestionsByTeacherIdAndCourseId(userInfo.getUserId(), courseId);
        CourseForTeacher courseForTeacher = MappingEntity2ModelConverter.ConvertTeacherCourse(course);
        courseForTeacher.getQuestions().addAll(convertQuestionList(questions));
        return courseForTeacher;
    }

    @Override
    public List<QuestionForTeacher> getQuestionsInParticularCourse(UserInfoForToken userInfo, String courseId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(userInfo.getUserId()) || StringUtils.isEmpty(courseId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        List<Question> questions = questionRepository.findQuestionsByTeacherIdAndCourseId(userInfo.getUserId(), courseId);
        return convertQuestionList(questions);
    }

    @Override
    public QuestionForTeacher getParticularQuestion(String questionId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(questionId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Question> result = questionRepository.findById(questionId);
        if(result.isPresent()){
            List<StudentAnswer> answers = studentAnswerRepository.findAllByQuestionId(questionId);
            QuestionForTeacher questionForTeacher =  MappingEntity2ModelConverter.ConvertTeacherQuestion(result.get());
            questionForTeacher.getAnswerForStudent().addAll(convertStudentAnswerList(answers));
            return questionForTeacher;
        }
        throw new TeacherCourseServiceException(ResultCode.QUESTION_NOT_EXISTED);
    }

    @Override
    public List<AnswerForStudent> getAnswersInPaticularCourse(String questionId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(questionId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        List<StudentAnswer> answers = studentAnswerRepository.findAllByQuestionId(questionId);
        return convertStudentAnswerList(answers);
    }

    @Override
    public AnswerForStudent getParticularAnswer(String questionId, String studentId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(questionId) || StringUtils.isEmpty(studentId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        StudentAnswer studentAnswer = studentAnswerRepository.findFirstByQuestionIdAndStudentIdOrderByUpdateTimeDesc(questionId, studentId);
        return MappingEntity2ModelConverter.ConvertStudentAnswer(studentAnswer);
    }

    @Override
    public CourseForTeacher saveCourse(UserInfoForToken userInfo, CourseForTeacher course){
       return MappingEntity2ModelConverter.ConvertTeacherCourse(
               courseRepository.save(MappingModel2EntityConverter.ConvertTeacherCourse(userInfo, course)));
    }

    @Override
    public void deleteCourse(UserInfoForToken userInfo, CourseForTeacher course){
        courseRepository.delete(MappingModel2EntityConverter.ConvertTeacherCourse(userInfo, course));
    }

    @Override
    public List<QuestionForTeacher> saveQuestions(String courseId, List<QuestionForTeacher> questions) throws TeacherCourseServiceException{
        Optional<Course> result = courseRepository.findById(courseId);
        if(result.isPresent()){
            Course course = result.get();
            List<Question> questionsList2Store = new ArrayList<Question>();
            questions.forEach(question -> {
                questionsList2Store.add(MappingModel2EntityConverter.ConvertTeacherQuestion(question));
            });
            if(questionsList2Store.size()>0){
                List<Question> questionsList2Trans = questionRepository.saveAll(questionsList2Store);
                List<String> oldQuestionIds = new ArrayList<String>(course.getQuestionIds());
                course.getQuestionIds().removeAll(course.getQuestionIds());
                questionsList2Trans.forEach(question -> {
                    course.getQuestionIds().add(question.getId());
                });
                courseRepository.save(course);
                Arrays.asList(CollectionsTools.getC(oldQuestionIds.toArray(), course.getQuestionIds().toArray())).forEach(cid -> {
                    if(oldQuestionIds.contains(cid)) questionRepository.deleteById((String)cid);
                });
                return convertQuestionList(questionsList2Trans);
            }
            throw new TeacherCourseServiceException(ResultCode.QUESTIONS_TO_SAVE_IS_EMPTY);
        }
        throw new TeacherCourseServiceException(ResultCode.COURSE_NOT_EXISTED);
    }

    @Override
    public void updateParticularCourseStatus(CourseForTeacher course) throws TeacherCourseServiceException {
        String courseId = course.getCourseId();
        if(StringUtils.isEmpty(courseId) || StringUtils.isEmpty(course.getCourseStatus())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Course> result = courseRepository.findById(courseId);
        if(result.isPresent()){
            Course course2Update = result.get();
            course2Update.setStatus(course.getCourseStatus().getName());
            course2Update.setUpdateTime(new Date().getTime());
            course2Update = courseRepository.save(course2Update);
        }else{
            throw new TeacherCourseServiceException(ResultCode.COURSE_NOT_EXISTED);
        }
    }

    @Override
    public void publishQuestion(String courseId, String questionId) throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(courseId)
                || StringUtils.isEmpty(questionId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Question> result = questionRepository.findById(questionId);
        if(result.isPresent()){//需要增加判断重复发布题目
            Question question2Update = result.get();
            question2Update.setStatus(Status.PROGRESS.getName());
            question2Update.setUpdateTime(new Date().getTime());
            question2Update = questionRepository.save(question2Update);
            mqService.sendMsg4PublishQuestion(MappingEntity2MessageConverter.ConvertQuestion(courseId, question2Update));
        }else{
            throw new TeacherCourseServiceException(ResultCode.QUESTION_NOT_EXISTED);
        }
    }

    @Override
    public void terminateQuestion(String courseId, String questionId)  throws TeacherCourseServiceException {
        if(StringUtils.isEmpty(courseId)
                || StringUtils.isEmpty(questionId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Question> result = questionRepository.findById(questionId);
        if(result.isPresent()){
            Question question2Update = result.get();
            question2Update.setStatus(Status.FINISH.getName());
            question2Update.setUpdateTime(new Date().getTime());
            question2Update = questionRepository.save(question2Update);
            mqService.sendMsg4TermQuestion(MappingEntity2MessageConverter.ConvertQuestion(courseId, question2Update));
        }else{
            throw new TeacherCourseServiceException(ResultCode.QUESTION_NOT_EXISTED);
        }
    }

    @Override
    public void updateParticularQuestionStatus(QuestionForTeacher questionStatus) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(questionStatus.getQuestionId())
                || StringUtils.isEmpty(questionStatus.getQuestionStatus())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Question> result = questionRepository.findById(questionStatus.getQuestionId());
        if(result.isPresent()){
            Question question2Update = result.get();
            question2Update.setStatus(questionStatus.getQuestionStatus().getName());
            question2Update.setUpdateTime(new Date().getTime());
            question2Update = questionRepository.save(question2Update);
        }else{
            throw new TeacherCourseServiceException(ResultCode.QUESTION_NOT_EXISTED);
        }
    }

    @Override
    public void startCourse(String courseId) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(courseId)) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<Course> result = courseRepository.findById(courseId);
        if(result.isPresent()){
            Course course2Update = result.get();
            course2Update.setStatus(Status.PROGRESS.getName());
            course2Update.setUpdateTime(new Date().getTime());
            course2Update = courseRepository.save(course2Update);
            mqService.sendMsg4StartCourse(MappingEntity2MessageConverter.ConvertCourse(course2Update));
        }else{
            throw new TeacherCourseServiceException(ResultCode.COURSE_NOT_EXISTED);
        }
    }

    @Override
    public void saveTeacherAnswer(UserInfoForToken userInfo, String questionId, String studentAnswerId, AnswerForTeacher revise) throws TeacherCourseServiceException{
        if(StringUtils.isEmpty(userInfo.getUserId())
                || StringUtils.isEmpty(questionId)
                || StringUtils.isEmpty(studentAnswerId)
                || StringUtils.isEmpty(revise.getStubForSubjective())) throw new TeacherCourseServiceException(ResultCode.PARAM_MISS_MSG);
        Optional<TeacherAnswer> resultForTeacherAnswer = Optional.ofNullable(teacherAnswerRepository.findFirstByTeacherIdAndQuestionIdAndStudentAnswerIdOrderByUpdateTimeDesc(userInfo.getUserId(), questionId, studentAnswerId));
        if(resultForTeacherAnswer.isPresent()){
            TeacherAnswer answer2Update = resultForTeacherAnswer.get();
            answer2Update.setSubjectivePic(revise.getPicForSubjective());
            answer2Update.setSubjectivePicStub(revise.getStubForSubjective());
            answer2Update.setSubjectiveScore(revise.getScore());
            answer2Update.setUpdateTime(new Date().getTime());
            teacherAnswerRepository.save(answer2Update);
        }else{
            teacherAnswerRepository.save(MappingModel2EntityConverter.ConvertTeacherAnswer(userInfo, questionId, studentAnswerId, revise));
        }

        Optional<StudentAnswer> resultForStudentAnswer = studentAnswerRepository.findById(studentAnswerId);
        if(resultForStudentAnswer.isPresent()){
            StudentAnswer answer2Update = resultForStudentAnswer.get();
            answer2Update.setResult(Result.PASS.getName());
            answer2Update.setSubjectiveScore(revise.getScore());
            answer2Update.setReviseTime(new Date().getTime());
            studentAnswerRepository.save(answer2Update);
        }
    }

    @Override
    public AnswerForTeacher uploadTeacherSubjectPic(UserInfoForToken userInfo, CourseFile file) throws TeacherCourseServiceException {
        try{
            fileStoreService.uploadCourseFile(file);
        }catch (Exception exp){
            throw new TeacherCourseServiceException(exp.getMessage());
        }
        AnswerForTeacher answerForTeacher = new AnswerForTeacher();
        answerForTeacher.setTeacherId(userInfo.getUserId());
        answerForTeacher.setStubForSubjective(file.getStoredName());
        return answerForTeacher;
    }

    @Override
    public CourseFile downloadTeacherSubjectPic(UserInfoForToken userInfo, String fileName) throws TeacherCourseServiceException {
        try{
            return fileStoreService.donwloadCourseFile(fileName);
        }catch (Exception exp){
            throw new TeacherCourseServiceException(exp.getMessage());
        }
    }

    @Override
    public void deleteTeacherSubjectPic(UserInfoForToken userInfo, String fileName) throws TeacherCourseServiceException {
        try{
            fileStoreService.deleteCourseFile(fileName);
        }catch (Exception exp){
            throw new TeacherCourseServiceException(exp.getMessage());
        }
    }

    private List<CourseForTeacher> convertCourseList(List<Course> courses){
        List<CourseForTeacher> courseForTeachers = new ArrayList<CourseForTeacher>();
        courses.forEach(course -> {
            courseForTeachers.add(MappingEntity2ModelConverter.ConvertTeacherCourse(course));
        });
        return courseForTeachers;
    }

    private List<QuestionForTeacher> convertQuestionList(List<Question> questions){
        List<QuestionForTeacher> questionForTeachers = new ArrayList<QuestionForTeacher>();
        questions.forEach(question -> {
            questionForTeachers.add(MappingEntity2ModelConverter.ConvertTeacherQuestion(question));
        });
        return questionForTeachers;
    }

    private List<AnswerForStudent> convertStudentAnswerList(List<StudentAnswer> answers){
        List<AnswerForStudent> AnswerForStudents = new ArrayList<AnswerForStudent>();
        answers.forEach(answer -> {
            AnswerForStudents.add(MappingEntity2ModelConverter.ConvertStudentAnswer(answer));
        });
        return AnswerForStudents;
    }


}
