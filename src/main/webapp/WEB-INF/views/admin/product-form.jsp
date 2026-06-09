<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Add Product</title>
</head>
<body>
	<h2>Add Product</h2>
	
	<form:form method="post" action="/admin/products/save" modelAttribute="product">
	
		<form:hidden path="id"/>
	
		<label>Name:</label><br>
		<form:input path="name"/><br><br>
		
		<label>Category:</label><br>
		<form:input path="category"/><br><br>
		
		<label>Product Type:</label><br>
		<form:select path="productType">
			<form:option value="WEIGHT">Weight Based</form:option>
			<form:option value="UNIT">Unit Based</form:option>
		</form:select><br><br>
		
		<label>Description:</label><br>
		<form:textarea path="description"/><br><br>
	
		<label>Image URL:</label><br>	
		<form:input path="image"/><br><br>	
	
		<div>
    	<label>
        	<input type="checkbox" name="available"
               ${product.available ? "checked" : ""}>
        		In Stock
    	</label>
		</div>
		
		
		<button type="submit">Save</button>
	
	
	</form:form>
	
</body>
</html>