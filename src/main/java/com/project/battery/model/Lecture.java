/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.project.battery.model;

import com.project.battery.service.FileService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author qntjd
 */
@Slf4j
@NoArgsConstructor
public class Lecture {
    
    @Getter@Setter private String thumnail = null; //1. 썸네일 url
    @Getter@Setter private String title= null; //2. 강의명
    @Getter@Setter private String text= null; // 3. 강의 내용
    @Getter@Setter private String text_image= null; // 4. 강의 내용
    @Getter@Setter private String postcode= null; //장소
    @Getter@Setter private String address= null; 
    @Getter@Setter private String detail= null;
    @Getter@Setter private String extra= null;
    @Getter@Setter private String rec_dt= null; //5. 모집기간
    @Getter@Setter private String rec_target= null; //6. 모집 대상
    @Getter@Setter private int rec_num = 0; // 7. 모집 인원
    @Getter@Setter private String datelist= null; // 8. 교육 기간
    @Getter@Setter private String keyword= null; // 9. 키워드
    @Getter@Setter private String price= null; // 10. 가격
    @Getter@Setter private int agree = 0; // 11. 자동 수락 동의(선착순)
    @Getter@Setter private int teacher = 0; // 12. 강사 모집
    @Getter@Setter private int teacher_num = 0;//13. 강사 인원 수
    @Getter@Setter private int staffe = 0; // 14. 스탭 모집
    @Getter@Setter private int staffe_num = 0;//15. 스탭 인원 수
    @Getter@Setter private String qualification= null; //16.모집 조건 작성
    @Getter@Setter private String resume= null; //18. 지원서 양식
    @Getter@Setter private String host= null; // 19. 강의자
    @Getter@Setter private Double grade = 0.0; // 20. 별점
    @Getter private int matCo;
    @Getter private String filename;
    @Getter private String fileuploader;
    @Getter private String filedate;

    //학습자료 정보 관리를 위한 생성자
    public Lecture(int matCo, String filename, String fileuploader, String filedate) {
        this.matCo = matCo;
        this.filename = filename;
        this.fileuploader = fileuploader;
        this.filedate = filedate;
    }
    
    DataSource ds = null;
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    
    
    public Boolean insertLecture(HikariConfiguration dbConfig, Lecture lecture,MultipartFile thumnail,MultipartFile text_image,MultipartFile resume, String[] path){
        boolean success=false;
        String sql = "INSERT INTO lecture values (default,default,?,?,default,?,?,?,?,?,?,?,?,?,?,?,?,default,?,default,0)";
        String addressSQL = "INSERT INTO address values (default,?,?,?,?,?,1)";
        String updateFileFathSql = "update lecture set thumbnail=?, text_image=?, resume=? where lectureid=?";
        
        try {
            ds = dbConfig.dataSource();
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, lecture.getTitle());
            pstmt.setString(2, lecture.getText());
            pstmt.setString(3, lecture.getRec_dt());
            pstmt.setString(4,lecture.getRec_target());
            pstmt.setInt(5,lecture.getRec_num());
            pstmt.setString(6,lecture.getDatelist());
            pstmt.setString(7,lecture.getKeyword());
            pstmt.setString(8,lecture.getPrice());
            pstmt.setInt(9, lecture.getAgree());
            pstmt.setInt(10,lecture.getTeacher());
            pstmt.setInt(11,lecture.getTeacher_num());
            pstmt.setInt(12,lecture.getStaffe());
            pstmt.setInt(13,lecture.getStaffe_num());
            pstmt.setString(14, lecture.getQualification());
            pstmt.setString(15,lecture.getHost());
            
            if( pstmt.executeUpdate() == 1){
                rs = pstmt.getGeneratedKeys();
                
                if(rs.next()){
                    pstmt = conn.prepareStatement(updateFileFathSql);
                    pstmt.setString(1, FileService.insertFolder(path[0],thumnail, Integer.toString(rs.getInt(1)),lecture.getHost()));
                    pstmt.setString(2, FileService.insertFolder(path[1],text_image, Integer.toString(rs.getInt(1)),lecture.getHost()));
                    pstmt.setString(3, FileService.insertFolder(path[2],resume, Integer.toString(rs.getInt(1)),lecture.getHost()));
                    pstmt.setInt(4, rs.getInt(1));
                    if(pstmt.executeUpdate() == 1){
                        pstmt = conn.prepareStatement(addressSQL);
                        pstmt.setInt(1, rs.getInt(1));
                        pstmt.setString(2,lecture.getPostcode());
                        pstmt.setString(3,lecture.getAddress());
                        pstmt.setString(4,lecture.getDetail());
                        pstmt.setString(5,lecture.getExtra());

                        if(pstmt.executeUpdate() == 1){
                            log.debug("강의 입력 성공 host={}, titlt={}",lecture.getHost(),lecture.getTitle());
                            success=true;
                        }else{
                            log.debug("강의 주소 정보 입력 실패 host={}, titlt={}",lecture.getHost(),lecture.getTitle());
                        }
                    }else{
                        log.debug("파일 정보 입력 실패 host={}, titlt={}",lecture.getHost(),lecture.getTitle());
                    }
                }
            }else{
                log.debug("강의 정보 입력 실패 host={}, titlt={}",lecture.getHost(),lecture.getTitle());
            }
        } catch (SQLException ex) {
           log.debug("강의 입력 실패 SqlError = {}",ex.getMessage());
        }finally{
            try {
                if(success){conn.commit();}else{conn.rollback();}
                conn.setAutoCommit(true);
                if(conn!=null){conn.close();}
                if(pstmt!=null){pstmt.close();}
                if(rs!=null){rs.close();}
            }catch (SQLException ex) {
                    Logger.getLogger(Lecture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    public boolean uploadMateria(HikariConfiguration dbConfig,String path, MultipartFile materia, String lecid, String id){
        boolean success=false;
        String sql = "insert into materia values(default, default, ?,?,?)";
        
        try {
            ds = dbConfig.dataSource();
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, FileService.insertFolder(path,
                                        materia, lecid, id));
            pstmt.setInt(2, Integer.parseInt(lecid));
            pstmt.setString(3, id);
            if(pstmt.executeUpdate() == 1){
                success = true;
            }else{
                log.debug("학습자료 입력 실패 host={}, lecture={}",id,lecid);
            }
       
        } catch (SQLException ex) {
            Logger.getLogger(Lecture.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    
    public List<Lecture> getMateriaList(HikariConfiguration dbConfig, int lectureid){
        List<Lecture> list = new ArrayList<>();
        String sql = "select materiaurl, uploader, date from materia where lectureid=? order by date asc";
        int co = 1;
        
        try {
            ds = dbConfig.dataSource();
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lectureid);
            rs = pstmt.executeQuery();
            while(rs.next()){
                list.add(new Lecture(co, rs.getString("materiaurl").substring(rs.getString("materiaurl").lastIndexOf("\\")+1),
                        rs.getString("uploader"),rs.getString("date")));
                co++;
            }
            Collections.reverse(list);
        } catch (SQLException ex) {
            Logger.getLogger(Lecture.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
}


