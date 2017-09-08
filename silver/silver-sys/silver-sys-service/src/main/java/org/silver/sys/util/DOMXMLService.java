package org.silver.sys.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.silver.sys.model.Head;
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

	public static List<Head> getHeadBeanList(File in) throws Exception {
		List<Head> headBeanList = new ArrayList<Head>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			Element root = document.getDocumentElement(); // 获取根节点
			System.out.println("根元素:" + root.getNodeName());
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node headBeanElement = nodes.item(i);
				Head headBean = new Head();
				// 遍历headBean孩子节点，注意这里孩子节点包括那些空格和换行（它们是文本节点）
				if ("Head".equals(headBeanElement.getNodeName())) {
					NodeList nodeDetail = headBeanElement.getChildNodes();
					for (int j = 0; j < nodeDetail.getLength(); j++) {
						Node detail = nodeDetail.item(j);
						if ("MessageID".equals(detail.getNodeName())) {
							headBean.setMessageID(detail.getFirstChild().getNodeValue().toString());
							System.out.println("------MessageID------"+detail.getFirstChild().getNodeValue().toString());
						}
						if ("MessageType".equals(detail.getNodeName())) {
							headBean.setMessageType(detail.getFirstChild().getNodeValue());
							System.out.println("------MessageType------"+detail.getFirstChild().getNodeValue());
						}
						if ("Sender".equals(detail.getNodeName())) {
							headBean.setSender(detail.getFirstChild().getNodeValue().toString());
							System.out.println("------Sender------"+detail.getFirstChild().getNodeValue().toString());
						}
						if ("Receiver".equals(detail.getNodeName())) {
							headBean.setReceiver(detail.getFirstChild().getNodeValue());
							System.out.println("------Receiver------"+detail.getFirstChild().getNodeValue());
						}
						if ("SendTime".equals(detail.getNodeName())) {
							headBean.setSendTime(detail.getFirstChild().getNodeValue());
							System.out.println("------SendTime------"+detail.getFirstChild().getNodeValue());
						}
						if ("FunctionCode".equals(detail.getNodeName())) {
							headBean.setFunctionCode(detail.getFirstChild().getNodeValue());
							System.out.println("------FunctionCode------"+detail.getFirstChild().getNodeValue());
						}
						if ("SignerInfo".equals(detail.getNodeName())) {
							headBean.setSignerInfo(detail.getFirstChild().getNodeValue());
							System.out.println("------SignerInfo------"+detail.getFirstChild().getNodeValue());
						}
						if ("Version".equals(detail.getNodeName())) {
							headBean.setVersion(detail.getFirstChild().getNodeValue());
							System.out.println("------Version------"+detail.getFirstChild().getNodeValue());
						}
					}
					headBeanList.add(headBean);

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
						if ("GoodsRegRecList".equals(declarationElement.getNodeName())) {
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
							}
							//加载Service
						    if(!"".equals(entGoodsNo)&&entGoodsNo!=null){
//								GoodsRecordService service = (GoodsRecordService)StaticObj.getService("goodsRecordService");
//								GoodsRecordBean goodsBean =service.findEntGoodsNo(entGoodsNo);
//								if(goodsBean!=null&&eportGoodsNo!=null&&ciqGoodsNo!=null&&ciqGregStatus!=null&&ciqNotes!=null){
//									goodsBean.setEportGoodsNo(eportGoodsNo);
//									System.out.println("------"+eportGoodsNo);
//									goodsBean.setCiqGoodsNo(ciqGoodsNo);
//									goodsBean.setCiqGregStatus(ciqGregStatus);
//									goodsBean.setCiqnotes(ciqNotes);
//									service.doModifyGood(goodsBean);
//									System.out.println("更新商品备案成功！+++++++"+entGoodsNo);
//								}
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
					if("KJ881111".equals(orgMessageType)){//订单报备 
//						OrderService orderService = (OrderService) StaticObj.getService("orderService");
//						OrderBean orderBean= orderService.getOrderbyOrgMessageId(orgMessageID);
//						if(orderBean!=null){
//							orderBean.setOrgMessageType(orgMessageType);
//							orderBean.setRespondBy(respondBy);
//							orderBean.setOrgRecTime(orgRecTime);
//							orderBean.setStatus(status);
//							System.out.println("1111111"+status+"22222"+respondBy);
//							//orderService.doModifyOrder(orderBean);
//						}
					}
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return headBeanList;

	}

}
