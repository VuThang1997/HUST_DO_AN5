package edu.hust.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.enumData.IsLearning;
import edu.hust.model.Account;
import edu.hust.model.ReportError;
import edu.hust.model.StudentClass;
import edu.hust.service.AccountService;
import edu.hust.service.ClassService;
import edu.hust.service.RoomService;
import edu.hust.service.StudentClassService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationAccountData;
import edu.hust.utils.ValidationRoomData;
import edu.hust.utils.ValidationStudentClassData;
import edu.hust.model.Class;

@RestController
public class StudentClassController {

	private StudentClassService studentClassService;
	private ClassService classService;
	private AccountService accountService;
	private RoomService roomService;
	private JsonMapUtil jsonMapUtil;
	private ValidationRoomData validationRoomData;
	private ValidationAccountData validationAccountData;
	private ValidationStudentClassData validationStudentClassData;

	public StudentClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassController(@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("ValidationRoomDataImpl1") ValidationRoomData validationRoomData,
			@Qualifier("ValidationStudentClassDataImpl1") ValidationStudentClassData validationStudentClassData,
			@Qualifier("ValidationAccountDataImpl1") ValidationAccountData validationAccountData,
			@Qualifier("AccountServiceImpl1") AccountService accountService,
			@Qualifier("ClassServiceImpl1") ClassService classService) {
		super();
		this.studentClassService = studentClassService;
		this.roomService = roomService;
		this.validationRoomData = validationRoomData;
		this.validationStudentClassData = validationStudentClassData;
		this.jsonMapUtil = jsonMapUtil;
		this.validationAccountData = validationAccountData;
		this.accountService = accountService;
		this.classService = classService;
	}

	@PostMapping(value = "/studentRollCall")
	public ResponseEntity<?> rollCall(@RequestBody String info) {
		int studentID;
		int classID;
		int roomID;
		double gpsLong;
		double gpsLa;
		ReportError report = null;
		String imei = null;
		// String macAddr = null;
		String identifyString = null;
		String errorMessage = null;
		LocalDateTime rollCallAt = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
//			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "studentID", "classID", "roomID", "gpsLong", "gpsLa",
//					"macAddr", "identifyString", "imei")) {
//				report = new ReportError(1, "You have to fill all required information!");
//				return ResponseEntity.badRequest().body(report);
//			}
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "studentID", "classID", "roomID", "gpsLong", "gpsLa",
					"imei")) {
				report = new ReportError(1, "You have to fill all required information!");
				return ResponseEntity.badRequest().body(report);
			}

			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "identifyString")) {
				report = new ReportError(1, "You have to scan QR code before rollcall!");
				return ResponseEntity.badRequest().body(report);
			}

			studentID = Integer.parseInt(jsonMap.get("studentID").toString());
			errorMessage = this.validationStudentClassData.validateIdData(studentID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			classID = Integer.parseInt(jsonMap.get("classID").toString());
			errorMessage = this.validationStudentClassData.validateIdData(classID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			errorMessage = this.validationRoomData.validateIdData(roomID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			gpsLong = Double.parseDouble(jsonMap.get("gpsLong").toString());
			gpsLa = Double.parseDouble(jsonMap.get("gpsLa").toString());
			if (gpsLong < -180 || gpsLong > 180 || gpsLa < -90 || gpsLa > 90) {
				report = new ReportError(91, "Longitude/Latitude is out of practical range!");
				return ResponseEntity.badRequest().body(report);
			}

			imei = jsonMap.get("imei").toString();
			if (imei == null || imei.isBlank()) {
				report = new ReportError(93, "Missing IMEI info");
				return ResponseEntity.badRequest().body(report);
			}

//			macAddr = jsonMap.get("macAddr").toString();
//			if (macAddr == null || macAddr.isBlank()) {
//				report = new ReportError(92, "Missing MAC address");
//				return ResponseEntity.badRequest().body(report);
//			}

			identifyString = jsonMap.get("identifyString").toString();
			if (identifyString == null || identifyString.isBlank()) {
				report = new ReportError(94, "You have to scan the right QR code to rollcall");
				return ResponseEntity.badRequest().body(report);
			}

			// check student has authority to roll call this class
			errorMessage = this.studentClassService.checkStudentHasAuthority(studentID, classID, roomID, identifyString,
					imei);
			if (errorMessage != null) {
				report = new ReportError(11, errorMessage);
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// check if MAC address is correct
//			if (!this.roomService.checkMacAddress(roomID, macAddr)) {
//				report = new ReportError(95, "MAC address is incorrect!");
//				return ResponseEntity.badRequest().body(report);
//			}

			// check if device is in distance limit - 20m
			if (this.roomService.calculateDistanceBetween2GPSCoord(roomID, gpsLong, gpsLa) > 50) {
				report = new ReportError(96, "It seems like you are not in classroom!");
				return ResponseEntity.badRequest().body(report);
			}

			// add rollcall time to student.listRollcall
			rollCallAt = LocalDateTime.now();
			errorMessage = this.studentClassService.rollCall(classID, studentID, rollCallAt, imei);
			if (errorMessage != null) {
				report = new ReportError(97, errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			report = new ReportError(200, "Roll call success!");
			return ResponseEntity.ok(report);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@PostMapping(value = "/createMultipleStudentClass")
	public ResponseEntity<?> createMultipleStudentClass (@RequestBody String studentEmailInfo) {
		ObjectMapper objectMapper = null;
		List<String> listStudentEmail = null;
		String errorMessage = null;
		ReportError report;
		Account account = null;
		int classID = -1;
		int invalidAccount = 0;
		int rowCounter = 1; // Excel table: first row = info of field
		String infoOfRow = "";
		StudentClass studentClass = null;
		Class classInstance = null;
		String tmpEmail = null;

		try {
			objectMapper = new ObjectMapper();
			listStudentEmail = objectMapper.readValue(studentEmailInfo, new TypeReference<List<String>>() {
			});
			
			//classID is stored in the last element of list
			classID = Integer.parseInt(listStudentEmail.get(listStudentEmail.size() - 1));
			classInstance = this.classService.findClassByID(classID);

			for (int i = 0; i < listStudentEmail.size() - 1; i++) {
				tmpEmail = listStudentEmail.get(i);
				System.out.println("==== Tmp email = " + tmpEmail);
				rowCounter++;
				
				errorMessage = this.validationAccountData.validateUsernameData(tmpEmail);
				if (errorMessage != null) {
					System.out.println(errorMessage);
					invalidAccount++;
					infoOfRow += (i+2) + ", ";
					continue;
				}

				//check if this student exists
				account = this.accountService.findAccountByEmail(tmpEmail);
				if (account == null 
						|| this.studentClassService.checkStudentIsLearning(account.getId(), classID)) {
					
					System.out.println("Account not exist or this student is not learning");
					invalidAccount++;
					infoOfRow += (i+2) + ", ";
					continue;
				}
				
				errorMessage = this.studentClassService.checkTimetableConflict(account, classID);
				if (errorMessage != null) {
					System.out.println("=========== Timetable conflict");
					invalidAccount++;
					infoOfRow += (i+2) + ", ";
					continue;
				}
				
				studentClass = new StudentClass();
				studentClass.setAccount(account);
				studentClass.setIsLearning(IsLearning.LEARNING.getValue());
				studentClass.setClassInstance(classInstance);
				
				this.studentClassService.saveNewStudentClass(studentClass);
			}

			report = new ReportError(200, "" + invalidAccount + "-" + infoOfRow);
			System.out.println("report body = " + report.getDescription());
			return ResponseEntity.ok(report);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
