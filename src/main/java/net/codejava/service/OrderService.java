package net.codejava.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.codejava.entity.Order;
import net.codejava.entity.OrderKey;
import net.codejava.entity.StoreInfo;
import net.codejava.entity.User;
import net.codejava.model.GraphData;
import net.codejava.model.GraphRow;
import net.codejava.model.OrderModel;
import net.codejava.repo.OrderRepository;
import net.codejava.utility.Constant;
import net.codejava.utility.CustomException;

@Service
@Slf4j
public class OrderService {
	
	@Autowired
	OrderRepository orderRepository;
	
	
	@Autowired
	WordPressService wordPressService;
	
	@Autowired
	PostExService postExService;
	

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	     
	 
	
	@Transactional
	public void fetchTodayOrder(StoreInfo store,User user){
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

			List<OrderModel> orderModel = new ArrayList<>();
			List<Order> orders = new ArrayList<>();
			if(store.getStoreType().getName().equals(Constant.WORDPRESS)) {
				 orderModel = wordPressService.getTodaysOrders(store);
			}
			for(OrderModel model:orderModel) {
				Order order = orderRepository.findFirstByIdOrderNoAndUser(model.getOrderNo(),user);
				if(order == null) {
					order = new Order();
					order.setStatus("Unpaid");
				}
				
				order.setUser(user);
				BeanUtils.copyProperties(model, order);
		        //LocalDate date = LocalDate.parse(model.getDateCreated(), formatter);
				OrderKey key = new OrderKey();			
				key.setOrderNo(model.getOrderNo());
				key.setStore(store);
				order.setId(key);
				order.setDateCreated(
						new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(model.getDateCreated()));
				orders.add(order);
			}
			orderRepository.saveAll(orders);
		}catch(Exception ex) {
			log.error("error in fetchTodayOrder user", ex);
			throw new CustomException("error in fetching orders",HttpStatus.CONFLICT);
		}
	}

	public List<Order> fetchOrders(User user) {
		return orderRepository.findAllByUser(user);
	}

	public Page<Order> fetchOrders(String keyword, User user,Pageable pageable) {
		return orderRepository.findByIdOrderNoContainingIgnoreCaseAndUser(keyword,user, pageable);
	}

	public Order findFirstByOrderNo(String orderNo, User user) {
		return orderRepository.findFirstByIdOrderNoAndUser(orderNo,user);
	}

	public void save(Order order) {
		orderRepository.save(order);	
	}
	
	@Transactional
	public void generateTrackingNo(String orderNo, StoreInfo store, User user) throws ParseException {
		if(store.getCourierType().getName().equals(Constant.POSTEX)) {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			Date toDate= c.getTime();
			String dateString = sdf1.format(toDate);
			toDate = sdf1.parse(dateString);
			Order  order =  orderRepository.findFirstByIdOrderNoAndUser(orderNo,user);
			if(order != null) {
				String no = postExService.generatTrackingNo(order, store.getField3());
				order.setTrackingNo(no);
				order.setShipmentDate(toDate);
				orderRepository.save(order);
			}			
		}
	}

	public GraphData totalOrdersByMonth(User user,Date fromDate , Date toDate ) {
		List<GraphRow> rows = orderRepository.countByUserAndMonth(user,fromDate,toDate);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(Long.valueOf(p.getCount()))).collect(Collectors.toList()));
	}
	
	public GraphData amountOrdersByMonth(User user,Date fromDate , Date toDate) {
		List<GraphRow> rows = orderRepository.amountByUserAndMonth(user,fromDate,toDate);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(p.getValue())).collect(Collectors.toList()), null);
	}
	
	public GraphData totalOrdersByCity(User user,Date fromDate , Date toDate) {
		List<GraphRow> rows = orderRepository.countByUserAndCity(user,fromDate,toDate);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(Long.valueOf(p.getCount()))).collect(Collectors.toList()));
	}
	
	public Long totalOrders(User user,Date fromDate , Date toDate) {
		if(fromDate == null && toDate == null)
			return orderRepository.countByUser(user);
		else {
			return orderRepository.countByUserAndDateCreatedBetween(user,fromDate,toDate);

		}
	}

	public Long trackedOrders(User user,Date fromDate , Date toDate) {
		if(fromDate == null && toDate == null)	
			// TODO Auto-generated method stub
			return orderRepository.countByUserAndTrackingNoNotNull(user);
		else {
			return orderRepository.countByUserAndTrackingNoNotNullAndDateCreatedBetween(user,fromDate,toDate);

		}
	}
	

	public Long paidOrders(User user,Date fromDate , Date toDate) {
		if(fromDate == null && toDate == null)	
			// TODO Auto-generated method stub
			return orderRepository.countByUserAndStatus(user,"Settled");
		else {
			return orderRepository.countByUserAndStatusAndDateCreatedBetween(user,"Settled",fromDate,toDate);

		}
	}

	public void generateReport(User user,HttpServletResponse response,String name,Date fromDate,Date toDate) throws IOException {
	   response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=orders" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);        
        export(response, user,name,fromDate, toDate);
	}
	 
    private void writeHeaderLine(String name,Date fromDate,Date toDate) {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Orders");         
        Row row = sheet.createRow(0);         
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        if(name.equals("1")) {
	        createCell(row, 0, "Order NO", style);      
	        createCell(row, 1, "Customer Name", style);       
	        createCell(row, 2, "Shipping Address", style);    
	        createCell(row, 3, "COD Amount", style);
	        createCell(row, 4, "Status", style);
	        createCell(row, 5, "Order Date", style);
        }
        if(name.equals("3") ) {
	        createCell(row, 0, "Order NO", style);      
	        createCell(row, 1, "Customer Name", style);       
	        createCell(row, 2, "Shipping Address", style);    
	        createCell(row, 3, "COD Amount", style);
	        createCell(row, 4, "Status", style);
	        createCell(row, 5, "Order Date", style);
	        createCell(row, 6, "Trackig NO", style);
	        createCell(row, 7, "Shipment Date", style);
        }
        if(name.equals("4") || name.equals("2") ) {
	        createCell(row, 0, "Order NO", style);      
	        createCell(row, 1, "Customer Name", style);       
	        createCell(row, 2, "Shipping Address", style);    
	        createCell(row, 3, "COD Amount", style);
	        createCell(row, 4, "Status", style);
	        createCell(row, 5, "Order Date", style);
	        createCell(row, 6, "Trackig NO", style);
	        createCell(row, 7, "Shipment Date", style);
	        createCell(row, 8, "Transaction ID", style);
	        createCell(row, 9, "Settlement Date", style);
        }        
    }
     
    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
            cell.setCellStyle(style);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellStyle(style);
        }else if (value instanceof Double) {
            cell.setCellValue((Double) value);
            cell.setCellStyle(style);
        }else {
            cell.setCellValue((String) value);
            cell.setCellStyle(style);
        }
    }
    private void createDateRow(Row row, int columnCount, Date value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);

        style.setDataFormat(  
        		workbook.getCreationHelper().createDataFormat().getFormat("d-mmm-yy"));  
        cell.setCellValue(value == null ? null : (Date) value);  
        cell.setCellStyle(style);  
            
        
    }
     
    private void writeDataLines(User user,String name,Date fromDate,Date toDate) {
        int rowCount = 1; 
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);        
        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
    	List<Order> orders = new ArrayList<>();

        if(name.equals("1")) {
        	orders = orderRepository.getUnprocessOrder(user,fromDate,toDate);       		
	        for (Order order : orders) {
	            Row row = sheet.createRow(rowCount++);
	            int columnCount = 0;	             
	            createCell(row, columnCount++, order.getId().getOrderNo(), style);
	            createCell(row, columnCount++, order.getFirstName() + " " + order.getLastName(), style);
	            createCell(row, columnCount++, order.getAddress1(), style);
	            createCell(row, columnCount++, order.getTotal(), style);	
	            createCell(row, columnCount++, order.getStatus() == null ? "Unpaid" : order.getStatus(), style);
	            createDateRow(row, columnCount++, order.getDateCreated(), style2);	             
	        }
        }
    	if(name.equals("2")) {
        	orders = orderRepository.getAllOrder(user,fromDate,toDate);       		
	        for (Order order : orders) {
	            Row row = sheet.createRow(rowCount++);
	            int columnCount = 0;	             
	            createCell(row, columnCount++, order.getId().getOrderNo(), style);
	            createCell(row, columnCount++, order.getFirstName() + " " + order.getLastName(), style);
	            createCell(row, columnCount++, order.getAddress1(), style);
	            createCell(row, columnCount++, order.getTotal(), style);
	            createCell(row, columnCount++, order.getStatus() == null ? "Unpaid" : order.getStatus(), style);
	            createDateRow(row, columnCount++, order.getDateCreated(), style2);	             
	            createCell(row, columnCount++, order.getTrackingNo(), style2);	             
	            createDateRow(row, columnCount++, order.getShipmentDate(), style2);
	            createCell(row, columnCount++, order.getTransactionId(), style);	
	            createDateRow(row, columnCount++, order.getSettledDate(), style2);
	        }
        }
    	if(name.equals("3")) {
        	orders = orderRepository.getAllTrackedOrder(user,fromDate,toDate);       		
	        for (Order order : orders) {
	            Row row = sheet.createRow(rowCount++);
	            int columnCount = 0;	             
	            createCell(row, columnCount++, order.getId().getOrderNo(), style);
	            createCell(row, columnCount++, order.getFirstName() + " " + order.getLastName(), style);
	            createCell(row, columnCount++, order.getAddress1(), style);
	            createCell(row, columnCount++, order.getTotal(), style);	
	            createCell(row, columnCount++, order.getStatus() == null ? "Unpaid" : order.getStatus(), style);
	            createDateRow(row, columnCount++, order.getDateCreated(), style2);	   
	            createCell(row, columnCount++, order.getTrackingNo(), style2);	             
	            createDateRow(row, columnCount++, order.getShipmentDate(), style2);	   
	        }
        }        
        if(name.equals("4")) {
        	if(fromDate == null && toDate == null) {
        		orders = orderRepository.findByUserOrderByDateCreated(user);
        	}else {
        		orders = orderRepository.findByUserAndDateCreatedBetweenOrderByDateCreated(user,fromDate,toDate);
        	}	
	        for (Order order : orders) {
	            Row row = sheet.createRow(rowCount++);
	            int columnCount = 0;	             
	            createCell(row, columnCount++, order.getId().getOrderNo(), style);
	            createCell(row, columnCount++, order.getFirstName() + " " + order.getLastName(), style);
	            createCell(row, columnCount++, order.getAddress1(), style);
	            createCell(row, columnCount++, order.getTotal(), style);
	            createCell(row, columnCount++, order.getStatus() == null ? "Unpaid" : order.getStatus(), style);
	            createDateRow(row, columnCount++, order.getDateCreated(), style2);	             
	            createCell(row, columnCount++, order.getTrackingNo(), style2);	             
	            createDateRow(row, columnCount++, order.getShipmentDate(), style2);
	            createCell(row, columnCount++, order.getTransactionId(), style);	
	            createDateRow(row, columnCount++, order.getSettledDate(), style2);	             
	        }
        }
    }
     
    public void export(HttpServletResponse response,User user,String name,Date fromDate,Date toDate) throws IOException {
        writeHeaderLine(name,fromDate, toDate);
        writeDataLines(user,name,fromDate, toDate);
         
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
         
        outputStream.close();
         
    }
}
