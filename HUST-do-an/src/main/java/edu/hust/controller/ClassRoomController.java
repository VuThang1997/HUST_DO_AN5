
package edu.hust.controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Class;
import edu.hust.model.ClassRoom;
import edu.hust.model.ReportError;
import edu.hust.model.Room;
import edu.hust.service.ClassRoomService;
import edu.hust.service.ClassService;
import edu.hust.service.RoomService;
import edu.hust.service.StudentClassService;
import edu.hust.service.TeacherClassService;
import edu.hust.utils.FrequentlyUtils;
import edu.hust.utils.ValidationClassRoomData;
import edu.hust.utils.ValidationData;

@CrossOrigin
@RestController
public class ClassRoomController {

	private ClassRoomService classRoomService;
	private ClassService classService;
	private RoomService roomService;
	private StudentClassService studentClassService;
	private TeacherClassService teacherClassService;
	private ValidationData validationData;
	private ValidationClassRoomData validationClassRoomData;
	private FrequentlyUtils frequentlyUtils;

	public ClassRoomController() {
		super();
	}

	@Autowired
	public ClassRoomController(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData,
			@Qualifier("FrequentlyUtilsImpl1") FrequentlyUtils frequentlyUtils,
			@Qualifier("ClassServiceImpl1") ClassService classService,
			@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("ValidationClassRoomDataImpl1") ValidationClassRoomData validationClassRoomData,
			@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService) {
		super();
		this.classRoomService = classRoomService;
		this.validationData = validationData;
		this.validationClassRoomData = validationClassRoomData;
		this.frequentlyUtils = frequentlyUtils;
		this.classService = classService;
		this.roomService = roomService;
		this.studentClassService = studentClassService;
		this.teacherClassService = teacherClassService;
	}

	@PostMapping("/classrooms")
	public ResponseEntity<?> addNewClassRoom(@RequestBody String infoClassRoom) {
		int weekday = -1;
		int classID = -1;
		int roomID = -1;
		String errorMessage = null;
		Class classInstance = null;
		Room room = null;
		LocalTime beginAt = null;
		LocalTime finishAt = null;
		ReportError report = null;
		ClassRoom classRoom = null;
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoClassRoom, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.frequentlyUtils.checkKeysExist(jsonMap, "beginAt", "finishAt", "weekday", "classID", "roomID")) {
				report = new ReportError(1, "You have to fill all required information!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateClassRoomData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(70, "Adding new class-room failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			// check if this class exists
			classID = Integer.parseInt(jsonMap.get("classID").toString());
			classInstance = this.classService.findClassByID(classID);
			if (classInstance == null) {
				report = new ReportError(63, "This class do not exist!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}

			// check if this room exists
			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			room = this.roomService.findRoomById(roomID);
			if (room == null) {
				report = new ReportError(53, "This room do not exist!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}

			beginAt = LocalTime.parse(jsonMap.get("beginAt").toString());
			finishAt = LocalTime.parse(jsonMap.get("finishAt").toString());
			weekday = Integer.parseInt(jsonMap.get("weekday").toString());

			// check if class is available at this duration
			if (!this.classRoomService.checkClassAvailable(classID, weekday, beginAt, finishAt)) {
				report = new ReportError(71, "This class is not available at this duration!");
				return ResponseEntity.badRequest().body(report);
			}

			// check if room is available at this duration
			if (!this.classRoomService.checkRoomAvailable(roomID, weekday, beginAt, finishAt)) {
				report = new ReportError(72, "This room is not available at this duration!");
				return ResponseEntity.badRequest().body(report);
			}

			classRoom = new ClassRoom(beginAt, finishAt, weekday);
			classRoom.setClassInstance(classInstance);
			classRoom.setRoom(room);

			this.classRoomService.addNewClassRoom(classRoom);
			report = new ReportError(200, "Adding new class-room suceeses!");
			return ResponseEntity.ok(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@GetMapping("/classrooms")
	public ResponseEntity<?> getInfoClassRoom(@RequestParam(value = "classID", required = true) int classID,
			@RequestParam(value = "roomID", required = true) int roomID) {

		String errorMessage = null;
		ReportError report = null;
		errorMessage = this.validationClassRoomData.validateIdData(classID);
		if (errorMessage != null) {
			report = new ReportError(73, "Getting class-room info failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		errorMessage = this.validationClassRoomData.validateIdData(roomID);
		if (errorMessage != null) {
			report = new ReportError(73, "Getting class-room info failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		List<ClassRoom> classRooms = this.classRoomService.getListClassRoom(classID, roomID);
		if (classRooms != null && !classRooms.isEmpty()) {
			return ResponseEntity.ok(classRooms);
		}

		report = new ReportError(74, "This class-room do not exist!");
		return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/classroomsForClass")
	public ResponseEntity<?> getInfoClassRoomForClass(@RequestParam(value = "classID", required = true) int classID) {
		String errorMessage = null;
		ReportError report = null;
		errorMessage = this.validationClassRoomData.validateIdData(classID);
		if (errorMessage != null) {
			report = new ReportError(73, "Getting class-room info failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		List<ClassRoom> classRooms = this.classRoomService.getListClassRoomByClassID(classID);
		if (classRooms != null && !classRooms.isEmpty()) {
			return ResponseEntity.ok(classRooms);
		}

		report = new ReportError(74, "This class do not have any lesson!");
		return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
	}

	@PutMapping("/classrooms")
	public ResponseEntity<?> updateInfoClassRoom(@RequestBody String infoClassRoom) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String errorMessage = null;
		Class classInstance = null;
		Room room = null;
		LocalTime beginAt = null;
		LocalTime finishAt = null;
		int id = -1;
		int weekday = -1;
		int classID = -1;
		int roomID = -1;
		ReportError report = null;
		ClassRoom classRoom = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoClassRoom, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.frequentlyUtils.checkKeysExist(jsonMap, "id", "beginAt", "finishAt", "weekday", "classID",
					"roomID")) {
				report = new ReportError(1, "You have to fill all required information!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateClassRoomData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(75, "Updating class-room info failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			// check if this class-room exists
			id = Integer.parseInt(jsonMap.get("id").toString());
			classRoom = this.classRoomService.findClassRoomByID(id);
			if (classRoom == null) {
				report = new ReportError(74, "This class-room do not exist!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);

			}

			// check if the class exists (if classID is new)
			classID = Integer.parseInt(jsonMap.get("classID").toString());
			if (classRoom.getClassInstance().getId() != classID) {
				classInstance = this.classService.findClassByID(classID);
				if (classInstance == null) {
					report = new ReportError(63, "This class do not exist!");
					return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
				} 
				classRoom.setClassInstance(classInstance);
			}

			// check if this room exists (if roomID is new)
			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			if (classRoom.getRoom().getId() != roomID) {
				room = this.roomService.findRoomById(roomID);
				if (room == null) {
					report = new ReportError(53, "This room do not exist!");
					return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
				}
				classRoom.setRoom(room);
			}

			beginAt = LocalTime.parse(jsonMap.get("beginAt").toString());
			finishAt = LocalTime.parse(jsonMap.get("finishAt").toString());
			weekday = Integer.parseInt(jsonMap.get("weekday").toString());

			// Check if class and room is available at this duration
			if (classRoom.getWeekday() != weekday || classRoom.getBeginAt().compareTo(beginAt) != 0
					|| classRoom.getFinishAt().compareTo(finishAt) != 0) {

				if (!this.classRoomService.checkUpdateClassTimeValid(classRoom.getId(), weekday, beginAt, finishAt)) {
						report = new ReportError(71, "This class is not available at this duration!");
						return ResponseEntity.badRequest().body(report);
				}

				if (!this.classRoomService.checkUpdateRoomTimeValid(classRoom.getId(), weekday, beginAt, finishAt)) {
					report = new ReportError(72, "This room is not available at this duration!");
					return ResponseEntity.badRequest().body(report);
				}

				classRoom.setWeekday(weekday);
				classRoom.setBeginAt(beginAt);
				classRoom.setFinishAt(finishAt);
			}

			this.classRoomService.updateClassRoomInfo(classRoom);
			report = new ReportError(200, "Updating class-room info suceeses!");
			return ResponseEntity.ok(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
	
	@DeleteMapping("/classrooms")
	public ResponseEntity<?> deleteClassRoom(@RequestParam(value = "id", required = true) int id) {
		String errorMessage = null;
		ReportError report = null;
		ClassRoom classRoom = null;
		errorMessage = this.validationClassRoomData.validateIdData(id);
		if (errorMessage != null) {
			report = new ReportError(76, "Deleting class-room failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		classRoom = this.classRoomService.findClassRoomByID(id);
		if (classRoom == null) {
			report = new ReportError(74, "This class-room do not exist!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}
		
		if (this.studentClassService.findByClassID(classRoom.getClassInstance().getId()) != null) {
			report = new ReportError(77, "This class-room still has dependants");
			return ResponseEntity.badRequest().body(report);
		}
		
		if (this.teacherClassService.findByClassID(classRoom.getClassInstance().getId()) != null) {
			report = new ReportError(77, "This class-room still has dependants");
			return ResponseEntity.badRequest().body(report);
		}
		
		this.classRoomService.deleteClassRoom(classRoom);
		report = new ReportError(200, "Deleting class-room suceeses!");
		return ResponseEntity.ok(report);
	}
	
	@GetMapping("/listRooms")
	public ResponseEntity<?> getListRoom(@RequestParam(value = "classID", required = true) int classID) {

		String errorMessage = null;
		ReportError report = null;
		errorMessage = this.validationClassRoomData.validateIdData(classID);
		if (errorMessage != null) {
			report = new ReportError(73, "Getting class-room info failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		//List<ClassRoom> classRooms = this.classRoomService.getListClassRoomByClassID(classID);
		List<Room> listRoom = this.classRoomService.getListRoomByClassID(classID);
		if (listRoom != null && !listRoom.isEmpty()) {
			
			return ResponseEntity.ok(listRoom);
		}

		report = new ReportError(74, "This class-room do not exist!");
		return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
	}

	
	
	@PostMapping("/createMultipleClassRoom")
	public ResponseEntity<?> createMultipleClassRoom( @RequestBody String requestBody, 
			@RequestParam(value = "roomID", required = true) int roomID) {

		ReportError report = null;
		ObjectMapper objectMapper = null;
		List<ClassRoom> listClassRoom = null;
		
		try {
			objectMapper = new ObjectMapper();
			objectMapper.findAndRegisterModules();
			listClassRoom = objectMapper.readValue(requestBody, new TypeReference<List<ClassRoom>>() {
			});
			
			List<ClassRoom> filteredList = this.classRoomService.checkListClassRoom(listClassRoom, roomID);

			if (filteredList == null || filteredList.isEmpty()) {
				report = new ReportError(200, "All accounts are invalid!");
			} else {
				for (int i = 0; i < filteredList.size() - 1; i++) {
					//add all missing info
					this.classRoomService.addNewClassRoom(filteredList.get(i), roomID);
				}
					
				String listOfInvalidRows = filteredList.get(filteredList.size() - 1).getClassInstance().getIdentifyString();
				if (listOfInvalidRows.equalsIgnoreCase("0-")) {
					report = new ReportError(200, listOfInvalidRows);
				
				} else {
					int counter = 0;
					for (int i = 0; i < listOfInvalidRows.length(); i++) {
						if (listOfInvalidRows.charAt(i) == ',') {
							counter ++;
						}
					}
					report = new ReportError(200, counter + "-" + listOfInvalidRows);
				}
			}
			
			System.out.println("report body = " + report.getDescription());
			return ResponseEntity.ok(report);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
