<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Products</title>
</head>
<body>
	<h2>Products</h2>
	<a href="/admin/products/add">Add Product</a>
	
	<table border="1">
		<tr>
			<th>Name</th>
			<th>Category</th>
			<th>Type</th>
			<th>Status</th>
		</tr>
			
		<c:forEach items="${products}" var="p">
			<tr>
				<td>${p.name}</td>
				<td>${p.category}</td>
				<td>${p.productType}</td>
				<td>
    				<span style="color:${product.available ? 'green' : 'red'}">
        			${product.available ? 'In Stock' : 'Out of Stock'}</span>
				</td>
				
			</tr>
		</c:forEach>
	
	</table>
	
</body>
</html>