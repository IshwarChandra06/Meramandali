package com.eikona.tech.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.DefaultConstants;
import com.eikona.tech.constants.MessageConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Department;
import com.eikona.tech.entity.Designation;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Organization;
import com.eikona.tech.repository.DepartmentRepository;
import com.eikona.tech.repository.DesignationRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.OrganizationRepository;

@Component
public class ExcelEmployeeImport {
	
	@Autowired
	private DepartmentRepository departmentrepository;
	
	@Autowired
	private DesignationRepository designationrepository;

	@Autowired
	private OrganizationRepository organizationrepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmployeeObjectMap employeeObjectMap;
	
	
	
	public Employee excelRowToEmployee(Row currentRow,Organization org, Map<String, Designation> designationMap,Map<String, Department> deptMap) throws ParseException {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();
			cellIndex = currentCell.getColumnIndex();
			if (null == employeeObj) {
				break;
			}
			else if (cellIndex == NumberConstants.ZERO) {
				String str=setStringCell( currentCell) ;
				employeeObj.setEmpId(str);
				employeeObj.setOrganization(org);
			} else if (cellIndex == NumberConstants.ONE) {
				String str=setStringCell( currentCell) ;
				employeeObj.setName(str);
			}
			 else if (cellIndex == NumberConstants.TWO) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setFatherName(str);
				}
			 else if (cellIndex == NumberConstants.THREE) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setMobile(str);
				}
			 else if (cellIndex == NumberConstants.FOUR) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setGender(str);
				}
			else if (cellIndex == NumberConstants.FIVE) {
				setDepartment(org, deptMap, employeeObj, currentCell);
			}
			else if (cellIndex == NumberConstants.SIX) {
				setDesignation(org, designationMap, employeeObj, currentCell);
			}
			 else if (cellIndex == NumberConstants.SEVEN) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setGrade(str);
				}
			 else if (cellIndex == NumberConstants.EIGHT) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setPermanentAddress(str);
				}
			 else if (cellIndex == NumberConstants.NINE) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setResidentialAddress(str);
				}
			 else if (cellIndex == NumberConstants.TEN) {
				 setJoinDate(employeeObj,currentCell);
				}
			 else if (cellIndex == NumberConstants.ELEVEN) {
				 String str=setStringCell( currentCell) ;
					employeeObj.setEmail(str);
				}

		}
		return employeeObj;
		
	}
	

	private void setDesignation(Organization org, Map<String, Designation> designationMap, Employee employeeObj,
			Cell currentCell) {
		String str = currentCell.getStringCellValue();
		if (null != str && !str.isEmpty()) {
			
			Designation designation = designationMap.get(str);
			if (null == designation) {
				designation = new Designation();
				designation.setName(str);
				designation.setOrganization(org);
				designationrepository.save(designation);
				designationMap.put(designation.getName(), designation);

			}
			employeeObj.setDesignation(designation);
		}
	}

	private void setDepartment(Organization org, Map<String, Department> deptMap, Employee employeeObj,
			Cell currentCell) {
		String str = currentCell.getStringCellValue();

		if (null != str && !str.isEmpty()) {
			
			Department department = deptMap.get(str);
			if (null == department) {
				department = new Department();
				department.setName(str);
				department.setOrganization(org);
				departmentrepository.save(department);
				deptMap.put(department.getName(), department);
			}
			employeeObj.setDepartment(department);
		}
	}
	
	private void setJoinDate(Employee employee,Cell currentCell) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date date =null;
		 if (currentCell.getCellType() == CellType.STRING) {
			 date=inputFormat.parse(currentCell.getStringCellValue().trim());
			 if(null!=date)
			 employee.setJoinDate(dateFormat.format(date));
		 }
		  else if (currentCell.getCellType() == CellType.NUMERIC) {
			  date = currentCell.getDateCellValue();
	           String dateStr = dateFormat.format(date);
	           employee.setJoinDate(dateStr);
			} 
		  else {
			  if(!currentCell.getStringCellValue().isEmpty()) {
				  date = (Date)currentCell.getDateCellValue();
					 if(null!=date)
					 employee.setJoinDate(dateFormat.format(date));
			  }
			
		 }
	}

	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private String setStringCell(Cell currentCell) {
		String str="";
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			str=String.valueOf(currentCell.getNumericCellValue());
		} else if (currentCell.getCellType() == CellType.STRING) {
			str=currentCell.getStringCellValue();
		}
		return str;
	}
	public List<Employee> parseExcelFileEmployeeList(InputStream inputStream) throws ParseException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			Map<String, Designation> designationMap = employeeObjectMap.getDesignation();
			Map<String, Department> deptMap = employeeObjectMap.getDepartment();
			List<String> empIdList=employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			Organization org = organizationrepository.findById(DefaultConstants.DEFAULT_ORGANIZATION_ID).get();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=excelRowToEmployee(currentRow,org,designationMap,deptMap);
				
				boolean isContains=empIdList.contains(employee.getEmpId());
				
				if(!isContains && null!=employee.getName() && !employee.getName().isEmpty() &&
						!(employee.getName().contains("N/A")) && null!=employee.getEmpId()&& !employee.getEmpId().isEmpty())
				employeeList.add(employee);
				
				if(rowNumber%NumberConstants.HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}
	
	public void parseEmployeeListForDelete(InputStream inputStream) {

		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = NumberConstants.ZERO;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;

				Iterator<Cell> cellsInRow = currentRow.iterator();
				int cellIndex = NumberConstants.ZERO;

				while (cellsInRow.hasNext()) {
					Cell currentCell = cellsInRow.next();
					cellIndex = currentCell.getColumnIndex();

					 if (cellIndex == NumberConstants.ZERO) {
						String value = getStringValue(currentCell);
						Employee employee = employeeRepository.findByEmpIdCustom(value.trim());
						if (null!=employee) {
							employee.setDeleted(true);
							employeeRepository.save(employee);
						}
					}

			}


			workbook.close();

		}
		}catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	
	}

	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private String getStringValue(Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		String value = "";
		if (currentCell.getCellType() == CellType.NUMERIC) {
			value = String.valueOf(currentCell.getNumericCellValue());
		} else if (currentCell.getCellType() == CellType.STRING) {
			value = currentCell.getStringCellValue();
		}
		return value;
	}
	
	
}
