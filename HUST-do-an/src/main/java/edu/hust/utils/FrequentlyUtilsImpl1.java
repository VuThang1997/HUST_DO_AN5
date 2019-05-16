package edu.hust.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("FrequentlyUtilsImpl1")
public class FrequentlyUtilsImpl1 implements FrequentlyUtils {

	@Override
	public boolean checkKeysExist(Map<String, Object> jsonMap, String... args) {
		List<String> listKeys = new ArrayList<>();
		for (String arg : args) {
			listKeys.add(arg);
		}

		if (!jsonMap.keySet().containsAll(listKeys)) {
			return false;
		}
		return true;
	}

	@Override
	public String makeRollcallRecord(LocalDateTime rollcallAt) {
		String newValue = "" + rollcallAt.getYear();
		newValue += GeneralValue.regexForSplitDate + rollcallAt.getDayOfYear();
		newValue += GeneralValue.regexForSplitDate + rollcallAt.toLocalTime().toSecondOfDay();
		
		return newValue;
	}

}
