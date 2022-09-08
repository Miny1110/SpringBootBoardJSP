package com.spring.boot.contorller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.spring.boot.dto.BoardDTO;
import com.spring.boot.service.BoardService;
import com.spring.boot.util.MyPage;


/**
 * @RequestBody:
 * JSP를 인식하지 못한다.
 * 
 * 클라이언트가 전송하는 JSON(application/Json)형태의 http body 내용을 java Object로 변환
 *그래서 body가 없는 get, delete 메소드에 @requestbody를 쓰면 에러가 발생한다.
 *
 * 이 어노테이션이 붙은 파라미터에는 HTTP요청의 본문 BODY부분이 그대로 전달된다.
 *
 * HTTP 요청의 바디내용을 통째로 자바객체로 변환해서 매핑된 메소드 파라미터로 전달해준다.
 *
 * @RequestBody로 받는 데이터는 스프링에서 관리하는 메세지 컨버터를 통해 자바 객체로 변환된다.
 *
 * 스프링은 메세지를 변환시키는 과정에서 객체의 기본 생성자를 통해 객체를 생성하고,
 * 내부적으로 reflection을 사용해서 값을 할당하므로 값을 주입하기 위한 생성자나 setter가 필요 없다.
 * */
/**@RestController : @Controller + @RequestBody*/
@Controller
public class BoardController {

    @Resource
    private BoardService boardService;

    @Autowired
    MyPage myPage;

    @GetMapping("/")
    public ModelAndView index() throws Exception{

        ModelAndView mav = new ModelAndView();

        mav.setViewName("index");

        return mav;
    }

    @GetMapping("/created.action")
    public ModelAndView create() throws Exception{

        ModelAndView mav = new ModelAndView();

        mav.setViewName("bbs/created");

        return mav;
    }

    @PostMapping("/created_ok.action")
    public ModelAndView create_ok(BoardDTO dto, HttpServletRequest request) throws Exception{

        ModelAndView mav = new ModelAndView();

        int maxNum = boardService.maxNum();

        dto.setNum(maxNum + 1);
        dto.setIpAddr(request.getRemoteAddr());

        boardService.insertData(dto);

        mav.setViewName("redirect:/list.action");
        return mav;
    }

    @GetMapping("/list.action")
    public ModelAndView list(BoardDTO dto, HttpServletRequest request) throws Exception{

        String pageNum = request.getParameter("pageNum");

        int currentPage = 1;

        if(pageNum!=null) {
            currentPage = Integer.parseInt(pageNum);
        }

        String searchKey = request.getParameter("searchKey");
        String searchValue = request.getParameter("searchValue");

        if(searchValue==null) {
            searchKey = "subject";
            searchValue = "";

        }else {
            if(request.getMethod().equalsIgnoreCase("GET")) {
                searchValue = URLDecoder.decode(searchValue,"UTF-8");
            }
        }

        int dataCount = boardService.getDataCount(searchKey, searchValue);

        int numPerPage = 5;

        int totalPage = myPage.getPageCount(numPerPage, dataCount);

        if(currentPage>totalPage) {
            currentPage=totalPage;
        }

        int start = (currentPage-1)*numPerPage+1;
        int end = currentPage * numPerPage;

        List<BoardDTO> lists = boardService.getLists(start, end, searchKey, searchValue);

        String param = "";
        if(searchValue!=null &&!searchValue.equals("")) {
            param = "searchKey=" + searchKey + "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
        }

        String listUrl = "/list.action";

        if(!param.equals("")) {
            listUrl += "?" + param;
        }

        String pageIndexList = myPage.pageIndexList(currentPage, totalPage, listUrl);

        String articleUrl = "/article.action?pageNum=" + currentPage;

        if(!param.equals("")) {
            articleUrl += "&" + param;
        }

        ModelAndView mav = new ModelAndView();
        
        //포워딩할 데이터
        mav.addObject("lists", lists);
        mav.addObject("pageIndexList", pageIndexList);
        mav.addObject("articleUrl", articleUrl);
        mav.addObject("dataCount", dataCount);
        mav.addObject("pageNum", currentPage);

        mav.setViewName("bbs/list");
        return mav;
    }

    @GetMapping("/article.action")
    public ModelAndView article(HttpServletRequest request) throws Exception{

        int num = Integer.parseInt(request.getParameter("num"));
        String pageNum = request.getParameter("pageNum");

        String searchKey = request.getParameter("searchKey");
        String searchValue = request.getParameter("searchValue");

        if(searchValue!=null && !searchValue.equals("")) {
            searchValue = URLDecoder.decode(searchValue,"UTF-8");
        }

        boardService.updateHitCount(num);

        BoardDTO dto = boardService.getReadData(num);

        if(dto==null) {

            ModelAndView mav = new ModelAndView();

            mav.setViewName("redirect:/list.action?pageNum="+pageNum);

            return mav;
        }

        int lineSu = dto.getContent().split("\n").length;

        //dto.setContent(dto.getContent().replaceAll("\r", "<br/>"));

        String param = "pageNum=" + pageNum;

        if(searchValue!=null&&!searchValue.equals("")) {

            param += "&searchKey=" + searchKey;
            param += "&searchValue=" + URLEncoder.encode(searchValue,"UTF-8");
        }

        ModelAndView mav = new ModelAndView();

        mav.addObject("dto",dto);
        mav.addObject("params",param);
        mav.addObject("lineSu",lineSu);
        mav.addObject("pageNum", pageNum);

        mav.setViewName("bbs/article");
        return mav;
    }

    @GetMapping("/updated.action")
    public ModelAndView update(HttpServletRequest request) throws Exception{

        int num = Integer.parseInt(request.getParameter("num"));
        String pageNum = request.getParameter("pageNum");

        String searchKey = request.getParameter("searchKey");
        String searchValue = request.getParameter("searchValue");

        if(searchValue!=null && !searchValue.equals("")) {
            searchValue = URLDecoder.decode(searchValue,"UTF-8");

        }
        ModelAndView mav = new ModelAndView();

        BoardDTO dto = boardService.getReadData(num);

        if(dto==null) {

            mav.setViewName("redirect:/list.action?pageNum="+pageNum);
        }

        String param = "pageNum=" + pageNum;

        if(searchValue!=null&&!searchValue.equals("")) {

            param += "&searchKey=" + searchKey;
            param += "&searchValue=" + URLEncoder.encode(searchValue,"UTF-8");
        }

        mav.addObject("dto", dto);
        mav.addObject("pageNum", pageNum);
        mav.addObject("params", param);
        mav.addObject("searchKey", searchKey);
        mav.addObject("searchValue", searchValue);

        mav.setViewName("bbs/updated");

        return mav;

    }
    
    
    @PostMapping("/updated_ok.action")
    public ModelAndView update_ok(BoardDTO dto, HttpServletRequest request) throws Exception{
    
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchValue!=null) {
			searchValue = URLDecoder.decode(searchValue,"utf-8");
		}		
		
		dto.setContent(dto.getContent().replaceAll("<br/>", "\r\n"));
		boardService.updateData(dto);
		
		String param = "pageNum=" + pageNum;
		
		if(searchValue!=null && !searchValue.equals("")) {
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue,"utf-8");
		}
	
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("redirect:/list.action?" + param);	
    
		return mav;
    
    }
    
    
    @GetMapping("/deleted_ok.action")
    public ModelAndView deleted_o(HttpServletRequest request) throws Exception{
    	
    	int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchValue!=null && searchValue.equals("")) {
			searchValue = URLDecoder.decode(searchValue,"utf-8");
					
		}	
		
		boardService.deleteData(num);
		
		String param = "pageNum=" + pageNum;
		
		if(searchValue!=null && !searchValue.equals("")) {
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue,"utf-8");
		}			
		
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("redirect:/list.action?" + param);	
    
		return mav;
    	
    	
    	
    }
    
    
    
}
