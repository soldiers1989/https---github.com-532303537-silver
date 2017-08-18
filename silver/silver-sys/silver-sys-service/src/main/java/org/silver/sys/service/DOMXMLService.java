package org.silver.sys.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xml报文解析
 * 
 * @author zhangxin 2016/8/5
 * 
 */
public class DOMXMLService {

	public static List<Map<String,Object>> getHeadBeanList(File in) throws Exception {
		List<Map<String,Object>> headBeanList = new ArrayList<Map<String,Object>>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			Element root = document.getDocumentElement(); // 获取根节点
			//System.out.println("根元素:" + root.getNodeName());
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node headBeanElement = nodes.item(i);
				if ("Head".equals(headBeanElement.getNodeName())) {
				} else if ("Declaration".equals(headBeanElement.getNodeName())) {
					NodeList nodeDetail1 = headBeanElement.getChildNodes();
					String orgMessageType ="";
					String orgMessageID ="";
					String orgSenderID ="";
					String orgReceiverID ="";
					String orgRecTime ="";
					String respondBy ="";
					String notes ="";
					String respondStatus ="";
					String respondNotes ="";
					String status ="";
					for (int k = 0; k < nodeDetail1.getLength(); k++) {
						Node declarationElement = nodeDetail1.item(k);
						if ("GoodsRegRecList".equals(declarationElement.getNodeName())) {//商品备案回执
							NodeList enodeDetail = declarationElement.getChildNodes();
							String entGoodsNo="";
							String eportGoodsNo="";
							String ciqGoodsNo="";
							String ciqGregStatus="";
							String ciqNotes="";
							String opType="";
							String opTime="";
							for (int n = 0; n < enodeDetail.getLength(); n++) {
								Node detail = enodeDetail.item(n);
								if ("DeclEntNo".equals(detail.getNodeName())) {
									System.out.println("--DeclEntNo--"+detail.getFirstChild().getNodeValue());
								}
								if ("EntGoodsNo".equals(detail.getNodeName())) {
									entGoodsNo=detail.getFirstChild().getNodeValue();
									System.out.println("--EntGoodsNo--"+entGoodsNo);
								}
								if ("EportGoodsNo".equals(detail.getNodeName())) {
									eportGoodsNo=detail.getFirstChild().getNodeValue();
									System.out.println("--EportGoodsNo--"+eportGoodsNo);
								}
								if ("CIQGoodsNo".equals(detail.getNodeName())) {
									ciqGoodsNo=detail.getFirstChild().getNodeValue();
									System.out.println("--3333CIQGoodsNo3333--"+ciqGoodsNo);
								}
								if ("CIQGRegStatus".equals(detail.getNodeName())) {
									ciqGregStatus=detail.getFirstChild().getNodeValue();
									System.out.println("--CIQGRegStatus--"+ciqGregStatus);
								}
								if ("CIQNotes".equals(detail.getNodeName())) {
									ciqNotes=detail.getFirstChild().getNodeValue();
									System.out.println("--CIQNotes--"+ciqNotes);
								}
								if ("OpType".equals(detail.getNodeName())) {
									opType=detail.getFirstChild().getNodeValue();
									System.out.println("--OpType--"+opType);
								}
								if ("OpTime".equals(detail.getNodeName())) {
									opTime=detail.getFirstChild().getNodeValue();
									System.out.println("--OpTime--"+opTime);
								}
								
							    if(!"".equals(ciqGoodsNo)&&ciqGoodsNo!=null){
					       	      /******存储商品备案回执信息*********/
							    	
							    	
							 
								}
							}
			
						}
						if ("OrgMessageID".equals(declarationElement.getNodeName())) {
							orgMessageID=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--OrgMessageID--"+orgMessageID);
						}
						if ("OrgMessageType".equals(declarationElement.getNodeName())) {
							orgMessageType=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--OrgMessageType--"+orgMessageType);
						}
						if ("OrgSenderID".equals(declarationElement.getNodeName())) {
							orgSenderID=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--OrgSenderID--"+orgSenderID);
						}
						if ("OrgReceiverID".equals(declarationElement.getNodeName())) {
							orgReceiverID=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--OrgReceiverID--"+orgReceiverID);
						}
						if ("OrgRecTime".equals(declarationElement.getNodeName())) {
							orgRecTime=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--OrgRecTime--"+orgRecTime);
						}
						if ("RespondBy".equals(declarationElement.getNodeName())) {
							respondBy=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--RespondBy--"+respondBy);
						}
						if ("RespondStatus".equals(declarationElement.getNodeName())) {
							respondStatus=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--RespondStatus--"+respondStatus);
						}
						if ("RespondNotes".equals(declarationElement.getNodeName())) {
							respondNotes=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--RespondNotes--"+respondNotes);
						}
						if ("Status".equals(declarationElement.getNodeName())) {
							status=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--Status--"+status);
						}
						if ("Notes".equals(declarationElement.getNodeName())) {
							notes=declarationElement.getFirstChild().getNodeValue();
							System.out.println("--Notes--"+notes);
						}
					}
					
					if("".equals(orgMessageType)||"".equals(orgMessageType)){
						
					}else if("KJ881111".equals(orgMessageType)||"661101".equals(orgMessageType)){//订单报备 
						//orgMessageID 回执对应的消息id
						
						/*	OrderService orderService = (OrderService) StaticObj.getService("orderService");
						OrderBean orderBean= orderService.getOrderbyOrgMessageId(orgMessageID);
						if(orderBean!=null){
							orderBean.setOrgMessageType(orgMessageType);
							orderBean.setRespondBy(respondBy);
							orderBean.setOrgRecTime(orgRecTime);
							orderBean.setStatus(status);
							System.out.println("1111111"+status+"22222"+respondBy);
							//orderService.doModifyOrder(orderBean);
						}
						*/
					}else if("".equals(orgMessageType)||"".equals(orgMessageType)){
						
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return headBeanList;

	}

}
