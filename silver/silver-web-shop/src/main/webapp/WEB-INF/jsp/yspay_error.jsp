<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<%-- <base href="<%=basePath%>"> --%>

<title>错误处理页</title>

<script type="text/javascript" src="../res/js/jquery.js"></script>
<script type="text/javascript" src="../res/css/layer/layer.js"></script>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
<style type="text/css">
* {
	padding: 0px;
	margin: 0px;
}

.error_M {
	width: 500px;
	height: 160px;
	margin: 150px auto;
}

.error_L {
	float: left;
	width: 190px;
}

.error_L.a {
	background: url(../res/images/error_ico.png) 0px 0px no-repeat;
	height: 130px;
}

.error_L.b {
	background: url(../res/images/error_ico.png) 0px -115px no-repeat;
	height: 170px;
}

.error_R {
	float: left;
	width: 298px;
	padding-top: 2px;
}

.error_R h1 {
	height: 40px;
	font-family: "微软雅黑";
	font-size: 24px;
	color: #ff6600;
	font-weight: normal;
}

.error_R dl {
	padding-left: 5px;
	color: #666;
}

.error_R dl dt {
	height: 25px;
	font-size: 14px;
	font-weight: bold;
}

.error_R dl dd {
	line-height: 30px;
	font-size: 18px;
}

.error_R dl dd b {
	line-height: 30px;
	font-size: 18px;
	color: #ff6600
}

.error_R dl dt.dt {
	padding-top: 11px;
}

.error_R dl dt.dt a {
	color: #0076ae;
	text-decoration: underline;
}

.error_R dl dt.dt a:hover {
	color: #0076ae;
	text-decoration: none;
}
</style>
</head>

<body>
	<div class="error_M">
		<div class="error_L a"></div>
		<div class="error_R">
			<h1>操作失败！</h1>
			<dl>
				<dd>充值金额错误,请重新输入!</dd>
				<%-- <dd> 异常:<b><%=request.getAttribute("javax.servlet.error.exception_type")%> </b> </dd> --%>
				<dt class="dt">
					页面自动 <a id="href" href="">跳转</a> 等待时间：
					<b id="wait">3</b>
				</dt>
			</dl>
		</div>
	</div>
	<script type="text/javascript">
	(function(){
		var wait = document.getElementById('wait');
		var interval = setInterval(function(){
			var time = --wait.innerHTML;
			if(time <= 0) {
				//layer.closeAll();
				parent._guanbi();
				//location.href = href;
				clearInterval(interval);
			};
		}, 1000);
		})();
	</script>
</body>
</html>
