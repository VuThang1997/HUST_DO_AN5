package edu.hust.service;

import java.util.List;

import edu.hust.model.Course;

public interface CourseService {

	boolean addNewCourse(Course course);

	Course getCourseInfo(int id);

	boolean updateCourseInfo(Course course);

	boolean deleteCourse(int id);

	List<Course> findAllCourse();

}
