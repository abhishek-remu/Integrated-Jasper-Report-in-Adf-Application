package com.abhishek.view.bean;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JasperReport;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;

import oracle.binding.BindingContainer;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;

public class JasperReportBean {
    public JasperReportBean() {
    }

    public String runReport() {
        //---Add event code here...

        //---To get the selected employee id...:
        DCIteratorBinding empIter = (DCIteratorBinding) getBindings().get("EmployeesVO1Iterator");
        String empId = empIter.getCurrentRow()
                              .getAttribute("EmployeeId")
                              .toString();

        //---where EmployeesView1Iterator is the iterator name in the page definition and EmployeeId is the attribute name in the EmployeesView.

        //---now we should pass the selected employee Id to jasper report so, you should make a map and set the parameter like
        Map m = new HashMap();
        m.put("employeeId", empId); // employeeId is a jasper parameter name

        //---then you should call the jasper report like this method
        try{
            System.out.println("mmmmmm   "+m);
                
        runReport("employeeReport.jasper", m);
        }
        catch(Exception e){
        e.printStackTrace();
        }

        //---where runReport is the method take jasper report name (empReport.jasper) and the map which hold the parameter
        return null;
    }
    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public Connection getDataSourceConnection(String dataSourceName) throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(dataSourceName);
        return ds.getConnection();
    }

    private Connection getConnection() throws Exception {
        return getDataSourceConnection("java:comp/env/jdbc/hrDS"); // ----use datasourse in your application
    }

    public ServletContext getContext() {
        return (ServletContext) getFacesContext().getExternalContext().getContext();
    }

    public HttpServletResponse getResponse() {
        return (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
    }

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public void runReport(String repPath, java.util.Map param) throws Exception {
        Connection conn = null;
        try {
            
            HttpServletResponse response = getResponse();
            ServletOutputStream out = response.getOutputStream();
            response.setHeader("Cache-Control", "max-age=0");
            response.setContentType("application/pdf");
            ServletContext context = getContext();
            InputStream fs =
                context.getResourceAsStream("/reports/" +
                                            repPath); //we will put the report under folder "reports" under Web Content
            
            JasperReport template = (JasperReport) JRLoader.loadObject(fs);
            template.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
            conn = getConnection();
            System.out.println("template  "+template);
            System.out.println("param  "+param);
            System.out.println("conn  "+conn);
            JasperPrint print = JasperFillManager.fillReport(template, param, conn);
           
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(print, baos);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception jex) {
            jex.printStackTrace();
        } finally {
            close(conn);
        }
    }

    public void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
    }
}
