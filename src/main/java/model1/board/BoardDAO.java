package model1.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.JDBConnect;
import jakarta.servlet.ServletContext;

//JDBC를 이용한 DB연결을 위해 클래스 상속
public class BoardDAO extends JDBConnect {
	
	//인수생성자에서는 application내장객체를 매개변수로 전달한다. 
    public BoardDAO(ServletContext application) {
    	/* 부모 생성자에서는 application을 통해 web.xml에 직접 
    	접근하여 컨텍스트 초기화 파라미터를 얻어온다. */
        super(application);
    }
    
    //게시물의 갯수를 카운트하여 int형으로 반환한다. 
    public int selectCount(Map<String, Object> map) {
    	
    	//게시물의 갯수를 반환하기 위한 변수 
    	int totalCount = 0; 

    	//게시물 수를 얻어오기 위한 퀴리문 작성 
        // String query = "SELECT COUNT(*) FROM board";
    	String query = "SELECT COUNT(*) FROM "+ map.get("tname");
    	
    	
    	
        /* 검색어가 있는 경우 where절을 추가하여 조건에 맞는 게시물만
        select한다. */
        if (map.get("searchWord") != null) {
            query += " WHERE " + map.get("searchField") + " "
                   + " LIKE '%" + map.get("searchWord") + "%'";
        }

        try {
        	//정적쿼리문 실행을 위한 Statement객체 생성
            stmt = con.createStatement();    
            //쿼리문 실행 후 결과는 ResultSet으로 반환한다. 
            rs = stmt.executeQuery(query);
            //커서를 첫번째 행으로 이동하여 레코드를 읽는다. 
            rs.next();  
            //첫번째 컬럼(count함수)의 값을 가져와서 변수에 저장한다. 
            totalCount = rs.getInt(1);   
        }
        catch (Exception e) {
            System.out.println("게시물 수를 구하는 중 예외 발생");
            e.printStackTrace();
        }
        
        return totalCount; 
    }
    
    /* 작성된 게시물을 인출하여 반환한다. 특히 반환값은 여러개의 레코드를
    반환할 수 있고, 순서를 보장해야 하므로 List 컬렉션을 사용한다. */
    public List<BoardDTO> selectList(Map<String, Object> map) { 
        
    	/* List계열의 컬렉션을 생성한다. 이때 타입 매개변수는 board테이블
    	을 대상으로 하므로 BoardDTO로 설정한다. */
    	List<BoardDTO> bbs = new Vector<BoardDTO>();  

    	/* 레코드 인출을 위한 select 쿼리문 작성. 최근 게시물이 상단에
    	출력되야 하므로 일련번호의 내림차순으로 정렬한다. */
        String query = "SELECT * FROM board "; 
        if (map.get("searchWord") != null) {
            query += " WHERE " + map.get("searchField") + " "
                   + " LIKE '%" + map.get("searchWord") + "%' ";
        }
        query += " ORDER BY num DESC "; 

        try {
        	//쿼리 실행 및 결과셋 반환 
            stmt = con.createStatement();    
            rs = stmt.executeQuery(query);
            //2개 이상의 레코드가 반환될 수 있으므로 while문을 사용한다. 
            while (rs.next()) {
            	//하나의 레코드를 저장할 수 있는 DTO객체를 생성한다.
                BoardDTO dto = new BoardDTO(); 

                //setter를 이용해서 각 컬럼의 값을 멤버변수에 저장한다. 
                dto.setNum(rs.getString("num"));          
                dto.setTitle(rs.getString("title"));       
                dto.setContent(rs.getString("content")); 
                dto.setPostdate(rs.getDate("postdate"));   
                dto.setId(rs.getString("id"));            
                dto.setVisitcount(rs.getString("visitcount"));  
                
                //List에 DTO를 추가한다. 
                bbs.add(dto);  
            }
        } 
        catch (Exception e) {
            System.out.println("게시물 조회 중 예외 발생");
            e.printStackTrace();
        }

        //인출한 레코드를 저장한 List를 호출한 지점으로 반환한다. 
        return bbs;
    }
    
    //게시물 입력을 위한 메서드. 폼값이 저장된 DTO객체를 인수로 받는다.
    public int insertWrite(BoardDTO dto) {
        int result = 0;        
        try {
        	/* 인파라미터가 있는 동적쿼리문으로 insert문을 작성한다. 
        	게시물의 일련번호는 시퀀스를 통해 자동부여하고, 조회수는 
        	0으로 입력한다. */
            String query = "INSERT INTO board ( "
                         + " num,title,content,id,visitcount) "
                         + " VALUES ( "
                         + " seq_board_num.NEXTVAL, ?, ?, ?, 0)";  
            psmt = con.prepareStatement(query); 
            //인파라미터는 DTO에 저장된 내용으로 채워준다. 
            psmt.setString(1, dto.getTitle());  
            psmt.setString(2, dto.getContent());
            psmt.setString(3, dto.getId());  
            //insert쿼리문을 실행한 후 결과값(int)을 반환받는다. 
            result = psmt.executeUpdate(); 
        }
        catch (Exception e) {
            System.out.println("게시물 입력 중 예외 발생");
            e.printStackTrace();
        }
        
        return result;
    }
    
    //인수로 전달된 게시물의 일련번호로 하나의 게시물을 인출한다. 
    public BoardDTO selectView(String num) { 
    	//하나의 레코드를 저장하기 위한 DTO객체 생성
    	BoardDTO dto = new BoardDTO();
    	
    	/* 내부조인(inner join)을 통해 member테이블의 name컬럼까지
    	select 한다. */
        String query = "SELECT B.*, M.name " 
                     + " FROM member M INNER JOIN board B " 
                     + " ON M.id=B.id "
                     + " WHERE num=?";
        try {
        	//쿼리문의 인파라미터를 설정한 후 쿼리문 실행 
            psmt = con.prepareStatement(query);
            psmt.setString(1, num);   
            rs = psmt.executeQuery();
            /*
            일련번호는 중복되지 않으므로 단 한개의 게시물만 인출하게된다. 
            따라서 while문이 아닌 if문으로 처리한다. next() 메서드는 
            ResultSet으로 반환된 게시물을 확인해서 존재하면 true를 
            반환해준다. 
            */
            if (rs.next()) { 
                dto.setNum(rs.getString(1)); 
                dto.setTitle(rs.getString(2)); 
                /* 
                각 컬럼의 값을 추출할때 1부터 시작하는 인덱스와 컬럼명
                둘 다 사용할 수 있다. 날짜인 경우에는 getDate() 메서드로
                추출할 수 있다.  
                */
                dto.setContent(rs.getString("content")); 
                dto.setPostdate(rs.getDate("postdate")); 
                dto.setId(rs.getString("id"));
                dto.setVisitcount(rs.getString(6));
                dto.setName(rs.getString("name")); 
            }
        } 
        catch (Exception e) {
            System.out.println("게시물 상세보기 중 예외 발생");
            e.printStackTrace();
        }
        
        return dto; 
    }
    
    //게시물의 조회수를 1 증가시킨다. 
    public void updateVisitCount(String num) {
    	/* 게시물의 일련번호를 통해 visitcount를 1 증가시킨다. 
    	해당 컬럼은 number 타입이므로 사칙연산이 가능하다. */
        String query = "UPDATE board SET "
                     + " visitcount=visitcount+1 "
                     + " WHERE num=?";
        
        try {
            psmt = con.prepareStatement(query);
            psmt.setString(1, num);   
            psmt.executeQuery();   
        } 
        catch (Exception e) {
            System.out.println("게시물 조회수 증가 중 예외 발생");
            e.printStackTrace();
        }
    }
    
    //게시물 수정하기 
    public int updateEdit(BoardDTO dto) { 
        int result = 0;        
        try {
        	//특정 일련번호에 해당하는 게시물을 수정한다. 
            String query = "UPDATE board SET "
                         + " title=?, content=? "
                         + " WHERE num=?";
            //쿼리문의 인파라미터 설정 
            psmt = con.prepareStatement(query);
            psmt.setString(1, dto.getTitle());
            psmt.setString(2, dto.getContent());
            psmt.setString(3, dto.getNum());
            //수정된 레코드의 갯수를 반환한다. 
            result = psmt.executeUpdate();
        } 
        catch (Exception e) {
            System.out.println("게시물 수정 중 예외 발생");
            e.printStackTrace();
        }
        
        return result; 
    }
    
    //게시물 삭제하기 
    public int deletePost(BoardDTO dto) { 
        int result = 0;

        try {
        	//인파라미터가 있는 delete쿼리문 작성
            String query = "DELETE FROM board WHERE num=?";
            psmt = con.prepareStatement(query); 
            psmt.setString(1, dto.getNum()); 
            result = psmt.executeUpdate(); 
        } 
        catch (Exception e) {
            System.out.println("게시물 삭제 중 예외 발생");
            e.printStackTrace();
        }
        
        return result;  
    }
    
    //게시물 목록 출력시 페이징 기능 추가 
    public List<BoardDTO> selectListPage(Map<String, Object> map) {
        List<BoardDTO> bbs = new ArrayList<BoardDTO>();  
        
        /* 검색조건에 일치하는 게시물을 얻어온 후 각 페이지에 출력할
        구간까지 설정한 서브 쿼리문 작성 */
        String query = " SELECT * FROM ( "
    			+ "    SELECT Tb.*, ROWNUM rNum FROM ( "
                + "        SELECT * FROM "+ map.get("tname");
            
        //검색어가 있는 경우에만 where절을 추가한다. 
        if (map.get("searchWord") != null) {
            query += " WHERE " + map.get("searchField")
                   + " LIKE '%" + map.get("searchWord") + "%' ";
        }
        /* 게시물의 구간을 결정하기 위해 between 혹은 비교연산자를 
        사용할 수 있다. 아래의 where절은 rNum>? 과 같이 변경할수있다. */
        query += "      	ORDER BY num DESC "
               + "     ) Tb "
               + " ) "
               + " WHERE rNum BETWEEN ? AND ?";

        try {
        	//인파라미터가 있는 쿼리문으로 prepared객체 생성
            psmt = con.prepareStatement(query);
            //인파라미터 설정
            psmt.setString(1, map.get("start").toString());
            psmt.setString(2, map.get("end").toString());
            //쿼리문 실행 및 ResultSet반환
            rs = psmt.executeQuery();
            while (rs.next()) {
                BoardDTO dto = new BoardDTO();
                
                dto.setNum(rs.getString("num"));
                dto.setTitle(rs.getString("title"));
                dto.setContent(rs.getString("content"));
                dto.setPostdate(rs.getDate("postdate"));
                dto.setId(rs.getString("id"));
                dto.setVisitcount(rs.getString("visitcount"));

                bbs.add(dto);
            }
        } 
        catch (Exception e) {
            System.out.println("게시물 조회 중 예외 발생");
            e.printStackTrace();
        }
        
        return bbs;
    }
}
