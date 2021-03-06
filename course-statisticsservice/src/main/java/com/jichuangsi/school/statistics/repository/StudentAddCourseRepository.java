/**
 * 
 */
package com.jichuangsi.school.statistics.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.jichuangsi.school.statistics.entity.StudentAddCourseEntity;

/**
 * @author huangjiajun
 *
 */
@Repository
public interface StudentAddCourseRepository extends MongoRepository<StudentAddCourseEntity, String> {
	StudentAddCourseEntity findOneByUserIdAndCourseId(String userId, String courseId);

}
