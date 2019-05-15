package edu.hust.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.enumData.IsLearning;
import edu.hust.model.Account;
import edu.hust.model.ClassRoom;
import edu.hust.model.StudentClass;
import edu.hust.repository.ClassRoomRepository;
import edu.hust.repository.StudentClassRepository;
import edu.hust.utils.GeneralValue;

@Service
@Qualifier("StudentClassServiceImpl1")
public class StudentClassServiceImpl1 implements StudentClassService {

	private ClassRoomService classRoomService;
	private BlacklistService blacklistService;
	private ClassRoomRepository classRoomRepository;
	private StudentClassRepository studentClassRepository;

	public StudentClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassServiceImpl1(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			@Qualifier("BlacklistServiceImpl1") BlacklistService blacklistService,
			ClassRoomRepository classRoomRepository, StudentClassRepository studentClassRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
		this.studentClassRepository = studentClassRepository;
		this.classRoomService = classRoomService;
		this.blacklistService = blacklistService;
	}

	@Override
	public List<ClassRoom> getTimeTable(int studentID, int semesterID) {
		List<Integer> listClassID = this.studentClassRepository.getListClass(studentID, semesterID);

		if (listClassID == null || listClassID.isEmpty()) {
			return null;
		}

		List<ClassRoom> listClassRoom = this.classRoomRepository.getListClassRoom(listClassID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		return listClassRoom;
	}

	@Override
	public String checkStudentHasAuthority(int studentID, int classID, int roomID, String identifyString, String imei) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());

		if (studentClass.isEmpty()) {
			System.out.println("\n\nMile 1");
			return "Mile1";
		}

		StudentClass instance = studentClass.get();
		String classIdentifyString = instance.getClassInstance().getIdentifyString();
		// String studentImei = instance.getAccount().getImei();
                if(classIdentifyString == null){
                    return "You have to wait until teacher rollcall";
                }
		// check if identifyString is incorrect
		if (!classIdentifyString.equals(identifyString)) {
			System.out.println("\n\nMile 2");
			return "You have to scan the right QR code to rollcall";
		}

		// check if ClassRoom exists
		//// Notice: weekday of java = weekday of mySQL - 1
		int weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
		LocalTime checkTime = LocalTime.now();
		ClassRoom classRoom = this.classRoomService.getInfoClassRoom(classID, roomID, weekday, checkTime);
		if (classRoom == null) {
			System.out.println("\n\nMile 3");
			return "Class is not in lesson's time!";
		}

		// check if student has already roll call
		// dateAndTime co dinh dang: Year - dayInYear - secondInDay
		String isChecked = instance.getIsChecked();

		if (!checkIsCheckedValid(isChecked, classRoom.getBeginAt(), classRoom.getFinishAt())) {
			System.out.println("\n\nMile 4");
			return "You have already rollcalled before";
		}

		return null;
	}

	@Override
	public String rollCall(int classID, int studentID, LocalDateTime rollCallAt, String imei) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());
		StudentClass instance = null;
		String newValue = null;
		String listRollCall = null;
		String isChecked = null;
                
		if (studentClass.isEmpty()) {
			return "Not found student-class";
		}

		instance = studentClass.get();
		listRollCall = instance.getListRollCall();
                // create a blacklist's record if imei is different
		if (!instance.getAccount().getImei().equals(imei)) {
			// this.blacklistService.createNewRecord(instance, imei);
			// return "Warning: System has created a record in blacklist for your incorrect
			// IMEI!";

			return "Sorry! It seem like you are using other's device!";
		}
		newValue = "" + rollCallAt.getYear();
		newValue += GeneralValue.regexForSplitDate + rollCallAt.getDayOfYear();
		newValue += GeneralValue.regexForSplitDate + rollCallAt.toLocalTime().toSecondOfDay()
				+ GeneralValue.regexForSplitListRollCall;

		if (listRollCall == null) {
			listRollCall = newValue;
		} else {
			listRollCall += newValue;
		}

		instance.setListRollCall(listRollCall);

		isChecked = rollCallAt.getYear() + GeneralValue.regexForSplitDate + rollCallAt.getDayOfYear()
				+ GeneralValue.regexForSplitDate + rollCallAt.toLocalTime().toSecondOfDay();
		instance.setIsChecked(isChecked);
		this.studentClassRepository.save(instance);

		
		return null;

	}

	@Override
	public List<StudentClass> findByClassID(int id) {
		List<StudentClass> listInstance = this.studentClassRepository.getListStudentClass(id);
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}

	@Override
	public boolean updateStudentClassInfo(StudentClass studentClass) {
		this.studentClassRepository.save(studentClass);
		return true;
	}

	@Override
	public List<StudentClass> findCurrentStudentsByClassID(int classID) {
		List<StudentClass> listInstance = this.studentClassRepository.getListCurrentStudentClass(classID,
				IsLearning.LEARNING.getValue());
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}

	@Override
	public boolean checkStudentIsLearning(int studentID, int classID) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());
		if (studentClass.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean checkIsCheckedValid(String isChecked, LocalTime beginAt, LocalTime finishAt) {
		if (isChecked != null) {
			String[] dateAndTime = isChecked.split(GeneralValue.regexForSplitDate);
			int year = Integer.parseInt(dateAndTime[0]);
			int dayOfYear = Integer.parseInt(dateAndTime[1]);
			int secondOfDay = Integer.parseInt(dateAndTime[2]);

			LocalDate checkedDate = LocalDate.ofYearDay(year, dayOfYear);
			LocalTime checkedTime = LocalTime.ofSecondOfDay(secondOfDay);

			// check if a day has more than one lesson of a class
			if (LocalDate.now().isEqual(checkedDate) && checkedTime.isAfter(beginAt)
					&& checkedTime.isBefore(finishAt)) {
				return false;
			}

			return true;
		}

		return true;
	}

	@Override
	public boolean checkStudentIsLearning(String studentEmail, int classID) {
		Optional<StudentClass> studentClass = this.studentClassRepository
				.findByStudentEmailAndClassIDAndStatus(studentEmail, classID, IsLearning.LEARNING.getValue());
		if (studentClass.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public String checkTimetableConflict(Account account, int classID) {
		List<StudentClass> listClasses = this.studentClassRepository.findByStudentIDAndStatus(account.getId(),
				IsLearning.LEARNING.getValue());
		if (listClasses == null || listClasses.isEmpty()) {
			// no conflict can happen
			return null;
		}
		
		//check if student has already studied in this class
		for (StudentClass target: listClasses) {
			if (target.getClassInstance().getId() == classID) {
				return "Student has already studied in this class";
			}
		}
		
		List<ClassRoom> listClassRoom = this.classRoomRepository.findByClassID(classID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			// this class has no lesson => no conflict
			//this situation is for special class
			return null;
		}
		
		//a week has 5 weekdays from Monday to Friday; int[0] and int[1] are not used
		//all weekdays have lessons are marked as 1
		int[] arrayOfWeekdayLearning = {-1, -1, 0, 0, 0, 0, 0};
		int tmpValue = -1;
		for (ClassRoom tmpClassRoom: listClassRoom) {
			tmpValue = tmpClassRoom.getWeekday();
			if (arrayOfWeekdayLearning[tmpValue] == 0) {
				arrayOfWeekdayLearning[tmpValue] = 1;
			}
		}
		
		List<ClassRoom> listClassRoomNeedCheck = null;
		int tmpClassID = -1;
		int tmpWeekday = -1;
		for (StudentClass tmpStudentClass: listClasses) {
			tmpClassID = tmpStudentClass.getClassInstance().getId();
			listClassRoomNeedCheck = this.classRoomRepository.findByClassID(tmpClassID);
			
			if (listClassRoomNeedCheck == null || listClassRoomNeedCheck.isEmpty()) {
				continue;
			}
			
			for (ClassRoom tmpClassRoom: listClassRoomNeedCheck) {
				tmpWeekday = tmpClassRoom.getWeekday();
				if (arrayOfWeekdayLearning[tmpValue] == 0) {
					//if not be overlapped day => no need to check
					continue;
				}
				
				for (ClassRoom target: listClassRoom) {
					if (target.getWeekday() != tmpWeekday) {
						continue;
					}
					
					//when 2 classroom is learned in the same day
					if (!checkTwoDurationConflict(target.getBeginAt(), tmpClassRoom.getBeginAt(),
							target.getFinishAt(), tmpClassRoom.getFinishAt())) {
						return "Conflict happened";
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public boolean checkTwoDurationConflict(LocalTime begin1, LocalTime begin2, LocalTime finish1, LocalTime finish2) {
		
		//check if 2 duration are partly overlapped
		if (begin1.isAfter(begin2) && begin1.isBefore(finish2)) {
			return false;
		}
		
		if (finish1.isAfter(begin2) && finish1.isBefore(finish2)) {
			return false;
		} 
		
		//check if 1 duration is totally overlapped by the other
		if (begin1.isBefore(begin2) && finish1.isAfter(finish2)) {
			return false;
		}
		
		if (begin2.isBefore(begin1) && finish2.isAfter(finish1)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void saveNewStudentClass(StudentClass studentClass) {
		this.studentClassRepository.save(studentClass);
		return;
		
	}

	
}
