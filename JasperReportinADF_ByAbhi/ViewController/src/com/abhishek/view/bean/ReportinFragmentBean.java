package com.abhishek.view.bean;

import com.abhishek.model.am.AppModuleImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.HashMap;

import java.util.Map;

import javax.faces.context.FacesContext;


import javax.servlet.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;


import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;


import oracle.jbo.client.Configuration;


import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;


@WebServlet(name = "ReportServlet", urlPatterns = { "/reportservlet" })
public class ReportinFragmentBean extends HttpServlet{
    public ReportinFragmentBean() {
        super();
    }
    
    //https://community.oracle.com/thread/3689943?start=15&tstart=0

        /**

         * @param config

         * @throws ServletException

         */

        public void init(ServletConfig config) throws ServletException {
            super.init(config);
        }


        /**

         * @return

         */

        protected Connection getConnection() {

            PreparedStatement st = null;

            String amDef = "com.abhishek.model.am.AppModule";

            String config = "AppModuleLocal";

            AppModuleImpl am = (AppModuleImpl) Configuration.createRootApplicationModule(amDef, config);


            st = am.getDBTransaction().createPreparedStatement("select 1 from dual", 0);

            Connection conn = null;


            try {

                conn = st.getConnection();
                System.out.println("conn---" + conn);
                return conn;

            } catch (SQLException e) {

            }


            return null;

        }


        public HttpServletResponse getResponse() {
            return (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
        }

        public static FacesContext getFacesContext() {
            return FacesContext.getCurrentInstance();
        }


        /**

         * @param request

         * @param response

         * @throws ServletException

         * @throws IOException

         */

        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            String param = "";
            System.out.println("doGetdoGetdoGetdoGetdoGetdoGet");

            try {

                param = request.getParameter("employeeId");
            
                System.out.println("param--" + param);

            } catch (Exception e) {

                e.printStackTrace();

            }
            Map m = new HashMap();
            m.put("employeeId", param);


            Connection conn = null;

            InputStream is = null;

            try

            {

                conn = getConnection();

                ServletOutputStream out = response.getOutputStream();
                response.setHeader("Cache-Control", "max-age=0");
                response.setContentType("application/pdf");

                InputStream fs =
                    getServletContext()
                    .getResourceAsStream("/reports/employeeReport.jasper"); //we will put the report under folder "reports" under Web Content

                JasperReport template = (JasperReport) JRLoader.loadObject(fs);
                template.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
                conn = getConnection();

                try {
                    JasperPrint print = JasperFillManager.fillReport(template, m, conn);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JasperExportManager.exportReportToPdfStream(print, baos);
                    out.write(baos.toByteArray());
                    out.flush();
                    out.close();

                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

            catch (Exception e) {

                e.printStackTrace();

            } finally {

                closeConnection(conn);

                try {

                    if (is != null) {

                        is.close();

                    }


                } catch (Exception localException1)

                {

                }

            }


        }


        /**
         * @param conn
         */

        protected static void closeConnection(Connection conn) {

            try {

                if (conn != null) {

                    conn.close();

                }


            } catch (Exception ex) {


            }

        }
}
