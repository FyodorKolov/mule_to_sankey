package com.df;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExtractFlows {

	private final static String TEMPLATE_PATH="template/index.html";
	private final static String NEW_PATH="index.html";
	private final static String TYPE_PATH="template/data/types.csv";
	public Map<String, Parametrs> setMule = new HashMap<String, Parametrs>();
	public ArrayList<Data> AllNodes = new ArrayList<Data>();
	public Integer LastIndexNode = 0;
	public ArrayList<Link> LinkNodes = new ArrayList<Link>();
	public HashSet<Integer> linkSet = new HashSet<Integer>();
	public ArrayList<Data> last = new ArrayList<Data>();

	public ExtractFlows() {

	}

	public ExtractFlows(ExtractFlows mainFlow) {
		this.setMule = mainFlow.setMule;
		this.LastIndexNode = mainFlow.LastIndexNode;
		this.last = new ArrayList<Data>();
		this.last.addAll(mainFlow.last);
	}

	public static String getRandomColor() {
		int R = (int) (Math.random() * 256);
		int G = (int) (Math.random() * 256);
		int B = (int) (Math.random() * 256);
		return "#" + Integer.toHexString(R) + Integer.toHexString(G) + Integer.toHexString(B);
	}

	public void addLink(Integer source, Integer target, String color, Integer flowIndex) {
		Link link = new Link();
		link.setSource(source);
		link.setTarget(target);
		link.setColor(color);
		link.setDirection(true);
		link.setFlowIndex(flowIndex);
		this.LinkNodes.add(link);
		this.linkSet.add(source);
		this.linkSet.add(target);
	}

	public void addLinkCommon(Integer source, Integer target) {
		Link link = new Link();
		link.setSource(source);
		link.setTarget(target);
		link.setColor("#000000");
		this.LinkNodes.add(link);
	}

	public Data Find(String Name) {
		Data findData = null;
		for (Data dataOther : this.AllNodes) {
			if (dataOther.getName().equals(Name)) {
				findData = dataOther;
			}
		}
		return findData;
	}

	public Link FindLink(Integer source, Integer target) {
		for (Link linkOther : this.LinkNodes) {
			if (linkOther.getSource().equals(source) && linkOther.getTarget().equals(target)) {
				return linkOther;
			}
		}
		return null;
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}

	public static void updateNode(ExtractFlows mainFlow, Node vmNode, String color, Integer flowIndex,
			Boolean flowState) {
		if (vmNode.getNodeType() == Node.ELEMENT_NODE) {
			Element vmElement = (Element) vmNode;
			System.out.println("Comp name : " + vmElement.getNodeName());
			String vmElementName = vmElement.getNodeName().trim();
			String nameSpaceElement = "mule";
			String currentElement = "element";
			if (vmElementName.split(":").length > 1) {
				nameSpaceElement = vmElementName.split(":")[0];
				currentElement = vmElementName.split(":")[1];
			} else {
				currentElement = vmElementName;
			}
			if (mainFlow.setMule.containsKey(nameSpaceElement)) {
				Parametrs Attribute = mainFlow.setMule.get(nameSpaceElement);
				Data data = new Data();
				if (Attribute.getListElem() != null && Attribute.getListElem().containsKey(currentElement)) {
					UnickAttribute unickAttribute = Attribute.getListElem().get(currentElement);
					data.setSubName(currentElement);
					if (!unickAttribute.getIsInserted()) {
						for (int i = 0; i < unickAttribute.getName().length; i++) {
							String AttributeName = unickAttribute.getName()[i];
							if (vmElement.getAttribute(AttributeName) != null
									&& !vmElement.getAttribute(AttributeName).equals("")) {
								data.setName(vmElement.getAttribute(AttributeName));
								break;
							}
						}
					} else {
						for (int k = 0; k < vmNode.getChildNodes().getLength(); k++) {
							Node insertNode = vmNode.getChildNodes().item(k);
							if (insertNode.getNodeType() == Node.ELEMENT_NODE) {
								Element insertElement = (Element) insertNode;
								for (int j = 0; j < unickAttribute.getName().length; j++) {
									if (insertElement.getNodeName().equals(unickAttribute.getName()[j])) {
										if (insertElement.getNodeValue() != null) {
											data.setName(insertElement.getNodeValue());
										} else {
											data.setName(getCharacterDataFromElement(insertElement));
										}
										break;
									}
								}
							}
						}
					}
				} else {
					data.setSubName(nameSpaceElement);
					data.setName(currentElement);
				}
				data.setType(Attribute.getType());
				data.setIndex(mainFlow.LastIndexNode);
				mainFlow.LastIndexNode++;
				data.setStopped(flowState);
				if (data != null) {
					mainFlow.AllNodes.add(data);
				}
				if (mainFlow.last.size() == 0) {
					mainFlow.last.add(data);
				} else {
					if (data != null) {
						for (Data newLast : mainFlow.last) {
							mainFlow.addLink(newLast.getIndex(), data.getIndex(), color, flowIndex);
						}
						mainFlow.last.clear();
						mainFlow.last.add(data);
					}
				}
			} else {
				if (currentElement.equals("choice")) {
					ArrayList<Data> lastCommon = new ArrayList<Data>();
					for (int k = 0; k < vmNode.getChildNodes().getLength(); k++) {
						Node choiceNode = vmNode.getChildNodes().item(k);
						if (choiceNode.getNodeType() == Node.ELEMENT_NODE) {
							Element choiceElement = (Element) choiceNode;
							System.out.println("Choice name : " + choiceElement.getNodeName() + "; Exspression="
									+ choiceElement.getAttribute("expression"));
							ExtractFlows choiseFlow = new ExtractFlows(mainFlow);
							updateNode(choiseFlow, choiceNode, color, flowIndex, flowState);
							System.out.println("End Choise");
							mainFlow.AllNodes.addAll(choiseFlow.AllNodes);
							mainFlow.LinkNodes.addAll(choiseFlow.LinkNodes);
							mainFlow.linkSet.addAll(choiseFlow.linkSet);
							mainFlow.LastIndexNode = choiseFlow.LastIndexNode;

							if (mainFlow.last.size() == 0 || mainFlow.last.get(0) != choiseFlow.last.get(0)) {
								lastCommon.addAll(choiseFlow.last);
							}
						}
					}
					if (lastCommon.size() > 0) {
						mainFlow.last = lastCommon;
					}
				} else {
					for (int k = 0; k < vmNode.getChildNodes().getLength(); k++) {
						updateNode(mainFlow, vmNode.getChildNodes().item(k), color, flowIndex, flowState);
					}
				}
			}
		}
	}

	public enum Merge {
		FalseMerge, MergeInOut, MergeAll
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		String muleProjects = args[0];
		Merge mergeAll = Merge.MergeInOut;
		if (args.length > 1) {
			mergeAll = Merge.valueOf(args[1]);
		}
		ExtractFlows mainFlow = new ExtractFlows();
		Map<String, UnickAttribute> vms = new HashMap<String, UnickAttribute>();
		vms.put("inbound-endpoint", new UnickAttribute("path"));
		vms.put("outbound-endpoint", new UnickAttribute("path"));
		mainFlow.setMule.put("vm", new Parametrs("vm", vms));
		Map<String, UnickAttribute> jmss = new HashMap<String, UnickAttribute>();
		jmss.put("inbound-endpoint", new UnickAttribute("queue", "topic"));
		jmss.put("outbound-endpoint", new UnickAttribute("queue", "topic"));
		mainFlow.setMule.put("jms", new Parametrs("jms", jmss));
		Map<String, UnickAttribute> https = new HashMap<String, UnickAttribute>();
		https.put("listener", new UnickAttribute("path"));
		https.put("inbound-endpoint", new UnickAttribute("path"));
		https.put("outbound-endpoint", new UnickAttribute("path"));
		https.put("request", new UnickAttribute("path"));
		mainFlow.setMule.put("http", new Parametrs("http_listener", https));
		Map<String, UnickAttribute> quartzs = new HashMap<String, UnickAttribute>();
		quartzs.put("inbound-endpoint", new UnickAttribute("jobName"));
		mainFlow.setMule.put("quartz", new Parametrs("quartz", quartzs));
		mainFlow.setMule.put("magento", new Parametrs("magento", null));
		Map<String, UnickAttribute> sfdcs = new HashMap<String, UnickAttribute>();
		sfdcs.put("query-single", new UnickAttribute("query"));
		sfdcs.put("query-all", new UnickAttribute("query"));
		sfdcs.put("update-single", new UnickAttribute("type"));
		sfdcs.put("update", new UnickAttribute("type"));
		sfdcs.put("create-single", new UnickAttribute("type"));
		sfdcs.put("create", new UnickAttribute("type"));
		mainFlow.setMule.put("sfdc", new Parametrs("sfdc", sfdcs));
		Map<String, UnickAttribute> dbs = new HashMap<String, UnickAttribute>();
		dbs.put("stored-procedure", new UnickAttribute(true, "db:parameterized-query"));
		dbs.put("select", new UnickAttribute(true, "db:parameterized-query", "db:dynamic-query"));
		dbs.put("update", new UnickAttribute(true, "db:parameterized-query"));
		mainFlow.setMule.put("db", new Parametrs("database", dbs));
		mainFlow.setMule.put("markavip", new Parametrs("markavip", null));
		mainFlow.setMule.put("responsys", new Parametrs("responsys", null));
		mainFlow.setMule.put("kinesis", new Parametrs("kinesis", null));
		File directory = new File(muleProjects);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		// get all the files from a directory
		File[] fList = directory.listFiles();
		Integer flowIndex = 0;
		for (File file : fList) {
			if (file.isDirectory()) {
				File f = new File(file.getAbsoluteFile() + "\\src\\main\\app");
				boolean flows = false;
				if (f.exists() && f.isDirectory()) {
					flows = true;
				}
				if (!file.getName().startsWith(".") && flows) {
					File[] flowList = f.listFiles();
					for (File flowFile : flowList) {
						String extension = "";
						int i = flowFile.getName().lastIndexOf('.');
						if (i > 0) {
							extension = flowFile.getName().substring(i + 1);
						}
						if (extension.equals("xml")) {
							System.out.println(flowFile.getName());
							InputStream fileStream = new FileInputStream(flowFile);
							Document doc = builder.parse(fileStream);
							NodeList list = doc.getElementsByTagName("flow");
							for (int j = 0; j < list.getLength(); j++) {
								Node nNode = list.item(j);
								if (nNode.getNodeType() == Node.ELEMENT_NODE) {
									Element eElement = (Element) nNode;
									System.out.println("Flow name : " + eElement.getAttribute("name"));
									mainFlow.last.clear();
									String flowColor = getRandomColor();
									String ST = eElement.getAttribute("initialState");
									Boolean stopped = false;
									if ((ST != null) && (ST.equals("stopped"))) {
										stopped = true;
									}
									flowIndex++;

									NodeList listCom = eElement.getChildNodes();
									for (int k = 0; k < listCom.getLength(); k++) {
										Node vmNode = listCom.item(k);
										updateNode(mainFlow, vmNode, flowColor, flowIndex, stopped);
									}
								}
							}
						}
					}

				}
			}
		}
		/*
		 * ArrayList<Data> DataNodes=new ArrayList<Data>(); for(int
		 * i=0;i<AllNodes.size();i++) { if (linkSet.contains(i)) {
		 * DataNodes.add(AllNodes.get(i)); } }
		 */
		if (mergeAll.equals(Merge.MergeAll) || mergeAll.equals(Merge.MergeInOut)) {
			List<Data> DataInNodes = mainFlow.AllNodes.stream()
					.filter(data -> data.getSubName().equals("inbound-endpoint")).collect(Collectors.toList());
			List<Data> DataOutNodes = mainFlow.AllNodes.stream()
					.filter(data -> data.getSubName().equals("outbound-endpoint")).collect(Collectors.toList());
			for (Data data : DataInNodes) {
				for (Data data2 : DataOutNodes) {
					if (data.getName().equals(data2.getName())
							&& mainFlow.FindLink(data.getIndex(), data2.getIndex()) == null) {
						mainFlow.addLinkCommon(data.getIndex(), data2.getIndex());
					}
				}
			}
			if (mergeAll.equals(Merge.MergeAll)) {
				Comparator<Data> byName = (Data e1, Data e2) -> e1.getName().compareTo(e2.getName());
				Comparator<Data> bySubName = (Data e1, Data e2) -> e1.getSubName().compareTo(e2.getSubName());
				Comparator<Data> byType = (object1, object2) -> object1.getType().compareTo(object2.getType());
				List<Data> DataNodes = mainFlow.AllNodes.stream()
						.sorted(byName.thenComparing(bySubName).thenComparing(byType)).collect(Collectors.toList());
				String lastGroup = DataNodes.get(0).getSubName() + ":" + DataNodes.get(0).getName();
				String lastType = DataNodes.get(0).getType();
				Integer firstIndexIter = 0;

				for (int i = 1; i < DataNodes.size(); i++) {
					String group = DataNodes.get(i).getSubName() + ":" + DataNodes.get(i).getName();
					if (group.equals(lastGroup) && DataNodes.get(i).getType().equals(lastType)) {
						for (int j = firstIndexIter; j < i; j++) {
							for (int k = j + 1; k <= i; k++) {
								if (mainFlow.FindLink(DataNodes.get(j).getIndex(),
										DataNodes.get(k).getIndex()) == null) {
									mainFlow.addLinkCommon(DataNodes.get(j).getIndex(), DataNodes.get(k).getIndex());
								}
							}
						}
					} else {
						firstIndexIter = i;
						lastGroup = DataNodes.get(i).getSubName() + ":" + DataNodes.get(i).getName();
						lastType = DataNodes.get(i).getType();
					}
				}
			}
		}
		Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
		// String jsonData = gson.toJson(DataNodes);
		String jsonData = gson.toJson(mainFlow.AllNodes);
		String jsonLink = new Gson().toJson(mainFlow.LinkNodes);
		System.out.println("Scanning compleated");
		// System.out.println(jsonData);
		// System.out.println(jsonLink);
		// Copy template file
		Path path = Paths.get(ExtractFlows.TEMPLATE_PATH);
		Path dest = Paths.get(ExtractFlows.NEW_PATH);
		Charset charset = StandardCharsets.UTF_8;
		Reader in = new FileReader(ExtractFlows.TYPE_PATH);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
		String types="";
		for (CSVRecord record : records) {
		    String name = record.get("name");
		    String color = record.get("color");
		    types+="legendData[\""+name+"\"]={color:\""+color+"\"};"+System.getProperty("line.separator");
		}
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replace("${nodes}", jsonData);
		content = content.replace("${links}", jsonLink);
		content = content.replace("${types}", types);
		Files.write(dest, content.getBytes(charset));

	}

}
