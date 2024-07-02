package net.codejava.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
	public void generateTrackingNo(String orderNo, StoreInfo store, User user) {
		if(store.getCourierType().getName().equals(Constant.POSTEX)) {
			Order  order =  orderRepository.findFirstByIdOrderNoAndUser(orderNo,user);
			if(order != null) {
				String no = postExService.generatTrackingNo(order, store.getField3());
				order.setTrackingNo(no);
				orderRepository.save(order);
			}			
		}
	}

	public GraphData totalOrdersByMonth(User user) {
		List<GraphRow> rows = orderRepository.countByUserAndMonth(user);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(Long.valueOf(p.getCount()))).collect(Collectors.toList()));
	}
	
	public GraphData amountOrdersByMonth(User user) {
		List<GraphRow> rows = orderRepository.amountByUserAndMonth(user);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(p.getValue())).collect(Collectors.toList()), null);
	}
	
	public GraphData totalOrdersByCity(User user) {
		List<GraphRow> rows = orderRepository.countByUserAndCity(user);
		return new GraphData(
				rows.stream().flatMap(p -> Stream.of(p.getKey())).collect(Collectors.toList())
				,rows.stream().flatMap(p -> Stream.of(Long.valueOf(p.getCount()))).collect(Collectors.toList()));
	}
	
	public Long totalOrders(User user) {
		return orderRepository.countByUser(user);
	}

	public Long trackedOrders(User user) {
		// TODO Auto-generated method stub
		return orderRepository.countByUserAndTrackingNoNotNull(user);
	}
	

	public Long paidOrders(User user) {
		// TODO Auto-generated method stub
		return orderRepository.countByUserAndStatus(user,"Settled");
	}

	public void generateReport(User user,HttpServletResponse response) throws IOException {
	   response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
         
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=orders" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
         
        export(response, user);
	}
	 
    private void writeHeaderLine() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Orders");
         
        Row row = sheet.createRow(0);
         
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
         
        createCell(row, 0, "Order NO", style);      
        createCell(row, 1, "Customer Name", style);       
        createCell(row, 2, "Shipping Address", style);    
        createCell(row, 3, "COD Amount", style);
        createCell(row, 4, "Transaction ID", style);
        createCell(row, 5, "Status", style);
        createCell(row, 6, "Order Date", style);
        createCell(row, 7, "Settlement Date", style);

         
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
     
    private void writeDataLines(User user) {
        int rowCount = 1;
 
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        
        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
        
		List<Order> orders = orderRepository.findByUserOrderByDateCreated(user);
        CreationHelper createHelper = workbook.getCreationHelper();  

        for (Order order : orders) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
             
            createCell(row, columnCount++, order.getId().getOrderNo(), style);
            createCell(row, columnCount++, order.getFirstName() + " " + order.getLastName(), style);
            createCell(row, columnCount++, order.getAddress1(), style);
            createCell(row, columnCount++, order.getTotal(), style);
            createCell(row, columnCount++, order.getTransactionId(), style);

            createCell(row, columnCount++, order.getStatus() == null ? "Unpaid" : order.getStatus(), style);
            createDateRow(row, columnCount++, order.getDateCreated(), style2);

            createDateRow(row, columnCount++, order.getSettledDate(), style2);

             
        }
    }
     
    public void export(HttpServletResponse response,User user) throws IOException {
        writeHeaderLine();
        writeDataLines(user);
         
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
         
        outputStream.close();
         
    }
}
