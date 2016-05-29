package com.df;

import java.io.File;

public class ExtractFlows {

	public static void main(String[] args) {
		String muleProjects=".";
		 File directory = new File(muleProjects);

		    // get all the files from a directory
		File[] fList = directory.listFiles();
		    for (File file : fList) {
		        if (file.isDirectory()) {
		        	File f = new File(file.getAbsoluteFile()+"\\src\\main\\app");boolean flows=false; 
		        	if (f.exists() && f.isDirectory()) {
		        	   flows=true;
		        	}
		        	if (!file.getName().startsWith(".")&&flows) {
		        		File[] flowList=f.listFiles();
		        		for (File flowFile : flowList) {
		        			String extension = "";
			        		int i = flowFile.getName().lastIndexOf('.');
			        		if (i > 0) {
			        		    extension = flowFile.getName().substring(i+1);
			        		}
			        		if (extension.equals("xml")) {
			        			System.out.println(flowFile.getName());
			        		}
		        		}
		        				        		
		        	}
		        }
		    }
	}

}
