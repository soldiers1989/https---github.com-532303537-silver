package org.silver.sys.model;

import java.io.Serializable;

public class Head implements Serializable {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//回执报文头类容
		private String messageID;	//报文编号	报文唯一编号，建议与报文名相同。
		private String messageType; //报文类型	参见报文类型说明。
		private String sender;	    //报文发送者标识	广州电子口岸数据交换企业编号。
		private String receiver;    //报文接收人标识	广州电子口岸数据交换编号，跨境系统可固定:KJPUBLIC
		private String sendTime;	//发送时间	报文生成的系统时间 YYYYMMDDHHMMSS
		private String functionCode;//业务类型	单向海关申报填CUS 单向国检申报填CIQ 同时发送时填写“BOTH”
		private String signerInfo;	//签名信息	按照特定算法生成的企业数字签名信息。
		private String version;     //版本号	默认为3.0

		private String RespondBy;
		private String DetailNo;
		private String Status;
		private String Notes;
		//商品备案回执内容
		private String DeclEntNo;
		private String EntGoodsNo;
		private String EportGoodsNo;
		private String CIQGoodsNo;
		private String CIQGRegStatus;
		private String CIQNotes;
		private String OpType;
		private String OpTime;
		public String getMessageID() {
			return messageID;
		}
		public void setMessageID(String messageID) {
			this.messageID = messageID;
		}
		public String getMessageType() {
			return messageType;
		}
		public void setMessageType(String messageType) {
			this.messageType = messageType;
		}
		public String getSender() {
			return sender;
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
		public String getReceiver() {
			return receiver;
		}
		public void setReceiver(String receiver) {
			this.receiver = receiver;
		}
		public String getSendTime() {
			return sendTime;
		}
		public void setSendTime(String sendTime) {
			this.sendTime = sendTime;
		}
		public String getFunctionCode() {
			return functionCode;
		}
		public void setFunctionCode(String functionCode) {
			this.functionCode = functionCode;
		}
		public String getSignerInfo() {
			return signerInfo;
		}
		public void setSignerInfo(String signerInfo) {
			this.signerInfo = signerInfo;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getRespondBy() {
			return RespondBy;
		}
		public void setRespondBy(String respondBy) {
			RespondBy = respondBy;
		}
		public String getDetailNo() {
			return DetailNo;
		}
		public void setDetailNo(String detailNo) {
			DetailNo = detailNo;
		}
		public String getStatus() {
			return Status;
		}
		public void setStatus(String status) {
			Status = status;
		}
		public String getNotes() {
			return Notes;
		}
		public void setNotes(String notes) {
			Notes = notes;
		}
		public String getDeclEntNo() {
			return DeclEntNo;
		}
		public void setDeclEntNo(String declEntNo) {
			DeclEntNo = declEntNo;
		}
		public String getEntGoodsNo() {
			return EntGoodsNo;
		}
		public void setEntGoodsNo(String entGoodsNo) {
			EntGoodsNo = entGoodsNo;
		}
		public String getEportGoodsNo() {
			return EportGoodsNo;
		}
		public void setEportGoodsNo(String eportGoodsNo) {
			EportGoodsNo = eportGoodsNo;
		}
		public String getCIQGoodsNo() {
			return CIQGoodsNo;
		}
		public void setCIQGoodsNo(String cIQGoodsNo) {
			CIQGoodsNo = cIQGoodsNo;
		}
		public String getCIQGRegStatus() {
			return CIQGRegStatus;
		}
		public void setCIQGRegStatus(String cIQGRegStatus) {
			CIQGRegStatus = cIQGRegStatus;
		}
		public String getCIQNotes() {
			return CIQNotes;
		}
		public void setCIQNotes(String cIQNotes) {
			CIQNotes = cIQNotes;
		}
		public String getOpType() {
			return OpType;
		}
		public void setOpType(String opType) {
			OpType = opType;
		}
		public String getOpTime() {
			return OpTime;
		}
		public void setOpTime(String opTime) {
			OpTime = opTime;
		}
		
		
		
}
