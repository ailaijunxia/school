package com.jichuangsi.school.courseservice.repository;

import com.jichuangsi.school.courseservice.entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String>, CourseExtraRepository{

    List<Course> findAllByTeacherId(String teacherId);

    Course findFirstByIdAndTeacherId(String courseId, String teacherId);

    Course findFirstByIdAndClassId(String courseId, String classId);
}
