package org.eclipse.jgit.mavenproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("sheet1");
		Row header = sheet.createRow(0);
	    header.createCell(0).setCellValue("Repositry");
	    header.createCell(1).setCellValue("Language");
	    header.createCell(2).setCellValue("Files Count");
		String path = "https://github.com/TheAlgorithms";
		int idx_path = path.lastIndexOf('/');
		String userName = path.substring(idx_path + 1);
		Map<String, HashMap<String, Integer>> info = new HashMap<>();
		try {
			Collection<AuthMsg> allRepo = getRepos(userName);
			if (allRepo != null) {
				for (AuthMsg authMsg : allRepo) {
					String repoName = authMsg.getRepository().toString();
					info.put(repoName, getFilePaths(userName, repoName));
				}
			}

			int rowno = 1;
			for (String repo : info.keySet()) {
				HashMap<String, Integer> langMap=info.get(repo);
				boolean flag=true;
				for(String lang: langMap.keySet()) {
					XSSFRow row = sheet.createRow(rowno++);
					if(flag) row.createCell(0).setCellValue((String) repo);
					else row.createCell(0).setCellValue("");
					flag=false;
					row.createCell(1).setCellValue(String.valueOf(lang));
					row.createCell(2).setCellValue(String.valueOf(langMap.get(lang)));
				}
			}
			
			File newFile=new File("./"+userName+".xlsx");
			FileOutputStream file = new FileOutputStream("./"+userName+".xlsx");
			workbook.write(file);
			file.close();
			System.out.println("Data Copied to Excel");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Collection<AuthMsg> getRepos(String name) {
		String url = "https://api.github.com/users/" + name + "/repos";

		String data = "";
		try {
			data = getJSON(url);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Type collectionType = new TypeToken<Collection<AuthMsg>>() {
		}.getType();
		Collection<AuthMsg> enums = new Gson().fromJson(data, collectionType);

		return enums;

	}

	public static HashMap<String, Integer> getFilePaths(String name, String repoName) {

		String url = "https://api.github.com/repos/" + name + "/" + repoName + "/git/trees/master?recursive=1";
		HashMap<String, Integer> langMap=new HashMap<>();
		String data = "";
		try {
			data = getJSON(url);
			JSONParser parser = new JSONParser();
			if (data != null) {
				JSONObject jsonObj = (JSONObject) parser.parse(data);
				JSONArray resultArray = (JSONArray) jsonObj.get("tree");
				for (int i = 0; i < resultArray.size(); i++) {
					JSONObject item = (JSONObject) resultArray.get(i);
					if (item == null)
						continue;
					String authPath = (String) item.get("path");
					if (authPath == null)
						continue;
					
					int idx=authPath.lastIndexOf("/");
					String fileName="";
					if(idx==-1) fileName=authPath;
					else fileName=authPath.substring(idx+1);
					if(fileName.contains(".") && idx!=-1) {
						int _idx=fileName.lastIndexOf(".");
						String lang=fileName.substring(_idx+1);
						if(langMap.get(lang)==null) langMap.put(lang, 1);
						else langMap.put(lang, langMap.getOrDefault(lang, 0)+1);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return langMap;

	}
	
	public static String getJSON(String url) {
		HttpURLConnection c = null;
		try {
			URL u = new URL(url);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			}

		} catch (MalformedURLException ex) {
			System.out.println(ex.getMessage());
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		}
		return null;
	}
}