package developerAPI;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import utilities.CommonFunctions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeveloperAPI {

	public static ResponseEntity<String> getAPI() throws IOException {
	//	public static void main(String[] args) throws IOException {

		Proxy proxyTest = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("fdcproxy.1dc.com", 8080));

		OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(5,TimeUnit.MINUTES)
			//.readTimeout(10,TimeUnit.MINUTES).writeTimeout(5,TimeUnit.MINUTES).
			.proxy(proxyTest)
				.build();
		Map<String, String> finalResult = new HashMap<>();
		Map<String, String> mapTIDs = new HashMap<>();
		Map<String, Double> mapTIDsTrans = new HashMap<>();
		List<JSONObject> jsonItems;
		List<JSONObject> jsonItemsTrans;
		String message = "test";
		int offset = 0;
		while (!message.trim().equals("[]")) {
			Request request = new Request.Builder().url("https://prod.emea.api.fiservapps.com/sandbox/exp/v1/terminals?sort=%2Bid&limit=100&offset=" + offset)
				.get().addHeader("Accept", "application/json").addHeader("Api-Key", "vAkeP4IokULTh1AhDJbe0nwOUtFiLz42").build();
			Response response = client.newCall(request).execute();
			message = response.body().string();
			JSONArray jsonArray = new JSONArray(message);
			jsonItems = IntStream.range(0, jsonArray.length()).mapToObj(index -> (JSONObject) jsonArray.get(index)).collect(Collectors.toList());
		   /*   jsonItems.forEach(
				arrayElement -> tidList.add((String) arrayElement.get("id")));*/
			for (int i = 0; i < jsonItems.size(); i++) {
				mapTIDs.put(jsonItems.get(i).get("id").toString(), jsonItems.get(i).get("merchantId").toString());
			}
			offset = offset + 100;
		}
		//System.out.println("Execution Started");


		String endpointURL = "https://prod.emea.api.fiservapps.com/sandbox/exp/v1/transactions?";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		String currentDay = dateFormat.format(c.getTime());
		c.add(Calendar.DATE, -15);
		String minus15Days = dateFormat.format(c.getTime());
		endpointURL = endpointURL + "postedAfter=" + minus15Days + "&postedBefore=" + currentDay + "&limit=1&terminalId=";
		//endpointURL = endpointURL + "postedAfter=" + "2021-06-20" + "&postedBefore=" + "2021-11-12" + "&limit=100&terminalId=" ;
		List<String> result = new ArrayList(mapTIDs.keySet());
		// overriding result.size() as it takes time
		for (int i = 0; i < 50; i++) {
			Request requestTrans = new Request.Builder().url(endpointURL + result.get(i)).get().addHeader("Accept", "application/json")
				.addHeader("Api-Key", "vAkeP4IokULTh1AhDJbe0nwOUtFiLz42").build();
			Response responseTrans = client.newCall(requestTrans).execute();
			String msg = responseTrans.body().string();
			//System.out.println(msg);
			if (msg.trim().equals("[]")) {
				finalResult.put(result.get(i), mapTIDs.get(result.get(i)));
			} else {
				continue;
			}
			responseTrans.close();
		}
		
		Map<String, String> newMapSortedByValue = finalResult.entrySet().stream().sorted(Map.Entry.<String, String>comparingByValue().reversed())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
		//System.out.println("Final TID List " + finalResult);
		//commented as of now
		/*String endPointURLTrans;
		c.add(Calendar.DATE, -30);
		String minus30Days = dateFormat.format(c.getTime());
		endPointURLTrans = "https://prod.emea.api.fiservapps.com/sandbox/exp/v1/transactions?postedAfter=" + minus30Days + "&postedBefore=" + minus15Days + "&limit=100&terminalId=";
		List<String> resultAmount = new ArrayList(finalResult.keySet());
		int offSetTrans = 0;
		String msg = "test";
		for(int i=0; i<resultAmount.size(); i++ ){
			while(!msg.trim().equals("[]")) {
				Request requestTrans = new Request.Builder().url(endPointURLTrans + resultAmount.get(i) + "&offset=" + offSetTrans).get()
					.addHeader("Accept", "application/json").addHeader("Api-Key", "vAkeP4IokULTh1AhDJbe0nwOUtFiLz42").build();
				System.out.println(endPointURLTrans + resultAmount.get(i) + "&offset=" + offSetTrans);
				Response responseTrans = client.newCall(requestTrans).execute();
				msg = responseTrans.body().string();
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode1 = objectMapper.readTree(msg);
				JSONArray jsonArray = new JSONArray(msg);
				jsonItemsTrans = IntStream.range(0, jsonArray.length()).mapToObj(index -> (JSONObject) jsonArray.get(index)).collect(Collectors.toList());
				for (int j = 0; j < jsonItemsTrans.size(); j++) {
					System.out.println("Running" + jsonNode1.get(j).at("/captureEnvironment/terminal/id"));
					if(mapTIDsTrans.containsKey(jsonNode1.get(j).at("/captureEnvironment/terminal/id").asText())){
						mapTIDsTrans.put(jsonNode1.get(j).at("/captureEnvironment/terminal/id").asText(),(mapTIDsTrans.get(jsonNode1.get(j).at("/captureEnvironment/terminal/id").asText()) + jsonNode1.get(j).at("/financial/amounts/transacted").asDouble()));
					}else {
						mapTIDsTrans.put(jsonNode1.get(j).at("/captureEnvironment/terminal/id").asText(), jsonNode1.get(j).at("/financial/amounts/transacted").asDouble());
					}
				}
				offSetTrans = offSetTrans + 100;
			}
			msg = "test";
			offSetTrans = 0;
		}
		System.out.println( "Amount result::: " + mapTIDsTrans);

		File file = new File("TerminalID.txt");
		BufferedWriter bf = null;
		try {
			// create new BufferedWriter for the output file
			bf = new BufferedWriter(new FileWriter(file));
			bf.write("Terminals with no transaction history for past 15 days");
			bf.newLine();
			bf.write("Terminal ID           |     Alliance Code | Potentail loss (based on previous cycle data)" + minus30Days + "-" + minus15Days );
			bf.newLine();
			// iterate map entries
			for (Map.Entry<String, String> entry : finalResult.entrySet()) {
					// put key and value separated by a colon
					bf.write(entry.getKey() + "                      |" + entry.getValue() + "| " +  mapTIDsTrans.get(entry.getKey()));
					// new line
					bf.newLine();

			}
			bf.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// always close the writer
				bf.close();
			} catch (Exception e) {
			}
		}
		CommonFunctions commonFunc = new CommonFunctions();
		String htmlCode = commonFunc.textFileUpdateHtml("TerminalID.txt");
		commonFunc.writeStringtoHtmlFile(htmlCode,"final.html"); */
		String json = new ObjectMapper().writeValueAsString(newMapSortedByValue);
		System.out.println("JSON response" + json);
		HttpHeaders responseHeaders = new HttpHeaders();
		//responseHeaders.setLocation(location);
		responseHeaders.set("Access-Control-Allow-Private-Network","true");
		responseHeaders.set("Content-Type","application/json");
		responseHeaders.setAccessControlAllowCredentials(true);
		responseHeaders.setAccessControlMaxAge(6800);
		responseHeaders.set("Access-Control-Allow-Origin", "http://localhost:13002");
        responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
        responseHeaders.set("Access-Control-Allow-Headers","Origin, Content-Type, Accept");
	     return new ResponseEntity<String>(json, responseHeaders, HttpStatus.OK);
	}

}
