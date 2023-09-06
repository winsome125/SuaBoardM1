<%@page import="model1.board.BoardDTO"%>
<%@page import="model1.board.BoardDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
/* 목록에서 제목을 클릭하면 게시물의 일련번호를 ?num=99와 
같이 받아온다. 게시물 인출을 위해 파라미터를 받아온다. */
String num = request.getParameter("num");
//DAO객체 생성을 통해 오라클에 연결한다. 
BoardDAO dao = new BoardDAO(application);
//게시물의 조회수 증가
dao.updateVisitCount(num);
//게시물의 내용을 인출하여 DTO에 저장한다. 
BoardDTO dto = dao.selectView(num);
dao.close();
%>

<%@ include file="../include/global_head.jsp" %>


 <body>
	<center>
	<div id="wrap">
		<%@ include file="../include/top.jsp" %>

		<img src="../images/space/sub_image.jpg" id="main_visual" />

		<div class="contents_box">
			<div class="left_contents">
				<%@ include file = "../include/space_leftmenu.jsp" %>
			</div>
			<div class="right_contents">
				<div class="top_title">
					<img src="../images/space/sub01_title.gif" alt="공지사항" class="con_title" />
					<p class="location"><img src="../images/center/house.gif" />&nbsp;&nbsp;열린공간&nbsp;>&nbsp;공지사항<p>
				</div>
				<div>
				
<!-- 게시판 들어가는 부분s -->

<form name="writeFrm">
<input type="hidden" name="num" value="<%= num %>" />

	<!-- DTO에 저장된 내용을 getter를 통해 웹브라우저에 출력한다. -->  
    <table border="1" width="90%" class="table table-bordered">
        <tr>
            <td>번호</td>
            <td><%= dto.getNum() %></td>
            <td>작성자</td>
            <td><%= dto.getName() %></td>
        </tr>
        <tr>
            <td>작성일</td>
            <td><%= dto.getPostdate() %></td>
            <td>조회수</td>
            <td><%= dto.getVisitcount() %></td>
        </tr>
        <tr>
            <td>제목</td>
            <td colspan="3"><%= dto.getTitle() %></td>
        </tr>
        <tr>
            <td>내용</td>
            <td colspan="3" height="100">
            	<!-- 입력시 줄바꿈을 위한 엔터는 \r\n으로 입력되므로 
            	웹	브라우저에 출력시에는 <br>태그로 변경해야한다. -->
                <%= dto.getContent().replace("\r\n", "<br/>") %>
            </td> 
        </tr>
        <tr>
            <td colspan="4" align="center">
<%
/* 로그인이 된 상태에서 세션영역에 저장된 아이디가 해당 게시물을 
작성한 아이디와 일치하면 수정, 삭제 버튼을 보이게 처리한다. 
즉, 작성자 본인이 해당 게시물을 조회했을때만 수정, 삭제 버튼이 보이게
처리한다. */
if(session.getAttribute("UserId")!=null &&  
	dto.getId().equals(session.getAttribute("UserId").toString())){
%>
     <button type="button" class="btn btn-outline-secondary" style="font-size: 12px"
             onclick="location.href='Edit.jsp?num=<%= dto.getNum() %>';">
         수정하기</button>
         &nbsp;
     <!-- 삭제하기 버튼을 누르면 JS의 함수를 호출한다. 해당 함수는 
     submit()을 통해 폼값을 서버로 전송한다.  -->
     <button type="button" class="btn btn-outline-secondary" style="font-size: 12px" onclick="deletePost();">삭제하기</button> 
      &nbsp;
<%
}
%>
                <button type="button" onclick="location.href='Edit.jsp?num=<%= dto.getNum() %>';">
                     수정하기</button>
                <button type="button" class="btn btn-outline-secondary" style="font-size: 12px" onclick="location.href='sub01List.jsp';">
                    목록 보기
                </button>
            </td>
        </tr>
    </table>
</form>
				
<!-- 게시판 들어가는 부분e-->
				</div>
			</div>
		</div>
		<%@ include file="../include/quick.jsp" %>
	</div>


	<%@ include file="../include/footer.jsp" %>
	</center>
 </body>
</html>