package com.jichuangsi.school.examservice.Utils;

import com.jichuangsi.microservice.common.model.UserInfoForToken;
import com.jichuangsi.school.examservice.Model.ExamModel;
import com.jichuangsi.school.examservice.Model.QuestionModel;
import com.jichuangsi.school.examservice.entity.Exam;
import com.jichuangsi.school.examservice.entity.Question;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public final  class MappingModel2EntityConverter {
    private MappingModel2EntityConverter(){}

    public final static Exam converterForExamModel(UserInfoForToken userInfo, ExamModel eaxmModel,
                                                   List<String> qids){
        Exam eaxm = new Exam();
        if(eaxmModel.getExamId()==null){
            eaxm.setExamId( UUID.randomUUID().toString().replace("-",""));
        }else {eaxm.setExamId(eaxmModel.getExamId());}
        eaxm.setExamName(eaxmModel.getExamName());
        eaxm.setCreateTime(eaxmModel.getCreateTime()==0?new Date().getTime():eaxmModel.getCreateTime());
        eaxm.setTeacherId(userInfo.getUserId());
        eaxm.setTeacherName(userInfo.getUserName());
        eaxm.setUpdateTime(new Date().getTime());
        eaxm.setQuestionIds(qids);
        return eaxm;
    }

    public final  static Question converterForQuestionModel(QuestionModel questionModel){
        Question question = new Question();
        question.setAnswer(questionModel.getAnswer());
        question.setAnswerDetail(questionModel.getAnswerDetail());
        question.setContent(questionModel.getQuestionContent());
        question.setCreateTime(questionModel.getCreateTime()==0?new Date().getTime():questionModel.getCreateTime());
        question.setDifficulty(questionModel.getDifficulty());
        question.setGradeId(questionModel.getGradeId());
        question.setId(questionModel.getQuestionId()==null?
                                    UUID.randomUUID().toString().replace("-",""):questionModel.getQuestionId());
        question.setIdMD52(questionModel.getQuestionIdMD52());
        question.setKnowledge(questionModel.getKnowledge());
        question.setOptions(questionModel.getOptions());
        question.setParse(questionModel.getParse());
        question.setPic(questionModel.getQuestionPic());
        question.setStatus(questionModel.getQuestionStatus().getName());
        question.setSubjectId(questionModel.getSubjectId());
        question.setType(questionModel.getQuesetionType());
        question.setUpdateTime(new Date().getTime());
        return question;
    }
}