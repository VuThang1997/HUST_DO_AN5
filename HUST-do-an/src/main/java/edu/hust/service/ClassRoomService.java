package edu.hust.service;

import java.time.LocalTime;
import java.util.List;

import edu.hust.model.ClassRoom;
import edu.hust.model.Room;

public interface ClassRoomService {

	boolean addNewClassRoom(ClassRoom classRoom);

	List<ClassRoom> getListClassRoom(int classID, int roomID);
	
	ClassRoom getInfoClassRoom(int classID, int roomID, int weekday, LocalTime checkTime);

	//boolean updateClassRoomInfo(int roomID, int classID, ClassRoom classRoom);

	boolean checkClassAvailable(int classID, int weekday, LocalTime beginAt, LocalTime finishAt);

	boolean checkRoomAvailable(int roomID, int weekday, LocalTime beginAt, LocalTime finishAt);

	ClassRoom findClassRoomByID(int id);

	void updateClassRoomInfo(ClassRoom classRoom);

	boolean deleteClassRoom(ClassRoom classRoom);

	List<ClassRoom> findClassRoomByWeekday(int currentDay);

	List<ClassRoom> getListClassRoomByClassID(int classID);

	List<Room> getListRoomByClassID(int classID);

	List<ClassRoom> checkListClassRoom(List<ClassRoom> listClassRoom, int roomID);

	boolean addNewClassRoom(ClassRoom classRoom, int roomID);

	boolean checkUpdateClassTimeValid(int classRoomID, int weekday, LocalTime beginAt, LocalTime finishAt);

	boolean checkUpdateRoomTimeValid(int classRoomID, int weekday, LocalTime beginAt, LocalTime finishAt);
	
}
