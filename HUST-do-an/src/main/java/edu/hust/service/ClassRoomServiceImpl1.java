
package edu.hust.service;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Account;
import edu.hust.model.Class;
import edu.hust.model.ClassRoom;
import edu.hust.model.Room;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.ClassRoomRepository;
import edu.hust.repository.RoomRepository;
import edu.hust.utils.FrequentlyUtils;
import edu.hust.utils.ValidationAccountData;
import edu.hust.utils.ValidationClassData;

@Service
@Qualifier("ClassRoomServiceImpl1")
public class ClassRoomServiceImpl1 implements ClassRoomService {

	private ClassRoomRepository classRoomRepository;
	private RoomRepository roomRepository;
	private ValidationClassData validationClassData;
	private FrequentlyUtils frequentlyUtils;
	private ClassRepository classRepository;

	public ClassRoomServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub }
	}

	@Autowired
	public ClassRoomServiceImpl1(ClassRoomRepository classRoomRepository,
			RoomRepository roomRepository, ClassRepository classRepository,
			@Qualifier("ValidationClassDataImpl1") ValidationClassData validationClassData,
			@Qualifier("FrequentlyUtilsImpl1") FrequentlyUtils frequentlyUtils) {

		super();
		this.classRoomRepository = classRoomRepository;
		this.validationClassData = validationClassData;
		this.frequentlyUtils = frequentlyUtils;
		this.roomRepository = roomRepository;
		this.classRepository = classRepository;
	}

	@Override
	public boolean addNewClassRoom(ClassRoom classRoom) {
		if (classRoom.getId() > 0) {
			classRoom.setId(-1);
		}

		this.classRoomRepository.save(classRoom);
		return true;
	}

	@Override
	public List<ClassRoom> getListClassRoom(int classID, int roomID) {
		return this.classRoomRepository.findByClassIDAndRoomID(classID, roomID);
	}

	@Override
	public ClassRoom getInfoClassRoom(int classID, int roomID, int weekday, LocalTime checkTime) {
		Optional<ClassRoom> classRoom = this.classRoomRepository.findByClassIDAndRoomIDAndWeekday(classID, roomID,
				weekday, checkTime);

		return classRoom.isPresent() ? classRoom.get() : null;
	}

	@Override
	public boolean checkClassAvailable(int classID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		List<ClassRoom> listClassRoom = this.getListClassRoomByClassID(classID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return true;
		}
		
		System.out.println("============= beginAt = " + beginAt.toString());
		System.out.println("============= finishAt = " + finishAt.toString());
		LocalTime tmpBeginAt = null;
		LocalTime tmpFinishAt = null;
		
		for (ClassRoom tmpClassRoom : listClassRoom) {
			
			//exclude all class-room that is not in this particular day
			if (tmpClassRoom.getWeekday() != weekday) {
				System.out.println("======= wrong weekday = " + tmpClassRoom.getWeekday());
				continue;
			}
			
			tmpBeginAt = tmpClassRoom.getBeginAt();
			tmpFinishAt = tmpClassRoom.getFinishAt();
			System.out.println("============= tmpBeginAt = " + tmpBeginAt.toString());
			System.out.println("============= tmpFinishAt = " + tmpFinishAt.toString());
			if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpBeginAt, finishAt, tmpFinishAt)) {
				return false;
			}
		}
		
		return true;
//		List<ClassRoom> listClass = this.classRoomRepository.findClassesByIdAndWeekdayAndDuration(classID, weekday,
//				beginAt, finishAt);
//		if (listClass == null || listClass.isEmpty()) {
//			return null;
//		}
//
//		return listClass;
	}
	
	@Override
	public boolean checkRoomAvailable(int roomID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		List<ClassRoom> listClassRoom = this.classRoomRepository.findAllByRoomID(roomID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return true;
		}
		
		LocalTime tmpBeginAt = null;
		LocalTime tmpFinishAt = null;
		
		for (ClassRoom tmpClassRoom : listClassRoom) {
			
			//exclude all class-room that is not in this particular day
			if (tmpClassRoom.getWeekday() != weekday) {
				continue;
			}
			
			tmpBeginAt = tmpClassRoom.getBeginAt();
			tmpFinishAt = tmpClassRoom.getFinishAt();
			if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpBeginAt, finishAt, tmpFinishAt)) {
				return false;
			}
		}
		
		return true;
//		List<ClassRoom> listRoom = this.classRoomRepository.findRoomByIdAndWeekdayAndDuration(roomID, weekday, beginAt,
//				finishAt);
//		if (listRoom == null || listRoom.isEmpty()) {
//			return null;
//		}
//		return listRoom;
	}
	
	@Override
	public boolean checkUpdateClassTimeValid(int classRoomID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		ClassRoom origin = this.findClassRoomByID(classRoomID);
		List<ClassRoom> listClassRoom = this.getListClassRoomByClassID(origin.getClassInstance().getId());
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return true;
		}
		
		LocalTime tmpBeginAt = null;
		LocalTime tmpFinishAt = null;
		
		for (ClassRoom tmpClassRoom : listClassRoom) {
			
			//exclude all class-room that is not in this particular day
			if (tmpClassRoom.getWeekday() != weekday) {
				continue;
			}
			
			//must exclude itself
			if (tmpClassRoom.getId() == origin.getId()) {
				continue;
			}
			
			tmpBeginAt = tmpClassRoom.getBeginAt();
			tmpFinishAt = tmpClassRoom.getFinishAt();
			if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpBeginAt, finishAt, tmpFinishAt)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean checkUpdateRoomTimeValid(int classRoomID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		ClassRoom origin = this.findClassRoomByID(classRoomID);
		List<ClassRoom> listClassRoom = this.classRoomRepository.findAllByRoomID(origin.getRoom().getId());
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return true;
		}
		
		System.out.println("============== beginAt = " + beginAt);
		System.out.println("============== finishAt = " + finishAt);
		
		LocalTime tmpBeginAt = null;
		LocalTime tmpFinishAt = null;
		
		for (ClassRoom tmpClassRoom : listClassRoom) {
			
			//exclude all class-room that is not in this particular day
			if (tmpClassRoom.getWeekday() != weekday) {
				continue;
			}
			
			//must exclude itself
			if (tmpClassRoom.getId() == origin.getId()) {
				continue;
			}
			
			tmpBeginAt = tmpClassRoom.getBeginAt();
			tmpFinishAt = tmpClassRoom.getFinishAt();
			System.out.println("============== tmpBeginAt = " + tmpBeginAt);
			System.out.println("============== tmpFinishAt = " + tmpFinishAt);
			if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpBeginAt, finishAt, tmpFinishAt)) {
				return false;
			}
		}
		
		return true;
	}

	

	@Override
	public ClassRoom findClassRoomByID(int id) {
		Optional<ClassRoom> classRoom = this.classRoomRepository.findById(id);
		if (classRoom.isPresent()) {
			return classRoom.get();
		}
		return null;
	}

	@Override
	public void updateClassRoomInfo(ClassRoom classRoom) {
		this.classRoomRepository.save(classRoom);
		return;
	}

	@Override
	public boolean deleteClassRoom(ClassRoom classRoom) {
		try {
			this.classRoomRepository.deleteById(classRoom.getId());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public List<ClassRoom> findClassRoomByWeekday(int currentDay) {
		List<ClassRoom> listClassRoom = this.classRoomRepository.findByWeekday(currentDay);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		return listClassRoom;
	}

	@Override
	public List<ClassRoom> getListClassRoomByClassID(int classID) {
		List<ClassRoom> listClassRoom = this.classRoomRepository.findByClassID(classID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		return listClassRoom;
	}

	@Override
	public List<Room> getListRoomByClassID(int classID) {
		List<Room> listRoom = this.classRoomRepository.findListRoomByClassID(classID);
		if (listRoom == null || listRoom.isEmpty()) {
			return null;
		}
		return listRoom;
	}

	@Override
	public List<ClassRoom> checkListClassRoom(List<ClassRoom> listClassRoom, int roomID) {
		int rowCounter = 1;						// the 1st row of file excel is header => list email begin from 2nd row
		int weekday = -1;
		int tmpClassID = -1;
		boolean isConflict = false;
		String errorMessage = null;
		String listOfInvalidRows = "";
		String className = null;
		Optional<Class> classOpt = null;
		ClassRoom tmpClassRoom = null;
		List<ClassRoom> tmpListClassRoom = null;
		LocalTime beginAt = null;
		LocalTime finishAt = null;
		Iterator<ClassRoom> listIte = listClassRoom.iterator();
		
		while (listIte.hasNext()) {
			tmpClassRoom = listIte.next();
			rowCounter++;
			System.out.println("========== row  = " + rowCounter);
			
			className = tmpClassRoom.getClassInstance().getClassName();
			System.out.println("className = " + className);
			errorMessage = this.validationClassData.validateClassNameData(className);
			if (errorMessage != null) {
				System.out.println("============= class name is invalid = " + errorMessage);
				listOfInvalidRows += rowCounter + ", ";
				listIte.remove();
				continue;
			}

			weekday = tmpClassRoom.getWeekday();
			if (weekday < 2 || weekday > 6 ) {
				System.out.println("============= weekday is invalid: weekday =  " + weekday);
				listOfInvalidRows += rowCounter + ", ";
				listIte.remove();
				continue;
			}
			
			beginAt = tmpClassRoom.getBeginAt();
			finishAt = tmpClassRoom.getFinishAt();
			if (beginAt == null || finishAt == null) {
				System.out.println("============= beginAt or finishAt is null");
				listOfInvalidRows += rowCounter + ", ";
				listIte.remove();
				continue;
			}

			if (beginAt.isAfter(finishAt)) {
				System.out.println("============= beginAt is after finishAt");
				listOfInvalidRows += rowCounter + ", ";
				listIte.remove();
				continue;
			}
			
			//check if 2 LocalTime are in the same time frame
			
			
			classOpt = this.classRepository.findByClassName(className);
			if (classOpt == null || classOpt.isEmpty()) {
				System.out.println("============= class does not exist");
				listOfInvalidRows += rowCounter + ", ";
				listIte.remove();
				continue;
			}
			
			tmpClassID = classOpt.get().getId();
			System.out.println("class id  = " + tmpClassID);
			tmpListClassRoom = this.classRoomRepository.findByWeekday(tmpClassRoom.getWeekday());
			for (ClassRoom tmpTarget: tmpListClassRoom) {
				
				//must exclude itself
				if (tmpTarget.getClassInstance().getId() != tmpClassID) {
					continue;
				}
				
				if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpTarget.getBeginAt(), 
						finishAt, tmpTarget.getFinishAt())) {
					isConflict = true;
					System.out.println("============= class time is conflict ");
					listOfInvalidRows += rowCounter + ", ";
					listIte.remove();
					break;
				}
			}
			
			if (isConflict == false) {
				tmpListClassRoom = this.classRoomRepository.findAllByRoomID(roomID);
				for (ClassRoom tmpTarget: tmpListClassRoom) {
					if (tmpTarget.getClassInstance().getId() != tmpClassID) {
						continue;
					}
					
					if (!this.frequentlyUtils.checkTwoTimeConflict(beginAt, tmpTarget.getBeginAt(), 
							finishAt, tmpTarget.getFinishAt())) {
						isConflict = true;
						System.out.println("============= room time is conflict");
						listOfInvalidRows += rowCounter + ", ";
						listIte.remove();
						break;
					}
				}
			}
		}
		
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		
		//add a last row to store list of invalid rows
		ClassRoom lastRow = new ClassRoom();
		lastRow.setClassInstance(new Class());
		lastRow.getClassInstance().setIdentifyString(listOfInvalidRows);
		listClassRoom.add(lastRow);
		
		return listClassRoom;
	}

	@Override
	public boolean addNewClassRoom(ClassRoom classRoom, int roomID) {
		if (classRoom.getId() >= 0) {
			classRoom.setId(-1);
		}
		
		String className = classRoom.getClassInstance().getClassName();
		System.out.println("className = " + className);
		
		Class classIntance = this.classRepository.findByClassName(className).get();
		Room room = this.roomRepository.findById(roomID).get();
		classRoom.setRoom(room);
		classRoom.setClassInstance(classIntance);

		this.classRoomRepository.save(classRoom);
		return true;
	}



	

}
