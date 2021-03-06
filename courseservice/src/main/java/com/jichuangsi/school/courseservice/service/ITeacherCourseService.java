package com.jichuangsi.school.courseservice.service;

import com.jichuangsi.microservice.common.model.UserInfoForToken;
import com.jichuangsi.school.courseservice.Exception.TeacherCourseServiceException;
import com.jichuangsi.school.courseservice.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ITeacherCourseService {
    @Transactional
    List<CourseForTeacher> getCoursesList(UserInfoForToken userInfo) throws TeacherCourseServiceException;

    @Transactional
    PageHolder<CourseForTeacher> getHistoryCoursesList(UserInfoForToken userInfo, CourseForTeacher pageInform) throws TeacherCourseServiceException;

    @Transactional
    List<CourseForTeacher> queryCoursesList(UserInfoForToken userInfo, CourseForTeacher course) throws TeacherCourseServiceException;

    @Transactional
    CourseForTeacher getParticularCourse(UserInfoForToken userInfo, String courseId) throws TeacherCourseServiceException;

    @Transactional
    List<QuestionForTeacher> getQuestionsInParticularCourse(UserInfoForToken userInfo, String courseId) throws TeacherCourseServiceException;

    @Transactional
    QuestionForTeacher getParticularQuestion(String questionId) throws TeacherCourseServiceException;

    @Transactional
    List<AnswerForStudent> getAnswersInPaticularCourse(String questionId) throws TeacherCourseServiceException;

    @Transactional
    AnswerForStudent getParticularAnswer(String questionId, String studentId) throws TeacherCourseServiceException;

    @Transactional
    CourseForTeacher saveCourse(UserInfoForToken userInfo, CourseForTeacher course);

    @Transactional
    void deleteCourse(UserInfoForToken userInfo, CourseForTeacher course);

    @Transactional
    List<QuestionForTeacher> saveQuestions(String courseId, List<QuestionForTeacher> questions) throws TeacherCourseServiceException;

    @Transactional
    void startCourse(String courseId) throws TeacherCourseServiceException;

    @Transactional
    void updateParticularCourseStatus(CourseForTeacher course) throws TeacherCourseServiceException;

    @Transactional
    void updateParticularQuestionStatus(QuestionForTeacher questionStatus) throws TeacherCourseServiceException;

    @Transactional
    void publishQuestion(String courseId, String questionId)  throws TeacherCourseServiceException;

    @Transactional
    void terminateQuestion(String courseId, String questionId)  throws TeacherCourseServiceException;

    @Transactional
    void saveTeacherAnswer(UserInfoForToken userInfo, String questinoId, String studentAnswerId, AnswerForTeacher revise) throws TeacherCourseServiceException;

    @Transactional
    AnswerForTeacher uploadTeacherSubjectPic(UserInfoForToken userInfo, CourseFile file) throws TeacherCourseServiceException;

    @Transactional
    CourseFile downloadTeacherSubjectPic(UserInfoForToken userInfo, String fileName) throws TeacherCourseServiceException;

    @Transactional
    void deleteTeacherSubjectPic(UserInfoForToken userInfo, String fileName) throws TeacherCourseServiceException;
}
