package controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import config.AppCtx;
import mvc.controller.DefaultFileRenamePolicy;
import mvc.controller.MultipartRequest;
import mvc.model.BoardDAO;
import mvc.model.PersonalDAO;
import mvc.model.PersonalDTO;
import mvc.model.Remember;
import mvc.model.StudentDAO;
import mvc.model.notice_boardDAO;
import mvc.model.notice_boardDTO;

@Controller
@RequestMapping("/board")
public class notice_boardController
	{
	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);
	notice_boardDAO nb_dao = ctx.getBean("notice_boardDAO", notice_boardDAO.class);
	PersonalDAO p_dao = ctx.getBean("personalDAO", PersonalDAO.class);
	StudentDAO s_dao = ctx.getBean("studentDAO", StudentDAO.class);
	
	@RequestMapping("/p_noticeboardlist")
	public String p_noticeboardlist(Model model, @RequestParam(value = "pageNum", required = false) String pageNum, @RequestParam(value = "items", required = false) String items, @RequestParam(value = "text", required = false) String text)
		{
		requestNoticeBoardList(model, pageNum, items, text);
		
		return "board/p_noticeboardlist";
		}
	
	@RequestMapping("/p_noticewrite")
	public String p_noticewrite(Model model, HttpSession session)
		{
		Remember remember = (Remember) session.getAttribute("remember");
		String p_id = remember.getId();
		
		getdto(model, p_id);
		
		return "board/p_noticewrite";
		}
	
	@RequestMapping("/p_noticewrite_update")
	public String p_noticewrite_update(Model model, @RequestParam(value = "pageNum", required = false) String pageNum, @RequestParam(value = "items", required = false) String items, @RequestParam(value = "text", required = false) String text)
		{
		newNotice(request);
		requestNoticeBoardList(model, pageNum, items, text);
		
		return "board/p_noticeboardlist";
		}
	
	@RequestMapping("/p_notice")
	public String p_notice(Model model, @RequestParam(value = "n_num", required = false) int num, @RequestParam(value = "pageNum", required = false) int pageNum)
		{
		read_notice(model, num, pageNum);
		
		return "board/p_notice";
		}
	
	@RequestMapping("/p_editnotice")
	public String p_editnotice(Model model, @RequestParam(value = "n_num", required = false) int num, @RequestParam(value = "pageNum", required = false) int pageNum)
		{
		edit_notice(model, num, pageNum);
		
		return "board/p_editnoticewrite";
		}
	
	@RequestMapping("/p_editnoticewrite_update")
	public String p_editnoticewrite_update(Model model, @RequestParam(value = "n_num", required = false) int num, @RequestParam(value = "pageNum", required = false) int pageNum)
		{
		int[] num_arr = update_edit_notice(model, num, pageNum);
		read_notice(model, num_arr[0], num_arr[1]);
		
		model.addAttribute("n_num", num_arr[0]);
		model.addAttribute("pageNum", num_arr[1]);
		
		return "board/p_notice";
		}
	
	@RequestMapping("/p_deletenotice")
	public String p_deletenotice(Model model, @RequestParam(value = "n_num", required = false) int num, @RequestParam(value = "pageNum", required = false) int PageNum, @RequestParam(value = "items", required = false) String items, @RequestParam(value = "text", required = false) String text)
		{
		int pageNum = delete_notice(num, PageNum);
		String PageNum2 = Integer.toString(pageNum);
		requestNoticeBoardList(model, PageNum2, items, text);
		
		return "";
		}
	
	@RequestMapping("/s_noticeboardlist")
	public String s_noticeboardlist(Model model, @RequestParam(value = "pageNum", required = false) String pageNum, @RequestParam(value = "items", required = false) String items, @RequestParam(value = "text", required = false) String text)
		{
		requestNoticeBoardList(model, pageNum, items, text);
		
		return "board/s_noticeboardlist";
		}
	
	@RequestMapping("/s_notice")
	public String s_notice(Model model, @RequestParam(value = "n_num", required = false) int num, @RequestParam(value = "pageNum", required = false) int pageNum)
		{
		read_notice(model, num, pageNum);
		
		return "board/s_notice";
		}
	
	
	
	
	// ???????????? ????????? ????????????
	public void requestNoticeBoardList(Model model, String PageNum, String items, String text)
		{
		int pageNum=1;
		int limit=10;
		
		if (PageNum != null)
			{
			pageNum = Integer.parseInt(PageNum);
			}
		
		int total_record=nb_dao.getListCount(items, text); // ??? ?????? ????????????
		ArrayList<notice_boardDTO> nblist = nb_dao.getBoardList(pageNum, limit, items, text); // ?????? ??? ????????? list
		
		int total_page; // ???????????? ?????? ????????? ?????? ??? ????????? ???
		
		if (total_record % limit == 0)
			{
			total_page =total_record/limit;
			Math.floor(total_page);
			}
		
		else
			{
			total_page = total_record/limit;
			Math.floor(total_page);
			total_page = total_page + 1;
			}
		
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("total_page", total_page);
		model.addAttribute("nblist", nblist);
		}
	
	// ????????? ?????? ?????? ???, ????????? ???????????? ????????? id??? ?????? ????????? ??? ???????????????
	public void getdto(Model model, String p_id)
		{
		PersonalDTO p_dto = p_dao.getinfo(p_id);
		
		model.addAttribute("p_id", p_id);
		model.addAttribute("dto", p_dto);
		}
	
	// ??? ??? ??????
	public void newNotice(HttpServletRequest request)
		{
		String realFolder = request.getSession().getServletContext().getRealPath("resource/upload");
		int maxSize = 10 * 1024 * 1024;
		
		try
			{	
			MultipartRequest multi = new MultipartRequest(request, realFolder, maxSize, "utf-8", new DefaultFileRenamePolicy());
			
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");
			String regist_day = formatter.format(new java.util.Date());
			
			String contents = multi.getParameter("summernote");
			contents = contents.replace("\r\n", "<br>");
			
			String filename = multi.getFilesystemName("filename");
			String realname = multi.getOriginalFileName("filename");
			
			notice_boardDTO nbDTO = new notice_boardDTO();
			nbDTO.setN_subject(multi.getParameter("title"));
			nbDTO.setN_date(regist_day);
			nbDTO.setP_department(multi.getParameter("p_department"));
			nbDTO.setP_oNumber(multi.getParameter("p_oNumber"));
			nbDTO.setN_contents(contents);
			nbDTO.setP_name(multi.getParameter("p_name"));
			nbDTO.setP_id(multi.getParameter("p_id"));
			nbDTO.setN_filename(filename);
			nbDTO.setN_realname(realname);
			
			nb_dao.insertBoard(nbDTO);
			}
		
		catch(Exception e)
			{
			e.printStackTrace();
			}
		}
	
	//?????? ?????? ??? ???????????? ?????????
	public void read_notice(Model model, int num, int pageNum)
		{
		notice_boardDTO nbDTO = nb_dao.getClick_title(num);
		
		model.addAttribute("n_num", num);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("nbDTO", nbDTO);
		}
	
	public void file_down(HttpServletRequest request, HttpServletResponse response)
		{
		//???????????? ??? ?????? ???
		String filename = request.getParameter("n_filename");
		String realname = request.getParameter("n_realname");
		
		//????????? ?????? ????????????
		String folder = request.getServletContext().getRealPath("resource/upload");
		
		//????????? ????????????
		String filePath = folder + "/" + filename;
		
		try
			{
			//?????? ????????? ???????????? ?????? ??????, ????????? ???????????? ????????? ?????? ??????
			File file = new File(filePath);
			byte b[] = new byte[(int) file.length()];
			
			//page??? ContentType?????? ???????????? ????????? ?????? ?????????
			response.reset();
			response.setContentType("application/octet-stream");
			
			//?????? ?????????
			//String encoding = new String(filename.getBytes("utf-8"));
			//String encoding = new String(filename.getBytes("euc-kr"),"8859_1");
			String encoding = new String(realname.getBytes("euc-kr"),"8859_1");
			
			//?????? ????????? ???????????? ??? ???????????? ?????? ????????? ???????????? ???????????? ??????
			response.setHeader("Content-Disposition", "atachement;filename="+ encoding);
			response.setHeader("Content-Length", String.valueOf(file.length()));
			
			//????????? ?????? ??????
			if(file.isFile())
				{
				FileInputStream fInputSt = new FileInputStream(file);
				ServletOutputStream sOutputSt = response.getOutputStream();
				
				//????????? ????????? ?????????????????? ??????
				int readNum =0;
				
				while((readNum = fInputSt.read(b)) != -1)
					{
					sOutputSt.write(b, 0, readNum);
					}
				
				sOutputSt.close();
				fInputSt.close();
				}
			}
		
		catch (Exception e)
			{
			System.out.println("file_down Exception : " + e.getMessage());
			}
		}
	
	//??? ?????? ???????????? ??????
	public void edit_notice(Model model, int num, int pageNum)
		{
		notice_boardDTO nb_dto = nb_dao.editform(num);
		
		model.addAttribute("n_num", num);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("nbDTO", nb_dto);
		}
	
	//?????? ?????? ????????? ???
	public int[] update_edit_notice(Model model, int num, int pageNum)
		{
		String realFolder = request.getSession().getServletContext().getRealPath("resource/upload");
		int maxSize = 10 * 1024 * 1024;
		
		int[] num_arr = {num, pageNum};
		
		try
			{
			MultipartRequest multi = new MultipartRequest(request, realFolder, maxSize, "utf-8", new DefaultFileRenamePolicy());
			
			String filename = multi.getFilesystemName("filename");
			String realname = multi.getOriginalFileName("filename");
			
			String title = multi.getParameter("title");
			String contents = multi.getParameter("summernote");
			contents = contents.replace("\r\n", "<br>");
			
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");
			String write_day = formatter.format(new java.util.Date());
			
			nb_dao.update_editnotice(num, title, contents, write_day);
			}
		
		catch(Exception e)
			{
			e.printStackTrace();
			}
		
		return num_arr;
		}
	
	// ?????? ?????? ????????? ???
	public int delete_notice(int n_num, int pageNum)
		{
		nb_dao.deleteDAO(n_num);
		
		return pageNum;
		}
	}