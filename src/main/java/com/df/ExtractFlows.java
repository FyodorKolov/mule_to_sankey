package com.df;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExtractFlows {

	public Map<String, Parametrs> map = new HashMap<String,Parametrs>();
	public ArrayList<Data> AllNodes=new ArrayList<Data>();
	public ArrayList<Link> LinkNodes=new ArrayList<Link>();
	public HashSet<Integer> linkSet = new HashSet<Integer>();
	public Data last=null;
	
	public static String getRandomColor() {
		int R = (int)(Math.random()*256);
		int G = (int)(Math.random()*256);
		int B= (int)(Math.random()*256);
		return "#"+Integer.toHexString(R)+Integer.toHexString(G)+Integer.toHexString(B);
	}
	public void addLink(Integer source,Integer target,String color,Integer flowIndex){
		Link link=new Link();
		link.setSource(source);
		link.setTarget(target);
		link.setColor(color);
		link.setDirection(true);
		link.setFlowIndex(flowIndex);
		this.LinkNodes.add(link);
		this.linkSet.add(source);
		this.linkSet.add(target);
	}
	
	public void addLinkCommon(Integer source,Integer target){
		Link link=new Link();
		link.setSource(source);
		link.setTarget(target);
		link.setColor("#000000");
		this.LinkNodes.add(link);
	}
	
	public Data Find(String Name) {
		Data findData=null;
		   for(Data dataOther:this.AllNodes) {
			   if (dataOther.getName().equals(Name)) {
				   findData=dataOther;
			   }
		   }
		   return findData;
	}
	
	public Link FindLink(Integer source,Integer target){		
		for(Link linkOther:this.LinkNodes) {
			if (linkOther.getSource().equals(source)&&linkOther.getTarget().equals(target)){
				return linkOther;
			}
		}
		return null;
	}
	public static void updateNode(ExtractFlows mainFlow,Node vmNode,String color,Integer flowIndex) {
		if (vmNode.getNodeType() == Node.ELEMENT_NODE) {
            Element vmElement = (Element) vmNode;
            System.out.println("Comp name : " 
               + vmElement.getNodeName());
            if (mainFlow.map.containsKey(vmElement.getNodeName().trim())) {
               Parametrs Attribute=mainFlow.map.get(vmElement.getNodeName().trim());
         	   Data data=new Data();
         	   if (!Attribute.getUnickAttribute().contains(",")) {
         		   data.setName(vmElement.getAttribute(Attribute.getUnickAttribute()));
         	   } else {
         		   String[] attributes=Attribute.getUnickAttribute().split(",");
         		   for(int i=0;i<attributes.length;i++) {
         			   if (vmElement.getAttribute(attributes[i])!=null&&!vmElement.getAttribute(attributes[i]).equals("")){
         				  data.setName(vmElement.getAttribute(attributes[i]));
         				  break;
         			   }
         		   }
         	   }
         	   data.setType(Attribute.getType());
         	   data.setIndex(mainFlow.AllNodes.size());
         	   mainFlow.AllNodes.add(data);
         	   if (mainFlow.last==null){
         		  mainFlow.last=data;         		  
         	   }
         	   else {
         		   mainFlow.addLink(mainFlow.last.getIndex(), data.getIndex(),color,flowIndex);
         		   mainFlow.last=data;         		   
         	   }         	   
            } else {
            	for(int k=0;k<vmNode.getChildNodes().getLength();k++){
            		updateNode(mainFlow, vmNode.getChildNodes().item(k),color,flowIndex);
            	}
            }
		}		
	}
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		String muleProjects=".";
		ExtractFlows mainFlow=new ExtractFlows();
		mainFlow.map.put("vm:inbound-endpoint", new Parametrs("path", "vm",true));		
		mainFlow.map.put("vm:outbound-endpoint", new Parametrs("path", "vm"));	
		mainFlow.map.put("jms:inbound-endpoint", new Parametrs("queue,topic", "jms",true));		
		mainFlow.map.put("jms:outbound-endpoint", new Parametrs("queue,topic", "jms"));
		mainFlow.map.put("http:listener", new Parametrs("path", "http_listener"));
		
		
		
		 File directory = new File(muleProjects);
		 DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		    // get all the files from a directory
		File[] fList = directory.listFiles();
		Integer flowIndex=0;
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
			        			InputStream fileStream = new FileInputStream(flowFile);
			        			Document doc = builder.parse(fileStream);
			        			NodeList list=doc.getElementsByTagName("flow");			        			
			        			for(int j=0;j<list.getLength();j++){
			        				Node nNode = list.item(j);
			        				 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			        		               Element eElement = (Element) nNode;
			        		               System.out.println("Flow name : " 
			        		                  + eElement.getAttribute("name"));
			        		               mainFlow.last=null;
			        		               String flowColor=getRandomColor();
			        		               
			        		               NodeList listCom=eElement.getChildNodes();
			        		               for(int k=0;k<listCom.getLength();k++)
			        		               {
			        		            	   Node vmNode=listCom.item(k);
			        		            	   updateNode(mainFlow, vmNode,flowColor,flowIndex++);
			        		               }
			        				 }
			        			}
			        		}
		        		}
		        				        		
		        	}
		        }
		    }
		    /*ArrayList<Data> DataNodes=new ArrayList<Data>();
		    for(int i=0;i<AllNodes.size();i++) {
		    	if (linkSet.contains(i)) {
		    		DataNodes.add(AllNodes.get(i));
		    	}
		    }*/
		    List<Data> DataNodes=mainFlow.AllNodes
		    .stream()
		    .sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
		    String lastGroup=DataNodes.get(0).getName();
		    Integer firstIndex=DataNodes.get(0).getIndex();Integer firstIndexIter=0;
		    for(int i=1;i<DataNodes.size();i++) {
		    	if (DataNodes.get(i).getName().equals(lastGroup)) {
		    		for(int j=firstIndexIter;j<i;j++) {
		    			for(int k=j+1;k<=i;k++){
		    				if (mainFlow.FindLink(DataNodes.get(j).getIndex(), DataNodes.get(k).getIndex())==null) {
		    					mainFlow.addLinkCommon(DataNodes.get(j).getIndex(), DataNodes.get(k).getIndex());
		    				}
		    			}
		    		}
		    	} else {
		    		firstIndex=DataNodes.get(i).getIndex();
		    		firstIndexIter=i;
		    		lastGroup=DataNodes.get(i).getName();
		    	}
		    }
		    Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
		    //String jsonData = gson.toJson(DataNodes);
		    String jsonData = gson.toJson(mainFlow.AllNodes);
		    String jsonLink=new Gson().toJson(mainFlow.LinkNodes);		    
		    System.out.println("Scanning compleated");
		    System.out.println(jsonData);
		    System.out.println(jsonLink);
		    
	}

}
