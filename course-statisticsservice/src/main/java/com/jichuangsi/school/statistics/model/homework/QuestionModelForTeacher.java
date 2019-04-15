package com.jichuangsi.school.statistics.model.homework;

import java.util.ArrayList;
import java.util.List;

public class QuestionModelForTeacher extends QuestionModel {

    List<AnswerModelForStudent> answerModelForStudent = new ArrayList<AnswerModelForStudent>();

    public List<AnswerModelForStudent> getAnswerModelForStudent() {
        return answerModelForStudent;
    }

    public void setAnswerModelForStudent(List<AnswerModelForStudent> answerModelForStudent) {
        this.answerModelForStudent = answerModelForStudent;
    }
}