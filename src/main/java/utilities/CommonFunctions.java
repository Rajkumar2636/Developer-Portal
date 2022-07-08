package utilities;


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonFunctions {

	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

	Date dt;

	private static String handleNewLine(String str) {

		StringBuilder buffer = new StringBuilder();
		char[] chars = str.toCharArray();

		boolean inquote = false;

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"') {
				inquote = !inquote;
			}

			if (chars[i] == '\n' && inquote) {
				buffer.append("<br>");
			} else {
				buffer.append(chars[i]);
			}
		}

		return buffer.toString();
	}

	private static List<String> getTableData(String str) {
		List<String> data = new ArrayList<>();

		boolean inquote = false;
		StringBuilder buffer = new StringBuilder();
		char[] chars = str.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"') {
				inquote = !inquote;
				continue;
			}

			if (chars[i] == ',') {
				if (inquote) {
					buffer.append(chars[i]);
				} else {
					data.add(buffer.toString());
					buffer.delete(0, buffer.length());
				}
			} else {
				buffer.append(chars[i]);
			}

		}

		data.add(buffer.toString().trim());

		return data;
	}

	private static boolean checkIfEmptyRow(List<String> rowData) {

		for (String td : rowData) {
			if (!td.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	//--------Read the JsonFile and return as stringbuilder-------
	public StringBuilder readJsonFileAndSendString(String filePath) {
		StringBuilder fileContent = new StringBuilder();

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(filePath));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				fileContent.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileContent;
	}

	public String dateforReport() {
		String today;
		dt = new Date();
		cal.setTime(dt);
		today = sdf.format(dt);
		System.out.println("today Date: " + today);
		return today;
	}

	public String lastWeekDayforReport() {
		String lastWeekDate;
		dt = new Date();
		cal.add(Calendar.DAY_OF_YEAR, -7);
		dt = cal.getTime();
		lastWeekDate = sdf.format(dt);
		System.out.println("lastweek Date : " + lastWeekDate);
		return lastWeekDate;
	}

	public String yesterdayForReport() {
	     	String yesterday = null;
			dt = new Date();
			cal.add(Calendar.DAY_OF_YEAR, -1);
			dt = cal.getTime();
			yesterday = sdf.format(dt);
			System.out.println("yesterday Date : " + yesterday);
		    return yesterday;
	}

	public void convertCSVtoTextFile(String csvFilePath, String textFilePath) throws IOException {
		File file = new File(csvFilePath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		String[] tempArr;
		FileWriter writer = new FileWriter(textFilePath);
		while ((line = br.readLine()) != null) {
			tempArr = line.split("\"");
			for (String str : tempArr) {
				writer.write(str + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public String textFileUpdateHtml(String textFilePath) throws IOException {
		String everything;
		BufferedReader br = new BufferedReader(new FileReader(textFilePath));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			Matcher matcher;
			Pattern pattern = Pattern.compile(",\\s+[a-z]");
			while (line != null) {
				matcher = pattern.matcher(line);
				if (matcher.find()) {
					String match = line.replaceFirst(",", ".");
					sb.append(match);
				} else {
					sb.append(line);
				}
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
			System.out.println(everything);
		} finally {
			br.close();
		}

		String str;
		str = handleNewLine(everything);
		String[] lines = str.split("\n");

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < lines.length; i++) {

			List<String> rowData = getTableData(lines[i]);

			if (checkIfEmptyRow(rowData)) {
				continue;
			}

			result.append("<tr>");
			for (String td : rowData) {
				result.append(String.format("<td>%s</td>\n", td));
			}
			result.append("</tr>");
		}
		System.out.println(String.format(
				"<style>\n" + "table, th, td {\n" + "  border: 1px solid black;\n" + "  border-collapse: collapse;\n" + "}\n" + "th, td {\n" + "  background-color: #DEDBDB;\n" + "}\n" + "</style><table class=\"table table-bordered table-striped table-condensed\">\n%s</table>",
				result));

		 return String.format(
				"<style>\n" + "table, th, td {\n" + "  border: 1px solid black;\n" + "  border-collapse: collapse;\n" + "}\n" + "th, td {\n" + "  background-color: #96D4D4;\n" + "}\n" + "</style><table class=\"table table-bordered table-striped table-condensed\">\n%s</table>",
				result);


	}

	public void writeStringtoHtmlFile(String str, String emailHtmlFilePath) {
		FileWriter fWriter = null;
		BufferedWriter writer = null;
		try {
			fWriter = new FileWriter(emailHtmlFilePath);
			writer = new BufferedWriter(fWriter);
			writer.write(str);
			writer.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void textFileSorting(String inputFile, String outputFile) throws IOException {
		String splitBy = ",";
		String line="";
		List<List<String>> marks = new ArrayList<List<String>>();
		List<List<String>> newRecords = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String firstLine = br.readLine(); // this will read the first line
		while((line = br.readLine()) != null){
			String[] b = line.split(splitBy);
			marks.add(Arrays.asList(b));
		}

		List<List<String>> finalList = new ArrayList<List<String>>();
		for (List list : marks)
		{
			if(!list.toString().contains("NEW")) {
				finalList.add(list);
			}else{
				newRecords.add(list);
			}
		}

		Comparator<List<String>> comp1 = new Comparator<List<String>>() {
			public int compare(List<String> csvLine1, List<String> csvLine2) {
				// TODO here convert to Integer depending on field.
				return Double.valueOf(csvLine1.get(4)).compareTo(Double.valueOf(csvLine2.get(4)));

			}
		};

		Collections.sort(finalList, comp1.reversed());

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
		String s1 = null,s2, s3=null;
		String c1 = null,c2, c3=null;
		writer.write(firstLine);
		writer.write("\n");
		for(List<String> i : newRecords) {
			c2 = i.toString().replaceAll("\\[", "");
			c3 = c2.replaceAll("\\]", "");
			System.out.println(c3);
			writer.write(c3);
			writer.write("\n");
		}

		//Printing the sorted csv
		for(List<String> i : finalList) {
			s2 = i.toString().replaceAll("\\[", "");
			s3 = s2.replaceAll("\\]", "");
			System.out.println(s3);
			writer.write(s3);
			writer.write("\n");
		}
		writer.close();
		//Close the BufferedReader
		br.close();
	}



}
