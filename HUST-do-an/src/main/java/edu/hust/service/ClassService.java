package edu.hust.service;

import java.time.LocalDate;
import java.util.List;

import edu.hust.model.Class;

public interface ClassService {

	boolean addNewClass(Class classInstance);

	Class findClassByID(int classID);

	boolean updateClassInfo(Class classInstance);

	boolean deleteClassInfo(int classID);

	boolean checkAddingTime(LocalDate addingDate, int semesterId);

	Class findClassByClassName(String className);

	List<Class> getListClassByCourseID(int courseID);

	List<Class> getClassBySemesterName(String semesterName);

}
