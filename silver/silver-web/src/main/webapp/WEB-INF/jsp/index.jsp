<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<html>
<body>
	<h2>通用网关接入密钥申请</h2>

	公司名或应用名:
	<input type="text" id="app_name"> 姓名:
	<input type="text" id="user_name"> 电话:
	<input type="text" id="user_mobile">
	<button id="reg_appkey">确定</button>
    <script type="text/javascript" src="res/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
	$("#reg_appkey").click(function(){
		$.post("Eport/Report",{type:1},function(data){
			//$("#app_name").val();
			var result = JSON.parse(data);
			
		});
	})
	
	function post(url, data, success, error) {
		var ajax;
		if (window.XMLHttpRequest) { //Mozilla 浏览器
			ajax = new XMLHttpRequest();

			if (ajax.overrideMimeType)
				ajax.overrideMimeType("application/json");

		} else if (window.ActiveXObject) { // IE浏览器
			try {
				ajax = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					ajax = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {
				}
			}
		}
		if (!ajax) {
			window.alert("不能创建XMLHttpRequest对象实例.");
			return false;
		}
		ajax.open("post", url, true);
		ajax.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		ajax.send(data);
		ajax.onreadystatechange = function() {
			if (ajax.readyState == 4 && ajax.status == 200) {
				if (success)
					success(ajax.responseText);
			} else {
				if (error)
					error(ajax);
			}
		}
	}
	
		var btn1 = document.getElementById('reg_appkey');
		btn1.onclick = function() {
			var app_name=document.getElementById('app_name').value;
			var user_name=document.getElementById('user_name').value;
			var user_mobile=document.getElementById('user_mobile').value;
			if(app_name.trim()==""||user_name.trim()==""||user_mobile.trim()==""){
				alert("请填写完整信息");
				return;
			}
			
			post("seekforteacher?id=das",null,
					function(data) {
						var result = JSON.parse(data);
					  console.info(result);
					}, function() {

					});
			
		};
		
	    

	$("#reg_appkey").click(function(){
		alert("rwe");
	})
		
	
	
		
		
	</script>
</body>
</html>
